plugins {
    id(libs.plugins.java.library.get().pluginId)
    alias(libs.plugins.kotlin.jvm)
}

ext.set("PUBLISH_ARTIFACT_ID", "security-ige")
apply(from = "${rootProject.projectDir}/scripts/publish-module.gradle")

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    // MTProto
    api(project(libs.mtproto.security.cipher.get().module.name))
}
