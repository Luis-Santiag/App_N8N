plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.lista_medica2dointento"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.lista_medica2dointento"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        // Configuración para AppAuth OAuth redirect
        manifestPlaceholders["appAuthRedirectScheme"] = "com.example.lista_medica2dointento"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.play.services.location)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.android.libraries.places:places:3.5.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")

    // OAuth y Google Sign-In
    implementation("net.openid:appauth:0.11.1")
    implementation("com.google.android.gms:play-services-auth:21.0.0")

    // Almacenamiento seguro
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // Interceptor para logging de OkHttp (usado en N8nUploader)
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

}

// Tarea personalizada para obtener SHA-1
tasks.register("getSha1") {
    doLast {
        val keystorePath = System.getProperty("user.home") + "/.android/debug.keystore"
        println("\n===========================================")
        println("OBTENIENDO SHA-1 DEL KEYSTORE DE DEBUG")
        println("===========================================\n")

        try {
            val proc = Runtime.getRuntime().exec(arrayOf(
                "keytool",
                "-list",
                "-v",
                "-keystore", keystorePath,
                "-alias", "androiddebugkey",
                "-storepass", "android",
                "-keypass", "android"
            ))

            proc.inputStream.bufferedReader().useLines { lines ->
                lines.forEach { line ->
                    if (line.contains("SHA1:") || line.contains("SHA-1:")) {
                        println("✅ COPIA ESTE SHA-1:")
                        println("   ${line.trim()}")
                        println("\n===========================================")
                    }
                }
            }

            val exitCode = proc.waitFor()
            if (exitCode != 0) {
                println("❌ Error: No se pudo ejecutar keytool")
                println("Usa el script get_sha1.bat en la carpeta del proyecto")
            }
        } catch (e: Exception) {
            println("❌ Error: ${e.message}")
            println("\nAlternativas:")
            println("1. Ejecuta el archivo get_sha1.bat")
            println("2. O ejecuta en terminal:")
            println("   keytool -list -v -keystore \"$keystorePath\" -alias androiddebugkey -storepass android -keypass android")
        }
    }
}
