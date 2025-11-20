@echo off
chcp 65001 >nul
echo ========================================
echo 修复登录问题
echo ========================================
echo.

echo [1/3] 编译代码...
javac -encoding UTF-8 -d bin -cp "lib\mysql-connector-j-9.5.0.jar" src\cn\edu\whut\sept\zuul\*.java
if %ERRORLEVEL% NEQ 0 (
    echo ❌ 编译失败！
    pause
    exit /b %ERRORLEVEL%
)
echo ✅ 编译成功
echo.

echo [2/3] 运行UserFixer确保用户存在...
java -cp "bin;lib\mysql-connector-j-9.5.0.jar" cn.edu.whut.sept.zuul.UserFixer
echo.

echo [3/3] 完成！
echo.
echo 请重启服务器并重新测试登录。
echo.
pause

