. "$PSScriptRoot/common.ps1"
Initialize-Tools
$previewDir = Join-Path $runDir 'preview-v3'
New-Item -ItemType Directory -Path $previewDir -Force | Out-Null
foreach ($port in @(18080, 15173)) {
    if (Test-Port $port) { throw "预览端口 $port 已被占用。" }
}
Copy-Item -LiteralPath (Join-Path $projectRoot 'backend/target/gdpm-system-v3.jar') `
    -Destination (Join-Path $previewDir 'server.jar') -Force
$backend = Start-Process -FilePath $script:java -WorkingDirectory $projectRoot -WindowStyle Hidden `
    -ArgumentList @('-Dfile.encoding=UTF-8', '-Djava.io.tmpdir=run/tmp', '-jar', 'run/preview-v3/server.jar',
        '--server.port=18080', '--spring.profiles.active=dev',
        '--spring.datasource.url=jdbc:h2:file:./run/preview-v3/data;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_ON_EXIT=FALSE',
        '--app.upload-dir=./run/preview-v3/uploads') -PassThru `
    -RedirectStandardOutput (Join-Path $previewDir 'backend.log') `
    -RedirectStandardError (Join-Path $previewDir 'backend.err.log')
$env:GDPM_API_TARGET = 'http://127.0.0.1:18080'
$frontend = Start-Process -FilePath $script:node -WorkingDirectory $projectRoot -WindowStyle Hidden `
    -ArgumentList @('frontend/node_modules/vite/bin/vite.js','preview','frontend','--host','127.0.0.1',
        '--port','15173','--strictPort') -PassThru `
    -RedirectStandardOutput (Join-Path $previewDir 'frontend.log') `
    -RedirectStandardError (Join-Path $previewDir 'frontend.err.log')
$record = @{}
foreach ($prefix in @('backend', 'frontend')) {
    $entry = Get-Variable -Name $prefix -ValueOnly
    $record[($prefix+'Pid')] = $entry.Id
    $record[($prefix+'StartedTicks')] = [string]$entry.StartTime.ToUniversalTime().Ticks
    $record[($prefix+'Executable')] = $entry.Path
}
$record | ConvertTo-Json | Set-Content -LiteralPath (Join-Path $previewDir 'runtime.json') -Encoding UTF8
Write-Host '第三轮预览：http://127.0.0.1:15173（后端18080）'
