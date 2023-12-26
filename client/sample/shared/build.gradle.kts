plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.native.cocoapods)
    alias(libs.plugins.android.library)
}

kotlin {
    applyDefaultHierarchyTemplate()
    jvmToolchain(17)

    jvm()
    js {
        browser()
    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "17"
            }
        }
    }

    cocoapods {
        summary = "Some description for the Shared Module"
        homepage = "Link to the Shared Module homepage"
        version = "1.0"
        ios.deploymentTarget = "16.0"
        podfile = project.file("../iosApp/Podfile")
        framework {
            baseName = "shared"
            isStatic = true
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                // MTProto
                api(project(libs.mtproto.client.connection.get().module.name))
                api(project(libs.mtproto.client.sockets.ktor.get().module.name))
                api(project(libs.mtproto.client.sockets.connect.get().module.name))

                // Ktor
                implementation(libs.ktor.webscokets)
                implementation(libs.ktor.logger)
                implementation(libs.ktor.core)

                // Kotlin
                api(libs.kotlin.coroutines.core)

                // IO
                implementation(libs.kotlin.io)
            }
        }

        val jvmMain by getting {
            dependsOn(commonMain)

            dependencies {
                // Ktor
                implementation(libs.ktor.cio)
            }
        }

        val androidMain by getting {
            dependsOn(commonMain)

            dependencies {
                // Ktor
                implementation(libs.ktor.cio)
            }
        }

        val jsMain by getting {
            dependsOn(commonMain)

            dependencies {
                implementation(libs.ktor.js)
            }
        }

        val iosMain by getting {
            dependsOn(commonMain)

            dependencies {
                implementation(libs.ktor.darwin)
            }
        }
    }
}

android {
    namespace = "com.attafitamim.mtproto.client.sample"
    compileSdk = 34
    defaultConfig {
        minSdk = 24
    }
}