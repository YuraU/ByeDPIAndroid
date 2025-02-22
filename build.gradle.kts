// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.9.2" apply false
    id("com.android.library") version "8.9.2" apply false
    id("org.jetbrains.kotlin.android") version "2.1.10" apply false
    id("io.gitlab.arturbosch.detekt") version "1.23.0" apply false
}

subprojects {
    apply(plugin = "io.gitlab.arturbosch.detekt")

    configure<io.gitlab.arturbosch.detekt.extensions.DetektExtension> {
        config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
        buildUponDefaultConfig = true
        parallel = true
    }

    tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
        jvmTarget = "1.8" // или "11", "17"

        reports {
            html.required.set(true)
            xml.required.set(true)
            txt.required.set(true)

            html.outputLocation.set(file(layout.buildDirectory.dir("reports/detekt.html")))
            xml.outputLocation.set(file(layout.buildDirectory.dir("reports/detekt.xml")))
            txt.outputLocation.set(file(layout.buildDirectory.dir("reports/detekt.txt")))
        }
    }
}
