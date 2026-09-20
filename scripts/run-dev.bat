@echo off
rem ==========================================================
rem Levanta los 3 microservicios en ventanas separadas, en los
rem puertos 1808x (asi no chocan con Jenkins u otra cosa en
rem 8080/8081). Perfil dev = H2 en memoria, no hace falta Docker.
rem
rem   -> http://localhost:18080   (admin / admin123)
rem
rem Para frenar: cerra las 3 ventanas.
rem ==========================================================
setlocal
cd /d "%~dp0.."
set "S=%CD%\backend"

if not exist "%S%\api-gateway\target\api-gateway.jar" (
  echo Faltan los .jar. Corriendo scripts\build.bat primero...
  call "%~dp0build.bat"
  if errorlevel 1 exit /b 1
)

start "negocio-service :18082" cmd /k java -jar "%S%\negocio-service\target\negocio-service.jar" --server.port=18082 --app.services.auth-url=http://localhost:18081
timeout /t 6 /nobreak >nul

rem app.services.usuarios-url: el nombre de la propiedad quedo del corte
rem original; hoy apunta a negocio-service, que es dueno del perfil.
start "auth-service :18081" cmd /k java -jar "%S%\auth-service\target\auth-service.jar" --server.port=18081 --app.services.usuarios-url=http://localhost:18082
timeout /t 4 /nobreak >nul

start "api-gateway :18080" cmd /k java -jar "%S%\api-gateway\target\api-gateway.jar" --server.port=18080 --SERVICES_AUTH_URL=http://localhost:18081 --SERVICES_NEGOCIO_URL=http://localhost:18082

echo.
echo ================================================================
echo   3 ventanas levantandose. Espera ~30s y abri:
echo       http://localhost:18080        (admin / admin123)
echo   Cada ventana debe decir "Started ...Application".
echo   Para frenar todo: cerra las 3 ventanas.
echo ================================================================
endlocal
