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


## Build instruction

### Build frontend web app

The app was developed using npm `9.3.1` and node.js `v18.4.0`.

To build the production version of the app, start by navigating to `Sources/frontend` and installing dependencies:

```bash
npm install
```

To build the app, run:

```bash
npm run build
```

The production version of the app should be in the `.next` folder.

To develop the app, see `Sources/frontend/Readme.md`.

## Usage instruction


## Author information

 * Name: Michal Majer
 * Email: xmajer21@stud.fit.vutbr.cz, misa@majer.cz
 * Data: 2023/2024
 * Phone: +420 773 959 458
