#!/usr/bin/env bash
# 一键打包（Linux / macOS）：前端构建后放入后端 jar，产出 release/gdpm-system.jar。
# 依赖：JDK 11+、Node.js 18+（npm）。Maven 可选，未安装时自动使用 backend/mvnw。
# 用法：bash scripts/build-release.sh [--skip-tests]
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
SKIP_TESTS=""
[[ "${1:-}" == "--skip-tests" ]] && SKIP_TESTS="-DskipTests"

command -v java >/dev/null || { echo "未找到 java，请先安装 JDK 11 或以上"; exit 1; }
command -v npm >/dev/null || { echo "未找到 npm，请先安装 Node.js 18 或以上"; exit 1; }

echo "[1/3] 构建前端"
cd "$ROOT/frontend"
npm ci --no-audit --no-fund
npm run build

echo "[2/3] 前端产物放入后端静态资源"
STATIC="$ROOT/backend/src/main/resources/static"
rm -rf "$STATIC"
mkdir -p "$STATIC"
cp -r "$ROOT/frontend/dist/." "$STATIC/"

echo "[3/3] 打包后端"
cd "$ROOT/backend"
if command -v mvn >/dev/null; then MVN=mvn; else chmod +x mvnw; MVN=./mvnw; fi
$MVN -B package $SKIP_TESTS

mkdir -p "$ROOT/release"
cp "$ROOT/backend/target/gdpm-system-v3.jar" "$ROOT/release/gdpm-system.jar"
echo "完成：$ROOT/release/gdpm-system.jar"
echo "运行：java -jar release/gdpm-system.jar   然后访问 http://服务器IP:8080"
