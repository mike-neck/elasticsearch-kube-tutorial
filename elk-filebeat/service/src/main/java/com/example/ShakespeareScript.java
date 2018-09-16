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

public class ShakespeareScript {

    public String toLogString() {
        return String.format("id: %s\tscene: %s\tperson: %s\ttext: %s", textId.toLogString(), scene, speech, text);
    }

    private String act;
    private String sceneId;
    private String scene;
    private int speechId;
    private String speech;
    private TextId textId;
    private String text;

    public ShakespeareScript(String act, String sceneId, String scene, int speechId, String speech, TextId textId,
                             String text) {
        this.act = act;
        this.sceneId = sceneId;
        this.scene = scene;
        this.speechId = speechId;
        this.speech = speech;
        this.textId = textId;
        this.text = text;
    }

    public ShakespeareScript() {
    }

    public String getAct() {
        return act;
    }

    public void setAct(String act) {
        this.act = act;
    }

    public String getSceneId() {
        return sceneId;
    }

    public void setSceneId(String sceneId) {
        this.sceneId = sceneId;
    }

    public String getScene() {
        return scene;
    }

    public void setScene(String scene) {
        this.scene = scene;
    }

    public int getSpeechId() {
        return speechId;
    }

    public void setSpeechId(int speechId) {
        this.speechId = speechId;
    }

    public String getSpeech() {
        return speech;
    }

    public void setSpeech(String speech) {
        this.speech = speech;
    }

    public TextId textId() {
        return textId;
    }

    public void textId(TextId textId) {
        this.textId = textId;
    }

    public String getTextId() {
        return textId.originalString();
    }

    public void setTextId(final String original) {
        this.textId = TextId.fromString(original);
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
