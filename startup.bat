@echo off
set SERVER_DIR=server
set CLIENT_DIR=client

:: Launching the server instances with a 2-second delay between each
start cmd /k wsl bash -c "cd %SERVER_DIR% && mvn exec:java -Dexec.args=\"8080 0\""

start cmd /k wsl bash -c "cd %SERVER_DIR% && mvn exec:java -Dexec.args=\"8080 1\""

start cmd /k wsl bash -c "cd %SERVER_DIR% && mvn exec:java -Dexec.args=\"8080 2\""

start cmd /k wsl bash -c "cd %SERVER_DIR% && mvn exec:java -Dexec.args=\"8080 3\""

start cmd /k wsl bash -c "cd %SERVER_DIR% && mvn exec:java -Dexec.args=\"8080 4\""

:: Wait for 2 seconds before launching the client
timeout /t 2 /nobreak >nul

:: Launch the client instance
start cmd /k wsl bash -c "cd %CLIENT_DIR% && mvn exec:java"

pause
