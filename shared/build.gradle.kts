import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.mavenPublish)
}

group = "io.github.timoptr"
version = "0.2.0"

kotlin {
    iosArm64()
    iosSimulatorArm64()

    jvm()

    js {
        browser()
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }

    android {
        namespace = "io.github.timoptr.mdiicons.shared"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.library.get().toInt()

        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
        withHostTest {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }.configure {
            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.androidx.annotation)
        }
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.ui)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        getByName("androidHostTest").dependencies {
            implementation(libs.kotlin.testJunit)
            implementation(libs.junit)
            implementation(libs.robolectric)
        }
    }
}

tasks.withType<Test>().matching { it.name == "testAndroidHostTest" }.configureEach {
    // https://robolectric.org/getting-started/#running-with-java-17-and-higher
    // the JVM requires --add-opens flags so that Robolectric can access internal
    // OpenJDK classes and APIs (java.lang, java.io, jdk.internal.access, etc.)
    jvmArgs(
        "--add-opens=java.base/java.lang=ALL-UNNAMED",
        "--add-opens=java.base/java.util=ALL-UNNAMED",
        "--add-opens=java.base/java.io=ALL-UNNAMED",
        "--add-opens=java.base/java.net=ALL-UNNAMED",
        "--add-opens=java.base/java.security=ALL-UNNAMED",
        "--add-opens=java.base/java.text=ALL-UNNAMED",
        "--add-opens=java.base/jdk.internal.access=ALL-UNNAMED",
        "--add-opens=java.desktop/java.awt.font=ALL-UNNAMED",
        "--add-opens=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED",
    )
}

val mdiGeneratedDirectory = layout.projectDirectory.dir("src/commonMain/kotlin/io/github/timoptr/mdiicons/generated")

tasks.register<UpdateMdiIconsTask>("updateMdiIcons") {
    group = "build setup"
    description = "Regenerates the MDI icon catalog from the @mdi/svg version pinned in gradle/libs.versions.toml"
    mdiVersion.set(libs.versions.mdi.svg)
    outputDirectory.set(mdiGeneratedDirectory)
}

tasks.register<VerifyMdiIconsTask>("verifyMdiIcons") {
    group = "verification"
    description = "Checks that the generated MDI icon catalog matches the pinned @mdi/svg version"
    mdiVersion.set(libs.versions.mdi.svg)
    generatedDirectory.set(mdiGeneratedDirectory)
}

mavenPublishing {
    publishToMavenCentral()
    // Signing needs the in-memory GPG key, provided in CI; skip it so publishToMavenLocal works without keys.
    if (providers.gradleProperty("signingInMemoryKey").isPresent) {
        signAllPublications()
    }
    coordinates(group.toString(), "mdi-icons", version.toString())
    pom {
        name = "mdi-icons"
        description = "Compose Multiplatform library exposing the Material Design Icons catalog as Kotlin."
        inceptionYear = "2026"
        url = "https://github.com/TimoPtr/mdi-icons/"
        licenses {
            license {
                name = "The Apache License, Version 2.0"
                url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
                distribution = "https://www.apache.org/licenses/LICENSE-2.0.txt"
            }
        }
        developers {
            developer {
                id = "TimoPtr"
                name = "Timothy"
                url = "https://github.com/TimoPtr/"
            }
        }
        scm {
            url = "https://github.com/TimoPtr/mdi-icons/"
            connection = "scm:git:git://github.com/TimoPtr/mdi-icons.git"
            developerConnection = "scm:git:ssh://git@github.com/TimoPtr/mdi-icons.git"
        }
    }
}
