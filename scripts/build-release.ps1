# 一键打包（Windows PowerShell）：前端构建后放入后端 jar，产出 release\gdpm-system.jar。
# 依赖：JDK 11+、Node.js 18+（npm）。Maven 可选，未安装时自动使用 backend\mvnw.cmd。
# 用法：powershell -ExecutionPolicy Bypass -File scripts\build-release.ps1 [-SkipTests]
param([switch]$SkipTests)
$ErrorActionPreference = 'Stop'
$root = Split-Path $PSScriptRoot -Parent

if (-not (Get-Command java -ErrorAction SilentlyContinue) -and -not $env:JAVA_HOME) { throw '未找到 java，请先安装 JDK 11 或以上' }
if (-not (Get-Command npm.cmd -ErrorAction SilentlyContinue)) { throw '未找到 npm，请先安装 Node.js 18 或以上' }

Write-Host '[1/3] 构建前端'
Push-Location "$root\frontend"
try {
    & npm.cmd ci --no-audit --no-fund
    if ($LASTEXITCODE -ne 0) { throw 'npm ci 失败' }
    & npm.cmd run build
    if ($LASTEXITCODE -ne 0) { throw 'npm run build 失败' }
} finally { Pop-Location }

Write-Host '[2/3] 前端产物放入后端静态资源'
$static = "$root\backend\src\main\resources\static"
if (Test-Path $static) { Remove-Item -Recurse -Force $static }
New-Item -ItemType Directory -Force $static | Out-Null
Copy-Item -Recurse "$root\frontend\dist\*" $static

Write-Host '[3/3] 打包后端'
Push-Location "$root\backend"
try {
    $mvnArgs = @('-B', 'package')
    if ($SkipTests) { $mvnArgs += '-DskipTests' }
    if (Get-Command mvn -ErrorAction SilentlyContinue) { & mvn @mvnArgs } else { & .\mvnw.cmd @mvnArgs }
    if ($LASTEXITCODE -ne 0) { throw 'Maven 打包失败' }
} finally { Pop-Location }

New-Item -ItemType Directory -Force "$root\release" | Out-Null
Copy-Item "$root\backend\target\gdpm-system-v3.jar" "$root\release\gdpm-system.jar" -Force
Write-Host "完成：$root\release\gdpm-system.jar"
Write-Host '运行：java -jar release\gdpm-system.jar   然后访问 http://localhost:8080'
