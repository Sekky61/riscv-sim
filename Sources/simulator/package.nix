{ lib
, jre_minimal
, jdk21_headless
, makeWrapper
, maven
, nix-gitignore
, pkgsCross
, riscv-gcc
}:

let
  my_jre = jre_minimal.override {
    jdk = jdk21_headless;
    modules = [
      "java.base"
      "java.logging"
      "java.security.sasl"
      "jdk.unsupported"
    ];
  };

  # A hack: version is marked with nix="true" to find it easily
  version = lib.removeSuffix "\n" (
    builtins.head (
      builtins.match ".*<version nix=\"true\">([^<]+)</version>.*"
        (builtins.readFile ./pom.xml)
    )
  );

  # gcc = pkgsCross.riscv64.gcc;
  riscvGccPath = "${riscv-gcc}/bin/riscv64-none-elf-gcc";

  gitignoreSource = nix-gitignore.gitignoreSource [ ];
in
maven.buildMavenPackage {
  pname = "riscv-sim-backend";
  inherit version;

  src = gitignoreSource ./.;

  mvnHash = "sha256-gipvSuxQXJzqwQ8to1FEOitID1RbGpVD1qgAKgEy5QY=";
  nativeBuildInputs = [ makeWrapper ];
  buildInputs = [ riscv-gcc ];
  mvnParameters = "-Dmaven.test.skip";

  installPhase = ''
    mkdir -p $out/bin/
    cp target/superscalar-simulator-${version}.jar $out/bin/backend.jar
    makeWrapper ${my_jre}/bin/java $out/bin/backend \
      --add-flags "-jar $out/bin/backend.jar"\
      --add-flags "server --gcc-path ${riscvGccPath}"
  '';

  meta = with lib; {
    description = "RISC-V simulator backend";
    homepage = "https://github.com/Sekky61/riscv-sim";
    license = licenses.gpl3;
    mainProgram = "riscv-sim-backend";
  };
}
