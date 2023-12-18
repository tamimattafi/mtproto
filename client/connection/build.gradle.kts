plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
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
                api(project(libs.mtproto.core.get().module.name))
                api(project(libs.mtproto.client.api.get().module.name))
                api(project(libs.mtproto.serialization.get().module.name))
                api(project(libs.mtproto.security.cipher.get().module.name))
                api(project(libs.mtproto.security.digest.get().module.name))
                api(project(libs.mtproto.security.utils.get().module.name))

                // Storage
                api(libs.settings)

                // Kotlin
                implementation(libs.kotlin.serialization.json)
                implementation(libs.kotlin.coroutines.core)
                implementation(libs.kotlin.datetime)

                // Math
                implementation(libs.bignum)
            }
        }
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
