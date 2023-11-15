import org.jetbrains.kotlin.konan.properties.Properties

plugins {
    alias(libs.plugins.android.gradle.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.attafitamim.mtproto.sample.android"
    compileSdk = 34

    // TODO: move reading properties to extensions
    val backendProperties = Properties()
    val backendPropertiesFile = File("$rootDir/config/properties/backend.properties")
    if (backendPropertiesFile.exists()) {
        backendProperties.load(backendPropertiesFile.reader())
    } else {
        println("${backendPropertiesFile.absolutePath} not found")
    }

    defaultConfig {
        applicationId = "com.attafitamim.mtproto.sample.android"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "android.support.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        buildConfigField(
            String::class.java.simpleName,
            "BACKEND_SOCKET_URL",
            backendProperties.getProperty("backend.websocket.url")
        )
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
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.4"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Lib
    implementation(libs.mtproto.client.sockets)

    // Ktor
    implementation(libs.ktor.logger)
    implementation(libs.ktor.core)
    implementation(libs.ktor.cio)
    implementation(libs.ktor.webscokets)

    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.activity.compose)
    implementation(platform(libs.compose.bom))
    implementation(libs.ui)
    implementation(libs.ui.graphics)
    implementation(libs.ui.tooling.preview)
    implementation(libs.material3)
    androidTestImplementation(platform(libs.compose.bom))
    debugImplementation(libs.ui.tooling)
}