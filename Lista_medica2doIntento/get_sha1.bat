@echo off
setlocal enabledelayedexpansion

echo ====================================
echo OBTENIENDO SHA-1 PARA ANDROID
echo ====================================
echo.

REM Buscar Android Studio
set "ANDROID_STUDIO_PATHS=C:\Program Files\Android\Android Studio C:\Program Files (x86)\Android\Android Studio"

for %%A in (%ANDROID_STUDIO_PATHS%) do (
    if exist "%%A\jbr\bin\keytool.exe" (
        echo Usando keytool de Android Studio...
        echo.
        "%%A\jbr\bin\keytool.exe" -list -v -keystore "%USERPROFILE%\.android\debug.keystore" -alias androiddebugkey -storepass android -keypass android 2>nul
        if !errorlevel! == 0 (
            echo.
            echo ====================================
            echo COPIA EL SHA1 DE ARRIBA
            echo ====================================
            pause
            exit /b 0
        )
    )
)

REM Si no encontró Android Studio, buscar Java
echo Buscando Java JDK...
echo.

set "JAVA_PATHS=C:\Program Files\Java C:\Program Files (x86)\Java"

for %%J in (%JAVA_PATHS%) do (
    if exist "%%J" (
        for /d %%D in ("%%J\*") do (
            if exist "%%D\bin\keytool.exe" (
                echo Usando keytool de Java: %%D
                echo.
                "%%D\bin\keytool.exe" -list -v -keystore "%USERPROFILE%\.android\debug.keystore" -alias androiddebugkey -storepass android -keypass android 2>nul
                if !errorlevel! == 0 (
                    echo.
                    echo ====================================
                    echo COPIA EL SHA1 DE ARRIBA
                    echo ====================================
                    pause
                    exit /b 0
                )
            )
        )
    )
)

REM Verificar si existe el keystore
if not exist "%USERPROFILE%\.android\debug.keystore" (
    echo.
    echo ERROR: No se encontro el archivo debug.keystore
    echo Ubicacion esperada: %USERPROFILE%\.android\debug.keystore
    echo.
    echo Esto significa que nunca has ejecutado una app Android en debug.
    echo Soluciones:
    echo 1. Ejecuta tu app una vez desde Android Studio
    echo 2. O usa el metodo de Gradle en Android Studio
    echo.
    pause
    exit /b 1
)

echo.
echo ====================================
echo NO SE ENCONTRO KEYTOOL
echo ====================================
echo.
echo Usa el metodo de Gradle:
echo 1. En Android Studio, abre panel Gradle (derecha)
echo 2. app ^> Tasks ^> android ^> signingReport
echo 3. Doble clic
echo 4. Copia el SHA1 de la salida
echo.
pause
exit /b 1

