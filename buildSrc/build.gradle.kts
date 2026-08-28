import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

plugins {
    `kotlin-dsl`
    alias(libs.plugins.ktlint)
}

repositories {
    mavenCentral()
}

ktlint {
    reporters {
        reporter(ReporterType.SARIF)
        reporter(ReporterType.PLAIN)
    }
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation(libs.junit)
}
