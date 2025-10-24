@echo off
echo ====================================
echo VERIFICANDO DEBUG KEYSTORE
echo ====================================
echo.

REM Verificar si existe el keystore
set "KEYSTORE_PATH=%USERPROFILE%\.android\debug.keystore"
echo Buscando keystore en: %KEYSTORE_PATH%
echo.

if exist "%KEYSTORE_PATH%" (
    echo [OK] Keystore encontrado!
    echo.

    REM Buscar keytool en Android Studio
    set "KEYTOOL_PATH="

    if exist "C:\Program Files\Android\Android Studio\jbr\bin\keytool.exe" (
        set "KEYTOOL_PATH=C:\Program Files\Android\Android Studio\jbr\bin\keytool.exe"
    ) else if exist "C:\Program Files (x86)\Android\Android Studio\jbr\bin\keytool.exe" (
        set "KEYTOOL_PATH=C:\Program Files (x86)\Android\Android Studio\jbr\bin\keytool.exe"
    )

    if defined KEYTOOL_PATH (
        echo [OK] Keytool encontrado en: %KEYTOOL_PATH%
        echo.
        echo ====================================
        echo EJECUTANDO KEYTOOL...
        echo ====================================
        echo.

        "%KEYTOOL_PATH%" -keystore "%KEYSTORE_PATH%" -list -v -alias androiddebugkey -storepass android -keypass android

        echo.
        echo ====================================
        echo BUSCA LA LINEA "SHA1:" ARRIBA
        echo Y COPIA TODO EL VALOR
        echo ====================================
    ) else (
        echo [ERROR] No se encontro keytool.exe
        echo.
        echo Intenta ejecutar esto MANUALMENTE en Command Prompt:
        echo.
        echo keytool -keystore "%KEYSTORE_PATH%" -list -v -alias androiddebugkey -storepass android -keypass android
        echo.
    )
) else (
    echo [ERROR] No se encontro debug.keystore
    echo.
    echo El archivo deberia estar en:
    echo %KEYSTORE_PATH%
    echo.
    echo SOLUCION: Ejecuta tu app una vez desde Android Studio
    echo para generar el keystore automaticamente.
    echo.
)

pause

