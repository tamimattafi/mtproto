plugins {
    alias(libs.plugins.kotlin.multiplatform)
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

                // IO
                implementation(libs.kotlinx.io)
            }
        }

        val jvmMain by getting {
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

    java {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}