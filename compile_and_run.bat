@echo off
chcp 65001 >nul
echo ========================================
echo Zuul游戏 - 编译和运行脚本
echo ========================================
echo.

REM 自动检测MySQL驱动JAR文件
set MYSQL_JAR=
for %%f in (lib\mysql-connector-j-*.jar) do set MYSQL_JAR=%%f

if "%MYSQL_JAR%"=="" (
    echo [错误] 未找到MySQL驱动文件！
    echo.
    echo 请按照以下步骤操作：
    echo 1. 访问 https://dev.mysql.com/downloads/connector/j/
    echo 2. 下载 Platform Independent 版本的驱动
    echo 3. 解压后找到 mysql-connector-j-*.jar 文件
    echo 4. 将JAR文件放到 lib\ 目录中
    echo.
    echo 详细说明请查看 MYSQL_DRIVER_SETUP.md
    echo.
    pause
    exit /b 1
)

echo [检测] 找到MySQL驱动: %MYSQL_JAR%
echo.

echo [1/2] 正在编译Java源代码...
javac -d bin -encoding UTF-8 -cp "%MYSQL_JAR%" src\cn\edu\whut\sept\zuul\*.java

if %errorlevel% neq 0 (
    echo [错误] 编译失败！
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
echo 按 Ctrl+C 停止服务器
echo.

java -cp "bin;%MYSQL_JAR%" cn.edu.whut.sept.zuul.WebMain

pause

