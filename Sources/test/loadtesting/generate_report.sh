#!/bin/bash

# This script generates a report from the load testing results.

# Try to find the JMeter path
# apache-jmeter-5.6.2/bin/jmeter
JMETER_PATH_DEFAULT=jmeter
JMETER_PATH="${JMETER_PATH:-$JMETER_PATH_DEFAULT}"

echo "Used JMeter path: $JMETER_PATH"

# Check if JMeter is installed
if ! [ -x "$(command -v $JMETER_PATH)" ]; then
  echo "Error: JMeter is not installed or not found." >&2
  exit 1
fi

# clear the previous results
rm -r report

# This script generates a report from the load testing results.
# -n: non-GUI mode
# -t: test plan
# -l: log file
$JMETER_PATH -n -t compiling_group.jmx -Jjmeter.reportgenerator.overall_granularity=1500 -l results.jtl

# Generate the report to HTML
$JMETER_PATH -g results.jtl -o report
