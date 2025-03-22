import { spawnSync } from "node:child_process";
import path from "node:path";

const wasmDestination = path.join(process.cwd(), "public/wasm/");

console.log("Building project... from ", process.cwd());

// Build Zig WASM
const zigBuild = spawnSync("bash", ["scripts/build_wasm.sh", wasmDestination], {
  cwd: "../simulator_zig",
  stdio: "inherit",
});

if (zigBuild.status !== 0) {
  console.error("Zig build failed");
  process.exit(1);
}

// Build Next.js
const nextBuild = spawnSync("bunx", ["next", "build"], {
  stdio: "inherit",
});

if (nextBuild.status !== 0) {
  console.error("Next.js build failed");
  process.exit(1);
}

console.log("Build completed successfully!");
