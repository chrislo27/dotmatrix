plugins {
    kotlin("jvm") version "1.8.22"
    `maven-publish`
}
group = "io.github.chrislo27"
version = "3.4.0"

repositories {
    mavenCentral()
}
kotlin {
    jvmToolchain(11)
    sourceSets {
        val main by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")
                implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
                implementation("com.madgag:animated-gif-lib:1.4")
            }
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["kotlin"])
        }
    }
}