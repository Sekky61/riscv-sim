# Web Based Simulator of Superscalar Processors

![image](https://github.com/user-attachments/assets/c0d9b4ea-a7fc-4445-bf57-5ec339f674c6)

## Introduction

This is a RISC-V Simulator Web App. The project builds on the superscalar simulator created by Jakub Horky and Jan Vavra. The goal is to add a **web** and a **CLI** interface.

In this readme, you will find instructions to build and run the app both natively and in Docker.
The project consists of two components: web app and Java simulator server. More detailed info can be found in their respective Readmes [Sources/frontend/Readme.md](Sources/frontend/Readme.md) and [Sources/simulator/Readme.md](Sources/simulator/Readme.md)).

## Repository Structure

    .
    +--Data       - Example data, measured results, etc.
    +--Literature - Publications, references, manuals, etc.
    +--Sources    - Root folder for the sources.
    +--Thesis     - Latex sources of the thesis.
    +--Misc       - Other auxiliary materials.
    Readme.md     - Read me file

## Installation

First, the one-line quickstart run-it-from-anywhere using Docker:
```bash
curl -L https://raw.githubusercontent.com/Sekky61/riscv-sim/refs/heads/master/Sources/docker-compose.http.yml | docker compose -f - up
```
The app will be available on `http://localhost:3120`.
For proper deployment and HTTPS support, clone the repository, create HTTPS certificates and run
```bash
cd Sources && docker compose up
```

Refer to Readmes in the respective directories, Dockerfiles and the Nix flake for more detailed instructions.

### Build and Run Frontend Web App

> Requirements: npm, node.js

The app was developed using npm `10.2.3` and node.js `v21.2.0`.
You need to have these installed to build the frontend app (later versions should work as well).

To build the production version of the app, start by navigating to `Sources/frontend` and installing dependencies:

```bash
npm install
```

To build the app, run:

```bash
npm run build
```

Unfortunately, some of the files need to be manually copied over:
```bash
cp -r .next/static/ .next/standalone/.next/static
```

Now that the app is built, you can run it using:
```bash
node .next/standalone/server.js
```
Navigate to `http://localhost:3000` to see the app (or the address shown in the console).

For more detailed documentation and to develop the app, see `Sources/frontend/Readme.md`.

### Build and Run Simulation Server

> Requirements: Java

The backend server is written in Java using version `17.0.6`. Gradle is bundled with the project, so you don't need to install it.

To build the backend server, navigate to `Sources/simulator` and run:

```bash
./scripts/install.sh
```

To use the app (either CLI or server), run `./scripts/run.sh help` to see the available options.

To run the server, type `./scripts/run.sh server`.

For more detailed documentation, see `Sources/simulator/Readme.md`.

### HTTPS

Add SSL/TLS certificates to `Sources/proxy/certs` to enable HTTPS.
An Nginx proxy is created as a Docker container during the startup.
Note, that this step is not necessary to run the app, but the nginx container will fail to start.
For details, see [Sources/proxy/Readme.md](Sources/proxy/Readme.md).

### Docker

Below is the recommended way to build and run the project using Docker.

```bash
cd Sources
docker compose up
```

or, in case you do not want to setup HTTPS certificates:

```bash
cd Sources
docker compose -f docker-compose.http.yml up
```

The Docker compose files located at `Sources/` build the frontend and backend and runs them together with nginx proxy.

Note that sudo might be required to run the docker commands.

> Note that older Docker versions use command `docker-compose` instead of `docker compose`. Developed using Docker `24.0.7` and `20.10.2`.

There is an alternative way to build the containers - scripts `Sources/build_container.sh`, `Sources/run_container.sh` and `Sources/stop_container.sh` to run and stop the container.

```bash
cd Sources
./build_container.sh
./run_container.sh
```

After running the container, the state of the app should be:
1. You chose the `.http.yml` file and the app is available on port 3120.
2. You supplied keys to `Sources/proxy/certs` and chose the `.yml` file. The app is available on port 3120 (http) and 3121 (https) (do check the `http://` prefix).

## Nix

Another way to use the project is through Nix.
I prefer this method as it is more reproducible and doesn't require installing anything manually.

- Run `nix run .#simulator` to run the simulator and `nix run .#frontend` to run the frontend.
- Run `nix build .#frontend-docker` (or `#backend-docker`) to create a Docker image. Load it with `docker load < result`.
- Run `nix develop` in the root directory to enter the development environment with all the necessary tools.

