# Web Based Simulator of Superscalar Processors

![image](https://github.com/user-attachments/assets/c0d9b4ea-a7fc-4445-bf57-5ec339f674c6)

## Introduction

This is a RISC-V Simulator Web App. The project builds on the superscalar simulator created by Jakub Horky and Jan Vavra. The goal is to add a **web** and a **CLI** interface.

In this readme, you will find instructions to build and run the app both natively and in Docker.
The project consists of two components: a web app and a Java simulator server. More detailed info can be found in their respective Readmes [Sources/frontend/Readme.md](Sources/frontend/Readme.md) and [Sources/simulator/Readme.md](Sources/simulator/Readme.md)).

## Installation

First, the one-line quickstart run-it-from-anywhere using Docker:
```bash
curl -L https://raw.githubusercontent.com/Sekky61/riscv-sim/refs/heads/master/Sources/docker-compose.http.yml | docker compose -f - up
```
The app will be available on `http://localhost:3120`.

For proper deployment, HTTPS support and custom base path, clone the repository, create HTTPS certificates and run
```bash
cd Sources && docker compose up
```
Refer to Readmes in the respective directories, Dockerfiles and the Nix flake for more detailed instructions.

### Build and Run Frontend Web App

> Requirements: bun

The app was developed using bun `1.1.31`. Any version should work to build the frontend app, and a modern node.js should work as well.
To build the production version of the app, start by navigating to `Sources/frontend` and installing dependencies:

```bash
bun install
```

To build the app, run:

```bash
bun run build
```

Now you can either `bun run start` or alternatively:

```bash
cp -r .next/static/ .next/standalone/.next/static
bun .next/standalone/server.js
```

Navigate to `http://localhost:3000` (or the address shown in the console on startup) to see the app.

For more detailed documentation including how to develop the app, see `Sources/frontend/Readme.md`.

### Build and Run Simulation Server

> Requirements: Java, Maven, gcc

The backend server is written in Java using version `17.0.6`.
It is built with Maven (developed on `3.9.9`).

To build the backend server, navigate to `Sources/simulator` and run:

```bash
./scripts/install.sh
```

To run the server, type `./scripts/run.sh server`.
To see full options, including the CLI mode, run `./scripts/run.sh help`.

For more detailed documentation, see `Sources/simulator/Readme.md`.

### Docker

Basically, you can pull prebuilt images from Docker hub, or build them yourself.
You need to build them only if you need to change the *base path* of the app (for example if you want to deploy the app under `example.com/riscvsim`).

Another choice to make is whether you need HTTPS or not.
If you only need HTTP, use
```bash
cd Sources
docker compose -f docker-compose.http.yml up
```
This is the easiest one for local deployment.

If you need both HTTP and HTTPS, create the certificates and use the `docker-compose.yml` file.
The Docker compose files are located at `Sources/`.

To use your own built images use the `build_container.sh` and `run_container.sh` commands.

> [!NOTE]  
> `sudo` might be required to run the docker commands.

> [!NOTE]  
> Older Docker versions use command `docker-compose` instead of `docker compose`. Developed using Docker `24.0.7` and `20.10.2`.

### HTTPS

Add SSL/TLS certificates to `Sources/proxy/certs` to enable HTTPS support.
An Nginx proxy is created as a Docker container during the startup via docker compose.
Note, that this step is not necessary to run the app, but the nginx container will fail to start without the certificates.
For details, see [Sources/proxy/Readme.md](Sources/proxy/Readme.md).

## Repository Structure

    .
    +--Literature    - Publications, references, manuals, etc.
    +--Sources       - Root folder for the sources.
    +--Thesis        - Latex sources of the thesis.
    flake.(nix|lock) - Nix flake
    LICENSE          - The projects license
    Readme.md        - Read me file

## Nix

Another way to use the project is through Nix.
I prefer this method as it is more reproducible and doesn't require installing anything manually.

- Run `nix run .#simulator` to run the simulator and `nix run .#frontend` to run the frontend.
- Run `nix build .#frontend-docker` (or `#backend-docker`) to create a Docker image. Load it with `docker load < result`.
- Run `nix develop` in the root directory to enter the development environment with all the necessary tools.

