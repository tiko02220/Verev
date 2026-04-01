import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

val localProperties: Properties by lazy {
    Properties().apply {
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            localPropertiesFile.inputStream().use { load(it) }
        }
    }
}

val defaultBackendBaseUrl = "http://37.252.74.243:4444"

fun configValue(
    key: String,
    envKey: String = key.replace('.', '_').uppercase(),
    defaultValue: String = "",
): String =
    localProperties.getProperty(key)
        ?.takeIf { it.isNotBlank() }
        ?: System.getenv(envKey)
            ?.takeIf { it.isNotBlank() }
        ?: defaultValue

fun asBuildConfigString(value: String): String =
    "\"" + value
        .replace("\\", "\\\\")
        .replace("\"", "\\\"") + "\""

android {
    namespace = "com.vector.verevcodex"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.vector.verevcodex"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        buildConfigField("String", "GOOGLE_WALLET_ISSUER_EMAIL", asBuildConfigString(configValue("google.wallet.issuerEmail")))
        buildConfigField("String", "GOOGLE_WALLET_LOYALTY_CLASS_ID", asBuildConfigString(configValue("google.wallet.loyaltyClassId")))
        buildConfigField("String", "GOOGLE_WALLET_PROGRAM_NAME", asBuildConfigString(configValue("google.wallet.programName")))
        buildConfigField("String", "GOOGLE_WALLET_ISSUER_NAME", asBuildConfigString(configValue("google.wallet.issuerName")))
        buildConfigField("String", "VEREV_BACKEND_BASE_URL", asBuildConfigString(configValue("verev.backend.baseUrl", defaultValue = defaultBackendBaseUrl)))
        buildConfigField("String", "FIREBASE_PROJECT_ID", asBuildConfigString(configValue("firebase.projectId")))
        buildConfigField("String", "FIREBASE_APPLICATION_ID", asBuildConfigString(configValue("firebase.applicationId")))
        buildConfigField("String", "FIREBASE_API_KEY", asBuildConfigString(configValue("firebase.apiKey")))
        buildConfigField("String", "FIREBASE_GCM_SENDER_ID", asBuildConfigString(configValue("firebase.gcmSenderId")))

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
    arg("room.incremental", "true")
    arg("room.generateKotlin", "true")
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(platform(libs.androidx.compose.bom))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.org.jetbrains.kotlinx.coroutines.android)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.biometric)
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.google.material)
    implementation(libs.google.play.services.code.scanner)
    implementation(libs.google.mlkit.barcode.scanning)
    implementation(libs.google.play.services.pay)
    implementation(libs.google.zxing.core)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)
    implementation(libs.airbnb.lottie.compose)
    implementation(libs.vico.compose)
    implementation(libs.vico.compose.m3)
    implementation(libs.hilt.android)
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.gson)

    ksp(libs.hilt.compiler)

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
