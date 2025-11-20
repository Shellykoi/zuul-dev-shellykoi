# API诊断脚本
# 用于诊断API端点问题

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "API端点诊断工具" -ForegroundColor Cyan
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
        # Continue to next port
    }
}

if (-not $serverPort) {
    Write-Host "未找到运行中的服务器" -ForegroundColor Red
    Write-Host "请先启动服务器" -ForegroundColor Yellow
    exit 1
}

$baseUrl = "http://localhost:$serverPort"
Write-Host "使用服务器: $baseUrl" -ForegroundColor Gray
Write-Host ""

# 测试1: 检查根路径
Write-Host "测试1: 检查根路径 /" -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "$baseUrl/" -Method GET -ErrorAction Stop
    Write-Host "  根路径可访问 (状态码: $($response.StatusCode))" -ForegroundColor Green
}
catch {
    Write-Host "  根路径访问失败: $_" -ForegroundColor Red
}
Write-Host ""

# 测试2: 检查API端点（使用详细输出）
Write-Host "测试2: 检查 /api/login 端点" -ForegroundColor Yellow
$loginBody = @{
    username = "koi"
    password = "123456"
} | ConvertTo-Json

Write-Host "  请求URL: $baseUrl/api/login" -ForegroundColor Gray
Write-Host "  请求方法: POST" -ForegroundColor Gray
Write-Host "  请求体: $loginBody" -ForegroundColor Gray

try {
    $response = Invoke-WebRequest -Uri "$baseUrl/api/login" -Method POST -Body $loginBody -ContentType "application/json" -ErrorAction Stop
    Write-Host "  API端点可访问 (状态码: $($response.StatusCode))" -ForegroundColor Green
    Write-Host "  响应内容: $($response.Content)" -ForegroundColor Gray
    
    $jsonResponse = $response.Content | ConvertFrom-Json
    if ($jsonResponse.success) {
        Write-Host "  登录成功" -ForegroundColor Green
    }
    else {
        Write-Host "  登录失败: $($jsonResponse.message)" -ForegroundColor Red
        if ($jsonResponse.debug) {
            Write-Host "  调试信息:" -ForegroundColor Yellow
            Write-Host "    原始路径: $($jsonResponse.debug.originalPath)" -ForegroundColor Gray
            Write-Host "    规范化路径: $($jsonResponse.debug.normalizedPath)" -ForegroundColor Gray
            Write-Host "    方法: $($jsonResponse.debug.method)" -ForegroundColor Gray
        }
    }
}
catch {
    Write-Host "  API端点访问失败" -ForegroundColor Red
    Write-Host "  错误: $_" -ForegroundColor Red
    
    if ($_.Exception.Response) {
        $statusCode = $_.Exception.Response.StatusCode.value__
        Write-Host "  状态码: $statusCode" -ForegroundColor Red
        
        try {
            $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
            $responseBody = $reader.ReadToEnd()
            Write-Host "  响应体: $responseBody" -ForegroundColor Red
            
            $jsonResponse = $responseBody | ConvertFrom-Json -ErrorAction SilentlyContinue
            if ($jsonResponse -and $jsonResponse.debug) {
                Write-Host "  调试信息:" -ForegroundColor Yellow
                Write-Host "    原始路径: $($jsonResponse.debug.originalPath)" -ForegroundColor Gray
                Write-Host "    规范化路径: $($jsonResponse.debug.normalizedPath)" -ForegroundColor Gray
                Write-Host "    方法: $($jsonResponse.debug.method)" -ForegroundColor Gray
            }
        }
        catch {
            Write-Host "  无法读取响应体" -ForegroundColor Red
        }
    }
}
Write-Host ""

# 测试3: 检查其他API端点
Write-Host "测试3: 检查其他API端点" -ForegroundColor Yellow
$endpoints = @("/api/register", "/api/status", "/api/command")

foreach ($endpoint in $endpoints) {
    Write-Host "  检查 $endpoint ..." -ForegroundColor Gray
    try {
        $testBody = @{} | ConvertTo-Json
        $response = Invoke-WebRequest -Uri "$baseUrl$endpoint" -Method POST -Body $testBody -ContentType "application/json" -ErrorAction SilentlyContinue
        Write-Host "    $endpoint 可访问" -ForegroundColor Green
    }
    catch {
        if ($_.Exception.Response) {
            $statusCode = $_.Exception.Response.StatusCode.value__
            if ($statusCode -eq 200) {
                Write-Host "    $endpoint 可访问 (但可能需要正确的参数)" -ForegroundColor Green
            }
            else {
                Write-Host "    $endpoint 状态码: $statusCode" -ForegroundColor Yellow
            }
        }
        else {
            Write-Host "    $endpoint 访问失败" -ForegroundColor Yellow
        }
    }
}
Write-Host ""

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "诊断完成" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "提示:" -ForegroundColor Yellow
Write-Host "1. 如果看到'未知的API端点'错误，请检查服务器控制台的调试输出" -ForegroundColor Gray
Write-Host "2. 确保服务器已重新编译并重启" -ForegroundColor Gray
Write-Host "3. 检查服务器控制台中的路径规范化输出" -ForegroundColor Gray
