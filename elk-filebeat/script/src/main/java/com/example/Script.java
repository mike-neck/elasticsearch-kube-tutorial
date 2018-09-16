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

class Script {
    private final String act;
    private final String sceneId;
    private final String scene;
    private final int speechId;
    private final String speech;
    private final String textId;
    private final String text;

    Script(String act, String sceneId, String scene, int speechId, String speech, String textId, String text) {
        this.act = act;
        this.sceneId = sceneId;
        this.scene = scene;
        this.speechId = speechId;
        this.speech = speech;
        this.textId = textId;
        this.text = text;
    }

    public String getAct() {
        return act;
    }

    public String getSceneId() {
        return sceneId;
    }

    public String getScene() {
        return scene;
    }

    public int getSpeechId() {
        return speechId;
    }

    public String getSpeech() {
        return speech;
    }

    public String getTextId() {
        return textId;
    }

    public String getText() {
        return text;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Script{");
        sb.append("act='").append(act).append('\'');
        sb.append(", sceneId='").append(sceneId).append('\'');
        sb.append(", scene='").append(scene).append('\'');
        sb.append(", speechId=").append(speechId);
        sb.append(", speech='").append(speech).append('\'');
        sb.append(", textId='").append(textId).append('\'');
        sb.append(", text='").append(text).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
