plugins {
    id(libs.plugins.java.library.get().pluginId)
    alias(libs.plugins.kotlin.jvm)
    id(libs.plugins.mtproto.generator.get().pluginId)
}

ext.set("PUBLISH_ARTIFACT_ID", "client-sockets")
apply(from = "${rootProject.projectDir}/scripts/publish-module.gradle")

tasks.generateProtoClasses {
    schemeFilesDir = "${projectDir}/schemes"
    outputDir = "${buildDir.absolutePath}/generated/mtproto"
    basePackage = "com.attafitamim.scheme.mtproto"
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17

    sourceSets {
        getByName("main") {
            java.srcDirs(tasks.generateProtoClasses.get().outputDir)
        }
    }
}

dependencies {
    // Local
    api(project(libs.mtproto.client.api.get().module.name))

    // Ktor
    implementation(libs.ktor.webscokets)
    implementation(libs.ktor.network)

    // IO
    implementation(libs.kotlinx.io)
    implementation("org.apache.commons:commons-crypto:1.2.0")

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.ktor.logger)
    testImplementation(libs.ktor.core)
    testImplementation(libs.ktor.cio)
    testImplementation(libs.kotlin.coroutines.core)
    testImplementation(libs.kotlin.coroutines.test)
}
