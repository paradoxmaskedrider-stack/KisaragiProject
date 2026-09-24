$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$src = Join-Path $root 'src\main\java\com\github\kisaragimikoto\tacticalbackpack\network\AE2BackpackActionPacket.java'
$dst = Join-Path (Get-Location) 'src\main\java\com\github\kisaragimikoto\tacticalbackpack\network\AE2BackpackActionPacket.java'
if (!(Test-Path $src)) { throw "Hotfix source file not found: $src" }
if (!(Test-Path (Split-Path -Parent $dst))) { throw "Run this script from the TacticalBackpack project root." }
Copy-Item $src $dst -Force
$bad = Select-String -Path $dst -Pattern 'private\s+AE2BackpackActionPacket\s*\(\s*\)\s*\{\s*\}' -Quiet
if ($bad) { throw 'Hotfix verification failed: obsolete empty constructor is still present.' }
Write-Host 'commit17 hotfix applied successfully.' -ForegroundColor Green
Write-Host $dst
