plugins {
    id(libs.plugins.java.library.get().pluginId)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
}

ext.set("PUBLISH_ARTIFACT_ID", "client-connection")
apply(from = "${rootProject.projectDir}/scripts/publish-module.gradle")

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    // MTProto
    api(project(libs.mtproto.core.get().module.name))
    api(project(libs.mtproto.client.api.get().module.name))
    api(project(libs.mtproto.serialization.get().module.name))
    api(project(libs.mtproto.security.cipher.get().module.name))
    api(project(libs.mtproto.security.digest.get().module.name))
    api(project(libs.mtproto.security.ige.get().module.name))
    api(project(libs.mtproto.security.utils.get().module.name))

    // Coroutines
    implementation(libs.kotlin.coroutines.core)

    // Storage
    api(libs.settings)

    // Serialization
    implementation(libs.kotlin.serialization.json)
}
