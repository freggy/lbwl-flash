plugins {
    kotlin("jvm") version "2.0.21"
    kotlin("plugin.serialization") version "2.0.21"
}

group = "cloud.luxor"
version = "1.3.1"

repositories {
    mavenCentral()
    maven {
        url =
            uri("https://repo.papermc.io/repository/maven-public/")
    }
}


dependencies {
    /**
     * paper
     */
    compileOnly("io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT")
    implementation("com.charleskorn.kaml:kaml:0.61.0")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks.withType<Jar> {
    val dependencies = configurations
        .runtimeClasspath
        .get()
        .map(::zipTree)
    from(dependencies)
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    base.archivesName.set("${project.name}-all")
}
