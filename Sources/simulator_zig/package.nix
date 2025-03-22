{
  lib,
  pkgs,
  gitignore,
  zig,
}: let
  gitignoreSource = gitignore.lib.gitignoreSource;
  depsPkg = pkgs.callPackage ./deps.nix {};
in
  pkgs.stdenvNoCC.mkDerivation {
    pname = "riscvsim";
    version = "master";

    src = gitignoreSource ./.;

    nativeBuildInputs = [zig];
    dontConfigure = true;
    dontInstall = true;
    doCheck = true;
    buildPhase = ''
      mkdir -p .cache
      ln -s ${depsPkg} .cache/p
      zig build install --cache-dir $(pwd)/.zig-cache --global-cache-dir $(pwd)/.cache -Dcpu=baseline -Doptimize=ReleaseSafe --prefix $out
    '';
    checkPhase = ''
      zig build test --cache-dir $(pwd)/.zig-cache --global-cache-dir $(pwd)/.cache -Dcpu=baseline
    '';

    meta = with lib; {
      description = "RISC-V simulator binary and library";
      homepage = "https://github.com/Sekky61/riscv-sim";
      license = licenses.gpl3;
      mainProgram = "riscvsim";
    };
  }
