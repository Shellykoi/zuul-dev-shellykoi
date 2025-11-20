# Detailed Login Test
$baseUrl = "http://localhost:8081"

Write-Host "Testing login with username='koi', password='123456'" -ForegroundColor Cyan
Write-Host ""

$loginBody = @{
    username = "koi"
    password = "123456"
} | ConvertTo-Json

Write-Host "Request JSON: $loginBody" -ForegroundColor Yellow
Write-Host ""

try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/login" -Method POST -Body $loginBody -ContentType "application/json" -ErrorAction Stop
    
    Write-Host "Response received:" -ForegroundColor Green
    $response | ConvertTo-Json | Write-Host
    
    if ($response.success) {
        Write-Host "`n✅ Login SUCCESS!" -ForegroundColor Green
        Write-Host "Session ID: $($response.sessionId)" -ForegroundColor Gray
    } else {
        Write-Host "`n❌ Login FAILED!" -ForegroundColor Red
        Write-Host "Message: $($response.message)" -ForegroundColor Red
    }
} catch {
    Write-Host "`n❌ Request FAILED!" -ForegroundColor Red
    Write-Host "Error: $_" -ForegroundColor Red
    
    if ($_.Exception.Response) {
        $statusCode = $_.Exception.Response.StatusCode.value__
        Write-Host "Status Code: $statusCode" -ForegroundColor Red
        
        try {
            $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
            $responseBody = $reader.ReadToEnd()
            Write-Host "Response Body: $responseBody" -ForegroundColor Red
        } catch {
            Write-Host "Could not read response body" -ForegroundColor Yellow
        }
    }
}

