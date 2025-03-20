{
  description = "RISC-V Simulator";
  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";
    flake-utils.url = "github:numtide/flake-utils";

    zig-overlay.url = "github:mitchellh/zig-overlay";
    zig-overlay.inputs.nixpkgs.follows = "nixpkgs";

    zls.url = "github:zigtools/zls";
    zls.inputs = {
      nixpkgs.follows = "nixpkgs";
      zig-overlay.follows = "zig-overlay";
      gitignore.follows = "gitignore";
    };

    gitignore.url = "github:hercules-ci/gitignore.nix";
    gitignore.inputs.nixpkgs.follows = "nixpkgs";
  };
  outputs = {
    self,
    nixpkgs,
    zig-overlay,
    flake-utils,
    zls,
    ...
  } @ inputs:
    flake-utils.lib.eachDefaultSystem (
      system: let
        pkgs = nixpkgs.legacyPackages.${system};
        zig = zig-overlay.packages.${system}.master;

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
          exec ./backend
        '';

        riscv-toolchain = import nixpkgs {
          localSystem = "${system}";
          crossSystem = {
            config = "riscv64-none-elf";
            libc = "newlib-nano";
            abi = "ilp32";
          };
        };
        riscv-gcc = riscv-toolchain.buildPackages.gcc;
      in {
        formatter = pkgs.nixpkgs-fmt;

        packages = {
          frontend = pkgs.callPackage ./Sources/frontend/package.nix {};

          backend = pkgs.callPackage ./Sources/simulator/package.nix {
            inherit riscv-gcc;
          };

          simulator_zig = pkgs.callPackage ./Sources/simulator_zig/package.nix inputs;

          # Publish: ```
          # docker tag <image> majeris/<image>:latest
          # docker tag <image> majeris/<image>:<version>
          # docker push majeris/<image>:latest
          # docker push majeris/<image>:<version>
          frontend-docker = pkgs.dockerTools.buildLayeredImage {
            name = "riscv-sim-frontend";
            tag = "v${self.packages.${system}.frontend.version}";
            config.Cmd = "${startFront}/bin/start-frontend";
          };

          backend-docker = pkgs.dockerTools.buildLayeredImage {
            name = "riscv-sim-backend";
            tag = "v${self.packages.${system}.backend.version}";
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
            # Frontend
            bun

            # Java server
            riscv-toolchain.buildPackages.gcc
            jre

            # Zig
            zls.packages.${system}.default
            zig
            wasmtime
            wabt # wasm tools
          ];
        };
      }
    );
}
