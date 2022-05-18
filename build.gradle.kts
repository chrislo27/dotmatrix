plugins {
    kotlin("jvm") version "1.6.21"
    `maven-publish`
}
group = "io.github.chrislo27"
version = "3.2"

repositories {
    mavenCentral()
}
kotlin {
    sourceSets {
        val main by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")
                implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
                implementation("com.madgag:animated-gif-lib:1.4")
            }
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
//            groupId = "org.gradle.sample"
//            artifactId = "library"
//            version = "1.1"

            from(components["kotlin"])
        }
    }
}