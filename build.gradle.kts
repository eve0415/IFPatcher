import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import net.minecraftforge.gradle.common.tasks.SignJar
import net.minecraftforge.gradle.userdev.UserDevExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.*

plugins {
  java
  kotlin("jvm") version "1.6.21"
  id("net.kyori.blossom") version "1.3.0"
  id("com.github.johnrengelman.shadow") version "7.1.2"
}

buildscript {
  repositories {
    maven { url = uri("https://repo.siro256.dev/repository/maven-public/") }
  }

  dependencies {
    classpath("net.minecraftforge.gradle:ForgeGradle:5.1.+") {
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
  maven(url = "https://www.cursemaven.com")
}

group = "net.eve0415"
version = "1.5.0-SNAPSHOT"

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions.jvmTarget = "1.8"
java.toolchain.languageVersion.set(JavaLanguageVersion.of(8))

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
  api(kotlin("stdlib"))
  implementation("curse.maven:industrialforegoing-266515:2745321")
  implementation("curse.maven:teslacorelib-254602:3438487")
}

blossom {
  replaceToken("@VERSION@", project.version)
  replaceToken("@FINGERPRINT@", signProps["signSHA1"])
}

/*reobf.create("shadowJar")*/

tasks {
  compileJava {
    sourceCompatibility = "1.8"
    targetCompatibility = "1.8"
  }

  compileKotlin {
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
          "Specification-Title"         to "IFPatcher",
          "Specification-Vendor"        to "eve0415",
          "Specification-Version"       to "1", // We are version 1 of ourselves
          "Implementation-Title"        to project.name,
          "Implementation-Version"      to project.version,
          "Implementation-Vendor"       to "eve0415",
          "FMLCorePlugin"               to "net.eve0415.ifpatcher.IFPatcher",
          "FMLCorePluginContainsFMLMod" to "true",
        )
      )
    }
  }

  named<ShadowJar>("shadowJar") {
    archiveFileName.set("IFPatcher-${project.version}.jar")
    exclude("**/module-info.class")
    minimize()
    finalizedBy("reobfShadowJar")

    dependencies {
      include(dependency("org.jetbrains.kotlin:kotlin-stdlib"))
    }
  }

  create<SignJar>("signJar") {
    dependsOn("reobfShadowJar")
    onlyIf {
      signProps.isNotEmpty()
    }

    keyStore.set(signProps["keyStore"] as String)
    storePass.set(signProps["keyStorePass"] as String)
    alias.set(signProps["keyStoreAlias"] as String)
    keyPass.set(signProps["keyStoreKeyPass"] as String)
    inputFile.set(named<Jar>("shadowJar").get().archiveFile)
    outputFile.set(named<Jar>("shadowJar").get().archiveFile)
  }

  named("build") {
    dependsOn("signJar")
  }
}
