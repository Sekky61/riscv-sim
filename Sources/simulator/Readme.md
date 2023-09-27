## Running the app

Run the bash script `run.sh`. Options will be passed to the app.

```bash
./run.sh --server
```

## Development

Pre-requisites:

1. Java 17
2. IntelliJ IDEA

Opening the project for the first time:

1. Open the project in IntelliJ IDEA (version 2022.3 was used for development)
2. `Open file or project`, select the `simulator` directory (select _trust project_ if prompted)
3. `build/build project` should successfully build the project
4. Right clicking the `src/test` directory and selecting `Run 'All Tests'` should run all tests successfully
5. Run configurations (such as `Server run`) should be loaded from the `.idea` directory

## Testing from the command line

Run:

```bash
./gradlew test
```

## Creating an executable

Run:

```bash
./gradlew assemble
```

A `.zip` file will be created in `build/distributions`.
It includes all dependencies and can be run on any machine with Java 17 installed.
Unzip the file and run the executable in `bin`.
