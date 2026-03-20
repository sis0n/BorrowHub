@echo off
echo [BorrowHub] Building and Installing App...
cd %~dp0
call gradlew.bat installDebug

echo [BorrowHub] Setting up port forwarding (8000)...
adb reverse tcp:8000 tcp:8000

echo [BorrowHub] Launching Activity...
adb shell am start -n com.example.borrowhub/com.example.borrowhub.view.LoginActivity
pause