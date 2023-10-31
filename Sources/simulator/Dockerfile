FROM gradle:8.4.0-jdk17-jammy AS base

# Install dependencies only when needed
FROM base AS deps
RUN apt-get update && apt-get install -y gcc-riscv64-linux-gnu
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
RUN gradle build