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

import java.util.regex.Pattern;

public class TextId {

    public static TextId fromSeparateString(String act, String scene, String line) {
        return textId(act, scene, line);
    }

    public String toLogString() {
        return originalString();
    }

    private final int act;
    private final int scene;
    private final int line;

    public TextId(int act, int scene, int line) {
        this.act = act;
        this.scene = scene;
        this.line = line;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TextId)) return false;

        TextId textId = (TextId) o;

        if (act != textId.act) return false;
        if (scene != textId.scene) return false;
        return line == textId.line;
    }

    @Override
    public int hashCode() {
        int result = act;
        result = 31 * result + scene;
        result = 31 * result + line;
        return result;
    }

    @Override
    public String toString() {
        return String.format("%d/%d/%d", act, scene, line);
    }

    public String originalString() {
        return String.format("%d.%d.%d", act, scene, line);
    }

    private static final Pattern pattern = Pattern.compile("^\\p{Digit}+\\.\\p{Digit}+\\.\\p{Digit}+$");

    public static TextId fromString(final String original) {
        if (!pattern.matcher(original).matches()) {
            throw new IllegalArgumentException(String.format("Cannot parse input string[%s].", original));
        }
        final String[] digits = original.split("\\.");
        return textId(digits);
    }

    private static TextId textId(final String... digits) {
        return new TextId(Integer.parseInt(digits[0]), Integer.parseInt(digits[1]), Integer.parseInt(digits[2]));
    }

    public int getAct() {
        return act;
    }

    public int getScene() {
        return scene;
    }

    public int getLine() {
        return line;
    }
}
