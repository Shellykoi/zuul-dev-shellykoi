# PowerShell综合测试脚本
# 测试数据库连接、登录功能、API端点等所有必要功能

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "World of Zuul 综合功能测试" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$testResults = @()

# ========================================
# 1. 测试数据库连接
# ========================================
Write-Host "1. 测试数据库连接..." -ForegroundColor Yellow
try {
    # 尝试连接MySQL数据库
    $dbTest = @"
import java.sql.*;
public class DBTest {
    public static void main(String[] args) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/zuul_game?useSSL=false&serverTimezone=UTC&characterEncoding=UTF-8",
                "shellykoi", "123456koiii");
            System.out.println("SUCCESS");
            conn.close();
        } catch (Exception e) {
            System.out.println("FAILED: " + e.getMessage());
        }
    }
}
"@
    
    $dbTest | Out-File -FilePath "DBTest.java" -Encoding UTF8
    javac -cp "lib\mysql-connector-j-9.5.0.jar" DBTest.java 2>&1 | Out-Null
    if ($LASTEXITCODE -eq 0) {
        $result = java -cp ".;lib\mysql-connector-j-9.5.0.jar" DBTest 2>&1
        if ($result -match "SUCCESS") {
            Write-Host "  [OK] 数据库连接成功" -ForegroundColor Green
            $testResults += @{Test="数据库连接"; Status="通过"}
        } else {
            Write-Host "  [FAIL] 数据库连接失败: $result" -ForegroundColor Red
            $testResults += @{Test="数据库连接"; Status="失败"; Error=$result}
        }
        Remove-Item DBTest.java, DBTest.class -ErrorAction SilentlyContinue
    } else {
        Write-Host "  [FAIL] 无法编译测试程序" -ForegroundColor Red
        $testResults += @{Test="数据库连接"; Status="失败"; Error="编译失败"}
    }
} catch {
    Write-Host "  [FAIL] 测试过程出错: $_" -ForegroundColor Red
    $testResults += @{Test="数据库连接"; Status="失败"; Error=$_.ToString()}
}

Write-Host ""

# ========================================
# 2. 测试Web服务器端口
# ========================================
Write-Host "2. 测试Web服务器端口..." -ForegroundColor Yellow
$ports = @(8080, 8081, 8082, 8083, 8084)
$availablePorts = @()

foreach ($port in $ports) {
    try {
        $connection = Test-NetConnection -ComputerName localhost -Port $port -WarningAction SilentlyContinue -ErrorAction SilentlyContinue
        if ($connection.TcpTestSucceeded) {
            Write-Host "  [OK] 端口 $port 正在监听" -ForegroundColor Green
            $availablePorts += $port
        } else {
            Write-Host "  [INFO] 端口 $port 未监听" -ForegroundColor Gray
        }
    } catch {
        Write-Host "  [INFO] 端口 $port 未监听" -ForegroundColor Gray
    }
}

if ($availablePorts.Count -gt 0) {
    Write-Host "  [OK] 找到 $($availablePorts.Count) 个可用端口" -ForegroundColor Green
    $testResults += @{Test="Web服务器端口"; Status="通过"; Ports=$availablePorts}
} else {
    Write-Host "  [WARN] 未找到运行中的服务器" -ForegroundColor Yellow
    $testResults += @{Test="Web服务器端口"; Status="警告"; Message="服务器未运行"}
}

Write-Host ""

# ========================================
# 3. 测试登录API
# ========================================
Write-Host "3. 测试登录API..." -ForegroundColor Yellow

if ($availablePorts.Count -gt 0) {
    $testPort = $availablePorts[0]
    $testUsername = "koi"
    $testPassword = "123456"
    
    Write-Host "  测试端口: $testPort" -ForegroundColor Gray
    Write-Host "  测试用户: $testUsername" -ForegroundColor Gray
    
    $url = "http://localhost:$testPort/api/login"
    $body = @{
        username = $testUsername
        password = $testPassword
    } | ConvertTo-Json
    
    try {
        $response = Invoke-WebRequest -Uri $url -Method POST -Body $body -ContentType "application/json" -UseBasicParsing -ErrorAction Stop
        
        Write-Host "  [OK] API响应成功 (状态码: $($response.StatusCode))" -ForegroundColor Green
        
        $jsonResponse = $response.Content | ConvertFrom-Json
        Write-Host "  响应内容:" -ForegroundColor Gray
        Write-Host "    success: $($jsonResponse.success)" -ForegroundColor Gray
        if ($jsonResponse.message) {
            Write-Host "    message: $($jsonResponse.message)" -ForegroundColor Gray
        }
        if ($jsonResponse.sessionId) {
            Write-Host "    sessionId: $($jsonResponse.sessionId)" -ForegroundColor Gray
        }
        if ($jsonResponse.username) {
            Write-Host "    username: $($jsonResponse.username)" -ForegroundColor Gray
        }
        
        if ($jsonResponse.success) {
            Write-Host "  [OK] 登录成功！" -ForegroundColor Green
            $testResults += @{Test="登录API"; Status="通过"; Port=$testPort}
        } else {
            Write-Host "  [FAIL] 登录失败: $($jsonResponse.message)" -ForegroundColor Red
            $testResults += @{Test="登录API"; Status="失败"; Error=$jsonResponse.message}
        }
    } catch {
        $statusCode = $_.Exception.Response.StatusCode.value__
        if ($statusCode) {
            Write-Host "  [FAIL] API响应失败 (状态码: $statusCode)" -ForegroundColor Red
            $testResults += @{Test="登录API"; Status="失败"; Error="HTTP $statusCode"}
        } else {
            Write-Host "  [FAIL] 无法连接到API (服务器可能未运行)" -ForegroundColor Red
            $testResults += @{Test="登录API"; Status="失败"; Error="连接失败"}
        }
    }
} else {
    Write-Host "  [SKIP] 服务器未运行，跳过API测试" -ForegroundColor Yellow
    $testResults += @{Test="登录API"; Status="跳过"; Message="服务器未运行"}
}

Write-Host ""

# ========================================
# 4. 测试注册API
# ========================================
Write-Host "4. 测试注册API..." -ForegroundColor Yellow

if ($availablePorts.Count -gt 0) {
    $testPort = $availablePorts[0]
    $testUsername = "test_user_$(Get-Random -Minimum 1000 -Maximum 9999)"
    $testPassword = "test123456"
    
    Write-Host "  测试端口: $testPort" -ForegroundColor Gray
    Write-Host "  测试用户: $testUsername" -ForegroundColor Gray
    
    $url = "http://localhost:$testPort/api/register"
    $body = @{
        username = $testUsername
        password = $testPassword
    } | ConvertTo-Json
    
    try {
        $response = Invoke-WebRequest -Uri $url -Method POST -Body $body -ContentType "application/json" -UseBasicParsing -ErrorAction Stop
        
        $jsonResponse = $response.Content | ConvertFrom-Json
        
        if ($jsonResponse.success) {
            Write-Host "  [OK] 注册成功！" -ForegroundColor Green
            $testResults += @{Test="注册API"; Status="通过"; Port=$testPort}
        } else {
            Write-Host "  [INFO] 注册失败: $($jsonResponse.message)" -ForegroundColor Yellow
            $testResults += @{Test="注册API"; Status="信息"; Message=$jsonResponse.message}
        }
    } catch {
        Write-Host "  [FAIL] 注册API测试失败" -ForegroundColor Red
        $testResults += @{Test="注册API"; Status="失败"; Error=$_.ToString()}
    }
} else {
    Write-Host "  [SKIP] 服务器未运行，跳过API测试" -ForegroundColor Yellow
    $testResults += @{Test="注册API"; Status="跳过"; Message="服务器未运行"}
}

Write-Host ""

# ========================================
# 5. 测试游戏状态API
# ========================================
Write-Host "5. 测试游戏状态API..." -ForegroundColor Yellow

if ($availablePorts.Count -gt 0) {
    $testPort = $availablePorts[0]
    $url = "http://localhost:$testPort/api/status"
    
    try {
        $response = Invoke-WebRequest -Uri $url -Method GET -UseBasicParsing -ErrorAction Stop
        
        $jsonResponse = $response.Content | ConvertFrom-Json
        
        if ($jsonResponse.currentRoom) {
            Write-Host "  [OK] 游戏状态API正常" -ForegroundColor Green
            Write-Host "    当前房间: $($jsonResponse.currentRoom.shortDescription)" -ForegroundColor Gray
            $testResults += @{Test="游戏状态API"; Status="通过"; Port=$testPort}
        } else {
            Write-Host "  [WARN] 游戏状态API返回异常数据" -ForegroundColor Yellow
            $testResults += @{Test="游戏状态API"; Status="警告"; Message="数据异常"}
        }
    } catch {
        Write-Host "  [FAIL] 游戏状态API测试失败" -ForegroundColor Red
        $testResults += @{Test="游戏状态API"; Status="失败"; Error=$_.ToString()}
    }
} else {
    Write-Host "  [SKIP] 服务器未运行，跳过API测试" -ForegroundColor Yellow
    $testResults += @{Test="游戏状态API"; Status="跳过"; Message="服务器未运行"}
}

Write-Host ""

# ========================================
# 测试结果汇总
# ========================================
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "测试结果汇总" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

$passed = ($testResults | Where-Object { $_.Status -eq "通过" }).Count
$failed = ($testResults | Where-Object { $_.Status -eq "失败" }).Count
$skipped = ($testResults | Where-Object { $_.Status -eq "跳过" }).Count

Write-Host "通过: $passed" -ForegroundColor Green
Write-Host "失败: $failed" -ForegroundColor $(if ($failed -gt 0) { "Red" } else { "Green" })
Write-Host "跳过: $skipped" -ForegroundColor Yellow
Write-Host ""

foreach ($result in $testResults) {
    $statusColor = switch ($result.Status) {
        "通过" { "Green" }
        "失败" { "Red" }
        "跳过" { "Yellow" }
        default { "Gray" }
    }
    Write-Host "  $($result.Test): $($result.Status)" -ForegroundColor $statusColor
    if ($result.Error) {
        Write-Host "    错误: $($result.Error)" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "测试完成" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

