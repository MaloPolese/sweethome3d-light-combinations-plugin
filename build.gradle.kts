plugins {
    kotlin("jvm") version "2.0.0"
    kotlin("plugin.serialization") version "1.4.20"
}

group = "com.lightscombinations"
version = project.findProperty("version") ?: "1.0"


repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("com.charleskorn.kaml:kaml:0.60.0")
    // Include the SweetHome3D jar
    implementation(files("libs/SweetHome3D-7.5.jar"))
}

tasks.withType<Jar> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    destinationDirectory = file("C:\\Users\\Malo\\AppData\\Roaming\\eTeks\\Sweet Home 3D\\plugins")

    manifest {
        attributes(
            "Main-Class" to "$group.App",
            "Plugin-Name" to "lightscombinations",
            "Plugin-Version" to version
        )
    }
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
}