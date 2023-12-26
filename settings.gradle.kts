enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenLocal()
        mavenCentral()
        google()
    }
}
dependencyResolutionManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        google()
    }
}

rootProject.name = "mtproto"
include(":server")
include(":core")
include(":generator")
include(":buffer")
include(":serialization")
include(":security")
include(":client:api")
include(":client:connection")
include(":client:sockets")
include(":client:sockets:infrastructure")
include(":client:sockets:ktor")
include(":client:sockets:connect")
include(":security:cipher")
include(":security:obfuscation")
include(":security:utils")
include(":security:digest")
include(":client:sample:shared")
include(":client:sample:androidApp")
include(":client:sample:jvmApp")

// Publish
includeBuild("convention-plugins")