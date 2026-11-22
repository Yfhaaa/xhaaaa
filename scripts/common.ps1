$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path $PSScriptRoot -Parent
$runDir = Join-Path $projectRoot 'run'
New-Item -ItemType Directory -Path $runDir -Force | Out-Null
$localEnv = Join-Path $PSScriptRoot 'local.env.ps1'
if (Test-Path -LiteralPath $localEnv) { . $localEnv }

function Initialize-Tools {
    foreach ($key in @('HTTP_PROXY', 'HTTPS_PROXY', 'ALL_PROXY', 'NPM_CONFIG_PROXY', 'NPM_CONFIG_HTTPS_PROXY')) {
        Remove-Item "Env:$key" -ErrorAction SilentlyContinue
    }
    [Net.WebRequest]::DefaultWebProxy = New-Object Net.WebProxy
    $env:MAVEN_OPTS = "$env:MAVEN_OPTS -Dfile.encoding=UTF-8 -Djansi.passthrough=true"
    $env:TEMP = Join-Path $runDir 'tmp'
    $env:TMP = $env:TEMP
    New-Item -ItemType Directory -Path $env:TEMP -Force | Out-Null
    if (!$env:NPM_CONFIG_REGISTRY) { $env:NPM_CONFIG_REGISTRY = 'https://registry.npmmirror.com' }
    if (!$env:NPM_CONFIG_CACHE) { $env:NPM_CONFIG_CACHE = Join-Path $runDir 'npm-cache' }
    if (!$env:MAVEN_USER_HOME) { $env:MAVEN_USER_HOME = '../run/maven' }
    $script:java = $null
    if ($env:JAVA_HOME) { $script:java = Join-Path $env:JAVA_HOME 'bin/java.exe' }
    if (!$script:java -or !(Test-Path -LiteralPath $script:java)) {
        $command = Get-Command java.exe -ErrorAction SilentlyContinue
        if ($command) { $script:java = $command.Source }
    }
    if (!$script:java) { throw '未找到 JDK，请安装 JDK 11 或更高版本，并设置 JAVA_HOME 或 PATH。' }
    $savedPreference = $ErrorActionPreference
    try {
        $ErrorActionPreference = 'Continue'
        $versionText = (& $script:java -XshowSettings:properties -version 2>&1 | Out-String)
    } finally { $ErrorActionPreference = $savedPreference }
    if ($versionText -notmatch 'java.version = (\d+)') { throw '无法识别 Java 版本，请配置 JDK 11+。' }
    if ([int]$Matches[1] -lt 11) { throw 'Java 版本过低，请安装 JDK 11 或更高版本。' }
    if ($versionText -match 'java.home = ([^\r\n]+)') { $env:JAVA_HOME = $Matches[1].Trim() }
    if (!(Test-Path -LiteralPath (Join-Path $env:JAVA_HOME 'bin/javac.exe'))) {
        throw '当前 Java 不包含 javac，请配置完整 JDK 11+，而不是 JRE。'
    }
    $script:java = Join-Path $env:JAVA_HOME 'bin/java.exe'
    $nodeCommand = Get-Command node.exe -ErrorAction SilentlyContinue
    $npmCommand = Get-Command npm.cmd -ErrorAction SilentlyContinue
    if (!$nodeCommand -or !$npmCommand) { throw '未找到 Node.js/npm，请安装 Node.js 18+ 并加入 PATH。' }
    $script:node = $nodeCommand.Source
    $script:npm = $npmCommand.Source
    $nodeVersion = ((& $script:node --version).TrimStart("v").Split("."))[0]
    if ([int]$nodeVersion -lt 18) { throw 'Node.js 版本过低，请安装 Node.js 18+。' }
}

function Invoke-Maven([string[]]$Goals) {
    $mavenCommand = Get-Command mvn.cmd -ErrorAction SilentlyContinue
    $maven = if ($mavenCommand) { $mavenCommand.Source } else { $null }
    if (!$maven -and $env:MAVEN_HOME) {
        $candidate = Join-Path $env:MAVEN_HOME 'bin/mvn.cmd'
        if (Test-Path -LiteralPath $candidate) { $maven = $candidate }
    }
    if (!$maven) { $maven = Join-Path $projectRoot 'backend/mvnw.cmd' }
    $settings = if ($env:GDPM_MAVEN_SETTINGS) { $env:GDPM_MAVEN_SETTINGS } else {
        Join-Path $PSScriptRoot 'maven-settings.xml'
    }
    Push-Location -LiteralPath (Join-Path $projectRoot 'backend')
    try {
        $relativeSettings = Resolve-Path -LiteralPath $settings -Relative
        & $maven -B -ntp -s $relativeSettings "-Dmaven.repo.local=../run/m2repo" @Goals
        if ($LASTEXITCODE -ne 0) { throw 'Maven 执行失败，请检查上述输出和镜像设置。' }
    } finally { Pop-Location }
}

function Test-Port([int]$Port) {
    $client = New-Object Net.Sockets.TcpClient
    try { $client.Connect('127.0.0.1', $Port); return $true } catch { return $false } finally { $client.Dispose() }
}

function Test-RecordedProcess($Record, [string]$Prefix) {
    $processId = $Record.($Prefix + 'Pid')
    if (!$processId) { return $false }
    $entry = Get-Process -Id $processId -ErrorAction SilentlyContinue
    return $entry -and $Record.($Prefix + 'StartedTicks') -eq [string]$entry.StartTime.ToUniversalTime().Ticks -and
        [string]::Equals($entry.Path, $Record.($Prefix + 'Executable'), [StringComparison]::OrdinalIgnoreCase)
}

