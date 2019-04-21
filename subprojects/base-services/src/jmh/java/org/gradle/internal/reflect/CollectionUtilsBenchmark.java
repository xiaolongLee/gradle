/*
 * Copyright 2019 the original author or authors.
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
package org.gradle.internal.reflect;

import com.google.common.collect.Sets;
import org.gradle.api.Transformer;
import org.gradle.util.CollectionUtils;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Benchmark                                    (size)   Mode  Cnt         Score        Error  Units
 * CollectionUtilsBenchmark.collect_collection      10  thrpt   20  17594714.598 ±  93418.600  ops/s
 * CollectionUtilsBenchmark.collect_collection     100  thrpt   20   1879849.351 ±  81698.302  ops/s
 * CollectionUtilsBenchmark.collect_collection    1000  thrpt   20    194278.105 ±   5444.248  ops/s
 * CollectionUtilsBenchmark.collect_iterable        10  thrpt   20  17400146.689 ± 159109.940  ops/s
 * CollectionUtilsBenchmark.collect_iterable       100  thrpt   20   2044732.571 ±  22123.885  ops/s
 * CollectionUtilsBenchmark.collect_iterable      1000  thrpt   20    206728.343 ±   2648.332  ops/s
 * CollectionUtilsBenchmark.collect_set             10  thrpt   20   9334146.882 ±  52347.921  ops/s
 * CollectionUtilsBenchmark.collect_set            100  thrpt   20    585551.459 ±  15429.135  ops/s
 * CollectionUtilsBenchmark.collect_set           1000  thrpt   20     62993.322 ±   1863.450  ops/s
 **/
@Fork(2)
@State(Scope.Benchmark)
@Warmup(iterations = 10, time = 1, timeUnit = SECONDS)
@Measurement(iterations = 10, time = 1, timeUnit = SECONDS)
public class CollectionUtilsBenchmark {
    @Param({"10", "100", "1000"})
    int size;

    Iterable<Integer> iterable;
    Collection<Integer> collection;
    Set<Integer> set;

    @Setup
    public void setup() {
        List<Integer> values = new ArrayList<Integer>(size);
        for (int i = 0; i < size; i++) {
            values.add(i);
        }
        iterable = values;
        collection = values;
        set = Sets.newHashSet(values);
    }

    @Benchmark
    public Object collect_iterable() {
        return CollectionUtils.collect(iterable, new Transformer<Integer, Integer>() {
            @Override
            public Integer transform(Integer i) {
                return i;
            }
        });
    }

    @Benchmark
    public Object collect_collection() {
        return CollectionUtils.collect(collection, new Transformer<Integer, Integer>() {
            @Override
            public Integer transform(Integer i) {
                return i;
            }
        });
    }

    @Benchmark
    public Object collect_set() {
        return CollectionUtils.collect(set, new Transformer<Integer, Integer>() {
            @Override
            public Integer transform(Integer i) {
                return i;
            }
        });
    }
}
