. "$PSScriptRoot/common.ps1"
$recordPath = Join-Path $runDir 'preview-v3/runtime.json'
if (!(Test-Path -LiteralPath $recordPath)) { exit 0 }
$record = Get-Content -LiteralPath $recordPath -Raw | ConvertFrom-Json
foreach ($prefix in @('frontend', 'backend')) {
    if (Test-RecordedProcess $record $prefix) {
        Stop-Process -Id $record.($prefix + 'Pid') -ErrorAction Stop
        Wait-Process -Id $record.($prefix + 'Pid') -Timeout 15 -ErrorAction SilentlyContinue
    }
}
Remove-Item -LiteralPath $recordPath
