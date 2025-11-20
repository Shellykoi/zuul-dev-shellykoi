$jdkPath  = "c:\Users\12237\oracleJdk-25"
$projPath = "d:\PycharmProjects\zuul-dev\se23-sept1-Shellykoi\src\cn\edu\whut\sept\zuul"

if (-not (Test-Path "$jdkPath\bin\java.exe")) {
    Write-Error "找不到 $jdkPath\bin\java.exe"
    exit 1
}

$env:JAVA_HOME = $jdkPath
$env:Path = "$jdkPath\bin;$env:Path"
[Environment]::SetEnvironmentVariable("JAVA_HOME", $jdkPath, "User")
[Environment]::SetEnvironmentVariable("JAVA_HOME", $jdkPath, "Machine")
$sysPath = [Environment]::GetEnvironmentVariable("Path", "Machine")
if ($sysPath -notlike "*$jdkPath\bin*") {
    [Environment]::SetEnvironmentVariable("Path", "$jdkPath\bin;$sysPath", "Machine")
}

& "$jdkPath\bin\java.exe" -version
& "$jdkPath\bin\javac.exe" -version
Push-Location $projPath
& "$jdkPath\bin\javac.exe" -encoding UTF-8 Main.java
if ($LASTEXITCODE -eq 0) {
    & "$jdkPath\bin\java.exe" Main
}
Pop-Location
