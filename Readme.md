# Web Based Simulator of Superscalar Processors

## Introduction

This repository extends the superscalar simulator created by Jakub Horky and Jan Vavra. The goal is to add a WEB and CLI interface.

## Repository structure

    .
    +--Data       - Example data, measured results, etc.
    +--Literature - Publications, references, manuals, etc.
    +--Sources    - Root folder for the sources.
    +--Thesis     - Latex / MS Word sources of the thesis.
    +--Misc       - Other auxiliary materials.
    Readme.md     - Read me file


## Build instructions

You can either build the project locally or use docker.
First the manual build is described, then the docker build.
If anything is unclear, you can refer to the Dockerfiles, which contain all the necessary steps.

### Build frontend web app

The app was developed using npm `9.3.1` and node.js `v18.4.0`.
You need to have these installed to build the frontend app (later versions should work as well).

To build the production version of the app, start by navigating to `Sources/frontend` and installing dependencies:

```bash
npm install
```

To build the app, run:

```bash
npm run build
```

The production version of the app should be in the `.next` folder.
There, you can run the app using:

```bash
node server.js
```

To develop the app, see `Sources/frontend/Readme.md`.

### Build backend server

The backend server is written in Java using version `17.0.6`. Gradle is bundled with the project.

To build the backend server, navigate to `Sources/simulator` and run:

```bash
./run.sh server
```
(see `--help` for more options).

### Docker

There is docker compose file in the `Sources`. It builds the frontend and backend and runs them together.
There is a prepared script `Sources/run_container.sh` and `Sources/stop_container.sh` to run and stop the container.

Developed using Docker `24.0.7`.

## Author information

 * Name: Michal Majer
 * Email: xmajer21@stud.fit.vutbr.cz, misa@majer.cz
 * Data: 2023/2024
 * Phone: +420 773 959 458
