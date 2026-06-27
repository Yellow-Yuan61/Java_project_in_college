@echo off
chcp 65001 >nul
echo ========================================
echo   福彩36选7 彩票系统 - 编译脚本
echo ========================================
echo.

if not exist "..\classes" mkdir "..\classes"

echo 正在编译源代码...
javac -encoding UTF-8 -d ..\classes -sourcepath ..\src ^
  ..\src\logic\util\Validator.java ^
  ..\src\logic\util\IDGenerator.java ^
  ..\src\logic\storage\DataStore.java ^
  ..\src\logic\model\User.java ^
  ..\src\logic\model\Ticket.java ^
  ..\src\logic\model\DrawResult.java ^
  ..\src\logic\service\UserService.java ^
  ..\src\logic\service\LotteryService.java ^
  ..\src\logic\service\DrawServer.java ^
  ..\src\ui\App.java ^
  ..\src\ui\LoginFrame.java ^
  ..\src\ui\MainFrame.java ^
  ..\src\ui\BuyPanel.java ^
  ..\src\ui\DrawPanel.java ^
  ..\src\ui\HistoryPanel.java ^
  ..\src\ui\UserPanel.java ^
  ..\src\test\AutoTest.java

if %ERRORLEVEL% == 0 (
    echo.
    echo 编译成功！class文件已输出到 ..\classes 目录
) else (
    echo.
    echo 编译失败，请检查错误信息。
)

echo.
echo 按任意键退出...
pause >nul
