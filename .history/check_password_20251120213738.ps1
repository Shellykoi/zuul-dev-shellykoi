# 检查数据库中的实际密码
Write-Host "========================================"
Write-Host "检查数据库中的用户密码"
Write-Host "========================================"

$DB_USER = "shellykoi"
$DB_PASSWORD = "123456koiii"
$DB_NAME = "zuul_game"

try {
    # 连接MySQL并查询
    $query = "SELECT user_id, username, password, LENGTH(password) as pwd_len, HEX(password) as pwd_hex FROM users WHERE username = 'koi'"
    
    Write-Host "执行查询: $query"
    Write-Host ""
    
    # 使用mysql命令行工具
    $result = & mysql -u$DB_USER -p$DB_PASSWORD $DB_NAME -e $query 2>&1
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "查询结果:"
        Write-Host $result
        Write-Host ""
        
        # 检查密码值
        $lines = $result -split "`n"
        foreach ($line in $lines) {
            if ($line -match "koi") {
                Write-Host "找到用户记录: $line"
                $fields = $line -split "`t"
                if ($fields.Length -ge 3) {
                    $pwd = $fields[2]
                    Write-Host "密码值: [$pwd]"
                    Write-Host "密码长度: $($fields[3])"
                    Write-Host "密码HEX: $($fields[4])"
                    Write-Host ""
                    Write-Host "比较测试:"
                    Write-Host "  '123456' == '$pwd': $('123456' -eq $pwd)"
                    Write-Host "  '123456'.Trim() == '$($pwd.Trim())': $('123456'.Trim() -eq $pwd.Trim())"
                }
            }
        }
    } else {
        Write-Host "查询失败: $result"
    }
} catch {
    Write-Host "错误: $_"
}

Write-Host ""
Write-Host "========================================"
Write-Host "检查完成"
Write-Host "========================================"

