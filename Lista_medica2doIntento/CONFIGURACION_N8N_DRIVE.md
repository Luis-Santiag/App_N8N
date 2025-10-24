# 🚀 CONFIGURACIÓN COMPLETA: Android App → n8n → Google Drive

## ✅ Estado actual de tu app

Tu app ya tiene implementado:
- ✅ `GoogleAuthManager.java` - Autenticación con Google funcionando
- ✅ `N8nUploader.java` - Cliente para enviar archivos a n8n
- ✅ Integración en `NotasFragment.java` para subir PDFs automáticamente

---

## 📋 LO QUE NECESITAS CONFIGURAR

### 1️⃣ EN TU APP ANDROID (YA ESTÁ HECHO ✅)

He actualizado `GoogleAuthManager.java` para:
- ✅ Obtener ID tokens válidos de Google
- ✅ Método `getFreshAccessToken()` para tokens frescos
- ✅ Verificar permisos de Google Drive

**Código actualizado:**
```java
// Obtener token directamente
String token = authManager.getAccessToken();

// O obtener token fresco (recomendado)
authManager.getFreshAccessToken(new GoogleAuthManager.TokenCallback() {
    @Override
    public void onSuccess(String token) {
        // Usar el token para enviar a n8n
    }
    
    @Override
    public void onError(String error) {
        // Manejar error
    }
});
```

---

### 2️⃣ EN N8N (LO QUE DEBES CONFIGURAR)

Tu webhook n8n está en:
```
https://primary-production-d141e.up.railway.app/webhook-test/cb1a04b1-110e-4951-8f88-f053923e60dd
```

#### Configuración del workflow de n8n:

**PASO 1: Crear workflow básico en n8n**

1. **Nodo 1: Webhook** (ya lo tienes)
   - URL: La que tienes arriba
   - Método: POST
   - Tipo de respuesta: JSON

2. **Nodo 2: Edit Fields (Set) - Extraer datos del formulario**
   
   **Tipo de nodo:** `Edit Fields` o `Set` (dependiendo de tu versión de n8n)
   
   **Configuración detallada:**
   
   a) Haz clic en el nodo "Edit Fields" o "Set"
   
   b) En "Mode" selecciona: **"Manual Mapping"**
   
   c) En "Fields to Set" haz clic en **"Add Field"** y agrega estos campos:
   
   **Campo 1 - tipo_nota:**
   - Name: `tipo_nota`
   - Type: `String`
   - Value: `{{ $json.tipo_nota }}`
   
   **Campo 2 - asunto:**
   - Name: `asunto`
   - Type: `String`
   - Value: `{{ $json.asunto }}`
   
   **Campo 3 - user_email:**
   - Name: `user_email`
   - Type: `String`
   - Value: `{{ $json.user_email }}`
   
   **Campo 4 - nombre_archivo:**
   - Name: `nombre_archivo`
   - Type: `String`
   - Value: `{{ $json.nombre_archivo }}`
   
   **Campo 5 - fecha_creacion:**
   - Name: `fecha_creacion`
   - Type: `String`
   - Value: `{{ $json.fecha_creacion }}`
   
   **Campo 6 - access_token (opcional):**
   - Name: `access_token`
   - Type: `String`
   - Value: `{{ $json.access_token }}`
   
   d) **IMPORTANTE:** El archivo PDF (binario) se pasa automáticamente, no necesitas configurarlo aquí
   
   **Configuración alternativa (si usas "Keep Only Set"):**
   - Si quieres mantener SOLO estos campos y descartar otros datos del webhook
   - Activa la opción: **"Options" → "Keep Only Set": true**

3. **Nodo 3: Subir a Google Drive**
   - Tipo: Google Drive
   - Operación: Upload a file
   - Configuración:
     - **File**: `{{$binary.file}}`
     - **Name**: `{{$node["Set"].json["nombre_archivo"]}}`
     - **Folder**: Especifica el ID de tu carpeta en Drive
   
   **IMPORTANTE: Autenticación en Google Drive**
   - **Opción A:** Usar credenciales OAuth propias de n8n
   - **Opción B:** Usar el `access_token` que envía la app

4. **Nodo 4: Respuesta exitosa**
   - Tipo: Respond to Webhook
   - Respuesta:
     ```json
     {
       "success": true,
       "message": "Archivo subido a Google Drive",
       "file_id": "{{$node["Google Drive"].json["id"]}}",
       "file_url": "{{$node["Google Drive"].json["webViewLink"]}}"
     }
     ```

---

### 3️⃣ CONFIGURACIÓN DE GOOGLE DRIVE API EN N8N

#### Opción A: Usar OAuth de n8n (MÁS FÁCIL) ✅

1. En n8n, ve a **Credentials** → **Create New**
2. Selecciona **Google Drive OAuth2 API**
3. Completa:
   - **Client ID**: Usa el mismo Web Client ID de tu app
     ```
     984493768973-udrm67kgft5d9l7l9pgqj0gvvhj2lnd1.apps.googleusercontent.com
     ```
   - **Client Secret**: Obtén esto de Google Cloud Console
     - Ve a [Credenciales](https://console.cloud.google.com/apis/credentials)
     - Haz clic en tu Web Client ID
     - Copia el "Client Secret"
   
4. Haz clic en **Connect my account**
5. Autoriza con tu cuenta de Google
6. ✅ Listo, n8n puede subir a tu Drive

#### Opción B: Usar el token de la app (MÁS COMPLEJO)

Si quieres que cada usuario suba a SU propio Drive:
- Tu app ya envía el `access_token` en el formulario
- En n8n, usa un nodo HTTP Request en lugar de Google Drive
- URL: `https://www.googleapis.com/upload/drive/v3/files?uploadType=multipart`
- Headers: `Authorization: Bearer {{$node["Set"].json["access_token"]}}`

---

## 🔧 WORKFLOW COMPLETO DE N8N (CÓDIGO)

```json
{
  "nodes": [
    {
      "name": "Webhook",
      "type": "n8n-nodes-base.webhook",
      "position": [250, 300],
      "parameters": {
        "httpMethod": "POST",
        "path": "webhook-test/cb1a04b1-110e-4951-8f88-f053923e60dd",
        "responseMode": "lastNode",
        "options": {}
      }
    },
    {
      "name": "Extraer Datos",
      "type": "n8n-nodes-base.set",
      "position": [450, 300],
      "parameters": {
        "values": {
          "string": [
            {
              "name": "tipo_nota",
              "value": "={{$json[\"tipo_nota\"]}}"
            },
            {
              "name": "asunto",
              "value": "={{$json[\"asunto\"]}}"
            },
            {
              "name": "user_email",
              "value": "={{$json[\"user_email\"]}}"
            },
            {
              "name": "nombre_archivo",
              "value": "={{$json[\"nombre_archivo\"]}}"
            }
          ]
        }
      }
    },
    {
      "name": "Google Drive",
      "type": "n8n-nodes-base.googleDrive",
      "position": [650, 300],
      "parameters": {
        "operation": "upload",
        "fileContent": "={{$binary.file}}",
        "name": "={{$node[\"Extraer Datos\"].json[\"nombre_archivo\"]}}",
        "parents": ["TU_FOLDER_ID_AQUI"]
      },
      "credentials": {
        "googleDriveOAuth2Api": {
          "id": "1",
          "name": "Google Drive Account"
        }
      }
    },
    {
      "name": "Respuesta",
      "type": "n8n-nodes-base.respondToWebhook",
      "position": [850, 300],
      "parameters": {
        "respondWith": "json",
        "responseBody": "={ \"success\": true, \"message\": \"Archivo subido exitosamente\", \"file_id\": \"{{$node[\"Google Drive\"].json[\"id\"]}}\" }"
      }
    }
  ],
  "connections": {
    "Webhook": {
      "main": [[{"node": "Extraer Datos", "type": "main", "index": 0}]]
    },
    "Extraer Datos": {
      "main": [[{"node": "Google Drive", "type": "main", "index": 0}]]
    },
    "Google Drive": {
      "main": [[{"node": "Respuesta", "type": "main", "index": 0}]]
    }
  }
}
```

---

## 🧪 PROBAR LA INTEGRACIÓN

### 1. Desde tu app Android:

Tu app ya tiene implementado el test. En `MainActivity.java`:
- Presiona largo (long press) en algún botón
- Ejecuta el test de conectividad con n8n

### 2. Verificar los logs:

En Android Studio → Logcat, busca:
```
N8nUploader: ========================================
N8nUploader: INICIANDO UPLOAD A N8N
N8nUploader: ========================================
```

### 3. Crear una nota y verificar:

1. Crea una nota en tu app
2. Genera el PDF
3. La app subirá automáticamente a n8n
4. n8n procesará y subirá a Google Drive
5. Verás el mensaje: "✓ Nota guardada en Google Drive"

---

## 🔑 OBTENER CLIENT SECRET PARA N8N

Para configurar las credenciales de Google Drive en n8n:

1. Ve a [Google Cloud Console - Credenciales](https://console.cloud.google.com/apis/credentials)

2. Busca tu Web Client ID:
   ```
   984493768973-udrm67kgft5d9l7l9pgqj0gvvhj2lnd1.apps.googleusercontent.com
   ```

3. Haz clic en él para ver los detalles

4. **Copia el "Client Secret"** (aparece en la parte superior)

5. En n8n:
   - Credentials → Create New → Google Drive OAuth2 API
   - Client ID: `984493768973-udrm67kgft5d9l7l9pgqj0gvvhj2lnd1.apps.googleusercontent.com`
   - Client Secret: [El que acabas de copiar]

---

## 📱 FLUJO COMPLETO (CÓMO FUNCIONA)

```
┌─────────────────┐
│   APP ANDROID   │
│                 │
│ 1. Usuario crea │
│    nota y PDF   │
│                 │
│ 2. Obtiene token│
│    de Google    │
│                 │
│ 3. Envía a n8n: │
│    - PDF file   │
│    - metadata   │
│    - token      │
└────────┬────────┘
         │
         │ HTTP POST
         │ Multipart/form-data
         ▼
┌─────────────────┐
│      N8N        │
│   (Railway)     │
│                 │
│ 4. Recibe datos │
│                 │
│ 5. Extrae PDF   │
│    y metadata   │
│                 │
│ 6. Sube a Drive │
│    usando OAuth │
└────────┬────────┘
         │
         │ Google Drive API
         ▼
┌─────────────────┐
│  GOOGLE DRIVE   │
│                 │
│ 7. Archivo      │
│    guardado ✅  │
│                 │
│ 8. Retorna URL  │
│    y file_id    │
└────────┬────────┘
         │
         │ Response
         ▼
┌─────────────────┐
│   APP ANDROID   │
│                 │
│ 9. Muestra msg: │
│ "✓ Guardado en  │
│  Google Drive"  │
└─────────────────┘
```

---

## ⚠️ TROUBLESHOOTING

### Error: "No se pudo obtener el token"
- **Solución:** Verifica que el usuario haya iniciado sesión con Google
- Llama a `authManager.isSignedIn()` antes de subir

### Error: "Error 401" en n8n
- **Solución:** El token expiró o es inválido
- Usa `getFreshAccessToken()` en lugar de `getAccessToken()`

### Error: "Insufficient permissions"
- **Solución:** Verifica que los scopes incluyan `drive.file`
- Ya está configurado en tu app: ✅

### El webhook no responde
- **Solución:** Verifica que Railway esté activo
- Prueba la URL directamente desde Postman

---

## 🎯 RESUMEN - PASOS SIGUIENTES

### En Google Cloud Console:
1. ✅ Ya tienes Web Client ID configurado
2. ✅ Obtén el Client Secret para n8n

### En n8n:
1. Crea el workflow con los 4 nodos (Webhook → Extraer → Google Drive → Respuesta)
2. Configura credenciales de Google Drive OAuth2
3. Conecta tu cuenta de Google
4. Especifica el Folder ID donde quieres guardar los archivos

### En tu app:
1. ✅ Ya está todo configurado
2. Solo necesitas probar que funcione

### Para probar:
1. Crea una nota en la app
2. Genera el PDF
3. Verifica que suba a n8n
4. Verifica que aparezca en Google Drive

---

## 💡 ALTERNATIVA SIMPLE (Si no quieres usar n8n)

Si prefieres subir directamente desde la app a Google Drive sin n8n:
- Puedo modificar tu app para usar la API de Google Drive directamente
- Ventaja: Más simple, sin servidor intermedio
- Desventaja: Pierdes la flexibilidad de n8n para procesamiento adicional

¿Quieres que implemente esa alternativa o prefieres continuar con n8n?

---

**📄 Tu app ya está lista del lado Android. Solo falta configurar n8n.** 🚀

