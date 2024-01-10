# Alpine image - originally ubuntu, but caused issues with olfer docker versions
FROM alpine:3.18 AS base

# Install dependencies only when needed
FROM base AS deps
# locking to specific versions threw errors
RUN apk add \
  gcc-riscv-none-elf \
  gradle
# RUN /usr/bin/riscv-none-elf-gcc-13.1.0 --version
RUN /usr/bin/riscv-none-elf-gcc --version
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
CMD gradle run --args="server --host=0.0.0.0 --port=8000" -Dconfig.profile=prod

FROM deps as test
WORKDIR /app
COPY . . 
RUN gradle build -i -Dconfig.profile=prod
