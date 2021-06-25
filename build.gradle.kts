import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "net.averkhoglyad"
version = "1.0-SNAPSHOT"

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath(kotlin("gradle-plugin", version = "1.5.10"))
    }
}

plugins {
    kotlin("jvm") version "1.5.10"
    id("org.openjfx.javafxplugin") version "0.0.10"
    id("io.spring.dependency-management") version "1.0.1.RELEASE"
}

dependencyManagement {
    imports {
        mavenBom("org.apache.logging.log4j:log4j-bom:2.14.1")
    }
}

javafx {
    modules("javafx.controls", "javafx.graphics")
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions.jvmTarget = JavaVersion.VERSION_11.toString()

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.5.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-javafx:1.5.0")

    implementation("no.tornado:tornadofx:1.7.20")
    implementation("no.tornado:tornadofx-controlsfx:0.1.1")
    implementation("org.controlsfx:controlsfx:11.1.0")

    implementation("org.slf4j:slf4j-api:1.7.30")
    implementation("org.slf4j:jul-to-slf4j:1.7.31")
    runtimeOnly("ch.qos.logback:logback-classic:1.2.3")
    runtimeOnly("org.apache.logging.log4j:log4j-api")
    runtimeOnly("org.apache.logging.log4j:log4j-core")
}

//repositories {
//    maven { url 'http://dl.bintray.com/kotlin/kotlin-eap-1.1' }
//    mavenCentral()
//}
//
//dependencies {
//    compile "org.jetbrains.kotlin:kotlin-stdlib-jre8:$kotlin_version"
//}
