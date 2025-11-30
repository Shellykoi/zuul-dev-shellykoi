# 测试登录功能修复
# 此脚本用于验证登录功能是否正常工作

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "测试登录功能修复" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 检查服务器是否运行
Write-Host "步骤1: 检查服务器是否运行..." -ForegroundColor Yellow
$response = $null
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/api/status" -Method GET -TimeoutSec 5 -ErrorAction SilentlyContinue
    if ($response.StatusCode -eq 200) {
        Write-Host "✅ 服务器正在运行" -ForegroundColor Green
    }
} catch {
    Write-Host "❌ 服务器未运行或无法访问" -ForegroundColor Red
    Write-Host "请先启动服务器: java -cp `"bin;lib\mysql-connector-j-8.0.33.jar`" cn.edu.whut.sept.zuul.WebMain" -ForegroundColor Yellow
    exit 1
}

Write-Host ""
Write-Host "步骤2: 测试登录API..." -ForegroundColor Yellow

# 测试登录（使用一个测试用户名和密码）
$loginData = @{
    username = "testuser"
    password = "testpass"
} | ConvertTo-Json

try {
    $loginResponse = Invoke-WebRequest -Uri "http://localhost:8080/api/login" -Method POST -Body $loginData -ContentType "application/json; charset=UTF-8" -TimeoutSec 10
    
    Write-Host "HTTP状态码: $($loginResponse.StatusCode)" -ForegroundColor Cyan
    
    $responseBody = $loginResponse.Content | ConvertFrom-Json
    Write-Host "响应内容:" -ForegroundColor Cyan
    $responseBody | ConvertTo-Json -Depth 3
    
    if ($responseBody.success) {
        Write-Host ""
        Write-Host "✅ 登录成功！" -ForegroundColor Green
        Write-Host "Session ID: $($responseBody.sessionId)" -ForegroundColor Cyan
        Write-Host "用户名: $($responseBody.username)" -ForegroundColor Cyan
    } else {
        Write-Host ""
        Write-Host "⚠️  登录失败: $($responseBody.message)" -ForegroundColor Yellow
        Write-Host ""
        Write-Host "可能的原因:" -ForegroundColor Yellow
        Write-Host "1. 用户名或密码错误" -ForegroundColor White
        Write-Host "2. 数据库连接问题" -ForegroundColor White
        Write-Host "3. 用户不存在（需要先注册）" -ForegroundColor White
    }
} catch {
    Write-Host "❌ 登录请求失败: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $responseBody = $reader.ReadToEnd()
        Write-Host "错误响应: $responseBody" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "步骤3: 测试注册API（如果登录失败）..." -ForegroundColor Yellow

# 如果登录失败，尝试注册
if (-not $responseBody.success) {
    $registerData = @{
        username = "testuser"
        password = "testpass"
    } | ConvertTo-Json
    
    try {
        $registerResponse = Invoke-WebRequest -Uri "http://localhost:8080/api/register" -Method POST -Body $registerData -ContentType "application/json; charset=UTF-8" -TimeoutSec 10
        
        $registerBody = $registerResponse.Content | ConvertFrom-Json
        Write-Host "注册响应:" -ForegroundColor Cyan
        $registerBody | ConvertTo-Json -Depth 3
        
        if ($registerBody.success) {
            Write-Host ""
            Write-Host "✅ 注册成功！现在可以尝试登录" -ForegroundColor Green
        } else {
            Write-Host ""
            Write-Host "⚠️  注册失败: $($registerBody.message)" -ForegroundColor Yellow
        }
    } catch {
        Write-Host "❌ 注册请求失败: $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "测试完成" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan



