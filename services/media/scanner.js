import { spawn } from "node:child_process";

export function scanFile(filePath, { command = process.env.CLAMSCAN_COMMAND || "clamscan", required = process.env.NODE_ENV === "production" } = {}) {
  return new Promise((resolve, reject) => {
    const child = spawn(command, ["--no-summary", "--infected", filePath], { stdio: ["ignore", "pipe", "pipe"] });
    let output = "";
    child.stdout.on("data", (chunk) => { output += chunk.toString(); });
    child.stderr.on("data", (chunk) => { output += chunk.toString(); });
    child.on("error", (error) => { if (required) reject(new Error("MALWARE_SCANNER_UNAVAILABLE")); else resolve({ state: "quarantined", reason: "scanner_unavailable", output: output.slice(0, 500) }); });
    child.on("close", (code) => {
      if (code === 0) return resolve({ state: "clean" });
      if (code === 1) return resolve({ state: "quarantined", reason: "malware_detected" });
      if (required) return reject(new Error("MALWARE_SCAN_FAILED"));
      return resolve({ state: "quarantined", reason: "scan_failed" });
    });
  });
}
