#!/bin/bash
# Run a simulation with default CPU configuration, no memory, and a basic non-branching assembly program
./run.sh cli --cpu=examples/cpuConfigurations/default.json --program=examples/asmPrograms/writeToArray.r5 --memory=examples/memory/ptr.json --pretty
