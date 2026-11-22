param([switch]$NoBrowser)
. "$PSScriptRoot/common.ps1"
Initialize-Tools
$backendPort = if ($env:GDPM_BACKEND_PORT) { [int]$env:GDPM_BACKEND_PORT } else { 8080 }
$frontendPort = if ($env:GDPM_FRONTEND_PORT) { [int]$env:GDPM_FRONTEND_PORT } else { 5173 }
$healthUrl = "http://127.0.0.1:$backendPort/api/health"
$pageUrl = "http://127.0.0.1:$frontendPort"
$runtimePath = Join-Path $runDir 'runtime.json'
$runtime = @{}
if (Test-Path -LiteralPath $runtimePath) {
    $previous = Get-Content -LiteralPath $runtimePath -Raw -Encoding UTF8 | ConvertFrom-Json
    if ((Test-RecordedProcess $previous 'backend') -and (Test-RecordedProcess $previous 'frontend') -and
        $previous.backendPort -eq $backendPort -and $previous.frontendPort -eq $frontendPort) {
        try {
            $health = Invoke-RestMethod $healthUrl -TimeoutSec 3
            $page = Invoke-WebRequest $pageUrl -UseBasicParsing -TimeoutSec 3
            if ($health.application -eq 'gdpm' -and $health.status -eq 'UP' -and $page.StatusCode -eq 200) {
                Write-Host "GDPM is already ready: $pageUrl"
                if (!$NoBrowser) { Start-Process $pageUrl }
                exit 0
            }
        } catch {}
    }
    if ((Test-RecordedProcess $previous 'backend') -or (Test-RecordedProcess $previous 'frontend')) {
        throw '已有本项目进程，请先运行 stop.bat 后重试。'
    }
}
foreach ($port in @($backendPort, $frontendPort)) {
    if (Test-Port $port) { throw "端口 $port 已被占用，请先停止相应服务或配置其他端口。" }
}
Write-Host '[1/4] Packaging backend...'
Invoke-Maven @('package')
Push-Location -LiteralPath (Join-Path $projectRoot 'frontend')
try {
    if (!(Test-Path -LiteralPath 'node_modules/.package-lock.json')) {
        Write-Host '[2/4] Installing npm dependencies...'
        & $script:npm ci --no-audit --no-fund
        if ($LASTEXITCODE -ne 0) { throw 'npm ci 失败。' }
    }
    Write-Host '[3/4] Building frontend...'
    & $script:npm run build
    if ($LASTEXITCODE -ne 0) { throw 'npm run build 失败。' }
} finally { Pop-Location }
function Save-Runtime {
    $runtime | ConvertTo-Json | Set-Content -LiteralPath $runtimePath -Encoding UTF8
}
function Record-Process($Entry, [string]$Prefix) {
    $runtime.($Prefix + 'Pid') = $Entry.Id
    $runtime.($Prefix + 'StartedTicks') = [string]$Entry.StartTime.ToUniversalTime().Ticks
    $runtime.($Prefix + 'Executable') = $Entry.Path
    Save-Runtime
}
try {
    Write-Host '[4/4] Starting services...'
    $runtime.backendPort = $backendPort
    $runtime.frontendPort = $frontendPort
    $backend = Start-Process -FilePath $script:java -WorkingDirectory (Join-Path $projectRoot 'backend') -WindowStyle Hidden `
        -ArgumentList @('-Dfile.encoding=UTF-8', '-Djava.io.tmpdir=../run/tmp', '-jar',
            'target/gdpm-system-v3.jar', "--server.port=$backendPort") -PassThru `
        -RedirectStandardOutput (Join-Path $runDir 'backend.out.log') `
        -RedirectStandardError (Join-Path $runDir 'backend.err.log')
    Record-Process $backend 'backend'
    $ready = $false
    for ($i = 0; $i -lt 120; $i++) {
        try {
            $health = Invoke-RestMethod $healthUrl -TimeoutSec 2
            if ($health.status -eq 'UP') { $ready = $true; break }
        } catch {}
        if ($backend.HasExited) { break }
        Start-Sleep -Milliseconds 500
    }
    if (!$ready) { throw '后端启动失败，请查看 run/backend.out.log 和 run/backend.err.log。' }
    $env:GDPM_API_TARGET = "http://127.0.0.1:$backendPort"
    $frontend = Start-Process -FilePath $script:node -WorkingDirectory $projectRoot -WindowStyle Hidden `
        -ArgumentList @('frontend/node_modules/vite/bin/vite.js', 'preview', 'frontend', '--host', '127.0.0.1',
            '--port', $frontendPort, '--strictPort') -PassThru `
        -RedirectStandardOutput (Join-Path $runDir 'frontend.out.log') `
        -RedirectStandardError (Join-Path $runDir 'frontend.err.log')
    Record-Process $frontend 'frontend'
    $ready = $false
    for ($i = 0; $i -lt 60; $i++) {
        try {
            $health = Invoke-RestMethod "$pageUrl/api/health" -TimeoutSec 2
            if ($health.status -eq 'UP') { $ready = $true; break }
        } catch {}
        if ($frontend.HasExited) { break }
        Start-Sleep -Milliseconds 500
    }
    if (!$ready) { throw '前端启动失败，请查看 run/frontend.err.log。' }
    Write-Host "GDPM is ready: $pageUrl"
    Write-Host "Backend: $healthUrl"
    if (!$NoBrowser) { Start-Process $pageUrl }
} catch {
    & "$PSScriptRoot/stop.ps1"
    throw
}
