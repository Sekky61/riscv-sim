FROM oven/bun:latest AS base

# Install dependencies only when needed
FROM base AS deps
WORKDIR /app

COPY package.json ./
COPY bun.lockb ./
COPY public/ ./

RUN bun install --frozen-lockfile

# Rebuild the source code only when needed
FROM base AS builder
WORKDIR /app
COPY --from=deps /app/node_modules ./node_modules
COPY . .

# The available args and default values are defined here

ARG INTERNAL_SIM_API_PREFIX=simserver:8000
ENV NEXT_PUBLIC_INTERNAL_SIM_API_PREFIX=$INTERNAL_SIM_API_PREFIX

ARG EXTERNAL_SIM_API_PREFIX="http://localhost:8000"
ENV NEXT_PUBLIC_EXTERNAL_SIM_API_PREFIX=$EXTERNAL_SIM_API_PREFIX

ARG BASE_PATH=""
ENV NEXT_PUBLIC_BASE_PATH=$BASE_PATH

# End of args

RUN bun run build

# Production image, copy all the files and run next
FROM base AS runner
WORKDIR /app

ENV NODE_ENV=production

# Copy the public directory from the builder stage
COPY --from=builder /app/public ./public

# Set the correct permission for prerender cache
RUN mkdir .next

COPY --from=builder --chown=nextjs:nodejs /app/.next/standalone ./
COPY --from=builder --chown=nextjs:nodejs /app/.next/static ./.next/static

EXPOSE 3000

ENV PORT=3000
# set hostname to localhost
ENV HOSTNAME="0.0.0.0"

CMD ["bun", "server.js"]

