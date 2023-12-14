plugins {
    id(libs.plugins.java.library.get().pluginId)
    alias(libs.plugins.kotlin.jvm)
}

ext.set("PUBLISH_ARTIFACT_ID", "client-sockets-ktor")
apply(from = "${rootProject.projectDir}/scripts/publish-module.gradle")

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    // MTProto
    api(project(libs.mtproto.client.sockets.infrastructure.get().module.name))

    // Ktor
    implementation(libs.ktor.webscokets)
    implementation(libs.ktor.network)

    // IO
    implementation(libs.kotlinx.io)
}
