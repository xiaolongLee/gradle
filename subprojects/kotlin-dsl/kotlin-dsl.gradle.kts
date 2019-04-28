/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.gradle.gradlebuild.unittestandcompile.ModuleType
import build.futureKotlin
import build.kotlin
import build.kotlinVersion
import codegen.GenerateKotlinDependencyExtensions

plugins {
    `kotlin-dsl-module`
}

description = "Kotlin DSL Provider"

gradlebuildJava {
    moduleType = ModuleType.CORE
}

dependencies {

    api(project(":distributionsDependencies"))

    compileOnly(project(":toolingApi"))

    compile(project(":kotlinDslToolingModels"))

    compile(project(":kotlinCompilerEmbeddable"))
    compile(futureKotlin("scripting-compiler-embeddable")) {
        isTransitive = false
    }

    compile(futureKotlin("stdlib-jdk8"))
    compile(futureKotlin("sam-with-receiver-compiler-plugin")) {
        isTransitive = false
    }
    compile("org.jetbrains.kotlinx:kotlinx-metadata-jvm:0.0.5") {
        isTransitive = false
    }

    testImplementation(project(":kotlinDslTestFixtures"))

    testImplementation(project(":buildCacheHttp"))
    testImplementation(project(":buildInit"))
    testImplementation(project(":jacoco"))
    testImplementation(project(":platformNative"))
    testImplementation(project(":plugins"))
    testImplementation(project(":versionControl"))

    testImplementation("com.tngtech.archunit:archunit:0.8.3")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.0.1")
    testImplementation("org.awaitility:awaitility-kotlin:3.1.6")

    testRuntimeOnly(project(":runtimeApiInfo"))

    integTestRuntimeOnly(project(":runtimeApiInfo"))
    integTestRuntimeOnly(project(":apiMetadata"))
    integTestRuntimeOnly(project(":pluginDevelopment"))
    integTestRuntimeOnly(project(":toolingApiBuilders"))
}

// --- Enable automatic generation of API extensions -------------------
val apiExtensionsOutputDir = layout.buildDirectory.dir("generated-sources/kotlin")

val publishedKotlinDslPluginVersion = "1.2.8" // TODO:kotlin-dsl

tasks {

    // TODO:kotlin-dsl
    verifyTestFilesCleanup {
        enabled = false
    }

    val generateKotlinDependencyExtensions by registering(GenerateKotlinDependencyExtensions::class) {
        outputDir.set(apiExtensionsOutputDir)
        embeddedKotlinVersion.set(kotlinVersion)
        kotlinDslPluginsVersion.set(publishedKotlinDslPluginVersion)
    }

    val generateExtensions by registering {
        dependsOn(generateKotlinDependencyExtensions)
    }

    sourceSets.main {
        kotlin.srcDir(files(apiExtensionsOutputDir).builtBy(generateExtensions))
    }

// -- Version manifest properties --------------------------------------
    val writeVersionsManifest by registering(WriteProperties::class) {
        outputFile = buildDir.resolve("versionsManifest/gradle-kotlin-dsl-versions.properties")
        property("kotlin", kotlinVersion)
    }

    processResources {
        from(writeVersionsManifest)
    }
}
