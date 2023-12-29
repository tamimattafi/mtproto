plugins {
    alias(libs.plugins.kotlin.jvm)
    id(libs.plugins.java.gradle.plugin.get().pluginId)
    id(libs.plugins.convention.legacy.publication.get().pluginId)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17

    kotlin {
        jvmToolchain(17)
    }
}

dependencies {
    api(projects.core)
    implementation(libs.kotlinpoet)
}

gradlePlugin {
    plugins {
        create("MTGenerator") {
            id = "mtproto-generator"
            implementationClass = "com.attafitamim.mtproto.generator.plugin.TLGeneratorPlugin"
        }
    }
}
