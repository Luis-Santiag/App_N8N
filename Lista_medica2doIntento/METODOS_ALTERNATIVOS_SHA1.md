# 🔑 MÉTODOS ALTERNATIVOS PARA OBTENER EL SHA-1

## Método 1: Usando Android Studio (GUI) - MÁS FÁCIL ✅

1. En Android Studio, ve al menú superior derecho donde dice **"Gradle"** (icono de elefante)
   - O ve a: **View** → **Tool Windows** → **Gradle**

2. Se abrirá un panel a la derecha con el árbol de tareas de Gradle

3. Navega por esta ruta:
   ```
   Lista_medica2doIntento
   └── app
       └── Tasks
           └── android
               └── signingReport
   ```

4. **Haz doble clic en "signingReport"**

5. En la ventana **"Run"** (abajo) verás la salida con tu SHA-1:
   ```
   Variant: debug
   SHA1: XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX
   ```

6. **Copia ese SHA-1**

---

## Método 2: Obtener SHA-1 desde el keystore directamente

Abre **Command Prompt (cmd)** en Windows y ejecuta:

```cmd
keytool -list -v -keystore %USERPROFILE%\.android\debug.keystore -alias androiddebugkey -storepass android -keypass android
```

Busca la línea que dice **"SHA1:"** y copia el valor.

---

## Método 3: ALTERNATIVA RÁPIDA - Sin Google Sign-In

Si no puedes obtener el SHA-1 o tienes prisa, puedo **desactivar Google Sign-In** y hacer que tu app funcione con **almacenamiento local** únicamente.

### Ventajas:
- ✅ Funciona inmediatamente, sin configuración adicional
- ✅ No necesitas configurar nada en Google Cloud Console
- ✅ Perfecto para entregar el trabajo rápido
- ✅ Las notas se guardan localmente en el dispositivo

### Desventajas:
- ❌ No sincroniza con Google Drive
- ❌ Si desinstalas la app, pierdes los datos

---

## 🚀 ¿Qué prefieres?

**OPCIÓN A:** Intentar el Método 1 o 2 para obtener el SHA-1 y configurar Google Sign-In correctamente

**OPCIÓN B:** Desactivar Google Sign-In temporalmente y usar solo almacenamiento local (funciona en 1 minuto)

---

## Si eliges OPCIÓN A (obtener SHA-1):

Intenta el **Método 1** (más fácil):
1. Ve a la pestaña **Gradle** en Android Studio (derecha)
2. Busca: **app** → **Tasks** → **android** → **signingReport**
3. Doble clic
4. Copia el SHA-1 de la salida

---

## Si eliges OPCIÓN B (sin Google Sign-In por ahora):

Responde "OPCIÓN B" y yo:
1. Modificaré WelcomeActivity para omitir Google Sign-In
2. Configuré la app para usar solo almacenamiento local
3. Tu app funcionará inmediatamente
4. Podrás agregar Google Sign-In más tarde si quieres

**¿Qué opción prefieres?** 🤔

