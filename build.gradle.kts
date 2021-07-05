import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "net.averkhoglyad"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

plugins {
    kotlin("jvm") version "1.5.20"
    id("org.openjfx.javafxplugin") version "0.0.10"
    id("io.spring.dependency-management") version "1.0.1.RELEASE"
    application
}

application {
    mainClass.set("net.averkhoglyad.grex.roo.MainKt")
}

javafx {
    modules("javafx.controls", "javafx.graphics")
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions.jvmTarget = JavaVersion.VERSION_11.toString()

dependencyManagement {
    imports {
        mavenBom("org.apache.logging.log4j:log4j-bom:2.14.1")
    }
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.5.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-javafx:1.5.0")

    implementation("no.tornado:tornadofx:1.7.20")
    implementation("no.tornado:tornadofx-controlsfx:0.1.1")
    implementation("org.controlsfx:controlsfx:11.1.0")

    implementation("org.slf4j:slf4j-api:1.7.31")
    implementation("org.slf4j:jul-to-slf4j:1.7.31")
    implementation("org.apache.logging.log4j:log4j-api")
    
    runtimeOnly("org.apache.logging.log4j:log4j-core")
    runtimeOnly("org.apache.logging.log4j:log4j-slf4j-impl")
}
