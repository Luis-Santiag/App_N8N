 <h1 style="text-align:center;">Aplicacion con N8N</h1>

## Descripción del proyecto

Aplicación para gestionar notas médicas (guardado en Google Drive opcional). Implementada en Java con arquitectura sencilla; incluye autenticación con Google (clase `GoogleAuthManager`) y pantallas como `WelcomeActivity` y `MainActivity`. Pensada para uso local y sincronización con Drive si el usuario lo autoriza, , se han añadido funciones para que el usuario pueda iniciar sesion con su cuenta de google y compartir con el las notas medicas que cree dentro de la aplicacion con un flujo de N8N y asi pueda tenerlas en la nube. Ademas de esto el usuario puede hablar con un chat IA para consultas medicas y recomendaciones, ademas, de un mapa para poder localizar hospitales o farmacias mas cercanas a su localidad.

## Instrucciones de instalación

1. Clonar el repositorio:
2. Abrir el proyecto en Android Studio (recomendado: Android Studio Narwhal 4 Feature Drop | 2025.1.4).

3. Si usas Windows, ejecutar el wrapper de Gradle desde la terminal del proyecto o usar la interfaz de Android Studio:
4. Conectar un dispositivo o arrancar un emulador y ejecutar desde Android Studio: Run > app.

5. (Opcional) Configurar credenciales de Google:
   - Añadir el archivo `google-services.json` a `app/` si usas Firebase/Google APIs.
   - Configurar SHA-1 en la consola de Google Cloud para autenticación
6. (Opcional) Instalar el APK agregado al repositorio.

## Requisitos

- Android Studio (versión recomendada: 2025.1.4 Narwhal).
- JDK 11 o superior.
- Android SDK (API level mínimo según `build.gradle`; recomendable SDK 31+).
- Conexión a Internet para dependencias y APIs externas.
- (Opcional) Cuenta de Google y acceso a Google Cloud / Drive API para sincronización.

## Dependencias principales 

```groovy
// add inside dependencies { ... }
implementation 'androidx.appcompat:appcompat:1.6.1'
implementation 'com.google.android.material:material:1.9.0'
implementation 'androidx.constraintlayout:constraintlayout:2.1.4'

// Google Sign-In
implementation 'com.google.android.gms:play-services-auth:20.7.0' g

// Opcional: Google Drive / APIs (si usas sincronización)
implementation 'com.google.api-client:google-api-client-android:1.34.1'
implementation 'com.google.apis:google-api-services-drive:v3-rev20230308-2.0.0'
```

## Demostrativos

### Inicio de Sesion con google
Se agrego un inicio de sesion con google para poder acceder a los datos de google del usuario.

<p align="center">
  <img src="ElementosGraficos/VidSesion.gif" alt="Video de Inicio de Sesion" />
</p>

### Perfil
Se añade una pestaña para el perfil del usuario donde agrega sus datos y puede conectar y desconectar con cuentas de google distintas.

<p align="center">
  <img src="ElementosGraficos/VidPerfil.gif" alt="Video de Pantalla de Perfil" />
</p>

### Notas
El usuario puede añadir notas, eligiendo entre, notas medicas, recetas o notas generales de la persona, estas se muestran en una pestaña donde se muestra una parte desplegable para mostrar el contenido completo de la nota, el usuario ve opciones para poder eliminarlas o compartirla a otras persnas en un pdf con formato agradable, ademas, cuando se crea una nota, si el usuario inicio sesion, estas se comparten al usuario automaticamente a traves de Google Drive. 

<p align="center">
  <img src="ElementosGraficos/VidNotas.gif" alt="Video de Pantalla de Notas" />
</p>

### Mapa y Chat IA
En estas dos pantallas se agrega un chat con una IA impulsada por Groq con um modelo de Open IA, esta solo responde consultas basicas sobre el contexto, saludos o gestiones medicas, en el mapa, se pueden localizar farmacias u hospitales que esten en un radio de 5km a la redonda del usuario, debajo del mapa se muestran tarjetas con el nombre del lugar, direccion y si hay dentro de google, el telefono de contacto. (No hay mucha presentacion de la IA porque se me acabaron los tokens)

<p align="center">
  <img src="ElementosGraficos/Vid4.gif" alt="Video de Pantalla de IA y Mapa" />
</p>
