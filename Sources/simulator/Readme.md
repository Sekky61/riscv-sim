# RISV-V Simulator

This Java application provides a simulator for the RISC-V superscalar processor.
It operates in two modes: CLI and HTTP server.

## Installation

Run the bash script `install.sh`.

```bash
./install.sh
```

This should create entry point `build/install/superscalar-simulator/bin/superscalar-simulator`.

Alternatively, a `.zip` and a `.tar` file are also be created in `build/distributions`.
It includes all dependencies and can be run on any machine with Java 17 installed.
Unzip the file and run the executable in `bin`.

## Usage

The app can be run directly (see installation) or using the bash script `run.sh`.
This script conveniently wraps the `superscalar-simulator` executable.

### Running the CLI

```bash
./run.sh cli [options]
```

To see all the available options, run:

```bash
./run.sh cli help
```

### Running the server

```bash
./run.sh server [options]
```

To see all the available options, run:

```bash
./run.sh server help
```

## Structure

```
./src
  +--main
    +--resources    - Data files
    +--java         - Entry point, CLI
      +--com/gradle/superscalarsim
        +--blocks   - Blocks of the processor
        +--code     - parsing code, execution
        +--compiler - GCC, code filtering
        +--cpu      - state, sim loop, config
        +--enums    - Enums
        +--factories- Factory classes
        +--loader   - Loading static data
        +--managers
        +--models   - Data classes
        +--serialization - Serialization and deserialization to JSON
        +--server   - HTTP server, request handlers
  +--test - Tests
Readme.md       - Read me file
install.sh      - Bash script to install the app
run.sh          - Bash script to run the app
```

## Development

Pre-requisites:

1. Java 17
2. IntelliJ IDEA

Opening the project for the first time:

1. Open the project in IntelliJ IDEA (version 2022.3 was used for development)
2. `Open file or project`, select the `simulator` directory (select _trust project_ if prompted)
3. `build/build project` should successfully build the project
4. Right-clicking the `src/test` directory and selecting `Run 'All Tests'` should run all tests successfully
5. Run configurations (such as `Server run`) should be loaded from the `.idea` directory

### Formatting

The file `.idea/codeStyles/codeStyleConfig.xml` contains the formatting rules
for the project.
The rules follow the *SC@FIT Handbook* where sensible.
IntelliJ IDEA should automatically pick up these rules.

## Testing from the command line

Run:

```bash
./gradlew test
```
