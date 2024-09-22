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

## Installation Instructions

> TLDR: cd Sources && ./build_container.sh && ./run_container.sh

You can either build the project locally or use docker.
First the manual build is described, then the docker build.

If anything is unclear, you can refer to the Dockerfiles, which contain all the necessary steps.
The `Sources/frontend` and `Sources/simulator` also contain their own, more detailed instructions.

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

Add SSL/TLS certificates to `Sources/proxy/certs` to enable HTTPS. A Nginx proxy is created as a Docker container during the startup.
Note, that this step is not necessary to run the app.
For details, see [Sources/proxy/Readme.md](Sources/proxy/Readme.md).

### Docker

The two components have their respective Dockerfiles in their directories.
There is a docker compose file located at `Sources/`. It builds the frontend and backend and runs them together.

There are prepared scripts `Sources/build_container.sh`, `Sources/run_container.sh` and `Sources/stop_container.sh` to run and stop the container.
Note that sudo might be required to run the docker commands.
Also note that older Docker versions use command `docker-compose` instead of `docker compose`.

Developed using Docker `24.0.7` and `20.10.2` (version on `sc-gpu1` server).

Below is the recommended way to build and run the project using Docker.

```bash
cd Sources
./build_container.sh
./run_container.sh
```

Once the containers are running, one of two things can happen:

1. You supplied keys to `Sources/proxy/certs` and the app is available on port 3120 (http) and 3121 (https).

2. You didn't supply keys and the app is available on port 3100 (http).

