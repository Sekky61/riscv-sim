FROM gradle:8.4.0-jdk17-focal AS base

# Install dependencies only when needed
FROM base AS deps
# Ignore the error code from apt-get update - happens on old versions of docker
RUN apt-get update && apt-get install -y gcc-riscv64-linux-gnu; exit 0
WORKDIR /app

FROM deps AS build
WORKDIR /app
COPY . . 
# gradle build would run the tests as well
RUN gradle assemble

# Package stage

FROM deps AS runner
WORKDIR /app
COPY . . 
EXPOSE 8000
CMD gradle run --args="server --host=0.0.0.0 --port=8000"

FROM deps as test
WORKDIR /app
COPY . . 
RUN gradle build -i