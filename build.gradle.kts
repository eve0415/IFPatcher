import java.util.*

plugins {
    java
    id("net.kyori.blossom") version "2.2.0"
    id("net.minecraftforge.gradle") version "[7.0,8.0)"
    id("net.minecraftforge.renamer") version "1.1.0"
}

group = "net.eve0415"
version = "2.5.0"

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

minecraft {
    mappings("snapshot", "20180814-1.12")
}

repositories {
    minecraft.mavenizer(this)
    maven(fg.forgeMaven)
    maven(fg.minecraftLibsMaven)
    mavenCentral()
    maven(url = "https://cursemaven.com") {
        content {
            includeGroup("curse.maven")
        }
    }
}

dependencies {
    implementation(minecraft.dependency("net.minecraftforge:forge:1.12.2-14.23.5.2864"))
    implementation("curse.maven:industrialforegoing-266515:2745321")
    implementation("curse.maven:teslacorelib-254602:3438487")
}

configurations.all {
    exclude(group = "ca.weblite", module = "java-objc-bridge")
}

val renameJar = renamer.classes(tasks.named<Jar>("jar")) {
    map.from(minecraft.dependency.toSrgFile)
    output.set(layout.buildDirectory.file("libs/${project.name}-${project.version}.jar"))
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
        archiveClassifier.set("dev")
        manifest {
            attributes(
                mapOf(
                    "Specification-Title" to "IFPatcher",
                    "Specification-Vendor" to "eve0415",
                    "Specification-Version" to "1",
                    "Implementation-Title" to project.name,
                    "Implementation-Version" to project.version,
                    "Implementation-Vendor" to "eve0415",
                    "FMLCorePlugin" to "net.eve0415.ifpatcher.IFPatcher",
                    "FMLCorePluginContainsFMLMod" to "true",
                )
            )
        }
    }

    register<Exec>("signJar") {
        dependsOn(renameJar)
        val shouldSign = signProps.isNotEmpty()
        onlyIf { shouldSign }
        executable = "jarsigner"
        args(
            "-keystore", signProps["keyStore"].toString(),
            "-storepass", signProps["keyStorePass"].toString(),
            "-keypass", signProps["keyStoreKeyPass"].toString(),
            renameJar.get().output.get().asFile.absolutePath,
            signProps["keyStoreAlias"].toString(),
        )
    }

    named("build") {
        dependsOn("signJar")
    }
}
