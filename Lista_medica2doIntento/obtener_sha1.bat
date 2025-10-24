@echo off
echo ====================================
echo Buscando keytool en tu sistema...
echo ====================================
echo.

REM Intentar con Java en PATH
where keytool
if %errorlevel% == 0 (
    echo.
    echo Keytool encontrado! Ejecutando...
    echo.
    keytool -list -v -keystore "%USERPROFILE%\.android\debug.keystore" -alias androiddebugkey -storepass android -keypass android
    pause
    exit
)

REM Buscar en ubicaciones comunes de Java
echo Keytool no encontrado en PATH. Buscando Java...
echo.

set "JAVA_LOCATIONS=C:\Program Files\Java C:\Program Files (x86)\Java C:\Program Files\Android\Android Studio\jbr"

for %%J in (%JAVA_LOCATIONS%) do (
    if exist "%%J" (
        echo Buscando en: %%J
        for /d %%D in ("%%J\*") do (
            if exist "%%D\bin\keytool.exe" (
                echo.
                echo Keytool encontrado en: %%D\bin\keytool.exe
                echo Ejecutando...
                echo.
                "%%D\bin\keytool.exe" -list -v -keystore "%USERPROFILE%\.android\debug.keystore" -alias androiddebugkey -storepass android -keypass android
                echo.
                echo ====================================
                echo Busca la linea SHA1 arriba ^^^
                echo ====================================
                pause
                exit
            )
        )
    )
)

echo.
echo ====================================
echo ERROR: No se encontro keytool
echo ====================================
echo.
echo Intenta usar el Metodo 1 desde Android Studio:
echo 1. Abre Gradle (panel derecho)
echo 2. app ^> Tasks ^> android ^> signingReport
echo 3. Doble clic
echo.
pause

