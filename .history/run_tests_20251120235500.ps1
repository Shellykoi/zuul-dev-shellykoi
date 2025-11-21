# 编译和运行单元测试
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "编译和运行单元测试" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 设置编码为UTF-8
$env:JAVA_TOOL_OPTIONS = "-Dfile.encoding=UTF-8"

# 编译测试类
Write-Host "[1/2] 编译测试类..." -ForegroundColor Yellow
javac -d bin -encoding UTF-8 -sourcepath "src;test" src/cn/edu/whut/sept/zuul/*.java test/cn/edu/whut/sept/zuul/*.java

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ 编译失败！" -ForegroundColor Red
    exit 1
}

Write-Host "✅ 编译成功" -ForegroundColor Green
Write-Host ""

# 运行测试
Write-Host "[2/2] 运行单元测试..." -ForegroundColor Yellow
Write-Host ""
java -cp bin cn.edu.whut.sept.zuul.TestRunner

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "✅ 所有测试通过！" -ForegroundColor Green
} else {
    Write-Host ""
    Write-Host "❌ 部分测试失败！" -ForegroundColor Red
}

