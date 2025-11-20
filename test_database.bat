@echo off
chcp 65001 >nul
echo ========================================
echo 数据库连接测试
echo ========================================
echo.

echo 正在编译数据库测试工具...
javac -d bin -encoding UTF-8 -cp "lib\mysql-connector-j-9.5.0.jar" src\cn\edu\whut\sept\zuul\DatabaseTest.java

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ❌ 编译失败！
    pause
    exit /b 1
)

echo ✅ 编译成功
echo.
echo 正在运行数据库测试...
echo.

java -cp "bin;lib\mysql-connector-j-9.5.0.jar" cn.edu.whut.sept.zuul.DatabaseTest

echo.
pause

