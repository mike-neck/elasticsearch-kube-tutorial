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
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class FluxApp {

    private static final Logger logger = LoggerFactory.getLogger(FluxApp.class);

    public static void main(String[] args) throws Exception {
        final Random random = new Random();
        final CountDownLatch latch = new CountDownLatch(1);

        final Stream<String> stream = IntStream.range(0, 100)
                .mapToObj(Integer::toHexString);
        final Disposable disposable = Flux.fromStream(() -> stream)
                .concatMap(hex -> Mono
                        .delay(Duration.ofMillis(((long) random.nextInt(200))))
                        .map(i -> hex))
                .doOnComplete(latch::countDown)
                .doOnError(th -> {
                    logger.warn("error", th);
                    latch.countDown();
                })
                .doOnCancel(latch::countDown)
                .subscribe(logger::info);
        latch.await();
    }
}
