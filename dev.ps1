param(
  [Parameter(Mandatory)]
  [ValidateSet("auth-service", "course-service")]
  [string]$Service
)

# Use JDK 21
$jdk = Get-ChildItem "C:\Program Files\Eclipse Adoptium" -Directory -Filter "jdk-21*" | Select-Object -First 1
if (-not $jdk) { throw "JDK 21 not found under C:\Program Files\Eclipse Adoptium" }
$env:JAVA_HOME = $jdk.FullName
$env:Path = "$env:JAVA_HOME\bin;$env:Path"

# Load .env into this window
Get-Content "$PSScriptRoot\.env" | Where-Object { $_ -match '^\s*[^#].*=' } | ForEach-Object {
  $k, $v = $_ -split '=', 2
  Set-Item "env:$($k.Trim())" $v.Trim()
}

Write-Host "Starting $Service with Java $($jdk.Name)"
Set-Location "$PSScriptRoot\$Service"
mvn spring-boot:run