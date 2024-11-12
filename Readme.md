# Web Based Simulator of Superscalar Processors

![image](https://github.com/user-attachments/assets/c0d9b4ea-a7fc-4445-bf57-5ec339f674c6)

## Introduction

Unlock the power of superscalar processor design with our cutting-edge RISC-V simulator! Tailored for IT students, researchers, and HPC professionals, this web-based tool brings complex architectures to life with an intuitive, customizable interface. Explore processor components, tweak configurations, and benchmark code snippets-all from your browser.

The simulator offers seamless support for C and assembly programs, built-in performance metrics, and full GCC compiler integration for various optimization levels. Whether you're learning or innovating, this tool enables you to experiment with different architectural setups, analyze results, and export configurations for sharing.

Designed to deepen your understanding of processor design and HW-SW co-design, the simulator supports both interactive exploration and batch processing via command-line. Perfect for those aiming to optimize RISC-V processors and HPC codes, it's more than just a learning tool-it's a powerful platform for research and development. Get ready to elevate your skills and performance optimization with this advanced simulator!

In this README, you'll find instructions on how to build and run the app both natively and in Docker.
The project consists of two components: a web app and a Java simulator server. For more details, refer to their respective READMEs:
- [Frontend](Sources/frontend/Readme.md)
- [Simulator](Sources/simulator/Readme.md).

## Installation

First, the one-line quickstart run-it-from-anywhere (x86 systems only) using Docker:
```bash
curl -L https://raw.githubusercontent.com/Sekky61/riscv-sim/refs/heads/master/Sources/docker-compose.yml | docker compose -f - up
```
The app will be available on [`http://localhost:3120`](http://localhost:3120).

For proper deployment, HTTPS support and parametrization like custom base path, do:

1. Clone the repository
2. (Optionally) [Create HTTPS certificates](Sources/proxy/Readme.md)
3. Run `cd Sources && ./manage-riscvsim.sh up`

`./manage-riscvsim.sh --help` explains all parameters.
Refer to Readmes in the respective directories, Dockerfiles and the Nix flake in case you need more context.

Generally, for production deployment you may need `--domain`, `--http-port` and `--https-port`.
If the app is exposed on a specific prefix, use the `--base-path`, so that the links on the web page are correct.

An extra example:
```bash
./manage-riscvsim.sh up --base-path /riscvapp --domain example.com --certs-path ~/certs
```

### Docker

You can use the `docker-compose.yml` file manually.
It uses environment variables for the parameters.

By default, the images are built using the Dockerfiles.
You can also pull prebuilt images from Docker hub (`--build-strategy`).

> [!WARNING]  
> `--base-path` is a build-time paremeter, and so it will **not** work with Docker hub images.

> [!NOTE]  
> `sudo` might be required to run the docker commands.

> [!NOTE]  
> Older Docker versions use command `docker-compose` instead of `docker compose`. See the `--compose-command` argument.


### Build and Run Frontend Web App

See the [frontend readme](Sources/frontend/Readme.md).

### Build and Run Simulation Server

See the [simulator readme](Sources/simulator/Readme.md).

## Nix

Another way to use the project is through Nix.
I prefer this method as it is more reproducible and doesn't require installing anything manually.

- Run `nix run .#simulator` to run the simulator and `nix run .#frontend` to run the frontend.
- Run `nix build .#frontend-docker` (or `#backend-docker`) to create a Docker image. Load it with `docker load < result`.
- Run `nix develop` in the root directory to enter the development environment with all the necessary tools.

## Analytics

Umami collects no PII and serves only as a visitor counter.
The data collection should work automatically, but `--domain` should be specified.

After running the docker compose, the analytics dashboard is available on port `8090`.
The login is `admin/umami`.

## Acknowledgements

I would like to thank everyone who contributed to this project through collaboration and valuable input. Special thanks to:

- Michal Majer - [misa@majer.cz](misa@majer.cz)
- Jakub Horky
- Jan Vavra
- Jiri Jaros - [jarosjir@fit.vut.cz](jarosjir@fit.vut.cz)

For further reading, refer to the theses available in the [Literature/](Literature/) directory.

<img src="Sources/frontend/public/FIT_color_EN.png" alt="BUT FIT" width="300"/>

## Repository Structure

```
.
+--Literature    - Publications, references, manuals, etc.
+--Sources       - Root folder for the sources.
+--Thesis        - Latex sources of the thesis.
flake.(nix|lock) - Nix flake
LICENSE          - The projects license
Readme.md        - Read me file
```

