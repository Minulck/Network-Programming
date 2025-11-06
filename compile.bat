@echo off
echo.
echo ===============================
echo   Compiling BidEasy Project
echo ===============================
echo.

javac -cp "lib\java-websocket-1.5.3.jar;lib\slf4j-api-1.7.36.jar;lib\slf4j-simple-1.7.36.jar" server\*.java shared\*.java client\Client.java

if %errorlevel% equ 0 (
    echo.
    echo ✓ Compilation successful!
    echo You can now run start-server.bat to start the server.
) else (
    echo.
    echo ✗ Compilation failed!
    echo Please check the error messages above.
)

echo.
pause