@echo off
rem ==========================================================
rem Compila los 5 modulos (Maven) y el frontend (React).
rem Dejar los .jar en services\*\target y el build de React
rem dentro del gateway. Correr una vez, y despues run-dev.bat.
rem ==========================================================
setlocal
cd /d "%~dp0.."

echo === Compilando microservicios (Maven) ===
call mvn -f services/pom.xml -DskipTests package
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
