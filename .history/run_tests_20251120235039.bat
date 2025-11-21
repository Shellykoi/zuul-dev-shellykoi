@echo off
chcp 65001 >nul
echo ========================================
echo 编译和运行单元测试
echo ========================================
echo.

REM 设置编码为UTF-8
set JAVA_TOOL_OPTIONS=-Dfile.encoding=UTF-8

REM 编译测试类
echo [1/2] 编译测试类...
javac -d bin -encoding UTF-8 -sourcepath src;test src/cn/edu/whut/sept/zuul/*.java test/cn/edu/whut/sept/zuul/*.java
if %errorlevel% neq 0 (
    echo ❌ 编译失败！
    pause
    exit /b 1
)
echo ✅ 编译成功
echo.

REM 运行测试
echo [2/2] 运行单元测试...
echo.
java -cp bin cn.edu.whut.sept.zuul.TestRunner

pause

