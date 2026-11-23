$ErrorActionPreference = "Stop"
$lib = Join-Path $PSScriptRoot "lib\javafx"
New-Item -ItemType Directory -Force -Path $lib | Out-Null
$ver = "25"
$base = "https://repo1.maven.org/maven2/org/openjfx"
$jars = @("javafx-base", "javafx-graphics", "javafx-controls")
foreach ($name in $jars) {
    $out = Join-Path $lib "$name-$ver-win.jar"
    if (-not (Test-Path $out)) {
        Write-Host "Downloading $name $ver..."
        Invoke-WebRequest -Uri "$base/$name/$ver/$name-$ver-win.jar" -OutFile $out -UseBasicParsing
    }
}
Write-Host "JavaFX is ready in $lib"
