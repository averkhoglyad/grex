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

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("no.tornado:tornadofx:1.7.20")
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
