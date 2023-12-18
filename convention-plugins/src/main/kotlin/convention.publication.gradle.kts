import GradleUtils.correctArtifactId
import GradleUtils.getExtraString

plugins {
    `maven-publish`
    signing
}

group = "com.attafitamim.mtproto"
version = "3.0.0-local31"

val javadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
}

afterEvaluate {
    val signingTasks = tasks.withType<Sign>()
    tasks.withType<AbstractPublishToMaven>().configureEach {
        dependsOn(signingTasks)
    }

    publishing {
        publications {
            // Configure all publications
            withType<MavenPublication> {
                correctArtifactId(this)

                // Stub javadoc.jar artifact
                artifact(javadocJar)

                // Provide artifacts information requited by Maven publish
                pom {
                    name.set("MTProto Multiplatform")
                    description.set("MTProto for Kotlin Multiplatform (Android/iOS/JVM/JS)")
                    url.set("https://github.com/tamimattafi/mtproto")

                    licenses {
                        license {
                            name.set("Library Licence")
                            url.set("https://github.com/tamimattafi/mtproto/blob/main/LICENSE")
                        }
                    }

                    developers {
                        developer {
                            id.set("attafitamim")
                            name.set("Tamim Attafi")
                            email.set("attafitamim@gmail.com")
                        }
                    }

                    scm {
                        connection.set("scm:git:github.com/tamimattafi/mtproto.git")
                        developerConnection.set("scm:git:ssh://github.com/tamimattafi/mtproto.git")
                        url.set("https://github.com/tamimattafi/mtproto/tree/main")
                    }
                }
            }
        }

        // Configure sonatype maven repository
        repositories {
            mavenLocal()
            mavenCentral()
        }

        extensions.configure<SigningExtension> {
            useInMemoryPgpKeys(
                rootProject.getExtraString("signing.keyId"),
                rootProject.getExtraString("signing.key"),
                rootProject.getExtraString("signing.password"),
            )

            //sign(publications)
        }
    }
}
