@echo off
chcp 65001 >nul
echo ========================================
echo Zuul游戏 - 编译和运行脚本
echo ========================================
echo.

REM 自动检测MySQL驱动JAR文件（取第一个匹配）
set MYSQL_JAR=
for %%f in (lib\mysql-connector-j-*.jar) do (
    if not defined MYSQL_JAR set MYSQL_JAR=%%f
)

if "%MYSQL_JAR%"=="" (
    echo [错误] 未找到MySQL驱动文件。
    echo 请下载 Platform Independent 版本的 mysql-connector-j-*.jar 放到 lib\ 目录（参见 MYSQL_DRIVER_SETUP.md）。
    pause
    exit /b 1
)

echo [检测] 找到MySQL驱动: %MYSQL_JAR%
echo.

echo [1/2] 正在编译Java源代码...
javac -d bin -encoding UTF-8 -cp "lib/*" src\cn\edu\whut\sept\zuul\*.java

if %errorlevel% neq 0 (
    echo [错误] 编译失败。
    pause
    exit /b 1
)

echo [成功] 编译完成！
echo.
echo [2/2] 正在启动Web服务器...
echo.
echo ========================================
echo 服务器启动后，请在浏览器中访问：
echo http://localhost:8080
echo ========================================
echo.
echo 按 Ctrl+C 停止服务器。
echo.

java -cp "bin;lib/*" cn.edu.whut.sept.zuul.WebMain

pause
