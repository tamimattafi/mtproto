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
                api(projects.mtproto.core)

                // Coroutines
                implementation(libs.kotlin.coroutines.core)
            }
        }
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
