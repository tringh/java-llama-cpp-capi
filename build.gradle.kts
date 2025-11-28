plugins {
    id("java")
    `maven-publish`
}

group = "io.github.tringh.jallama"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

tasks.register<Exec>("compileNative") {
    group = "build"
    description = "Compile core library"
    workingDir = layout.projectDirectory.dir("scripts").asFile
    commandLine("bash", "./build_corelib.sh")
    standardOutput = System.out
}

tasks.register<Copy>("copyNative") {
    dependsOn("compileNative")
    from(layout.projectDirectory.file("core/build/libllama_tokenizer.so"))
    into(layout.buildDirectory.dir("resources/main"))
}

tasks.register<Exec>("generateBindings") {
    group = "build"
    description = "Run jextract"
    dependsOn("copyNative")
    workingDir = layout.projectDirectory.dir("scripts").asFile
    commandLine("bash", "./jextract.sh")
    standardOutput = System.out
}

tasks.named("compileJava") {
    dependsOn("generateBindings")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }

    repositories {
        maven {
            name = "GithubPackages"
            url = uri("https://maven.pkg.github.com/tringh/java-llama-cpp-capi")

            credentials {
                username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR")
                password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }
}

sourceSets {
    main {
        java {
            srcDir(
                "${project.layout.buildDirectory.get().asFile}/generated/jextract")
        }
    }
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}