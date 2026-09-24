$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$src = Join-Path $root "src\main\java\com\github\kisaragimikoto\tacticalbackpack"

$stale = @(
    (Join-Path $src "menu\util\BackpackLogger.java"),
    (Join-Path $src "menu\util\BackpackTags.java"),
    (Join-Path $src "menu\util\BackpackVersion.java")
)

foreach ($path in $stale) {
    if (Test-Path $path) {
        Remove-Item -Force $path
        Write-Host "Removed stale source: $path"
    }
}

Write-Host "commit15 stale-source cleanup complete."
