@echo off
echo.
echo ===================================
echo   BidEasy Auction System Server
echo ===================================
echo.
echo Starting server with all required dependencies...
echo.

REM Check if Java is installed
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo Error: Java is not installed or not in PATH
    echo Please install Java 8 or higher and try again.
    pause
    exit /b 1
)

REM Check if required JAR files exist
if not exist "lib\java-websocket-1.5.3.jar" (
    echo Error: java-websocket-1.5.3.jar not found in lib directory
    echo Please ensure all dependencies are in the lib folder.
    pause
    exit /b 1
)

if not exist "lib\slf4j-api-1.7.36.jar" (
    echo Error: slf4j-api-1.7.36.jar not found in lib directory
    echo Please ensure all dependencies are in the lib folder.
    pause
    exit /b 1
)

if not exist "lib\slf4j-simple-1.7.36.jar" (
    echo Error: slf4j-simple-1.7.36.jar not found in lib directory
    echo Please ensure all dependencies are in the lib folder.
    pause
    exit /b 1
)

echo Compiling Java files...
javac -encoding UTF-8 -cp "lib\java-websocket-1.5.3.jar;lib\slf4j-api-1.7.36.jar;lib\slf4j-simple-1.7.36.jar" server\auction\*.java server\chat\*.java server\core\*.java server\ftp\*.java server\notifications\*.java server\security\*.java shared\*.java client\Client.java

if %errorlevel% neq 0 (
    echo.
    echo Error: Compilation failed!
    echo Please check the error messages above.
    pause
    exit /b 1
)

echo.
echo Compilation successful!
echo.
echo Starting BidEasy Server...
echo.
echo Server ports:
echo - Console clients: 5000
echo - WebSocket: 5001  
echo - HTTP Web UI: 8080
echo.
echo Web Interface: http://localhost:8080
echo.
echo Press Ctrl+C to stop the server
echo.

java -cp ".;lib\java-websocket-1.5.3.jar;lib\slf4j-api-1.7.36.jar;lib\slf4j-simple-1.7.36.jar" server.core.Server