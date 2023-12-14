plugins {
    id(libs.plugins.java.library.get().pluginId)
    alias(libs.plugins.kotlin.jvm)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    // MTProto
    api(project(libs.mtproto.client.connection.get().module.name))
    api(project(libs.mtproto.client.sockets.ktor.get().module.name))
    api(project(libs.mtproto.client.sockets.connect.get().module.name))

    // Ktor
    implementation(libs.ktor.webscokets)
    implementation(libs.ktor.network)
    implementation(libs.ktor.cio)
    implementation(libs.ktor.logger)
    implementation(libs.ktor.core)

    // IO
    implementation(libs.kotlinx.io)
}
