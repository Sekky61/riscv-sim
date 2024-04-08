#!/bin/bash

# build_container.sh
# Description: Build the docker containers
# Author: Michal Majer

# You may need to run this script with sudo

# Run docker-compose
docker compose -f "docker-compose.yml" build
