import org.gradle.gradlebuild.unittestandcompile.ModuleType

/*
 * Copyright 2014 the original author or authors.
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
    `java-library`
    // Cannot use strict compile because JDK 7 doesn't recognize
    // @SuppressWarnings("deprecation"), used in org.gradle.internal.resource.transport.http.HttpClientHelper.AutoClosedHttpResponse
    // in the context of a delegation pattern
    // gradlebuild.`strict-compile`
    gradlebuild.classycle
}

dependencies {
    api(project(":resources"))
    api(project(":baseServices"))
    api(project(":core"))
    api(library("commons_httpclient"))

    implementation(library("slf4j_api"))
    implementation(library("jcl_to_slf4j"))
    implementation(library("jcifs"))
    implementation(library("guava"))
    implementation(library("commons_lang"))
    implementation(library("commons_io"))
    implementation(library("xerces"))
    implementation(library("nekohtml"))

    testImplementation(testLibrary("jetty"))

    testFixturesImplementation(project(":internalIntegTesting"))
}

gradlebuildJava {
    moduleType = ModuleType.CORE
}

testFixtures {
    from(":core")
    from(":logging")
}
