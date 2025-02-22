pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "ByeDpi"
include(":app")
include(":feature-connection-ui")
include(":common-storage")
include(":app-bypass-services")
include(":common-system")
include(":feature-settings")
include(":feature-bypass-test")
include(":feature-bypass-api")
include(":common-ui")
