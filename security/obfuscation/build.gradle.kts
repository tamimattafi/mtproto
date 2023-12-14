plugins {
    id(libs.plugins.java.library.get().pluginId)
    alias(libs.plugins.kotlin.jvm)
}

ext.set("PUBLISH_ARTIFACT_ID", "security-obfuscation")
apply(from = "${rootProject.projectDir}/scripts/publish-module.gradle")

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    // MTProto
    api(project(libs.mtproto.buffer.get().module.name))
    api(project(libs.mtproto.security.cipher.get().module.name))
    api(project(libs.mtproto.security.utils.get().module.name))

    // Coroutines
    implementation(libs.kotlin.coroutines.core)
}