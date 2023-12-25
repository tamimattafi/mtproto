plugins {
    id(libs.plugins.kotlin.jvm.get().pluginId)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17

    kotlin {
        jvmToolchain(17)
    }
}

dependencies {
    implementation(project(libs.mtproto.sample.shared.get().module.name))
}
