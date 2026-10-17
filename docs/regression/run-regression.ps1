<#
.SYNOPSIS
    Runs the non-regression scenarios of Requirement 7 (GRASP correction).

.DESCRIPTION
    Compiles the current source code into target/regression/<Label>/classes
    and runs the application once per scenario, each one in its own working
    folder with its own copy of the data files, so the data/ folder of the
    project is never touched. For every scenario it keeps the screen output
    (<scenario>-output.txt) and the data files written by the application
    (work-<scenario>/data).

.PARAMETER Label
    Name of the result folder, for example "reference" (before the
    corrections) or "final" (after the corrections).

.PARAMETER Source
    Optional Git reference (a branch or a commit) whose source code is
    compiled instead of the current files, for example "develop". The code
    is taken from a temporary copy, so the current files are not changed.

.EXAMPLE
    powershell -ExecutionPolicy Bypass -File docs/regression/run-regression.ps1 -Label reference -Source develop

.EXAMPLE
    powershell -ExecutionPolicy Bypass -File docs/regression/run-regression.ps1 -Label final
#>
param(
    [Parameter(Mandatory = $true)]
    [string]$Label,
    [string]$Source = ""
)

$ErrorActionPreference = "Stop"
$regressionDir = $PSScriptRoot
$projectRoot = (Resolve-Path (Join-Path $regressionDir "../..")).Path
$outDir = Join-Path (Join-Path (Join-Path $projectRoot "target") "regression") $Label
$classesDir = Join-Path $outDir "classes"

# Find the JDK tools: first in the PATH, then in JAVA_HOME.
$javac = "javac"
$java = "java"
if (-not (Get-Command $javac -ErrorAction SilentlyContinue)) {
    if (-not $env:JAVA_HOME) {
        throw "javac was not found. Install a JDK 17 or later, or set JAVA_HOME."
    }
    $javac = Join-Path (Join-Path $env:JAVA_HOME "bin") "javac"
    $java = Join-Path (Join-Path $env:JAVA_HOME "bin") "java"
}

if (Test-Path $outDir) {
    Remove-Item -Recurse -Force $outDir
}
New-Item -ItemType Directory -Force $classesDir | Out-Null

$codeRoot = $projectRoot
$temporaryCopy = $null
if ($Source -ne "") {
    # Temporary copy of the requested version, outside the project folder.
    $temporaryCopy = Join-Path ([System.IO.Path]::GetTempPath()) ("gamezone-regression-" + [guid]::NewGuid())
    # Git writes progress messages to the error stream; in Windows PowerShell
    # they must not stop the script, so errors are not treated as fatal here.
    $ErrorActionPreference = "Continue"
    & git -C $projectRoot worktree add --detach $temporaryCopy $Source *> $null
    $created = ($LASTEXITCODE -eq 0)
    $ErrorActionPreference = "Stop"
    if (-not $created) {
        throw "The Git reference '$Source' was not found."
    }
    $codeRoot = $temporaryCopy
    Write-Host "Compiling the source code of '$Source'..."
} else {
    Write-Host "Compiling the current source code..."
}
$sources = Get-ChildItem -Recurse -Filter "*.java" (Join-Path (Join-Path $codeRoot "src") "main") |
        ForEach-Object { $_.FullName }
$ErrorActionPreference = "Continue"
& $javac -encoding UTF-8 -d $classesDir $sources
$compiled = ($LASTEXITCODE -eq 0)
if ($temporaryCopy) {
    & git -C $projectRoot worktree remove --force $temporaryCopy *> $null
}
$ErrorActionPreference = "Stop"
if (-not $compiled) {
    throw "The project does not compile."
}

function Copy-DataFiles([string]$from, [string]$to) {
    Get-ChildItem -File $from | ForEach-Object { Copy-Item $_.FullName (Join-Path $to $_.Name) }
}

function Invoke-Scenario([string]$name, [string]$inputFile, [scriptblock]$prepareData) {
    $workDir = Join-Path $outDir "work-$name"
    $dataDir = Join-Path $workDir "data"
    New-Item -ItemType Directory -Force $dataDir | Out-Null
    & $prepareData $dataDir
    $process = New-Object System.Diagnostics.Process
    $process.StartInfo.FileName = $java
    $process.StartInfo.Arguments = "-Dstdout.encoding=UTF-8 -Dfile.encoding=UTF-8 -cp `"$classesDir`" com.gamezone.Main"
    $process.StartInfo.WorkingDirectory = $workDir
    $process.StartInfo.UseShellExecute = $false
    $process.StartInfo.RedirectStandardInput = $true
    $process.StartInfo.RedirectStandardOutput = $true
    $process.StartInfo.RedirectStandardError = $true
    $process.Start() | Out-Null
    # Read the error stream in the background so the process never blocks.
    $errorTask = $process.StandardError.ReadToEndAsync()
    $outputFile = [System.IO.File]::Create((Join-Path $outDir "$name-output.txt"))
    $outputTask = $process.StandardOutput.BaseStream.CopyToAsync($outputFile)
    $inputBytes = [System.IO.File]::ReadAllBytes($inputFile)
    $process.StandardInput.BaseStream.Write($inputBytes, 0, $inputBytes.Length)
    $process.StandardInput.Close()
    $process.WaitForExit()
    $outputTask.Wait()
    $outputFile.Close()
    [System.IO.File]::WriteAllText((Join-Path $outDir "$name-stderr.txt"), $errorTask.Result)
    Write-Host "Scenario '$name' finished."
}

$initialData = Join-Path $regressionDir "initial-data"

# 1. Every menu operation with valid data.
Invoke-Scenario "main" (Join-Path $regressionDir "main-input.txt") {
    param($data) Copy-DataFiles $initialData $data
}
# 2. Invalid data, unknown ids, and business rule errors in every module.
Invoke-Scenario "errors" (Join-Path $regressionDir "errors-input.txt") {
    param($data) Copy-DataFiles $initialData $data
}
# 3. The accessories file cannot be read or written (it is a folder).
Invoke-Scenario "nowrite" (Join-Path $regressionDir "nowrite-input.txt") {
    param($data)
    Copy-DataFiles $initialData $data
    Remove-Item (Join-Path $data "accessories.csv")
    New-Item -ItemType Directory (Join-Path $data "accessories.csv") | Out-Null
}
# 4. The products file cannot be read at startup (it is a folder).
Invoke-Scenario "noread" (Join-Path $regressionDir "exit-input.txt") {
    param($data)
    Copy-DataFiles $initialData $data
    New-Item -ItemType Directory (Join-Path $data "products.txt") | Out-Null
}
# 5. The data written by scenario 1 is loaded again and listed.
Invoke-Scenario "reload" (Join-Path $regressionDir "reload-input.txt") {
    param($data) Copy-DataFiles (Join-Path (Join-Path $outDir "work-main") "data") $data
}
# 6. Data files with unknown types and invalid lines.
Invoke-Scenario "corrupt" (Join-Path $regressionDir "corrupt-input.txt") {
    param($data) Copy-DataFiles (Join-Path $regressionDir "corrupt-data") $data
}
# 7. A products file with an invalid number.
Invoke-Scenario "corruptproducts" (Join-Path $regressionDir "exit-input.txt") {
    param($data) Copy-DataFiles (Join-Path $regressionDir "corruptproducts-data") $data
}

Write-Host "Results saved in $outDir"