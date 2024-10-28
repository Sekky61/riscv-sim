{ lib, jre, makeWrapper, maven }:

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
    makeWrapper ${jre}/bin/java $out/bin/backend \
      --add-flags "-jar $out/bin/backend.jar"
  '';

  meta = with lib; {
    description = "Simple command line wrapper around JD Core Java Decompiler project";
    homepage = "https://github.com/intoolswetrust/jd-cli";
    license = licenses.gpl3Plus;
    maintainers = with maintainers; [ majiir ];
  };
}
