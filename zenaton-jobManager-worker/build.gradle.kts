plugins {
    kotlin("jvm")
    id("org.jlleitschuh.gradle.ktlint") version "9.2.1"

    // Apply the java-library plugin for API and implementation separation.
    `java-library`
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.apache.avro:avro:1.9.+")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.11.+")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.11.+")
    implementation("org.slf4j:slf4j-api:1.7.+")

    implementation(project(":zenaton-common"))
    implementation(project(":zenaton-jobManager"))

    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.8")
    testImplementation("org.jeasy:easy-random-core:4.2.+")
    testImplementation("io.kotest:kotest-runner-junit5-jvm:4.0.+")
    testImplementation("io.kotest:kotest-property-jvm:4.0.+")
    testImplementation("io.kotest:kotest-core-jvm:4.0.+")
    testImplementation("io.mockk:mockk:1.9.+")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = JavaVersion.VERSION_1_8.toString()
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = JavaVersion.VERSION_1_8.toString()
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
