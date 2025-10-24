# Configuración de OAuth 2.0 para Google (Modo Desarrollo)

## ⚠️ SOLUCIÓN AL ERROR 10 - DEVELOPER_ERROR

Si ves **Error 10**, significa que necesitas crear un **Client ID tipo Android** (no Web) con el SHA-1 de tu app.

---

## 🔧 SOLUCIÓN COMPLETA AL ERROR 10 (5 minutos)

### PASO 1: Obtener el SHA-1 de tu app

1. Abre la **Terminal** en Android Studio (View → Tool Windows → Terminal)
2. Ejecuta este comando:
   ```cmd
   .\gradlew signingReport
   ```
3. Busca en la salida la línea que dice **SHA1:** bajo "Variant: debug"
4. **Copia ese SHA-1 completo** (ejemplo: `A1:B2:C3:D4:E5:F6:...`)

**Ejemplo de lo que verás:**
```
Variant: debug
Config: debug
Store: C:\Users\USUARIO\.android\debug.keystore
Alias: AndroidDebugKey
SHA1: XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX
SHA-256: ...
```

### PASO 2: Crear Client ID tipo Android

1. Ve a [Google Cloud Console - Credenciales](https://console.cloud.google.com/apis/credentials)

2. Haz clic en **"+ CREAR CREDENCIALES"** → **"ID de cliente de OAuth 2.0"**

3. En **"Tipo de aplicación"**, selecciona **"Android"** (NO "Aplicación web")

4. Completa los campos:
   - **Nombre:** `MediNotas Android`
   - **Nombre del paquete:** `com.example.lista_medica2dointento`
   - **Huella digital del certificado SHA-1:** Pega el SHA-1 del PASO 1

5. Haz clic en **"CREAR"**

6. Se mostrará tu nuevo Client ID. **Cópialo** (será como `123456-abc.apps.googleusercontent.com`)

### PASO 3: Actualizar GoogleAuthManager.java

1. Abre `app/src/main/java/.../GoogleAuthManager.java`

2. Busca la línea:
   ```java
   private static final String CLIENT_ID = "TU_CLIENT_ID_ANDROID_AQUI.apps.googleusercontent.com";
   ```

3. **Reemplaza** `TU_CLIENT_ID_ANDROID_AQUI.apps.googleusercontent.com` con el Client ID que copiaste en el PASO 2

### PASO 5: Configurar Pantalla de Consentimiento OAuth

1. Ve a [Google Cloud Console - Pantalla de consentimiento OAuth](https://console.cloud.google.com/apis/credentials/consent)
2. Verifica que esté en modo **"Externo"** (o "Interno" si tu cuenta es de Google Workspace)
3. Baja hasta **"Usuarios de prueba"**
4. Haz clic en **"+ AÑADIR USUARIOS"**
5. Agrega tu correo de Gmail que usarás para probar la app
6. Guarda

### PASO 6: Verificar APIs habilitadas

1. Ve a [Biblioteca de APIs](https://console.cloud.google.com/apis/library)
2. Busca y habilita:
   - ✅ **Google Drive API**
   - ✅ **People API** (para obtener email del usuario)

---

## ✅ ¿Qué cambió y por qué funciona ahora?

**ANTES (AppAuth - NO funcionaba):**
- ❌ Usaba navegador web externo
- ❌ Requería configurar redirect URI (`http://localhost`, `http://127.0.0.1`, esquemas personalizados)
- ❌ El navegador intentaba redirigir y daba error "rechazó la conexión"
- ❌ Complejo de configurar

**AHORA (Google Sign-In nativo - SÍ funciona):**
- ✅ Usa el sistema nativo de Google en Android
- ✅ **NO requiere redirect URI** - funciona con intents de Android
- ✅ No hay problemas de redirección
- ✅ Experiencia de usuario más fluida (selector de cuentas de Google)
- ✅ Mucho más simple de configurar

---

## 🎯 ¿Por qué este método es mejor?

- ✅ **Sin redirect URI**: No necesitas configurar ninguna URL de redirección
- ✅ **Nativo de Android**: Usa la API oficial de Google Sign-In
- ✅ **Más seguro**: No expone tokens en URLs
- ✅ **Mejor UX**: Muestra el selector de cuentas de Google integrado
- ✅ **Funciona sin Play Store**: Perfecto para desarrollo y trabajos académicos

---

## 🧪 Probar la configuración

1. **Limpia y reconstruye el proyecto:**
   - En Android Studio: **Build** → **Clean Project**
   - Luego: **Build** → **Rebuild Project**

2. **Ejecuta la app** en tu dispositivo/emulador

3. **Toca "Iniciar sesión con Google"**

4. **Deberías ver:**
   - El selector de cuentas de Google (interfaz nativa)
   - Pantalla de consentimiento para autorizar permisos
   - ✅ Regreso automático a tu app con sesión iniciada

---

## ⚠️ Notas importantes

- **Solo funciona con correos agregados como "Usuarios de prueba"** en Google Cloud Console
- **Límite: 100 usuarios de prueba máximo**
- **La app está en modo "Testing"** - no es público
- **Perfecto para proyectos académicos y desarrollo**
- **NO necesitas publicar en Play Store**

---

## 🆘 Si aún tienes problemas

Verifica estos puntos:

1. ✅ Tu correo está agregado en "Usuarios de prueba" en Google Cloud Console
2. ✅ Las APIs de **Google Drive** y **People API** están habilitadas
3. ✅ Limpiaste y reconstruiste la app después de los cambios
4. ✅ Estás usando el mismo correo que agregaste como usuario de prueba
5. ✅ Has esperado 1-2 minutos después de guardar cambios en Google Console

---

## 🚀 Cambios técnicos realizados

- **GoogleAuthManager.java**: Reescrito completamente para usar `GoogleSignInClient` en lugar de `AppAuth`
- **AndroidManifest.xml**: Eliminado el intent-filter de OAuth redirect (ya no se necesita)
- **Dependencias**: Se usa `com.google.android.gms:play-services-auth` que ya estaba en tu proyecto

**¡Todo debería funcionar ahora sin errores de redirección!** 🎉

