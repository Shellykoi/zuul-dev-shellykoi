# 编译并测试脚本
# 用于编译代码、启动服务器并测试API

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "编译并测试脚本" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 检测MySQL驱动
$mysqlJar = Get-ChildItem -Path "lib\mysql-connector-j-*.jar" | Select-Object -First 1

if (-not $mysqlJar) {
    Write-Host "错误: 未找到MySQL驱动文件！" -ForegroundColor Red
    Write-Host "请确保 lib\ 目录中有 mysql-connector-j-*.jar 文件" -ForegroundColor Yellow
    exit 1
}

Write-Host "找到MySQL驱动: $($mysqlJar.Name)" -ForegroundColor Green
Write-Host ""

# 清理并创建输出目录
Write-Host "[1/4] 清理输出目录..." -ForegroundColor Yellow
Remove-Item -Recurse -Force bin -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Force -Path bin | Out-Null
Write-Host "完成" -ForegroundColor Green
Write-Host ""

# 编译Java代码
Write-Host "[2/4] 正在编译Java源代码..." -ForegroundColor Yellow
$compileResult = javac -d bin -encoding UTF-8 -cp "$($mysqlJar.FullName)" src\cn\edu\whut\sept\zuul\*.java 2>&1

if ($LASTEXITCODE -ne 0) {
    Write-Host "编译失败！" -ForegroundColor Red
    Write-Host $compileResult -ForegroundColor Red
    exit 1
}

Write-Host "编译成功！" -ForegroundColor Green
Write-Host ""

# 检查是否有服务器在运行
Write-Host "[3/4] 检查服务器状态..." -ForegroundColor Yellow
$ports = @(8080, 8081, 8082, 8083, 8084)
$runningPort = $null

foreach ($port in $ports) {
    try {
        $connection = Test-NetConnection -ComputerName localhost -Port $port -WarningAction SilentlyContinue -ErrorAction SilentlyContinue
        if ($connection.TcpTestSucceeded) {
            $runningPort = $port
            Write-Host "检测到服务器运行在端口 $port" -ForegroundColor Yellow
            Write-Host "请先停止现有服务器，然后重新运行此脚本" -ForegroundColor Yellow
            exit 1
        }
    }
    catch {
        # Continue
    }
}

Write-Host "未检测到运行中的服务器，可以启动新服务器" -ForegroundColor Green
Write-Host ""

# 启动服务器（后台运行）
Write-Host "[4/4] 启动服务器..." -ForegroundColor Yellow
$serverProcess = Start-Process -FilePath "java" -ArgumentList "-cp", "`"bin;$($mysqlJar.FullName)`"", "cn.edu.whut.sept.zuul.GameWebServer" -PassThru -NoNewWindow

# 等待服务器启动
Write-Host "等待服务器启动..." -ForegroundColor Gray
Start-Sleep -Seconds 3

# 检测服务器端口
$serverPort = $null
foreach ($port in $ports) {
    try {
        $connection = Test-NetConnection -ComputerName localhost -Port $port -WarningAction SilentlyContinue -ErrorAction SilentlyContinue
        if ($connection.TcpTestSucceeded) {
            $serverPort = $port
            Write-Host "服务器已启动在端口 $port" -ForegroundColor Green
            break
        }
    }
    catch {
        # Continue
    }
}

if (-not $serverPort) {
    Write-Host "错误: 服务器启动失败或无法检测到端口" -ForegroundColor Red
    Stop-Process -Id $serverProcess.Id -Force -ErrorAction SilentlyContinue
    exit 1
}

Write-Host ""

# 运行测试
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "开始测试API端点" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$baseUrl = "http://localhost:$serverPort"

# 测试1: 登录
Write-Host "测试1: 登录API" -ForegroundColor Yellow
$loginBody = @{
    username = "koi"
    password = "123456"
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/login" -Method POST -Body $loginBody -ContentType "application/json" -ErrorAction Stop
    if ($response.success) {
        Write-Host "  登录成功！" -ForegroundColor Green
        Write-Host "  会话ID: $($response.sessionId)" -ForegroundColor Gray
        $sessionId = $response.sessionId
    }
    else {
        Write-Host "  登录失败: $($response.message)" -ForegroundColor Red
        $sessionId = $null
    }
}
catch {
    Write-Host "  登录错误: $_" -ForegroundColor Red
    $sessionId = $null
}
Write-Host ""

# 测试2: 状态
if ($sessionId) {
    Write-Host "测试2: 状态API" -ForegroundColor Yellow
    try {
        $response = Invoke-RestMethod -Uri "$baseUrl/api/status?sessionId=$sessionId" -Method GET -ErrorAction Stop
        Write-Host "  状态获取成功" -ForegroundColor Green
    }
    catch {
        Write-Host "  状态获取失败: $_" -ForegroundColor Red
    }
    Write-Host ""
}

# 测试3: 命令
if ($sessionId) {
    Write-Host "测试3: 命令API" -ForegroundColor Yellow
    $commandBody = @{
        command = "look"
        sessionId = $sessionId
    } | ConvertTo-Json
    
    try {
        $response = Invoke-RestMethod -Uri "$baseUrl/api/command" -Method POST -Body $commandBody -ContentType "application/json" -ErrorAction Stop
        Write-Host "  命令执行成功" -ForegroundColor Green
    }
    catch {
        Write-Host "  命令执行失败: $_" -ForegroundColor Red
    }
    Write-Host ""
}

# 测试4: 注册
Write-Host "测试4: 注册API" -ForegroundColor Yellow
$randomUser = "testuser_$(Get-Random)"
$registerBody = @{
    username = $randomUser
    password = "testpass123"
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/register" -Method POST -Body $registerBody -ContentType "application/json" -ErrorAction Stop
    if ($response.success) {
        Write-Host "  注册成功: $randomUser" -ForegroundColor Green
    }
    else {
        Write-Host "  注册失败: $($response.message)" -ForegroundColor Yellow
    }
}
catch {
    Write-Host "  注册错误: $_" -ForegroundColor Red
}
Write-Host ""

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "测试完成" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "服务器仍在运行，进程ID: $($serverProcess.Id)" -ForegroundColor Gray
Write-Host "要停止服务器，请运行: Stop-Process -Id $($serverProcess.Id)" -ForegroundColor Gray
Write-Host "或按 Ctrl+C 然后输入 'y' 确认停止" -ForegroundColor Gray
Write-Host ""




