#!/bin/bash

JMETER_PATH=apache-jmeter-5.6.2/bin/jmeter

# This script generates a report from the load testing results.
# -n: non-GUI mode
# -t: test plan
# -l: log file
$JMETER_PATH -n -t compiling_group.jmx -l results.jtl

# Generate the report to HTML
$JMETER_PATH -g results.jtl -o report
