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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

class Summarizer {

    @Nullable
    private final String act;
    @Nullable
    private final String sceneId;
    @Nullable
    private final String scene;
    @Nullable
    private final Integer speechId;
    @Nullable
    private final String speech;
    private final List<Script> scripts;

    Summarizer() {
        this.act = null;
        this.sceneId = null;
        this.scene = null;
        this.speechId = null;
        this.speech = null;
        this.scripts = new ArrayList<>();
    }

    Summarizer(@NotNull String act, @NotNull List<Script> scripts) {
        this(act, null, null, null, null, scripts);
    }

    Summarizer(@NotNull String act, @NotNull String sceneId, @NotNull String scene, @NotNull List<Script> scripts) {
        this(act, sceneId, scene, null, null, scripts);
    }

    Summarizer(@Nullable String act, @Nullable String sceneId, @Nullable String scene, @Nullable Integer speechId,
               @Nullable String speech,
               List<Script> scripts) {
        this.act = act;
        this.sceneId = sceneId;
        this.scene = scene;
        this.speechId = speechId;
        this.speech = speech;
        this.scripts = scripts;
    }

    private static final Pattern pattern = Pattern.compile("^\\p{Digit}+\\.\\p{Digit}+\\.\\p{Digit}+$");

    Summarizer newNextLine(final String line) {
        if (line.startsWith("<h3>")) {
            final String text = line.replace("<h3>", "").replace("</h3>", "");
            if (text.startsWith("ACT")) {
                final String act = text.replace("ACT", "").trim();
                return new Summarizer(act, scripts);
            } else if (text.startsWith("SCENE ")) {
                final String mid = text.replace("SCENE", "").trim();
                final String sceneId = mid.substring(0, mid.indexOf('.'));
                final String scene = mid.substring(mid.indexOf('.') + 1).trim();
                if (act == null) {
                    throw new IllegalStateException("act is not defined.");
                }
                return new Summarizer(act, sceneId, scene, scripts);
            }
            throw new IllegalStateException("Cannot parse script. [" + line + "]");
        } else if (line.startsWith("<i>")) {
            return this;
        } else if (line.trim().isEmpty()) {
            return this;
        } else if (line.startsWith("<A")) {
            final String[] script = line.replace("<A", "").replace("</A>", "").trim().split(">");
            final String[] id = script[0].split("=");
            if (id[1].contains("speech")) {
                final int speechId = Integer.parseInt(id[1].replace("speech", ""));
                final String speech = script[1];
                return new Summarizer(act, sceneId, scene, speechId, speech, scripts);
            } else if (pattern.matcher(id[1]).matches()) {
                final String textId = id[1];
                final String text = script[1];
                scripts.add(new Script(act, sceneId, scene, speechId, speech, textId, text));
                return new Summarizer(act, sceneId, scene, speechId, speech, scripts);
            }
        }
        throw new IllegalStateException("Cannot parse script. [" + line + "]");
    }

    Stream<Script> scriptsAsStream() {
        return scripts
                .stream();
    }
}
