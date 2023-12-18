plugins {
    alias(libs.plugins.kotlin.multiplatform)
    id(libs.plugins.convention.plugin.get().pluginId)
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
                api(project(libs.mtproto.client.sockets.infrastructure.get().module.name))
                api(project(libs.mtproto.client.connection.get().module.name))
                api(project(libs.mtproto.serialization.get().module.name))
                api(project(libs.mtproto.security.obfuscation.get().module.name))

                // Kotlin
                implementation(libs.kotlin.coroutines.core)
            }
        }
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
