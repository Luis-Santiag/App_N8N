# ⚠️ TODAVÍA SALE ERROR 10 - VERIFICACIÓN COMPLETA

## El error 10 persiste porque necesitas CREAR UN NUEVO Client ID tipo ANDROID

El Client ID que me diste (`984493768973-qnm36hkjf51tob9e0ddtt425r4rugbqa`) podría ser:
- El mismo Client ID Web anterior con otro formato
- Un Client ID que no está correctamente configurado

---

## ✅ SOLUCIÓN DEFINITIVA - PASO A PASO

### PASO 1: Verificar qué tipo de Client ID tienes

1. Ve a [Google Cloud Console - Credenciales](https://console.cloud.google.com/apis/credentials)

2. En la lista de "IDs de cliente de OAuth 2.0", busca todos tus Client IDs

3. **Verifica el TIPO de cada uno:**
   - Si dice **"Aplicación web"** → ❌ NO sirve para Error 10
   - Si dice **"Android"** → ✅ Este es el que necesitas

4. **¿Tienes alguno que diga "Android"?**
   - **SÍ** → Copia ese Client ID y ve al PASO 3
   - **NO** → Continúa con el PASO 2 para crearlo

---

### PASO 2: Crear Client ID tipo ANDROID (si no existe)

1. En [Google Cloud Console - Credenciales](https://console.cloud.google.com/apis/credentials)

2. Haz clic en **"+ CREAR CREDENCIALES"**

3. Selecciona **"ID de cliente de OAuth 2.0"**

4. **IMPORTANTE:** En "Tipo de aplicación" selecciona **"Android"** (NO "Aplicación web")

5. Completa el formulario:
   ```
   Nombre: MediNotas Android Debug
   Nombre del paquete: com.example.lista_medica2dointento
   Huella digital del certificado SHA-1: [EL SHA-1 QUE OBTUVISTE]
   ```

6. Haz clic en **"CREAR"**

7. **COPIA EL NUEVO CLIENT ID** que aparece (será diferente al Web)

---

### PASO 3: Actualizar GoogleAuthManager.java

Abre `GoogleAuthManager.java` y reemplaza la línea del CLIENT_ID con el nuevo:

```java
private static final String CLIENT_ID = "TU_NUEVO_CLIENT_ID_ANDROID.apps.googleusercontent.com";
```

---

### PASO 4: Configurar usuarios de prueba

1. Ve a [Pantalla de consentimiento OAuth](https://console.cloud.google.com/apis/credentials/consent)

2. Baja hasta **"Usuarios de prueba"**

3. Agrega tu correo de Gmail (el que usarás en el teléfono/emulador)

---

### PASO 5: Habilitar APIs

Ve a [Biblioteca de APIs](https://console.cloud.google.com/apis/library) y habilita:
- ✅ Google Drive API
- ✅ People API

---

### PASO 6: Limpiar y reconstruir

En Android Studio:
1. **Build** → **Clean Project**
2. **Build** → **Rebuild Project**
3. **Run**

---

## 🔍 CÓMO VERIFICAR QUE TIENES EL CLIENT ID CORRECTO

El Client ID tipo Android se ve así:
```
123456789-abcdefghijklmnop.apps.googleusercontent.com
```

Pero lo importante es que en Google Cloud Console debe aparecer como:
```
Tipo: Android
Nombre del paquete: com.example.lista_medica2dointento
SHA-1: XX:XX:XX:XX:...
```

---

## 📸 CAPTURA DE PANTALLA DE LO QUE DEBES VER

En Google Cloud Console → Credenciales, debes tener:

```
IDs de cliente de OAuth 2.0

📱 MediNotas Android Debug          Tipo: Android
   984493768973-XXXXX.apps.googleusercontent.com
   Nombre del paquete: com.example.lista_medica2dointento
   SHA-1: A1:B2:C3:...

🌐 [Tu Client ID Web antiguo]       Tipo: Aplicación web
   984493768973-udrm67kgft5d9l7l9pgqj0gvvhj2lnd1.apps.googleusercontent.com
```

---

## ❓ ¿Qué Client ID me diste?

El que me proporcionaste fue:
```
984493768973-qnm36hkjf51tob9e0ddtt425r4rugbqa.apps.googleusercontent.com
```

**VERIFICA en Google Cloud Console si este es tipo "Android" o "Aplicación web"**

Si es "Aplicación web", necesitas crear uno nuevo de tipo "Android".

---

## 🆘 SI YA HICISTE TODO ESTO Y AÚN SALE ERROR 10

Entonces hay 3 posibilidades:

### 1. El SHA-1 está mal configurado
- El SHA-1 que pegaste en Google Cloud Console debe ser EXACTAMENTE el mismo que genera tu keystore
- Vuelve a ejecutar `buscar_keystore.bat` y verifica que coincida

### 2. Estás usando una cuenta diferente
- El correo que usas para iniciar sesión DEBE estar en "Usuarios de prueba"
- Verifica que sea exactamente el mismo correo

### 3. Los cambios no se aplicaron
- Espera 2-3 minutos después de crear el Client ID
- Limpia completamente el proyecto (Build → Clean)
- Cierra y vuelve a abrir Android Studio
- Reconstruye todo

---

## 🎯 ACCIÓN INMEDIATA

**HAZ ESTO AHORA:**

1. Ve a [Google Cloud Console - Credenciales](https://console.cloud.google.com/apis/credentials)

2. Toma una captura de pantalla de tus Client IDs (tapa info sensible si quieres)

3. **Responde:** ¿Tienes algún Client ID que diga "Tipo: Android"?

4. Si NO tienes ninguno tipo Android, créalo siguiendo el PASO 2 de arriba

5. Copia el nuevo Client ID y dímelo para actualizarlo en el código

