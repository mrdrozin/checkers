plugins {
    kotlin("jvm") version "1.5.10"
    application
}

group = "ru.spbu.math-cs"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://dl.bintray.com/kotlin/kotlin-eap")
    maven("https://packages.jetbrains.team/maven/p/skija/maven")
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

val osName = System.getProperty("os.name")
val targetOs = when {
    osName == "Mac OS X" -> "macos"
    osName.startsWith("Win") -> "windows"
    osName.startsWith("Linux") -> "linux"
    else -> error("Unsupported OS: $osName")
}

val osArch = System.getProperty("os.arch")
var targetArch = when (osArch) {
    "x86_64", "amd64" -> "x64"
    "aarch64" -> "arm64"
    else -> error("Unsupported arch: $osArch")
}

val versionk = "0.7.34"
val target = "${targetOs}-${targetArch}"

val skijaArtifact = "skija-$targetOs"
val skijaVersion = when (targetOs) {
    "linux" -> "0.93.1"
    else -> "0.93.6"
}

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation(kotlin("stdlib"))
    testImplementation(kotlin("test"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.6.4")
    implementation("org.jetbrains.skija:${skijaArtifact}:${skijaVersion}")
    implementation("org.jetbrains.skiko:skiko-awt-runtime-$target:$versionk")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

application {
    mainClass.set("MainKt")
}