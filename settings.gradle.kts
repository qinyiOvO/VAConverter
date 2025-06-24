// settings.gradle.kts

pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS) // 强制使用 settings 中的仓库
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "SY"
include(":app")