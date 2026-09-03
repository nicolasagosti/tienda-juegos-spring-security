@echo off
rem ==========================================================
rem Levanta los 4 microservicios en ventanas separadas, en los
rem puertos 1808x (asi no chocan con Jenkins u otra cosa en
rem 8080/8081). Perfil dev = H2 en memoria, no hace falta Docker.
rem
rem   -> http://localhost:18080   (admin / admin123)
rem
rem Para frenar: cerra las 4 ventanas.
rem ==========================================================
setlocal
cd /d "%~dp0.."
set "S=%CD%\services"

if not exist "%S%\api-gateway\target\api-gateway.jar" (
  echo Faltan los .jar. Corriendo scripts\build.bat primero...
  call "%~dp0build.bat"
  if errorlevel 1 exit /b 1
)

start "usuarios-service :18082" cmd /k java -jar "%S%\usuarios-service\target\usuarios-service.jar" --server.port=18082 --app.services.auth-url=http://localhost:18081 --app.services.catalogo-url=http://localhost:18083
timeout /t 6 /nobreak >nul

start "catalogo-service :18083" cmd /k java -jar "%S%\catalogo-service\target\catalogo-service.jar" --server.port=18083 --app.services.usuarios-url=http://localhost:18082
timeout /t 4 /nobreak >nul

start "auth-service :18081" cmd /k java -jar "%S%\auth-service\target\auth-service.jar" --server.port=18081 --app.services.usuarios-url=http://localhost:18082
timeout /t 4 /nobreak >nul

start "api-gateway :18080" cmd /k java -jar "%S%\api-gateway\target\api-gateway.jar" --server.port=18080 --SERVICES_AUTH_URL=http://localhost:18081 --SERVICES_USUARIOS_URL=http://localhost:18082 --SERVICES_CATALOGO_URL=http://localhost:18083

echo.
echo ================================================================
echo   4 ventanas levantandose. Espera ~30s y abri:
echo       http://localhost:18080        (admin / admin123)
echo   Cada ventana debe decir "Started ...Application".
echo   Para frenar todo: cerra las 4 ventanas.
echo ================================================================
endlocal
