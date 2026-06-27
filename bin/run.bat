@echo off
chcp 65001 >nul
echo ========================================
echo   福彩36选7 彩票购买抽奖系统
echo ========================================
echo.

cd /d "%~dp0"
cd ..

java -cp classes ui.App

pause >nul
