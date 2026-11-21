. "$PSScriptRoot/common.ps1"
$runtimePath = Join-Path $runDir 'runtime.json'
if (!(Test-Path -LiteralPath $runtimePath)) { Write-Host '未找到本项目启动记录。'; exit 0 }
$runtime = Get-Content -LiteralPath $runtimePath -Raw -Encoding UTF8 | ConvertFrom-Json
$failed = $false
foreach ($prefix in @('frontend', 'backend')) {
    if (Test-RecordedProcess $runtime $prefix) {
        try {
            $processId = $runtime.($prefix + 'Pid')
            Stop-Process -Id $processId -ErrorAction Stop
            Wait-Process -Id $processId -Timeout 15 -ErrorAction SilentlyContinue
            Write-Host "Stopped $prefix ($processId)."
        } catch { $failed = $true; Write-Warning "无法停止 $prefix，请使用启动服务的同一用户运行 stop.bat。" }
    }
}
if ($failed) { throw '停止失败，已保留启动记录。' }
Remove-Item -LiteralPath $runtimePath
