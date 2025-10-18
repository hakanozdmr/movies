@echo off
setlocal enabledelayedexpansion
REM Movies API Performance Tests Runner Script for Windows

echo 🚀 Starting Movies API Performance Tests...
echo.
echo 📍 Current directory: %CD%
echo 📅 Date: %DATE%
echo ⏰ Time: %TIME%
echo.

REM Check if we're in the right directory (should have pom.xml)
if not exist "pom.xml" (
    echo ❌ Error: pom.xml not found in current directory.
    echo 💡 Please run this script from the project root directory.
    echo 📍 Current location: %CD%
    echo.
    echo Press any key to exit...
    pause >nul
    exit /b 1
)
echo ✅ Found pom.xml - running from correct directory.

REM Create reports directory
if not exist "target\performance-reports" mkdir "target\performance-reports"
set REPORTS_DIR=target\performance-reports
for /f "tokens=2-4 delims=/ " %%a in ('date /t') do (set mydate=%%c%%a%%b)
for /f "tokens=1-2 delims=/:" %%a in ('time /t') do (set mytime=%%a%%b)
set TIMESTAMP=%mydate%_%mytime%

echo 📊 Reports will be saved to: %REPORTS_DIR%

REM Check if Maven is available - try both mvn and mvnw
echo 🔍 Checking for Maven...
where mvn >nul 2>&1
if %errorlevel% equ 0 (
    set MVN_CMD=mvn
    echo ✅ Found Maven at: mvn
) else (
    if exist "mvnw.cmd" (
        set MVN_CMD=.\mvnw.cmd
        echo ✅ Found Maven wrapper: mvnw.cmd
    ) else (
        echo ❌ Maven not found. Please install Maven to run performance tests.
        echo 💡 Tried: mvn, mvnw.cmd
        echo.
        echo Press any key to exit...
        pause >nul
        exit /b 1
    )
)

REM Set JAVA_HOME if not set
if "%JAVA_HOME%"=="" (
    echo 🔍 JAVA_HOME not set, attempting to find it...
    
    REM Try common Java installation paths
    if exist "C:\Program Files\Java\jdk-21" (
        set "JAVA_HOME=C:\Program Files\Java\jdk-21"
        echo ✅ Found Java 21: !JAVA_HOME!
    ) else if exist "C:\Program Files\Java\jdk-17" (
        set "JAVA_HOME=C:\Program Files\Java\jdk-17"
        echo ✅ Found Java 17: !JAVA_HOME!
    ) else if exist "C:\Program Files\Java\jdk-11" (
        set "JAVA_HOME=C:\Program Files\Java\jdk-11"
        echo ✅ Found Java 11: !JAVA_HOME!
    ) else if exist "C:\Program Files\Java\jdk-8" (
        set "JAVA_HOME=C:\Program Files\Java\jdk-8"
        echo ✅ Found Java 8: !JAVA_HOME!
    ) else (
        echo ❌ JAVA_HOME not set and could not find Java JDK automatically
        echo 💡 Please set JAVA_HOME environment variable manually
        echo 💡 Example: set JAVA_HOME=C:\Program Files\Java\jdk-21
        pause
        exit /b 1
    )
) else (
    echo ✅ JAVA_HOME is set: %JAVA_HOME%
)

REM Update PATH to include JAVA_HOME\bin
set "PATH=!JAVA_HOME!\bin;!PATH!"

REM Skip detailed Maven/Java testing to avoid timeouts
echo ✅ Using Maven command: %MVN_CMD%
echo ✅ Proceeding with performance tests...

REM Check if JMeter is available
jmeter --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ⚠️  JMeter not found. JMeter tests will be skipped.
    set JMETER_AVAILABLE=false
) else (
    echo ✅ JMeter found. JMeter tests will be included.
    set JMETER_AVAILABLE=true
)

REM Ensure reports directory exists
if not exist "%REPORTS_DIR%" (
    echo 📁 Creating reports directory: %REPORTS_DIR%
    mkdir "%REPORTS_DIR%"
)

REM 1. Run JMH Benchmarks  
echo 🔄 Running JMH Benchmarks...
echo 📝 Running: %MVN_CMD% compile test-compile (skip clean to avoid conflicts)
%MVN_CMD% compile test-compile > "%REPORTS_DIR%\maven-compile-%TIMESTAMP%.log" 2>&1
set COMPILE_EXIT_CODE=%errorlevel%

if %COMPILE_EXIT_CODE% neq 0 (
    echo ❌ Maven compile failed. Check logs in %REPORTS_DIR%\maven-compile-%TIMESTAMP%.log
    echo.
    echo Press any key to continue or exit...
    pause >nul
    exit /b 1
)

echo ✅ Maven compile completed successfully

REM Check if benchmarks.jar exists, if not, try to build it or skip JMH
echo 📝 Checking for benchmarks.jar...
if exist "target\benchmarks.jar" (
    echo ✅ Benchmarks JAR found, running JMH benchmarks...
    java -jar target\benchmarks.jar -rf json -rff "%REPORTS_DIR%\jmh-results-%TIMESTAMP%.json" > "%REPORTS_DIR%\jmh-output-%TIMESTAMP%.txt" 2>&1
    
    if %errorlevel% equ 0 (
        echo ✅ JMH Benchmarks completed successfully
    ) else (
        echo ❌ JMH Benchmarks failed - check logs in %REPORTS_DIR%\jmh-output-%TIMESTAMP%.txt
    )
) else (
    echo ⚠️  Benchmarks JAR not found, attempting to build...
    echo 📝 Running: %MVN_CMD% package
    %MVN_CMD% package > "%REPORTS_DIR%\maven-package-%TIMESTAMP%.log" 2>&1
    
    if exist "target\benchmarks.jar" (
        echo ✅ Benchmarks JAR built successfully, running JMH...
        java -jar target\benchmarks.jar -rf json -rff "%REPORTS_DIR%\jmh-results-%TIMESTAMP%.json" > "%REPORTS_DIR%\jmh-output-%TIMESTAMP%.txt" 2>&1
        
        if %errorlevel% equ 0 (
            echo ✅ JMH Benchmarks completed successfully
        ) else (
            echo ❌ JMH Benchmarks failed - check logs in %REPORTS_DIR%\jmh-output-%TIMESTAMP%.txt
        )
    ) else (
        echo ⚠️  Could not build benchmarks JAR, skipping JMH tests
        echo 📝 This might be due to missing benchmark profile or JMH configuration
    )
)

:skip_jmh
REM 2. Run JUnit Performance Tests
echo 🔄 Running JUnit Performance Tests...
echo 📝 Running: %MVN_CMD% test -Dtest="*PerformanceTest,*LoadTest"
%MVN_CMD% test -Dtest="*PerformanceTest,*LoadTest" > "%REPORTS_DIR%\junit-performance-%TIMESTAMP%.txt" 2>&1
set JUNIT_EXIT_CODE=%errorlevel%

if %JUNIT_EXIT_CODE% equ 0 (
    echo ✅ JUnit Performance Tests completed successfully
    echo 📋 Results saved to: %REPORTS_DIR%\junit-performance-%TIMESTAMP%.txt
) else (
    echo ❌ JUnit Performance Tests failed with exit code: %JUNIT_EXIT_CODE%
    echo 📋 Check logs in: %REPORTS_DIR%\junit-performance-%TIMESTAMP%.txt
)

REM Also run regular unit tests if performance tests are missing
echo 🔄 Running all unit tests as fallback...
%MVN_CMD% test > "%REPORTS_DIR%\junit-all-%TIMESTAMP%.txt" 2>&1
set ALL_TESTS_EXIT_CODE=%errorlevel%

if %ALL_TESTS_EXIT_CODE% equ 0 (
    echo ✅ All unit tests completed successfully
    echo 📋 Results saved to: %REPORTS_DIR%\junit-all-%TIMESTAMP%.txt
) else (
    echo ⚠️  Some unit tests failed, but continuing...
    echo 📋 Check logs in: %REPORTS_DIR%\junit-all-%TIMESTAMP%.txt
)

REM 3. Run JMeter Tests (if available)
if "%JMETER_AVAILABLE%"=="true" (
    echo 🔄 Running JMeter Load Tests...
    
    REM Start application in background for JMeter tests
    echo Starting application for JMeter tests...
    echo 📝 Starting app with: %MVN_CMD% spring-boot:run
    start /b %MVN_CMD% spring-boot:run > "%REPORTS_DIR%\app-startup-%TIMESTAMP%.log" 2>&1
    
    REM Wait for application to start
    echo Waiting for application to start...
    timeout /t 30 /nobreak >nul
    
    REM Run JMeter tests
    jmeter -n -t "src\test\jmeter\movies-performance-test.jmx" ^
           -l "%REPORTS_DIR%\jmeter-results-%TIMESTAMP%.jtl" ^
           -e -o "%REPORTS_DIR%\jmeter-html-report-%TIMESTAMP%"
    
    if %errorlevel% equ 0 (
        echo ✅ JMeter Tests completed successfully
    ) else (
        echo ❌ JMeter Tests failed
    )
    
    REM Stop application (find and kill Java process running Spring Boot)
    echo Stopping application...
    for /f "tokens=2" %%i in ('tasklist /fi "imagename eq java.exe" /fo table ^| findstr "java.exe"') do (
        taskkill /f /pid %%i >nul 2>&1
    )
)

REM 4. Generate Summary Report
echo 📋 Generating Performance Test Summary...

(
echo # Movies API Performance Test Summary
echo.
echo **Test Date:** %date% %time%
echo.
echo ## Test Results
echo.
echo ### JMH Benchmarks
echo - Results file: `jmh-results-%TIMESTAMP%.json`
echo - Output log: `jmh-output-%TIMESTAMP%.txt`
echo.
echo ### JUnit Performance Tests
echo - Results log: `junit-performance-%TIMESTAMP%.txt`
echo.
echo ### JMeter Load Tests
) > "%REPORTS_DIR%\performance-summary-%TIMESTAMP%.md"

if "%JMETER_AVAILABLE%"=="true" (
    (
        echo - Results file: `jmeter-results-%TIMESTAMP%.jtl`
        echo - HTML Report: `jmeter-html-report-%TIMESTAMP%\index.html`
    ) >> "%REPORTS_DIR%\performance-summary-%TIMESTAMP%.md"
) else (
    (
        echo - Status: Skipped ^(JMeter not available^)
    ) >> "%REPORTS_DIR%\performance-summary-%TIMESTAMP%.md"
)

(
echo.
echo ## How to View Results
echo.
echo 1. **JMH Results**: Open the JSON file in a JSON viewer or convert to CSV
echo 2. **JUnit Results**: Check the output log for detailed test results
echo 3. **JMeter Results**: Open `jmeter-html-report-%TIMESTAMP%\index.html` in a web browser
echo.
echo ## Performance Thresholds
echo.
echo - **Response Time**: ^< 1000ms for GET requests
echo - **Throughput**: ^> 100 requests/second  
echo - **Error Rate**: ^< 1%%
echo - **Memory Usage**: ^< 512MB for load tests
) >> "%REPORTS_DIR%\performance-summary-%TIMESTAMP%.md"

echo.
echo ========================================
echo 🎉 Performance tests completed!
echo ========================================
echo 📁 Results are available in: %REPORTS_DIR%
echo 📋 Summary: %REPORTS_DIR%\performance-summary-%TIMESTAMP%.md
echo.

REM List available report files
echo 📋 Available reports:
if exist "%REPORTS_DIR%\jmh-results-%TIMESTAMP%.json" (
    echo ✅ JMH Benchmarks: jmh-results-%TIMESTAMP%.json
)
if exist "%REPORTS_DIR%\junit-performance-%TIMESTAMP%.txt" (
    echo ✅ JUnit Tests: junit-performance-%TIMESTAMP%.txt
)
if exist "%REPORTS_DIR%\jmeter-results-%TIMESTAMP%.jtl" (
    echo ✅ JMeter Results: jmeter-results-%TIMESTAMP%.jtl
)
echo.

REM Open results in default browser (if JMeter report exists)
if exist "%REPORTS_DIR%\jmeter-html-report-%TIMESTAMP%\index.html" (
    echo 🌐 Opening JMeter HTML report in browser...
    start "" "%REPORTS_DIR%\jmeter-html-report-%TIMESTAMP%\index.html"
    echo 👀 JMeter report opened in your default browser
) else (
    echo ℹ️  No JMeter HTML report found to open
)

echo.
echo Press any key to exit...
pause >nul
