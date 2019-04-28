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

import org.gradle.cache.internal.CrossBuildInMemoryCacheFactory;
import org.gradle.internal.instantiation.DefaultInstantiatorFactory;
import org.gradle.internal.instantiation.InjectAnnotationHandler;
import org.gradle.internal.instantiation.InstantiatorFactory;
import org.gradle.internal.service.DefaultServiceRegistry;
import org.gradle.internal.service.ServiceRegistry;
import org.gradle.internal.service.ServiceRegistryBuilder;
import org.gradle.internal.service.scopes.WorkerSharedGlobalScopeServices;

import javax.inject.Inject;
import java.util.Collections;

public class WorkerDaemonServer extends DefaultWorkerServer {
    @Inject
    public WorkerDaemonServer(ServiceRegistry serviceRegistry) {
        super(createWorkerDaemonServices(serviceRegistry));
    }

    static ServiceRegistry createWorkerDaemonServices(ServiceRegistry parent) {
        ServiceRegistry workerSharedGlobalServices = ServiceRegistryBuilder.builder()
                .parent(parent)
                .provider(new WorkerSharedGlobalScopeServices())
                .build();
        return new WorkerDaemonServices(workerSharedGlobalServices);
    }

    @Override
    public DefaultWorkResult execute(ActionExecutionSpec spec) {
        try {
            return super.execute(spec);
        } catch (Throwable t) {
            return new DefaultWorkResult(true, t);
        }
    }

    @Override
    public String toString() {
        return "WorkerDaemonServer{}";
    }

    private static class WorkerDaemonServices extends DefaultServiceRegistry {
        public WorkerDaemonServices(ServiceRegistry... parents) {
            super("WorkerDaemonServices", parents);
        }

        InstantiatorFactory createInstantiatorFactory(CrossBuildInMemoryCacheFactory cacheFactory) {
            return new DefaultInstantiatorFactory(cacheFactory, Collections.<InjectAnnotationHandler>emptyList());
        }

        WorkerDaemonServices createWorkerDaemonServices() {
            return this;
        }
    }
}
