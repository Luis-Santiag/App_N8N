.\gradlew signingReport# 🔧 SOLUCIÓN AL ERROR 10 - Google Sign-In

## ❌ Error 10: DEVELOPER_ERROR

Este error significa que el Client ID Web no es suficiente para Google Sign-In nativo. Necesitas crear un **Client ID tipo Android** con el SHA-1 de tu app.

---

## 📋 SOLUCIÓN PASO A PASO (5 minutos)

### PASO 1: Obtener el SHA-1 de tu app de desarrollo

Abre la terminal en Android Studio (View → Tool Windows → Terminal) y ejecuta:

**En Windows (cmd):**
```cmd
cd android
gradlew signingReport
```

O directamente desde la raíz del proyecto:
```cmd
.\gradlew signingReport
```

**Busca en la salida la sección "Variant: debug" y copia el SHA-1:**
```
Variant: debug
Config: debug
Store: C:\Users\USUARIO\.android\debug.keystore
Alias: AndroidDebugKey
SHA1: XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX
```

**Copia ese SHA-1 completo** (ejemplo: `A1:B2:C3:D4:E5:F6:...`)

---

### PASO 2: Crear Client ID tipo Android en Google Cloud Console

1. Ve a [Google Cloud Console - Credenciales](https://console.cloud.google.com/apis/credentials)

2. Haz clic en **"+ CREAR CREDENCIALES"** → **"ID de cliente de OAuth 2.0"**

3. En **"Tipo de aplicación"**, selecciona **"Android"**

4. Completa los campos:
   - **Nombre:** `MediNotas Android Debug`
   - **Nombre del paquete:** `com.example.lista_medica2dointento`
   - **Huella digital del certificado SHA-1:** Pega el SHA-1 que copiaste en el PASO 1

5. Haz clic en **"CREAR"**

6. **IMPORTANTE:** Aparecerá un popup con el nuevo Client ID. **Cópialo** (será algo como `123456789-xxxxxxx.apps.googleusercontent.com`)

---

### PASO 3: Actualizar GoogleAuthManager.java con el nuevo Client ID

1. Abre el archivo `GoogleAuthManager.java`

2. Busca la línea:
```java
private static final String CLIENT_ID = "984493768973-udrm67kgft5d9l7l9pgqj0gvvhj2lnd1.apps.googleusercontent.com";
```

3. **Reemplázala** con tu nuevo Client ID tipo Android que acabas de crear

---

### PASO 4: Limpiar y reconstruir

En Android Studio:
1. **Build** → **Clean Project**
2. **Build** → **Rebuild Project**
3. **Run**

---

## ✅ ¿Por qué necesitas esto?

**Error 10 significa:**
- ❌ Estás usando un Client ID Web
- ❌ Google Sign-In nativo requiere un Client ID Android
- ❌ El Client ID Android requiere el SHA-1 de tu certificado de firma

**Con el Client ID Android:**
- ✅ Google verifica que la app es legítima usando el SHA-1
- ✅ Funciona sin necesidad de Play Store (usando debug keystore)
- ✅ Perfecto para desarrollo y trabajos académicos

---

## 🎯 ALTERNATIVA RÁPIDA: Si no tienes tiempo para crear Client ID Android

Si necesitas que funcione YA y no quieres hacer todo esto, puedo cambiar el código para:

1. **Omitir Google Sign-In temporalmente**
2. **Usar solo almacenamiento local** (sin sincronización con Drive)
3. **Agregar Google Sign-In más tarde** cuando tengas tiempo

¿Quieres que implemente esta alternativa rápida o prefieres seguir los pasos para configurar el Client ID Android correctamente?

---

## 📝 Resumen rápido

1. Ejecuta: `.\gradlew signingReport` → Copia el SHA-1
2. Ve a Google Cloud Console → Crear Client ID Android
3. Usa: nombre paquete `com.example.lista_medica2dointento` + tu SHA-1
4. Copia el nuevo Client ID y actualiza `GoogleAuthManager.java`
5. Clean + Rebuild + Run

**¡Esto solucionará el Error 10!** 🎉

