# Git 中文提交配置脚本
# 运行此脚本可以一键配置 Git 和 PowerShell 以支持中文提交

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Git 中文提交配置脚本" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 1. 配置 Git 编码
Write-Host "[1/4] 配置 Git 编码设置..." -ForegroundColor Yellow
git config --global i18n.commitencoding utf-8
git config --global i18n.logoutputencoding utf-8
git config --global core.quotepath false
Write-Host "✓ Git 编码配置完成" -ForegroundColor Green
Write-Host ""

# 2. 配置 Git 编辑器（使用 VS Code，如果已安装）
Write-Host "[2/4] 配置 Git 编辑器..." -ForegroundColor Yellow
$vscodePath = Get-Command code -ErrorAction SilentlyContinue
if ($vscodePath) {
    git config --global core.editor "code --wait"
    Write-Host "✓ 已设置 VS Code 为 Git 编辑器" -ForegroundColor Green
} else {
    Write-Host "⚠ VS Code 未找到，使用系统默认编辑器" -ForegroundColor Yellow
    git config --global core.editor notepad
}
Write-Host ""

# 3. 配置 PowerShell 编码（当前会话）
Write-Host "[3/4] 配置当前 PowerShell 会话编码..." -ForegroundColor Yellow
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$env:LANG = "zh_CN.UTF-8"
chcp 65001 | Out-Null
Write-Host "✓ 当前会话编码已设置为 UTF-8" -ForegroundColor Green
Write-Host ""

# 4. 配置 PowerShell 配置文件（永久设置）
Write-Host "[4/4] 配置 PowerShell 配置文件（永久设置）..." -ForegroundColor Yellow
$profileContent = @"
# Git 中文支持 - UTF-8 编码设置
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
`$env:LANG = "zh_CN.UTF-8"
chcp 65001 | Out-Null
"@

if (!(Test-Path $PROFILE)) {
    New-Item -ItemType File -Path $PROFILE -Force | Out-Null
    Write-Host "✓ 已创建 PowerShell 配置文件: $PROFILE" -ForegroundColor Green
}

$existingContent = Get-Content $PROFILE -ErrorAction SilentlyContinue -Raw
if ($existingContent -notmatch "Git 中文支持") {
    Add-Content -Path $PROFILE -Value "`n$profileContent"
    Write-Host "✓ 已添加 UTF-8 编码配置到 PowerShell 配置文件" -ForegroundColor Green
} else {
    Write-Host "⚠ PowerShell 配置文件中已存在 UTF-8 编码设置" -ForegroundColor Yellow
}
Write-Host ""

# 验证配置
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "配置验证" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "Git 配置：" -ForegroundColor Yellow
git config --global i18n.commitencoding
git config --global i18n.logoutputencoding
git config --global core.quotepath
Write-Host ""

Write-Host "PowerShell 编码：" -ForegroundColor Yellow
Write-Host "  控制台编码: $([Console]::OutputEncoding.EncodingName)"
Write-Host "  代码页: $(chcp.com | Select-String -Pattern '\d+').Matches.Value"
Write-Host ""

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "配置完成！" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "现在你可以使用以下方式提交中文消息：" -ForegroundColor Green
Write-Host "  git commit -m `"中文提交消息`"" -ForegroundColor White
Write-Host ""
Write-Host "注意：" -ForegroundColor Yellow
Write-Host "  - 当前 PowerShell 会话已配置 UTF-8" -ForegroundColor White
Write-Host "  - 新打开的 PowerShell 窗口会自动加载 UTF-8 配置" -ForegroundColor White
Write-Host "  - 如果遇到问题，请重新打开 PowerShell 窗口" -ForegroundColor White
Write-Host ""

