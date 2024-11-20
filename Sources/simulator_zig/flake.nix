{
  inputs =
    {
      nixpkgs.url = "github:NixOS/nixpkgs/nixos-24.05";

      zig-overlay.url = "github:mitchellh/zig-overlay";
      zig-overlay.inputs.nixpkgs.follows = "nixpkgs";

      gitignore.url = "github:hercules-ci/gitignore.nix";
      gitignore.inputs.nixpkgs.follows = "nixpkgs";

      flake-utils.url = "github:numtide/flake-utils";

      zls.url = "github:zigtools/zls";
      zls.inputs = {
        nixpkgs.follows = "nixpkgs";
        zig-overlay.follows = "zig-overlay";
        gitignore.follows = "gitignore";
      };
    };

  outputs = { self, nixpkgs, zig-overlay, gitignore, flake-utils, zls }:
    flake-utils.lib.eachDefaultSystem (system:
      let
        pkgs = nixpkgs.legacyPackages.${system};
        zig = zig-overlay.packages.${system}.master;
        gitignoreSource = gitignore.lib.gitignoreSource;
      in
      rec {
        formatter = pkgs.nixpkgs-fmt;
        packages.default = packages.riscvsim;
        packages.riscvsim = pkgs.stdenvNoCC.mkDerivation {
          name = "riscvsim";
          version = "master";
          src = gitignoreSource ./.;
          nativeBuildInputs = [ zig ];
          dontConfigure = true;
          dontInstall = true;
          doCheck = true;
          buildPhase = ''
            mkdir -p .cache
            zig build install --cache-dir $(pwd)/.zig-cache --global-cache-dir $(pwd)/.cache -Dcpu=baseline -Doptimize=ReleaseSafe --prefix $out
          '';
          checkPhase = ''
            zig build test --cache-dir $(pwd)/.zig-cache --global-cache-dir $(pwd)/.cache -Dcpu=baseline
          '';
        };

        devShells.default = pkgs.mkShell {
          buildInputs = [ zls.packages.${system}.default zig ];
        };
      }
    );
}
