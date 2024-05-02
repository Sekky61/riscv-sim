# RISV-V Simulator

This Java application provides a simulator for the RISC-V superscalar processor.
It operates in two modes: CLI and HTTP server.

## Installation

Simply run the bash script `scripts/install.sh`. In case of troubles, make sure you have Java 17 installed.

```bash
./scripts/install.sh
```

The script should be runnable from any directory.

## Usage

The build should create entry point `build/install/superscalar-simulator/bin/superscalar-simulator`.

Alternatively, a `.zip` and a `.tar` file are also created in `build/distributions`.
It includes all dependencies and can be run on any machine with Java 17 installed.
Unzip the file and run the executable inside `bin`.

The app can be run directly (see above) or using the bash script `scripts/run.sh`.
This script conveniently wraps the `superscalar-simulator` executable.

The results are printed to the stdout.
Logs are printed to the stderr.

### Running the CLI

```bash
./scripts/run.sh cli [options]
```

To see all the available options, run:

```bash
./scripts/run.sh cli help
```

### Running the server

```bash
./scripts/run.sh server
```

To see all the available options, run:

```bash
./scripts/run.sh server help
```

### Examples

Find example invocations and example data in the `examples` directory.

Use of the simulator requires configuration and program (and optionally data, see `help`).

```bash
./scripts/run.sh cli --cpu=examples/cpuConfigurations/default.json --program=examples/asmPrograms/basicFloatArithmetic.r5 --pretty
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
./scripts
  +--install.sh      - Bash script to install the app
  +--run.sh          - Bash script to run the app
  +--testExamples.sh - Bash script to test example programs
  +--runProd.sh      - Bash script to run the app in production mode
Readme.md       - Read me file
```

## Development

Pre-requisites:

1. Java 17
2. IntelliJ IDEA (ideal for testing, formatting)

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

Run Java tests using the following command:

```bash
./gradlew test
```

Run the script that tests the example programs:

```bash
./scripts/testExamples.sh
```

### Generating code coverage report

Find the gradle task `jacocoTestReport` in the Gradle tool window.
Run the task to generate the report.
`build/reports/jacoco/test/html/index.html` will contain the report.

## Using `jq` to inspect JSON

The simulator outputs JSON.
Automating the inspection of simulation results can be done using `jq` ([GitHub](https://github.com/jqlang/jq)).

For example, to extract the number of cycles from the JSON output:

```bash
./scripts/run.sh cli --cpu=examples/cpuConfigurations/default.json --program=examples/asmPrograms/basicFloatArithmetic.r5 | jq '.statistics.clockCycles'
```

Find out how many times an instruction was committed:

```bash
... | jq '.statistics.instructionStats[2].committedCount'
```

Value of a register:

```bash
... | jq '.registerValues.x4'
```

Program ended by running out of instructions:

```bash
... | jq '.stopReason == "kEndOfCode"'
```

Number of logged messages:

```bash
... | jq '.debugLog.entries | length'
```

Is there a logged message with a specific content?

```bash
...  | jq '.debugLog.entries | map(select(.message | contains("Value of x3 is")))'
```

To also extract the messages:

```bash
... | jq '.debugLog.entries | map(select(.message | contains("Value of x3"))) | .[] .message'
```

## Benchmarking

To run a specific benchmark, use the following command:

```bash
java -jar build/libs/superscalar-simulator-1.0-jmh.jar -i 5  "CpuLoopBenchmark"
```
