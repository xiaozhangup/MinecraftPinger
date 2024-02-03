plugins {
    kotlin("jvm") version "1.9.21"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "me.xiaozhangup.pinger"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation(kotlin("stdlib"))
    implementation(fileTree("libs"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}

tasks {
    shadowJar {
        manifest {
            attributes["Main-Class"] = "me.xiaozhangup.pinger.PingerKt"
        }
    }
}