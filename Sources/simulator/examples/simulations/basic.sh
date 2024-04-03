#!/bin/bash
# Run a simulation with default CPU configuration, no memory, and a basic non-branching assembly program
./scripts/run.sh cli --cpu=examples/cpuConfigurations/default.json --program=examples/asmPrograms/basicFloatArithmetic.r5 --pretty
