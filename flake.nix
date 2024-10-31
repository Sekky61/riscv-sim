{
  description = "RISC-V Simulator";
  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";
    flake-utils.url = "github:numtide/flake-utils";
  };
  outputs = { self, nixpkgs, flake-utils }:
    flake-utils.lib.eachDefaultSystem (system:
      let
        pkgs = nixpkgs.legacyPackages.${system};

        cross = import nixpkgs.outPath {
          crossSystem = { config = "riscv64-unknown-linux-gnu"; };
          inherit system;
        };
        riscvGcc = "${cross.buildPackages.gcc12}/bin/riscv64-unknown-linux-gnu-gcc";

        # Wrapper script to run the Next.js server
        startFront = pkgs.writeShellScriptBin "start-frontend" ''
          #!/bin/sh
          PORT=''${PORT:-3000}
          cd ${self.packages.${system}.frontend}/standalone
          exec ${pkgs.bun}/bin/bun server.js
        '';

        # Wrapper script to run the simulator
        startSim = pkgs.writeShellScriptBin "start-simulator" ''
          #!/bin/sh
          cd ${self.packages.${system}.backend}/bin
          exec ./backend server --gcc-path ${riscvGcc}
        '';
      in
      {
        formatter = pkgs.nixpkgs-fmt;
        
        packages = {
          frontend = pkgs.buildNpmPackage {
            name = "riscv-sim-frontend";
            version = "1.0.0";
            src = ./Sources/frontend;
            nodejs = pkgs.bun // { python = pkgs.python3; };
            npmDepsHash = "sha256-LUqXgp/60j69u7ZqEm2OrYq39ovntZO/cUm1g83zcjc=";
            dontNpmInstall = true;
            nativeBuildInputs = [ pkgs.nodejs_20 ]; # for npm
            installPhase = ''
              mkdir -p $out/standalone/.next/
              cp -r .next/standalone $out/
              cp -r .next/static $out/standalone/.next/
            '';

            meta = with pkgs.lib; {
              description = "RISC-V Simulator Frontend";
              homepage = "https://github.com/Sekky61/riscv-sim";
              license = licenses.gpl3;
              mainProgram = "riscv-sim-frontend";
            };
          };

          backend = pkgs.callPackage ./Sources/simulator/package.nix { };

          # Publish: ```
          # docker tag <image> majeris/<image>:latest
          # docker tag <image> majeris/<image>:<version>
          # docker push majeris/<image>:latest
          # docker push majeris/<image>:<version>
          frontend-docker = pkgs.dockerTools.buildLayeredImage {
            name = "riscv-sim-frontend";
            tag = "latest";
            config.Cmd = "${startFront}/bin/start-frontend";
          };

          backend-docker = pkgs.dockerTools.buildLayeredImage {
            name = "riscv-sim-backend";
            tag = "latest";
            config.Cmd = "${startSim}/bin/start-simulator";
          };
        };

        apps.frontend = {
          type = "app";
          pname = "riscv-sim-frontend";
          program = "${startFront}/bin/start-frontend";
        };

        apps.simulator = {
          type = "app";
          pname = "riscv-sim-backend";
          program = "${startSim}/bin/start-simulator";
        };

        # Development environment
        devShell = pkgs.mkShell {
          buildInputs = with pkgs; [
            nodejs_20
            nodePackages.npm
            cross.buildPackages.gcc12
            startSim
            startFront
            jre
          ];
        };
      }
    );
}
