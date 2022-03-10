import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "net.averkhoglyad"
version = "1.0-SNAPSHOT"

val targetJvmVersion = JavaVersion.VERSION_17.toString()

repositories {
    mavenCentral()
}

plugins {
    kotlin("jvm") version "1.6.10"
    id("org.openjfx.javafxplugin") version "0.0.10"
    id("io.spring.dependency-management") version "1.0.1.RELEASE"
    application
}

application {
    mainClass.set("net.averkhoglyad.grex.arrow.MainKt")
}

javafx {
    version = targetJvmVersion
    modules("javafx.controls", "javafx.graphics", "javafx.swing")
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions.jvmTarget = targetJvmVersion

val compileJava: JavaCompile by tasks
compileJava.sourceCompatibility = targetJvmVersion
compileJava.targetCompatibility = targetJvmVersion

dependencyManagement {
    imports {
        mavenBom("org.apache.logging.log4j:log4j-bom:2.17.2")
    }
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.6.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-javafx:1.6.0")

    implementation("no.tornado:tornadofx:1.7.20")
    implementation("no.tornado:tornadofx-controlsfx:0.1.1")
    implementation("org.controlsfx:controlsfx:11.1.0")

    compileOnly("org.slf4j:slf4j-api:1.7.32")
    compileOnly("org.slf4j:jul-to-slf4j:1.7.32")

    implementation("org.apache.logging.log4j:log4j-api")
    implementation("org.apache.logging.log4j:log4j-jul")

    runtimeOnly("org.apache.logging.log4j:log4j-core")
    runtimeOnly("org.apache.logging.log4j:log4j-slf4j-impl")
}
