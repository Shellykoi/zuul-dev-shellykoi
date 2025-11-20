# Final API Test Script
# This script tests all API endpoints

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "API Endpoint Comprehensive Test" -ForegroundColor Cyan
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
        # Continue to next port
    }
}

if (-not $serverPort) {
    Write-Host "No server found" -ForegroundColor Red
    Write-Host "Please start server first" -ForegroundColor Yellow
    exit 1
}

$baseUrl = "http://localhost:$serverPort"
Write-Host "Using server: $baseUrl" -ForegroundColor Gray
Write-Host ""

# Test 1: Login
Write-Host "Test 1: Login API" -ForegroundColor Yellow
$loginBody = @{
    username = "koi"
    password = "123456"
} | ConvertTo-Json

Write-Host "  Request body: $loginBody" -ForegroundColor Gray

$sessionId = $null
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/login" -Method POST -Body $loginBody -ContentType "application/json" -ErrorAction Stop
    Write-Host "  Response: $($response | ConvertTo-Json -Compress)" -ForegroundColor Gray
    if ($response.success) {
        Write-Host "  Login success" -ForegroundColor Green
        Write-Host "  Session ID: $($response.sessionId)" -ForegroundColor Gray
        $sessionId = $response.sessionId
    }
    else {
        Write-Host "  Login failed" -ForegroundColor Red
        Write-Host "  Error message: $($response.message)" -ForegroundColor Red
    }
}
catch {
    Write-Host "  Login error: $_" -ForegroundColor Red
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $responseBody = $reader.ReadToEnd()
        Write-Host "  Response body: $responseBody" -ForegroundColor Red
    }
}

Write-Host ""

# Test 2: Status
if ($sessionId) {
    Write-Host "Test 2: Status API" -ForegroundColor Yellow
    try {
        $response = Invoke-RestMethod -Uri "$baseUrl/api/status?sessionId=$sessionId" -Method GET -ErrorAction Stop
        Write-Host "  Status retrieved" -ForegroundColor Green
    }
    catch {
        Write-Host "  Status error: $_" -ForegroundColor Red
    }
    Write-Host ""
}

# Test 3: Command
if ($sessionId) {
    Write-Host "Test 3: Command API" -ForegroundColor Yellow
    $commandBody = @{
        command = "look"
        sessionId = $sessionId
    } | ConvertTo-Json
    
    try {
        $response = Invoke-RestMethod -Uri "$baseUrl/api/command" -Method POST -Body $commandBody -ContentType "application/json" -ErrorAction Stop
        Write-Host "  Command executed" -ForegroundColor Green
    }
    catch {
        Write-Host "  Command error: $_" -ForegroundColor Red
    }
    Write-Host ""
}

# Test 4: Register
Write-Host "Test 4: Register API" -ForegroundColor Yellow
$randomUser = "testuser_$(Get-Random)"
$registerBody = @{
    username = $randomUser
    password = "testpass123"
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/register" -Method POST -Body $registerBody -ContentType "application/json" -ErrorAction Stop
    Write-Host "  Register attempted" -ForegroundColor Green
}
catch {
    Write-Host "  Register error: $_" -ForegroundColor Red
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Test completed" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
