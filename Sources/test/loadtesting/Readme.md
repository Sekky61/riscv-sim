
# Load testing

## Installation

1. Download and install [jmeter](https://jmeter.apache.org/download_jmeter.cgi)
2. Open the `.jmx` file in jmeter

## Generate report

1. Run the `generate_report.sh` script
2. Open the `report/index.html` file in your browser

You may need to change the JMETER_PATH variable in the script. For example:
```bash
JMETER_PATH="apache-jmeter-5.6.2/bin/jmeter" ./generate_report.sh 
```
