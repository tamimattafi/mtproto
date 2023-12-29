plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    id(libs.plugins.convention.publication.get().pluginId)
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
                api(projects.mtproto.core)
                api(projects.mtproto.client.api)
                api(projects.mtproto.serialization)
                api(projects.mtproto.security.cipher)
                api(projects.mtproto.security.digest)
                api(projects.mtproto.security.obfuscation)
                api(projects.mtproto.security.utils)

                // Storage
                api(libs.settings)

                // Kotlin
                implementation(libs.kotlin.serialization.json)
                implementation(libs.kotlin.coroutines.core)
                implementation(libs.kotlin.datetime)

                // For ConcurrentHash
                api(libs.ktor.core)

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
