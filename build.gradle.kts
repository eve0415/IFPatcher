import net.minecraftforge.gradle.common.tasks.SignJar
import net.minecraftforge.gradle.userdev.UserDevExtension
import java.util.*

plugins {
    java
    kotlin("jvm") version "1.9.23"
    id("net.kyori.blossom") version "2.1.0"
}

buildscript {
    repositories {
        maven { url = uri("https://repo.siro256.dev/repository/maven-public/") }
    }

    dependencies {
        classpath("net.minecraftforge.gradle:ForgeGradle:[6.0,6.2)") {
            isChanging = true
        }
    }
}

apply(plugin = "net.minecraftforge.gradle")

repositories {
    mavenCentral()
    gradlePluginPortal()
    maven(url = "https://maven.minecraftforge.net")
    maven(url = "https://plugins.gradle.org/m2/")
    maven(url = "https://cursemaven.com") {
        content {
            includeGroup("curse.maven")
        }
    }
}

group = "net.eve0415"
version = "2.4.0"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

val signProps = if (!System.getenv("KEY_STORE").isNullOrEmpty()) {
    System.getenv("KEY_STORE").reader().let {
        val prop = Properties()
        prop.load(it)
        return@let prop
    }
} else if (file("secret.properties").exists()) {
    file("secret.properties").inputStream().let {
        val prop = Properties()
        prop.load(it)
        return@let prop
    }
} else {
    Properties()
}

configure<UserDevExtension> {
    mappings("snapshot", "20180814-1.12")
}

dependencies {
    "minecraft"("net.minecraftforge:forge:1.12.2-14.23.5.2860")
    implementation("curse.maven:industrialforegoing-266515:2745321")
    implementation("curse.maven:teslacorelib-254602:3438487")
    implementation(kotlin("stdlib-jdk8"))
}

sourceSets {
    main {
        blossom {
            javaSources {
                property("VERSION", project.version.toString())
                property("FINGERPRINT", signProps["signSHA1"].toString())
            }
        }
    }
}

tasks {
    compileJava {
        sourceCompatibility = "1.8"
        targetCompatibility = "1.8"
    }

    named<ProcessResources>("processResources") {
        inputs.property("version", project.version)
        from(sourceSets.main.get().resources.srcDirs) {
            duplicatesStrategy = DuplicatesStrategy.INCLUDE
            include("mcmod.info")
            expand("version" to project.version)
        }
    }

    named<Jar>("jar") {
        manifest {
            attributes(
                mapOf(
                    "Specification-Title" to "IFPatcher",
                    "Specification-Vendor" to "eve0415",
                    "Specification-Version" to "1", // We are version 1 of ourselves
                    "Implementation-Title" to project.name,
                    "Implementation-Version" to project.version,
                    "Implementation-Vendor" to "eve0415",
                    "FMLCorePlugin" to "net.eve0415.ifpatcher.IFPatcher",
                    "FMLCorePluginContainsFMLMod" to "true",
                )
            )
        }
    }

    create<SignJar>("signJar") {
        dependsOn("reobfJar")
        onlyIf {
            signProps.isNotEmpty()
        }

        keyStore.set(signProps["keyStore"] as String?)
        storePass.set(signProps["keyStorePass"] as String?)
        alias.set(signProps["keyStoreAlias"] as String?)
        keyPass.set(signProps["keyStoreKeyPass"] as String?)
        inputFile.set(named<Jar>("jar").get().archiveFile)
        outputFile.set(named<Jar>("jar").get().archiveFile)
    }

    named("build") {
        dependsOn("signJar")
    }
}
