$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent $PSScriptRoot
$stagingDir = Join-Path $repoRoot "build\elastic-beanstalk"
$bundleRoot = Join-Path $stagingDir "bundle"
$zipPath = Join-Path $stagingDir "gimtrackerbackend-elastic-beanstalk.zip"

Write-Host "Building Spring Boot jar..."
& (Join-Path $repoRoot "gradlew.bat") bootJar

$jar = Get-ChildItem (Join-Path $repoRoot "build\libs") -Filter "*.jar" |
    Where-Object { $_.Name -notlike "*-plain.jar" } |
    Sort-Object LastWriteTimeUtc -Descending |
    Select-Object -First 1

if (-not $jar) {
    throw "No executable jar found in build\\libs."
}

if (Test-Path $stagingDir) {
    Remove-Item $stagingDir -Recurse -Force
}

New-Item -ItemType Directory -Path $bundleRoot | Out-Null
Copy-Item $jar.FullName (Join-Path $bundleRoot "application.jar")
Copy-Item (Join-Path $repoRoot "Procfile") $bundleRoot

Compress-Archive -Path (Join-Path $bundleRoot "*") -DestinationPath $zipPath -Force

Write-Host "Elastic Beanstalk bundle created:"
Write-Host $zipPath
