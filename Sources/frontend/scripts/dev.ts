import { spawn } from "node:child_process";
import path from "node:path";

const wasmDestination = path.join(process.cwd(), "public/wasm/");

console.log("Starting development environment...");

// Start Zig watcher
const zigWatcher = spawn("bash", ["scripts/watch_wasm.sh", wasmDestination], {
  cwd: "../simulator_zig",
  stdio: "inherit",
});

// Start Next.js dev server
const nextDev = spawn("bunx", ["next", "dev"], {
  stdio: "inherit",
});

// Handle process termination
const cleanup = () => {
  console.log("Shutting down processes...");
  zigWatcher.kill();
  nextDev.kill();
  process.exit(0);
};

process.on("SIGINT", cleanup);
process.on("SIGTERM", cleanup);

// Handle child process errors
zigWatcher.on("error", (error) => {
  console.error("Zig watcher error:", error);
  cleanup();
});

nextDev.on("error", (error) => {
  console.error("Next.js dev error:", error);
  cleanup();
});

// Handle child process exits
zigWatcher.on("exit", (code) => {
  if (code !== 0 && code !== null) {
    console.error(`Zig watcher exited with code ${code}`);
    cleanup();
  }
});

nextDev.on("exit", (code) => {
  if (code !== 0 && code !== null) {
    console.error(`Next.js dev exited with code ${code}`);
    cleanup();
  }
});
