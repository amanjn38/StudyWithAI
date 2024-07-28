import java.net.URL

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        jcenter()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        maven {
            url = uri("https://pdftron-maven.s3.amazonaws.com/release")
        }
        jcenter()
        mavenCentral()
    }
}

rootProject.name = "StudyWithAI"
include(":app")
 