plugins {
    id(libs.plugins.java.library.get().pluginId)
    alias(libs.plugins.kotlin.jvm)
}

ext.set("PUBLISH_ARTIFACT_ID", "client-sockets-connect")
apply(from = "${rootProject.projectDir}/scripts/publish-module.gradle")

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    // MTProto
    api(project(libs.mtproto.client.sockets.infrastructure.get().module.name))
    api(project(libs.mtproto.client.connection.get().module.name))
    api(project(libs.mtproto.serialization.get().module.name))
    api(project(libs.mtproto.security.obfuscation.get().module.name))

    // Kotlin
    implementation(libs.kotlin.coroutines.core)
}
