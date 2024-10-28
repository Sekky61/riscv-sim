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

        # Wrapper script to run the Next.js server
        startFront = pkgs.writeShellScriptBin "start-frontend" ''
          #!/bin/sh
          PORT=''${PORT:-3000}
          cd ${self.packages.${system}.frontend}/standalone
          exec ${pkgs.nodejs_20}/bin/node server.js
        '';

        # Wrapper script to run the simulator
        startSim = pkgs.writeShellScriptBin "start-simulator" ''
          #!/bin/sh
          cd ${self.packages.${system}.backend}/bin
          exec ./backend server --gcc-path ${cross.buildPackages.gcc12}/bin/riscv64-unknown-linux-gnu-gcc
        '';
      in
      {
        formatter = pkgs.nixpkgs-fmt;
        
        packages = {
          frontend = pkgs.buildNpmPackage {
            name = "riscv-sim-frontend";
            src = ./Sources/frontend;
            npmDepsHash = "sha256-LUqXgp/60j69u7ZqEm2OrYq39ovntZO/cUm1g83zcjc=";
            dontNpmInstall = true;
            installPhase = ''
              mkdir -p $out/standalone/.next/
              cp -r .next/standalone $out/
              cp -r .next/static $out/standalone/.next/
            '';
          };

          backend = pkgs.callPackage ./Sources/simulator/package.nix { };
        };

        apps.frontend = {
          type = "app";
          program = "${startFront}/bin/start-frontend";
        };

        apps.simulator = {
          type = "app";
          program = "${startSim}/bin/start-simulator";
        };

        # For development
        devShell = pkgs.mkShell {
          buildInputs = with pkgs; [
            nodejs_20
            nodePackages.npm
            cross.buildPackages.gcc12
            startSim
            startFront
          ];
        };
      }
    );
}
