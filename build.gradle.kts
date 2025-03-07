// Project-level Gradle (settings for all modules)
buildscript {
    dependencies {
        classpath("com.google.gms:google-services:4.4.0") // Firebase Plugin
    }
}

plugins {
    id("com.android.application") version "8.2.0" apply false
    id("com.android.library") version "8.2.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.0" apply false
}
