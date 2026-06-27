@echo off
chcp 65001 >nul
echo ========================================
echo   福彩36选7 自动测试程序
echo   注册100000用户 + 自动购票 + 模拟抽奖
echo ========================================
echo.

cd /d "%~dp0"
cd ..

java -cp classes test.AutoTest

pause >nul
