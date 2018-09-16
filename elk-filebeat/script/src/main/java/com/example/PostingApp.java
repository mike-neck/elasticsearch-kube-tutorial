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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;

import static org.asynchttpclient.Dsl.asyncHttpClient;

import javafx.geometry.Pos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.function.Function;
import java.util.stream.Stream;

public class PostingApp<R> implements AutoCloseable, Runnable {

    private static final Logger logger = LoggerFactory.getLogger(PostingApp.class);

    private static final String MACBETH = "macbeth.txt";

    private static Stream<String> macbethText() {
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();
        final URL resource = loader.getResource(MACBETH);
        if (resource == null) {
            return Stream.empty();
        }
        final InputStream input = loader.getResourceAsStream(MACBETH);
        return new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8)).lines();
    }

    private static Summarizer loadMacbethText() {
        final Summarizer summarizer = new Summarizer();
        try (final Stream<String> fileContents = macbethText()) {
            return fileContents.reduce(summarizer, Summarizer::newNextLine, (l, r) -> l);
        }
    }

    public static void main(String[] args) throws Exception {
        final Summarizer summarizer = loadMacbethText();
        final ObjectMapper objectMapper = new ObjectMapper()
                .setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
        final Function<Script, String> toJson = script -> {
            try {
                return objectMapper.writeValueAsString(script);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        };
        final Random random = new Random();
        try (final PostingApp<String> app = new PostingApp<>(summarizer, () -> logger.info("close"),
                script -> Mono.delay(Duration.ofMillis((long) random.nextInt(500)))
                        .<Script>map(i -> script)
                        .<String>map(toJson))) {
            app.run();
        }
    }

    private final Scheduler textDelay = Schedulers.newElastic("text-delay");

    private final Summarizer summarizer;
    private final Function<Script, ? extends Mono<R>> client;
    private final AutoCloseable resource;

    private PostingApp(Summarizer summarizer, final AutoCloseable resource, Function<Script, ? extends Mono<R>> client) {
        this.summarizer = summarizer;
        this.resource = resource;
        this.client = client;
    }

    @SuppressWarnings("EmptyTryBlock")
    @Override
    public void close() throws Exception {
        try (final AutoCloseable ignore = resource) {
            textDelay.dispose();
        }
    }

    @Override
    public void run() {
        final CountDownLatch countDownLatch = new CountDownLatch(1);

        final Disposable disposable = Flux.fromStream(summarizer::scriptsAsStream)
                .delayElements(Duration.ofMillis(200L))
                .doOnComplete(countDownLatch::countDown)
                .doOnError(throwable -> {
                    logger.warn("Error", throwable);
                    countDownLatch.countDown();
                })
                .doOnCancel(countDownLatch::countDown)
                .concatMap(client)
                .subscribe(location -> logger.info("url: {}", location));

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        disposable.dispose();
    }
}
