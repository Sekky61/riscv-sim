{ lib, jre_minimal, jdk21_headless, makeWrapper, maven }:

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
in
maven.buildMavenPackage {
  pname = "riscv-sim-backend";
  version = "1.0.0";

  src = ./.;

  mvnHash = "sha256-gipvSuxQXJzqwQ8to1FEOitID1RbGpVD1qgAKgEy5QY=";

  nativeBuildInputs = [ makeWrapper ];

  mvnParameters = "-Dmaven.test.skip";


  installPhase = ''
    mkdir -p $out/bin/
    cp target/superscalar-simulator-1.0.jar $out/bin/backend.jar
    makeWrapper ${my_jre}/bin/java $out/bin/backend \
      --add-flags "-jar $out/bin/backend.jar"
  '';

  meta = with lib; {
    description = "Java-based RISC-V simulator backend";
    homepage = "https://github.com/Sekky61/riscv-sim";
    license = licenses.gpl3;
    mainProgram = "riscv-sim-backend";
  };
}
