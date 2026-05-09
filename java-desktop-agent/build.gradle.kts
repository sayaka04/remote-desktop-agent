plugins {
    id("java")
    id("application")
}

group = "remoteagent"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

// --- OS detection for JavaFX
val currentPlatform = System.getProperty("os.name").lowercase().let {
    when {
        it.contains("win") -> "win"
        it.contains("mac") -> "mac"
        else -> "linux"
    }
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // --- GSON
    implementation("com.google.code.gson:gson:2.14.0")

    // --- JavaFX
    // Explicit platform-specific binaries
    val javafxVersion = "21"
    implementation("org.openjfx:javafx-controls:$javafxVersion:$currentPlatform")
    implementation("org.openjfx:javafx-graphics:$javafxVersion:$currentPlatform")
    implementation("org.openjfx:javafx-base:$javafxVersion:$currentPlatform")
    implementation("org.openjfx:javafx-fxml:$javafxVersion:$currentPlatform")
}

configure<JavaApplication> {
    mainClass.set("remoteagent.Main")
}

tasks.withType<JavaExec> {
    // 1. Get the path to all jars in the runtime classpath
    val classpathFiles = configurations.runtimeClasspath.get().files

    // 2. Filter for JavaFX jars only and join them into a single string path
    val javafxModulePath = classpathFiles
        .filter { it.name.contains("javafx") }
        .joinToString(System.getProperty("path.separator")) { it.absolutePath }

    // 3. Set the JVM arguments properly
    jvmArgs(
        "--module-path", javafxModulePath,
        "--add-modules", "javafx.controls,javafx.fxml"
    )
}

tasks.test {
    useJUnitPlatform()
}