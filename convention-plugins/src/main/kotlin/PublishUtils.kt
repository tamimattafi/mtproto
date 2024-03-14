import GradleUtils.correctArtifactId
import GradleUtils.requireExtraString
import gradle.kotlin.dsl.accessors._b6bea14fb88fd11e46d6fb1ebe601eab.publishing
import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.tasks.AbstractPublishToMaven
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import org.gradle.plugins.signing.Sign
import org.gradle.plugins.signing.SigningExtension

object PublishUtils {

    const val VERSION = "3.0.3-alpha01"
    const val GROUP_ID = "com.attafitamim.mtproto"

    fun Project.configurePublishing(
        configureArtifacts: MavenPublication.() -> Unit = {}
    ) {
        val signingTasks = tasks.withType<Sign>()
        tasks.withType<AbstractPublishToMaven>().configureEach {
            dependsOn(signingTasks)
        }

        publishing {
            publications {
                // Configure all publications
                withType<MavenPublication> {
                    correctArtifactId(this)

                    configureArtifacts()

                    // Stub artifacts
                    artifact(tasks.named("javadocJar"))

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
                    rootProject.requireExtraString("signing.keyId"),
                    rootProject.requireExtraString("signing.key"),
                    rootProject.requireExtraString("signing.password"),
                )

                //sign(publications)
            }
        }
    }
}