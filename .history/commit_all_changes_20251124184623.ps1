# Git 提交所有更改的脚本
# 提交消息：完善优化

Write-Host "正在检查 Git 状态..." -ForegroundColor Cyan
git status

Write-Host "`n正在添加所有更改的文件..." -ForegroundColor Cyan
git add -A

Write-Host "`n当前暂存区的文件：" -ForegroundColor Cyan
git status --short

Write-Host "`n正在提交更改（提交消息：完善优化）..." -ForegroundColor Cyan
git commit -m "完善优化"

Write-Host "`n正在推送到远程仓库..." -ForegroundColor Cyan
git push

Write-Host "`n完成！所有更改已提交并推送到 GitHub。" -ForegroundColor Green

