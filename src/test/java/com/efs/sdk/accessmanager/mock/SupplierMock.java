/*
Copyright (C) 2023 e:fs TechHub GmbH (sdk@efs-techhub.com)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */
package com.efs.sdk.accessmanager.mock;

import com.azure.core.http.rest.PagedResponse;
import com.azure.core.util.IterableStream;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Supplier;

public class SupplierMock<T> implements Supplier<Mono<PagedResponse<T>>> {

    private final PagedResponse<T> response;

    public SupplierMock(T object) {

        this.response = new PagedResponseMock<>() {
            @Override
            public IterableStream<T> getElements() {
                return new IterableStream<>(List.of(object));
            }
        };

    }

    @Override
    public Mono<PagedResponse<T>> get() {
        return Mono.just(response);
    }

}