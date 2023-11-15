plugins {
    alias(libs.plugins.android.gradle.library)
    alias(libs.plugins.kotlin.android)
}

ext.set("PUBLISH_ARTIFACT_ID", "client-android")
apply(from = "${rootProject.projectDir}/scripts/publish-module.gradle")

android {
    compileSdk = 34
    namespace = "com.attafitamim.mtproto.client"

    ndkVersion = "16.1.4479499"
    externalNativeBuild {
        ndkBuild.path("jni/Android.mk")
    }

    defaultConfig {
        minSdk = 21
        targetSdk = 34


        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")

        externalNativeBuild {
            ndkBuild {
                arguments(
                        "NDK_APPLICATION_MK:=jni/Application.mk",
                        "APP_PLATFORM:=android-21",
                        "-j8"
                )

                abiFilters(
                        "armeabi-v7a",
                        "arm64-v8a",
                        "x86",
                        "x86_64"
                )
            }
        }
    }

    buildTypes {
        release {
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

    kotlin {
        jvmToolchain(17)
    }

    sourceSets {
        getByName("main") {
            jniLibs.srcDir("./jni/")
        }
    }
}

dependencies {
    api(project(":core"))
    api(project(":client:api"))
}
