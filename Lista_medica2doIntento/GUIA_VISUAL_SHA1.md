# 📸 GUÍA VISUAL: Obtener SHA-1 desde Android Studio

## Método más fácil - SIN usar comandos ✅

### PASO 1: Abrir panel de Gradle

En Android Studio, mira en el **lado derecho** de la ventana:
- Verás un icono de **elefante** que dice "Gradle"
- Haz clic en él

**Si no lo ves:**
- Ve al menú superior: **View** → **Tool Windows** → **Gradle**

---

### PASO 2: Navegar al signingReport

En el panel de Gradle que se abrió, verás un árbol de carpetas:

```
📁 Lista_medica2doIntento
   └─ 📁 Lista_medica2doIntento
       └─ 📁 app
           └─ 📁 Tasks
               └─ 📁 android
                   └─ ⚡ signingReport  ← HAZ DOBLE CLIC AQUÍ
```

**Expande cada carpeta haciendo clic en la flechita (▶)** hasta llegar a **signingReport**

---

### PASO 3: Ejecutar signingReport

1. Cuando encuentres **signingReport**, **haz DOBLE CLIC** en él

2. Espera 5-10 segundos mientras se ejecuta

3. En la parte **inferior** de Android Studio se abrirá una pestaña que dice **"Run"**

---

### PASO 4: Copiar el SHA-1

En la ventana "Run" verás algo como esto:

```
> Task :app:signingReport
Variant: debug
Config: debug
Store: C:\Users\USUARIO\.android\debug.keystore
Alias: AndroidDebugKey
MD5: 12:34:56:78:9A:BC:DE:F0:12:34:56:78:9A:BC:DE:F0
SHA1: A1:B2:C3:D4:E5:F6:G7:H8:I9:J0:K1:L2:M3:N4:O5:P6:Q7:R8:S9:T0  ← COPIA ESTO
SHA-256: ...
```

**COPIA la línea completa del SHA1** (los 20 pares de caracteres separados por `:`)

---

### PASO 5: Usar el SHA-1

Ahora que tienes el SHA-1, ve a:
1. [Google Cloud Console - Credenciales](https://console.cloud.google.com/apis/credentials)
2. Crear credenciales → OAuth 2.0 → Tipo: **Android**
3. Pega tu SHA-1

---

## 🆘 ¿Aún no puedes obtenerlo?

Si ninguno de estos métodos funciona, responde **"OPCIÓN B"** y desactivaré Google Sign-In para que tu app funcione inmediatamente con almacenamiento local.

---

## 🎯 Resumen rápido

1. **Gradle** (panel derecho) → clic en el elefante
2. **app** → **Tasks** → **android** → **signingReport** → doble clic
3. En ventana "Run" (abajo) → busca **SHA1:**
4. **Copia** los 20 pares de caracteres
5. Úsalo en Google Cloud Console

**¡Eso es todo!** 🎉

