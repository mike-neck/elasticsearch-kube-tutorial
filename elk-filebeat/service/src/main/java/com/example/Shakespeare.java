/*
 * Copyright 2018 Shinya Mochida
 *
 * Licensed under the Apache License,Version2.0(the"License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,software
 * Distributed under the License is distributed on an"AS IS"BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@Component
public class Shakespeare {

    private static final Logger logger = LoggerFactory.getLogger(Shakespeare.class);

    private static final Map<TextId, ShakespeareScript> scripts = new HashMap<>();

    private final Consumer<ShakespeareScript> saveScriptAction = this::saveScript;

    private final Consumer<ShakespeareScript> logAction = this::log;

    void saveScript(final ShakespeareScript script) {
        scripts.put(script.textId(), script);
    }

    private void log(final ShakespeareScript script) {
        logger.info(script.toLogString());
    }

    Mono<ServerResponse> newLine(final ServerRequest request) {
        return request.bodyToMono(ShakespeareScript.class)
                .doOnNext(saveScriptAction.andThen(logAction))
                .flatMap(script ->
                        ServerResponse.created(
                                URI.create(String.format("/macbeth/%s", script.textId())))
                            .body(Mono.empty(), Void.class));
    }

    Mono<ServerResponse> findScript(final ServerRequest request) {
        final String act = request.pathVariable("act");
        final String scene = request.pathVariable("scene");
        final String line = request.pathVariable("line");

        final TextId textId = TextId.fromSeparateString(act, scene, line);
        
        final Mono<ShakespeareScript> script = Mono.justOrEmpty(scripts.get(textId));
        return script
                .flatMap(sc -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(Mono.just(sc), ShakespeareScript.class))
                .switchIfEmpty(ServerResponse.notFound().build());
    }
}
