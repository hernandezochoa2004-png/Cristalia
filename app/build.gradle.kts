plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "me.mariahernandez.cristalia"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "me.mariahernandez.cristalia"
        minSdk = 29                                     // Se cambió de 28 a 29
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)


    dependencies {

        // 1. INICIO

        // JetPack Media3
        implementation("androidx.media3:media3-exoplayer:1.4.1")
        implementation("androidx.media3:media3-ui:1.4.1")

        // Para icons
        implementation("androidx.compose.material:material-icons-core:1.7.5")

        // Para viewModel
        implementation("androidx.lifecycle:lifecycle-viewmodel-compose:<versión>")

        // 2. MENÚ





        // 3. CELDA UNITARIA

        // Sceneview
        implementation("io.github.sceneview:sceneview:2.3.3")

        // -----------------------------------------------------------     (Navegación entre pantallas)
        implementation("androidx.navigation:navigation-compose:2.8.0")

        // Sceneview
        implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.0")
        implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.0")

        // Iconos
        implementation("androidx.compose.material:material-icons-extended")





    }
}