plugins {
    id("java")
    id("application")
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://bio.informatik.uni-jena.de/repository/libs-release-oss/")
    }
}

dependencies {
    implementation("net.sourceforge:javaml:0.1.7")
    implementation(group = "com.graphhopper", name = "graphhopper-core", version = "7.0")
    implementation(group = "com.graphhopper", name = "graphhopper-reader-osm", version = "3.0-pre3")
    implementation(group = "org.slf4j", name = "slf4j-simple", version = "2.0.9")
    implementation("org.json:json:20210307")
    implementation("org.knowm.xchart:xchart:3.8.0")
}

tasks.named<JavaExec>("run") {
    mainClass.set("org.example.RoutingExample")
}