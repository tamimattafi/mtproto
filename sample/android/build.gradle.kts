plugins {
    id("com.android.application")
    id("mtproto-generator")
}

tasks.generateProtoClasses {
    schemeFilesDir = "${projectDir.path}/schemes"
    outputDir = "${buildDir.absolutePath}/generated/mtproto"
    basePackage = "com.tezro.data.remote.mtproto"
}

android {
    compileSdk = 32

    defaultConfig {
        applicationId = "com.attafitamim.mtproto.sample.android"
        minSdk = 16
        targetSdk = 32
        versionCode = 1
        versionName = "1.0"
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    sourceSets {
        getByName("main") {
            java.srcDirs(tasks.generateProtoClasses.get().outputDir)
        }
    }

    val tlGenerator = tasks.getByName("generateProtoClasses")
    namespace = "com.attafitamim.mtproto.sample.android"
    applicationVariants.all {
        registerResGeneratingTask(tlGenerator)
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.4.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.attafitamim.mtproto:core:1.0.0-beta04")
}