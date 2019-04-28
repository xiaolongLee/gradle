/*
 * Copyright 2017 the original author or authors.
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

package org.gradle.workers.internal;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.Sets;
import org.gradle.process.JavaForkOptions;
import org.gradle.process.internal.JavaForkOptionsInternal;

import java.io.File;
import java.util.Set;

public class DaemonForkOptions {
    private final JavaForkOptionsInternal forkOptions;
    // TODO classpath will be replaced by classLoaderStructure once we make worker daemons match isolated workers
    private final Iterable<File> classpath;
    private final Iterable<String> sharedPackages;
    private final KeepAliveMode keepAliveMode;
    private final ClassLoaderStructure classLoaderStructure;

    DaemonForkOptions(JavaForkOptionsInternal forkOptions, Iterable<File> classpath,
                      Iterable<String> sharedPackages, KeepAliveMode keepAliveMode,
                      ClassLoaderStructure classLoaderStructure) {
        this.forkOptions = forkOptions;
        this.classpath = classpath;
        this.sharedPackages = sharedPackages;
        this.keepAliveMode = keepAliveMode;
        this.classLoaderStructure = classLoaderStructure;
    }

    public Iterable<File> getClasspath() {
        return classpath;
    }

    public Iterable<String> getSharedPackages() {
        return sharedPackages;
    }

    public KeepAliveMode getKeepAliveMode() {
        return keepAliveMode;
    }

    public JavaForkOptions getJavaForkOptions() {
        return forkOptions;
    }

    public ClassLoaderStructure getClassLoaderStructure() {
        return classLoaderStructure;
    }

    public boolean isCompatibleWith(DaemonForkOptions other) {
        return forkOptions.isCompatibleWith(other.forkOptions)
                && getNormalizedClasspath(classpath).containsAll(getNormalizedClasspath(other.getClasspath()))
                && getNormalizedSharedPackages(sharedPackages).containsAll(getNormalizedSharedPackages(other.sharedPackages))
                && keepAliveMode == other.getKeepAliveMode()
                && Objects.equal(classLoaderStructure, other.getClassLoaderStructure());
    }

    // one way to merge fork options, good for current use case
    public DaemonForkOptions mergeWith(DaemonForkOptions other) {
        if (keepAliveMode != other.getKeepAliveMode()) {
            throw new IllegalArgumentException("Cannot merge a fork options object with a different keep alive mode (this: " + keepAliveMode + ", other: " + other.getKeepAliveMode() + ").");
        }

        if (!Objects.equal(classLoaderStructure, other.getClassLoaderStructure())) {
            throw new IllegalArgumentException("Cannot merge a fork options object with a different value for classloader hierarchy.");
        }

        Set<File> mergedClasspath = getNormalizedClasspath(classpath);
        mergedClasspath.addAll(getNormalizedClasspath(other.classpath));
        Set<String> mergedAllowedPackages = getNormalizedSharedPackages(sharedPackages);
        mergedAllowedPackages.addAll(getNormalizedSharedPackages(other.sharedPackages));

        return new DaemonForkOptions(forkOptions.mergeWith(other.forkOptions), mergedClasspath, mergedAllowedPackages, keepAliveMode, classLoaderStructure);
    }

    private Set<File> getNormalizedClasspath(Iterable<File> classpath) {
        return Sets.newLinkedHashSet(classpath);
    }

    private Set<String> getNormalizedSharedPackages(Iterable<String> allowedPackages) {
        return Sets.newLinkedHashSet(allowedPackages);
    }

    public String toString() {
        return MoreObjects.toStringHelper(this).add("executable", forkOptions.getExecutable()).add("minHeapSize", forkOptions.getMinHeapSize()).add("maxHeapSize", forkOptions.getMaxHeapSize()).add("jvmArgs", forkOptions.getJvmArgs()).add("classpath", classpath).add("keepAliveMode", keepAliveMode).toString();
    }
}
