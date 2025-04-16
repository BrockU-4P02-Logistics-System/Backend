plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.google.ortools:ortools-java:9.12.4544")
    implementation(group = "com.graphhopper", name = "graphhopper-core", version = "7.0")
    implementation(group = "com.graphhopper", name = "graphhopper-reader-osm", version = "3.0-pre3")
    implementation(group = "org.slf4j", name = "slf4j-simple", version = "1.7.32")
    implementation("org.json:json:20210307")
    implementation("org.knowm.xchart:xchart:3.8.0")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testImplementation("junit:junit:4.13.1")
}

tasks.test {
    useJUnitPlatform()
}

