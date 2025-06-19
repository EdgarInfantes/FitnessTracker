plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
}

android {
    namespace = "dev.einfantesv.fitnesstracker"
    compileSdk = 35

    defaultConfig {
        applicationId = "dev.einfantesv.fitnesstracker"
        minSdk = 31
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        //multiDexEnabled = true //agrego

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.animation.core.lint)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    debugImplementation("androidx.compose.ui:ui-tooling:1.5.3") //Imagenes
    implementation("androidx.navigation:navigation-compose:2.7.7") //Navegacion
    implementation("androidx.compose.material:material-icons-extended:1.5.0") //Iconos
    implementation("io.coil-kt:coil-compose:2.3.0") //Links de internet
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0") //Para los graficos
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("androidx.datastore:datastore-preferences:1.0.0")
    implementation(platform("com.google.firebase:firebase-bom:33.15.0")) //Firebase
    implementation("com.google.firebase:firebase-auth-ktx") //Auth Firebase
    implementation("com.google.firebase:firebase-firestore-ktx") //Fire
    implementation("com.google.firebase:firebase-storage") //Sotrage

}