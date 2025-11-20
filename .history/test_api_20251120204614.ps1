# API Test Script

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "API Endpoint Test" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Detect server port
$ports = @(8080, 8081, 8082, 8083, 8084)
$serverPort = $null

foreach ($port in $ports) {
    try {
        $connection = Test-NetConnection -ComputerName localhost -Port $port -WarningAction SilentlyContinue -ErrorAction SilentlyContinue
        if ($connection.TcpTestSucceeded) {
            $serverPort = $port
            Write-Host "Server found on port $port" -ForegroundColor Green
            break
        }
    }
    catch {
        # Continue
    }
}

if (-not $serverPort) {
    Write-Host "No server found" -ForegroundColor Red
    Write-Host "Please start server: java -cp out cn.edu.whut.sept.zuul.WebMain" -ForegroundColor Yellow
    exit 1
}

$baseUrl = "http://localhost:$serverPort"
Write-Host "Using server: $baseUrl" -ForegroundColor Gray
Write-Host ""

# Test Login API
Write-Host "Test 1: Login API" -ForegroundColor Yellow
$loginBody = @{
    username = "koi"
    password = "123456"
} | ConvertTo-Json

Write-Host "Request URL: $baseUrl/api/login" -ForegroundColor Gray
Write-Host "Method: POST" -ForegroundColor Gray

$sessionId = $null
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/login" -Method POST -Body $loginBody -ContentType "application/json" -ErrorAction Stop
    Write-Host "Response: $($response | ConvertTo-Json -Compress)" -ForegroundColor Gray
    
    if ($response.success) {
        Write-Host "Login SUCCESS" -ForegroundColor Green
        $sessionId = $response.sessionId
        Write-Host "Session ID: $sessionId" -ForegroundColor Gray
    }
    else {
        Write-Host "Login FAILED: $($response.message)" -ForegroundColor Red
        if ($response.debug) {
            Write-Host "Debug Info:" -ForegroundColor Yellow
            Write-Host "  Original Path: $($response.debug.originalPath)" -ForegroundColor Gray
            Write-Host "  Normalized Path: $($response.debug.normalizedPath)" -ForegroundColor Gray
            Write-Host "  Method: $($response.debug.method)" -ForegroundColor Gray
        }
    }
}
catch {
    Write-Host "Login request FAILED" -ForegroundColor Red
    Write-Host "Error: $_" -ForegroundColor Red
    
    if ($_.Exception.Response) {
        $statusCode = $_.Exception.Response.StatusCode.value__
        Write-Host "Status Code: $statusCode" -ForegroundColor Red
        
        try {
            $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
            $responseBody = $reader.ReadToEnd()
            Write-Host "Response Body: $responseBody" -ForegroundColor Red
            
            $jsonResponse = $responseBody | ConvertFrom-Json -ErrorAction SilentlyContinue
            if ($jsonResponse -and $jsonResponse.debug) {
                Write-Host "Debug Info:" -ForegroundColor Yellow
                Write-Host "  Original Path: $($jsonResponse.debug.originalPath)" -ForegroundColor Gray
                Write-Host "  Normalized Path: $($jsonResponse.debug.normalizedPath)" -ForegroundColor Gray
                Write-Host "  Method: $($jsonResponse.debug.method)" -ForegroundColor Gray
            }
        }
        catch {
            Write-Host "Cannot read response body" -ForegroundColor Red
        }
    }
}
Write-Host ""

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Test completed" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

