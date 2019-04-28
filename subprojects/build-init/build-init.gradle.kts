import org.gradle.gradlebuild.unittestandcompile.ModuleType
import org.gradle.util.VersionNumber
import java.util.*

/*
 * Copyright 2013 the original author or authors.
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
plugins {
    gradlebuild.classycle
}

dependencies {
    implementation("org.codehaus.plexus:plexus-container-default")
    implementation("org.apache.maven:maven-compat")
    implementation("org.apache.maven:maven-plugin-api")
    implementation(library("groovy"))

    implementation(project(":core"))
    implementation(project(":platformNative"))
    implementation(project(":plugins"))
    implementation(project(":wrapper"))

    testFixturesImplementation(project(":internalTesting"))

    val allTestRuntimeDependencies: DependencySet by rootProject.extra
    allTestRuntimeDependencies.forEach {
        integTestRuntime(it)
    }
}

gradlebuildJava {
    moduleType = ModuleType.CORE
}

testFixtures {
    from(":core")
    from(":platformNative")
}

tasks {
    register("updateInitPluginTemplateVersionFile") {
        group = "Build init"
        doLast {

            val versionProperties = Properties()

            // Currently no scalatest for 2.13
            findLatest("scala-library", "org.scala-lang:scala-library:2.12.+", versionProperties)
            val scalaVersion = VersionNumber.parse(versionProperties["scala-library"] as String)
            versionProperties["scala"] = "${scalaVersion.major}.${scalaVersion.minor}"

            findLatest("scalatest", "org.scalatest:scalatest_${versionProperties["scala"]}:(3.0,)", versionProperties)
            findLatest("scala-xml", "org.scala-lang.modules:scala-xml_${versionProperties["scala"]}:latest.release", versionProperties)
            findLatest("groovy", "org.codehaus.groovy:groovy:(2.5,)", versionProperties)
            findLatest("junit", "junit:junit:(4.0,)", versionProperties)
            findLatest("junit-jupiter", "org.junit.jupiter:junit-jupiter-api:(5,)", versionProperties)
            findLatest("testng", "org.testng:testng:(6.0,)", versionProperties)
            findLatest("slf4j", "org.slf4j:slf4j-api:(1.7,)", versionProperties)

            val groovyVersion = VersionNumber.parse(versionProperties["groovy"] as String)
            versionProperties["spock"] = "1.2-groovy-${groovyVersion.major}.${groovyVersion.minor}"

            findLatest("guava", "com.google.guava:guava:(20,)", versionProperties)
            findLatest("commons-math", "org.apache.commons:commons-math3:latest.release", versionProperties)
            findLatest("kotlin", "org.jetbrains.kotlin:kotlin-gradle-plugin:(1.3,)", versionProperties)

            val libraryVersionFile = file("src/main/resources/org/gradle/buildinit/tasks/templates/library-versions.properties")
            org.gradle.build.ReproduciblePropertiesWriter.store(
                versionProperties,
                libraryVersionFile,
                "Generated file, please do not edit - Version values used in build-init templates"
            )
        }
    }
}

fun findLatest(name: String, notation: String, dest: Properties) {
    val libDependencies = arrayOf(project.dependencies.create(notation))
    val templateVersionConfiguration = project.configurations.detachedConfiguration(*libDependencies)
    templateVersionConfiguration.resolutionStrategy.componentSelection.all {
        devSuffixes.forEach {
            if (candidate.version.matches(".+$it\$".toRegex())) {
                reject("don't use snapshots")
                return@forEach
            }
        }
    }
    templateVersionConfiguration.isTransitive = false
    val resolutionResult: ResolutionResult = templateVersionConfiguration.incoming.resolutionResult
    val matches: List<ResolvedComponentResult> = resolutionResult.allComponents.filter { it != resolutionResult.root }
    if (matches.isEmpty()) {
        throw GradleException("Could not locate any matches for $notation")
    }
    matches.forEach { dep -> dest[name] = (dep.id as ModuleComponentIdentifier).version }
}

val devSuffixes = arrayOf(
    "-SNAP\\d+",
    "-SNAPSHOT",
    "-alpha-?\\d+",
    "-beta-?\\d+",
    "-dev-?\\d+",
    "-rc-?\\d+",
    "-RC-?\\d+",
    "-M\\d+",
    "-eap-?\\d+"
)
