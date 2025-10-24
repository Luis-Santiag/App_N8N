# 🚀 SOLUCIÓN RÁPIDA - Sin acceder a Pantalla de Consentimiento

## El problema: No puedes acceder a "Pantalla de consentimiento OAuth"

Esto pasa cuando la pantalla de consentimiento no está configurada o está en modo incompleto.

---

## ✅ SOLUCIÓN ALTERNATIVA - Agregar usuario de prueba desde Credenciales

No necesitas entrar a "Pantalla de consentimiento" para agregar usuarios de prueba. Puedes hacerlo de otra forma:

### MÉTODO 1: Configurar desde API Library

1. Ve a [Google Cloud Console - API Library](https://console.cloud.google.com/apis/dashboard)

2. En el menú lateral izquierdo, busca y haz clic en **"Pantalla de consentimiento de OAuth"**

3. Si te redirige, intenta esta URL directa:
   ```
   https://console.cloud.google.com/apis/credentials/consent?project=TU_PROJECT_ID
   ```
   (Reemplaza TU_PROJECT_ID con el ID de tu proyecto)

### MÉTODO 2: Desde la página principal de APIs

1. Ve a [Google Cloud Console](https://console.cloud.google.com/)

2. Asegúrate de que estás en el proyecto correcto (arriba a la izquierda)

3. En el menú de hamburguesa (☰), ve a:
   **APIs y servicios** → **Pantalla de consentimiento de OAuth**

4. Si aún te redirige, puede ser que la pantalla de consentimiento no esté creada

---

## 🎯 SOLUCIÓN DEFINITIVA - Configurar pantalla de consentimiento desde cero

Si nunca has configurado la pantalla de consentimiento, sigue estos pasos:

### PASO 1: Crear la pantalla de consentimiento

1. Ve a [APIs y servicios - Pantalla de consentimiento OAuth](https://console.cloud.google.com/apis/credentials/consent)

2. Si te pide elegir el tipo de usuario:
   - Selecciona **"Externo"** (o "Interno" si tienes Google Workspace)
   - Haz clic en **"CREAR"**

3. Rellena SOLO los campos obligatorios:
   - **Nombre de la aplicación:** `MediNotas`
   - **Correo electrónico de asistencia del usuario:** Tu email
   - **Correo electrónico de contacto del desarrollador:** Tu email

4. Haz clic en **"GUARDAR Y CONTINUAR"**

5. En la página de "Permisos" (Scopes):
   - Haz clic en **"GUARDAR Y CONTINUAR"** (sin agregar nada)

6. En la página de "Usuarios de prueba":
   - Haz clic en **"+ ADD USERS"** o **"+ AÑADIR USUARIOS"**
   - Agrega tu correo de Gmail
   - Haz clic en **"GUARDAR Y CONTINUAR"**

7. En la página de "Resumen":
   - Haz clic en **"VOLVER AL PANEL"**

---

## 🔥 SOLUCIÓN MÁS RÁPIDA - Probar sin usuarios de prueba primero

En realidad, para desarrollo con el debug keystore, **a veces funciona sin agregar usuarios de prueba**. 

### HAZ ESTO AHORA:

1. **NO te preocupes por la pantalla de consentimiento por ahora**

2. Asegúrate de que tienes:
   ✅ Client ID tipo Android creado con tu SHA-1
   ✅ Client ID actualizado en `GoogleAuthManager.java`
   ✅ Proyecto limpio y reconstruido

3. **Ejecuta la app y prueba iniciar sesión**

4. Si sale un error diciendo "Esta app no está verificada" o similar:
   - Haz clic en **"Opciones avanzadas"** o **"Advanced"**
   - Luego en **"Ir a MediNotas (no seguro)"** o **"Go to MediNotas (unsafe)"**
   - Esto es normal en modo desarrollo

---

## 🆘 Si aún necesitas agregar usuarios de prueba

### URL DIRECTA (reemplaza con tu project ID):

```
https://console.cloud.google.com/apis/credentials/consent?project=PROJECT_ID
```

Para obtener tu PROJECT_ID:
1. Ve a [Google Cloud Console](https://console.cloud.google.com/)
2. Arriba aparece el nombre del proyecto y al lado el ID
3. Cópialo y úsalo en la URL de arriba

---

## ✅ PLAN DE ACCIÓN INMEDIATO

**HAZ ESTO EN ORDEN:**

1. **Ignora la pantalla de consentimiento por ahora**

2. **Verifica que el Client ID Android esté correcto:**
   - Ve a [Credenciales](https://console.cloud.google.com/apis/credentials)
   - Debe existir uno de tipo "Android" con tu SHA-1
   - Ese Client ID debe estar en tu código

3. **Clean + Rebuild en Android Studio**

4. **Ejecuta la app y prueba iniciar sesión**

5. **Si sale "Esta app no está verificada":**
   - Clic en "Opciones avanzadas"
   - Clic en "Ir a [nombre app] (no seguro)"
   - Autoriza los permisos

6. **Si sale Error 10 todavía:**
   - Responde y te daré otra solución

---

## 💡 TIP: La app funcionará en modo desarrollo

Para apps en desarrollo con debug keystore, Google permite:
- ✅ Probar sin verificación completa
- ✅ Usar tu cuenta personal sin estar en "usuarios de prueba"
- ✅ Saltar advertencias de seguridad durante el desarrollo

**¡Prueba ejecutar la app ahora y dime qué error específico te sale!** 🚀

