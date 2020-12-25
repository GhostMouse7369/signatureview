buildscript {
    repositories {
        google()
        jcenter()
        gradlePluginPortal()
    }
    dependencies {
        classpath(top.laoshuzi.dependencies.deps.BuildTools.android_gradle_build_plugin)
        classpath(top.laoshuzi.dependencies.deps.BuildTools.kotlin_gradle_build_plugin)
    }
}

val ziweiRepositoryDomainName: String by project

allprojects {
    repositories {
        google()
        jcenter()
        maven("https://jitpack.io")
        maven("${ziweiRepositoryDomainName}repository/maven-public/")
    }
}

tasks {
    val clean by registering(Delete::class) {
        delete(buildDir)
    }
}
