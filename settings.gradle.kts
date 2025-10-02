pluginManagement {
    repositories {
        // Repositorio de Google (Android, Firebase, Play Services)
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        // Repositorios adicionales
        mavenCentral()
        gradlePluginPortal()
        // jcenter() // ⚠️ Usar solo si alguna dependencia antigua lo necesita
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()        // ✅ Siempre primero
        mavenCentral()  // ✅ Repositorio central
        // jcenter()    // ⚠️ Solo si es necesario
    }
}

rootProject.name = "SmartFashionEcommerce"
include(":app")
