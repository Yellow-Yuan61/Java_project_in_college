@echo off
chcp 65001 >nul
echo ========================================
echo   福彩36选7 抽奖服务器
echo ========================================
echo.
echo 启动抽奖Socket服务器，监听端口 9527
echo.

cd /d "%~dp0"
cd ..

java -cp classes logic.service.DrawServer

pause >nul
