<#
.SYNOPSIS
    Compares two results of run-regression.ps1 (screen output and data files).

.PARAMETER Reference
    Label of the reference result, for example "reference".

.PARAMETER Candidate
    Label of the result to check, for example "final".

.PARAMETER IgnoreDates
    Replaces every date (yyyy-MM-dd) with the text DATE before comparing.
    Use it only when the two results were produced on different days.

.EXAMPLE
    powershell -ExecutionPolicy Bypass -File docs/regression/compare-regression.ps1 -Reference reference -Candidate final
#>
param(
    [Parameter(Mandatory = $true)]
    [string]$Reference,
    [Parameter(Mandatory = $true)]
    [string]$Candidate,
    [switch]$IgnoreDates
)

$ErrorActionPreference = "Stop"
$projectRoot = (Resolve-Path (Join-Path $PSScriptRoot "../..")).Path
$resultsDir = Join-Path (Join-Path $projectRoot "target") "regression"
$referenceDir = Join-Path $resultsDir $Reference
$candidateDir = Join-Path $resultsDir $Candidate
$scenarios = @("main", "errors", "nowrite", "noread", "reload", "corrupt", "corruptproducts")

function Read-Normalized([string]$path) {
    if (-not (Test-Path $path -PathType Leaf)) {
        return $null
    }
    $text = [System.IO.File]::ReadAllText($path)
    if ($IgnoreDates) {
        $text = $text -replace '\d{4}-\d{2}-\d{2}', 'DATE'
    }
    return $text
}

$differences = 0
foreach ($scenario in $scenarios) {
    $files = @("$scenario-output.txt")
    $referenceData = Join-Path (Join-Path $referenceDir "work-$scenario") "data"
    $candidateData = Join-Path (Join-Path $candidateDir "work-$scenario") "data"
    $dataNames = @(Get-ChildItem -File $referenceData) + @(Get-ChildItem -File $candidateData) |
            ForEach-Object { $_.Name } | Sort-Object -Unique
    foreach ($name in $dataNames) {
        $files += (Join-Path (Join-Path "work-$scenario" "data") $name)
    }
    foreach ($file in $files) {
        $a = Read-Normalized (Join-Path $referenceDir $file)
        $b = Read-Normalized (Join-Path $candidateDir $file)
        if ($a -ceq $b) {
            Write-Host "  same      $file"
        } else {
            Write-Host "  DIFFERENT $file"
            $differences++
        }
    }
}

if ($differences -eq 0) {
    Write-Host "RESULT: no differences between '$Reference' and '$Candidate'."
} else {
    Write-Host "RESULT: $differences file(s) with differences between '$Reference' and '$Candidate'."
    exit 1
}
