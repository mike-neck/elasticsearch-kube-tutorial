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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;
import java.util.stream.Stream;

public class ShakespeareScriptParseTest {

    private static Stream<String> load(final String file) {
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        final URL resource = classLoader.getResource(file);
        if (resource == null) {
            return Stream.empty();
        }
        final InputStream stream = classLoader.getResourceAsStream(file);
        final InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
        return new BufferedReader(reader).lines();
    }

    @Test
    void run() {
        final ObjectMapper objectMapper = new ObjectMapper()
                .setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
        final Function<Script, String> toJson = script -> {
            try {
                return objectMapper.writeValueAsString(script);
            } catch (JsonProcessingException e) {
                throw new UncheckedIOException(e);
            }
        };
        try (final Stream<String> stream = load("macbeth.txt")) {
            final Summarizer summarizer = stream.reduce(new Summarizer(), Summarizer::newNextLine, (l, r) -> l);
            summarizer.scriptsAsStream()
                    .map(toJson)
                    .forEach(System.out::println);
        }
    }

}
