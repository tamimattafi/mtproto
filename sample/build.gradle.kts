buildscript {
    repositories {
        mavenLocal()
        mavenCentral()
        google()
    }

    dependencies {
        classpath("com.android.tools.build:gradle:7.2.1")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.6.21")
        classpath("com.attafitamim.mtproto:generator:1.0.0-beta04")
    }
}

allprojects {
    repositories {
        mavenLocal()
        mavenCentral()
        google()
    }
}
