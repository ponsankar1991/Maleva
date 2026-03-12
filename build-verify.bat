@echo off
REM Build verification script for SelectSaleOrder implementation
REM This script validates that all dependencies are properly resolved

echo ========================================
echo SelectSaleOrder Build Verification
echo ========================================
echo.

echo [1] Checking Maven version...
mvn --version
echo.

echo [2] Cleaning previous build artifacts...
mvn clean
echo.

echo [3] Validating pom.xml...
mvn validate
echo.

echo [4] Checking dependencies...
echo     - Checking for Jackson databind:
mvn dependency:tree | findstr /C:"jackson-databind"
echo     - Checking for MapStruct:
mvn dependency:tree | findstr /C:"mapstruct"
echo.

echo [5] Compiling project...
mvn compile
if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo ✅ BUILD SUCCESSFUL
    echo ========================================
    echo The SelectSaleOrder implementation is ready!
    echo.
    echo Next steps:
    echo 1. Run tests: mvn test
    echo 2. Start application: mvn spring-boot:run
    echo 3. Test endpoint: curl -X POST http://localhost:8080/api/sale-orders/search ...
    echo.
) else (
    echo.
    echo ========================================
    echo ❌ BUILD FAILED
    echo ========================================
    echo Please check the errors above and fix them.
    echo.
)

pause

