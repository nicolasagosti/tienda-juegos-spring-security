@echo off
rem ==========================================================
rem Compila los 5 modulos Maven (api-gateway, auth-service,
rem negocio-service y las libs common-security / common-web) y
rem el frontend (React). Deja los .jar en backend\*\target y el
rem build de React dentro del gateway. Correr una vez, y
rem despues run-dev.bat.
rem ==========================================================
setlocal
cd /d "%~dp0.."

echo === Compilando microservicios (Maven) ===
call mvn -f backend/pom.xml -DskipTests package
if errorlevel 1 (echo FALLO el build de Maven & exit /b 1)

echo.
echo === Compilando frontend (React) ===
pushd frontend
call npm install
if errorlevel 1 (popd & echo FALLO npm install & exit /b 1)
call npm run build
if errorlevel 1 (popd & echo FALLO npm run build & exit /b 1)
popd

echo.
echo LISTO. Ahora corre:  scripts\run-dev.bat
endlocal
