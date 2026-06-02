import { mkdir, readFile, rm, writeFile } from "node:fs/promises";
import { dirname, resolve } from "node:path";
import { fileURLToPath } from "node:url";

const rootDir = resolve(dirname(fileURLToPath(import.meta.url)), "..");
const srcFile = resolve(rootDir, "src/index.js");
const distDir = resolve(rootDir, "dist");
const mjsFile = resolve(distDir, "index.mjs");
const cjsFile = resolve(distDir, "index.cjs");

const source = await readFile(srcFile, "utf8");
const cjsSource = source.replace(/\nexport default fuIds;\s*$/u, "\nmodule.exports = fuIds;\n");

if (cjsSource === source) {
  throw new Error("Build failed: cannot find default export marker");
}

await rm(distDir, { recursive: true, force: true });
await mkdir(distDir, { recursive: true });
await writeFile(mjsFile, source, "utf8");
await writeFile(cjsFile, cjsSource, "utf8");
