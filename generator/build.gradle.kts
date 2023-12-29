plugins {
    alias(libs.plugins.kotlin.jvm)
    id(libs.plugins.java.gradle.plugin.get().pluginId)
}

ext.set("PUBLISH_ARTIFACT_ID", "generator")
apply(from = "$rootDir/convention-plugins/src/main/kotlin/publish-module.gradle")

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
