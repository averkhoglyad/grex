import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "net.averkhoglyad"
version = "1.0-SNAPSHOT"

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath(kotlin("gradle-plugin", version = "1.3.72"))
    }
}

plugins {
    kotlin("jvm") version "1.3.72"
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions.jvmTarget = JavaVersion.VERSION_1_8.toString()

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.3.6")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-javafx:1.3.6")

    implementation("no.tornado:tornadofx:1.7.20")
    implementation("no.tornado:tornadofx-controlsfx:0.1.1")
    implementation("org.controlsfx:controlsfx:8.40.16")

    implementation("org.slf4j:slf4j-api:1.7.30")
    implementation("org.slf4j:jul-to-slf4j:1.7.30")
    runtimeOnly("ch.qos.logback:logback-classic:1.2.3")
}

//repositories {
//    maven { url 'http://dl.bintray.com/kotlin/kotlin-eap-1.1' }
//    mavenCentral()
//}
//
//dependencies {
//    compile "org.jetbrains.kotlin:kotlin-stdlib-jre8:$kotlin_version"
//}
