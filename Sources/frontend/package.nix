{
  lib,
  nix-gitignore,
  stdenv,
  bun,
  nodejs-slim_latest,
  base-path ? ""
}:
let
  # These properties are overridable, like this:
  # nixpkgs.overlays = [
  #   (self: super: {
  #     frontend = super.frontend.override {
  #       base-path = "/riscvapp";
  #     };
  #   })

  # Source: https://github.com/NixOS/nixpkgs/issues/255890#issuecomment-2308881422

  pname = packageJson.name;
  version = packageJson.version;

  gitignoreSource = nix-gitignore.gitignoreSource [];
  src = gitignoreSource ./.;
  packageJson = lib.importJSON "${src}/package.json";

  node_modules = stdenv.mkDerivation {
    pname = "${pname}_node-modules";
    inherit src version;

    nativeBuildInputs = [ bun ];
    buildInputs = [ nodejs-slim_latest ];

    dontConfigure = true;
    dontFixup = true; # patchShebangs produces illegal path references in FODs

    buildPhase = ''
      runHook preBuild

      export HOME=$TMPDIR

      bun install --no-progress --frozen-lockfile
      bun pm trust --all

      runHook postBuild
    '';

    installPhase = ''
      runHook preInstall

      mkdir -p $out/node_modules
      mv node_modules $out/

      runHook postInstall
    '';

    outputHash = "sha256-z8JihwTKqr4rDPUgXbJzr2tDxxFbGDqNOUFVdiAhfrY=";
    outputHashAlgo = "sha256";
    outputHashMode = "recursive";
  };
in
stdenv.mkDerivation {
  inherit pname version src;

  nativeBuildInputs = [
    node_modules
    nodejs-slim_latest
    bun
  ];

  env = {
    BASE_PATH = base-path;
    # Rest of the args are runtime
  };

  configurePhase = ''
    runHook preConfigure

    cp -a ${node_modules}/node_modules ./node_modules
    chmod -R u+rw node_modules
    chmod -R u+x node_modules/.bin
    patchShebangs node_modules

    export HOME=$TMPDIR
    export PATH="$PWD/node_modules/.bin:$PATH"

    runHook postConfigure
  '';

  buildPhase = ''
    runHook preBuild

    # bun run typecheck # fails due to public directory import
    bun run build

    runHook postBuild
  '';

  installPhase = ''
    runHook preInstall

    mkdir -p $out
    cp -r .next/standalone $out/
    cp -r .next/static $out/standalone/.next/

    runHook postInstall
  '';
}
