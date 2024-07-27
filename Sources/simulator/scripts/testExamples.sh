#!/bin/bash
# Must be run from the root directory of the project

# For every example shell script in examples/simulations, run it. It should exit with status 0 and output JSON containing the word "statitics".
# Run the tests from the root directory of the simulator (Sources/simulator)

# Get the file paths of all the example shell scripts
exampleScripts=$(find examples/simulations -name "*.sh")

# Run each
for exampleScript in $exampleScripts; do
  echo "Running $exampleScript"
  out=$($exampleScript)
  if [ $? -ne 0 ]; then
    echo "Failed to run $exampleScript"
    exit 1
  fi
  if [[ $out != *"statistics"* ]]; then
    echo "Failed to find statistics in the output of $exampleScript"
    exit 1
  fi
  echo "Success"
done

