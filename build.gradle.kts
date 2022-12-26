import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.bundling.BootBuildImage
import java.time.Instant

plugins {
    id("org.springframework.boot") version "2.7.6"
    id("io.spring.dependency-management") version "1.0.15.RELEASE"
    kotlin("jvm") version "1.6.21"
    kotlin("plugin.spring") version "1.6.21"
    id("com.google.cloud.tools.jib") version "3.3.1"
}

group = "org.injinity.cw"
version = "0.0.1"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")

    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("org.springframework.cloud:spring-cloud-starter")
    implementation("org.springframework.cloud:spring-cloud-starter-config")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

jib {
    from {
        image = "amazoncorretto:17"
        platforms {
            platform {
                architecture = "arm64"
                os = "linux"
            }
        }
    }
    to {
        val projectName = project.name.removePrefix("cw-")
        val environment = (project.findProperty("environment") as String?) ?: "local"
        if (environment != "local" && environment != "dev" && environment != "stag" && environment != "prod")
            throw IllegalArgumentException("Invalid environment: \"$environment\". List of environments: local, dev, stag, prod")

        if (environment != "local") {
            // example: test-0.0.1-dev
            image = "remote.injinity.org/coin-well/$projectName"
            tags = setOf("$version-$environment", "latest")
            auth {
                username = project.findProperty("registryUsername") as String?
                password = project.findProperty("registryPassword") as String?
            }
        } else {
            image = "coin-well/$projectName:latest"
        }
    }
    container {
        creationTime.set(Instant.now().toString())
    }
}

tasks.withType<BootBuildImage> {
    val projectName = project.name.removePrefix("cw-")
    val environment = (project.findProperty("environment") as String?) ?: "local"
    if (environment != "local" && environment != "dev" && environment != "stag" && environment != "prod")
        throw IllegalArgumentException("Invalid environment: \"$environment\". List of environments: local, dev, stag, prod")

    if (environment != "local") {
        isPublish = true
        imageName = "remote.injinity.org/coin-well/$projectName:$version-$environment"
        docker {
            publishRegistry {
                username = project.findProperty("registryUsername") as String?
                password = project.findProperty("registryPassword") as String?
            }
        }
    } else {
        isPublish = false
        imageName = "coin-well/$projectName:latest"
    }
}
