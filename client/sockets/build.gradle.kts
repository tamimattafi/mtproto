plugins {
    id(libs.plugins.java.library.get().pluginId)
    alias(libs.plugins.kotlin.jvm)
}

ext.set("PUBLISH_ARTIFACT_ID", "client-sockets")
apply(from = "${rootProject.projectDir}/scripts/publish-module.gradle")

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    // Local
    api(project(libs.mtproto.client.api.get().module.name))

    // REST
    implementation(libs.ktor.logger)

    // Web Sockets
    implementation(libs.ktor.webscokets)

    // IO
    implementation(libs.kotlinx.io)
}
