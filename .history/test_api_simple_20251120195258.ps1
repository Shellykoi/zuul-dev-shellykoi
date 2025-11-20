# 简单的API测试脚本

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "API端点测试" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 检测服务器端口
$ports = @(8080, 8081, 8082, 8083, 8084)
$serverPort = $null

foreach ($port in $ports) {
    try {
        $connection = Test-NetConnection -ComputerName localhost -Port $port -WarningAction SilentlyContinue -ErrorAction SilentlyContinue
        if ($connection.TcpTestSucceeded) {
            $serverPort = $port
            Write-Host "服务器运行在端口 $port" -ForegroundColor Green
            break
        }
    }
    catch {
        # Continue
    }
}

if (-not $serverPort) {
    Write-Host "未找到运行中的服务器" -ForegroundColor Red
    Write-Host "请先启动服务器: java -cp out cn.edu.whut.sept.zuul.WebMain" -ForegroundColor Yellow
    exit 1
}

$baseUrl = "http://localhost:$serverPort"
Write-Host "使用服务器: $baseUrl" -ForegroundColor Gray
Write-Host ""

# 测试登录API
Write-Host "测试1: 登录API" -ForegroundColor Yellow
$loginBody = @{
    username = "koi"
    password = "123456"
} | ConvertTo-Json

Write-Host "请求URL: $baseUrl/api/login" -ForegroundColor Gray
Write-Host "请求方法: POST" -ForegroundColor Gray

$sessionId = $null
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/login" -Method POST -Body $loginBody -ContentType "application/json" -ErrorAction Stop
    Write-Host "响应: $($response | ConvertTo-Json -Compress)" -ForegroundColor Gray
    
    if ($response.success) {
        Write-Host "登录成功" -ForegroundColor Green
        $sessionId = $response.sessionId
        Write-Host "会话ID: $sessionId" -ForegroundColor Gray
    }
    else {
        Write-Host "登录失败: $($response.message)" -ForegroundColor Red
        if ($response.debug) {
            Write-Host "调试信息:" -ForegroundColor Yellow
            Write-Host "  原始路径: $($response.debug.originalPath)" -ForegroundColor Gray
            Write-Host "  规范化路径: $($response.debug.normalizedPath)" -ForegroundColor Gray
            Write-Host "  方法: $($response.debug.method)" -ForegroundColor Gray
        }
    }
}
catch {
    Write-Host "登录请求失败" -ForegroundColor Red
    Write-Host "错误: $_" -ForegroundColor Red
    
    if ($_.Exception.Response) {
        $statusCode = $_.Exception.Response.StatusCode.value__
        Write-Host "状态码: $statusCode" -ForegroundColor Red
        
        try {
            $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
            $responseBody = $reader.ReadToEnd()
            Write-Host "响应体: $responseBody" -ForegroundColor Red
            
            $jsonResponse = $responseBody | ConvertFrom-Json -ErrorAction SilentlyContinue
            if ($jsonResponse -and $jsonResponse.debug) {
                Write-Host "调试信息:" -ForegroundColor Yellow
                Write-Host "  原始路径: $($jsonResponse.debug.originalPath)" -ForegroundColor Gray
                Write-Host "  规范化路径: $($jsonResponse.debug.normalizedPath)" -ForegroundColor Gray
                Write-Host "  方法: $($jsonResponse.debug.method)" -ForegroundColor Gray
            }
        }
        catch {
            Write-Host "无法读取响应体" -ForegroundColor Red
        }
    }
}
Write-Host ""

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "测试完成" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

