plugins {
    java
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "net.azisaba"
version = "1.0.0-SNAPSHOT"

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(11))
}

repositories {
    mavenCentral()
    maven { url = uri("https://nexus.velocitypowered.com/repository/maven-public/") }
    maven { url = uri("https://repo.acrylicstyle.xyz/repository/maven-public/") }
    maven { url = uri("https://jitpack.io/") }
}

dependencies {
    implementation("com.github.oshi:oshi-core:6.1.6")
    implementation("redis.clients:jedis:4.2.3")
    annotationProcessor("com.velocitypowered:velocity-api:3.0.1")
    compileOnly("com.velocitypowered:velocity-api:3.0.1")
    compileOnly("net.azisaba.simpleProxy:proxy:1.1.2") // AzisabaNetwork/SimpleProxy
    compileOnly("com.github.AzisabaNetwork:VelocityRedisBridge:b96753f4df") // AzisabaNetwork/VelocityRedisBridge
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.2")

}

tasks {
    test {
        useJUnitPlatform()
    }

    processResources {
        filteringCharset = "UTF-8"
        from(sourceSets.main.get().resources.srcDirs) {
            include("**/*.yml")

            val tokenReplacementMap = mapOf(
                "version" to project.version,
                "name" to project.name,
            )

            filter<org.apache.tools.ant.filters.ReplaceTokens>("tokens" to tokenReplacementMap)
        }

        duplicatesStrategy = DuplicatesStrategy.INCLUDE

        from(projectDir) { include("LICENSE") }
    }

    compileJava {
        options.encoding = "UTF-8"
    }

    shadowJar {
        relocate("javassist", "net.azisaba.autoreboot.libs.javassist")
        archiveFileName.set("AutoReboot-${project.version}-universal.jar")
    }
}
