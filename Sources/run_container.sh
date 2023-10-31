#!/bin/bash

# run_container.sh
# Description: Run the docker container (both web server and simulator)
# Author: Michal Majer

# You may need to run this script with sudo

# Run docker-compose
docker-compose -f "docker-compose.yml" up -d --build
