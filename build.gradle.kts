plugins {
    id("java")
    id("com.gradleup.shadow") version "9.0.0-beta13"
}

group = "com.minehut.moderators"
version = "1.1.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    implementation("org.incendo:cloud-annotations:2.0.0")
    implementation("org.incendo:cloud-paper:2.0.0-beta.10")
    implementation(platform("com.intellectualsites.bom:bom-newest:1.52"))
    compileOnly("com.intellectualsites.plotsquared:plotsquared-core")
    compileOnly("com.intellectualsites.plotsquared:plotsquared-bukkit") { isTransitive = false }
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")


    compileOnly("org.projectlombok:lombok:1.18.38")
    annotationProcessor("org.projectlombok:lombok:1.18.38")
}