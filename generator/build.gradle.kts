plugins {
    alias(libs.plugins.kotlin.jvm)
    id(libs.plugins.java.gradle.plugin.get().pluginId)
    id(libs.plugins.convention.plugin.get().pluginId)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    api(project(":core"))
    implementation("com.squareup:kotlinpoet:1.11.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.1")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.7.1")
}

gradlePlugin {
    plugins {
        create("MTGenerator") {
            id = "mtproto-generator"
            implementationClass = "com.attafitamim.mtproto.generator.plugin.TLGeneratorPlugin"
        }
    }
}
