# Alpine image - originally ubuntu, but caused issues with older docker versions
FROM alpine:3.18 AS base

# Install dependencies only when needed
FROM base AS deps
# locking to specific versions threw errors
RUN apk add \
  gcc-riscv-none-elf \
  maven
# RUN /usr/bin/riscv-none-elf-gcc-13.1.0 --version
RUN /usr/bin/riscv-none-elf-gcc --version
WORKDIR /app

FROM deps AS build
WORKDIR /app
COPY . . 

# Package stage

FROM deps AS runner
WORKDIR /app
COPY . . 
EXPOSE 8000
RUN ./scripts/install.sh
CMD ./scripts/run.sh server --host=0.0.0.0 --port=8000 --gcc-path=/usr/bin/riscv-none-elf-gcc

FROM deps AS test
WORKDIR /app
COPY . . 
# Run mvn test with property gcc.path
RUN mvn test -Dgcc.path=/usr/bin/riscv-none-elf-gcc -Dconfig.profile=prod

