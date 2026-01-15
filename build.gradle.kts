import org.gradle.api.artifacts.VersionCatalogsExtension

// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

    dependencies {
        classpath(libs.findLibrary("javapoet").get())
    }
    configurations.classpath {
        val javapoetVersion = libs.findVersion("javapoet").get().requiredVersion
        resolutionStrategy.force("com.squareup:javapoet:$javapoetVersion")
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
}
