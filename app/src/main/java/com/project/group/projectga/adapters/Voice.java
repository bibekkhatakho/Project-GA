package com.project.group.projectga.adapters;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import java.util.Locale;

/**
 * Created by Chris on 7/12/2017.
 */

public class Voice {

    private TextToSpeech tts;
    private int id = 0;
    public Context context;

    public Voice(Context context) {
        tts = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    tts.setLanguage(Locale.US);
                    tts.setSpeechRate(0.3f);
                }
            }
        });
        this.context = context;
    }

    public void say(String words) {
        tts.speak(words, TextToSpeech.QUEUE_ADD, null, Integer.toString(id++));
    }
    public void stop() {
        tts.stop();
    }

    public void shutDown(){
        if(tts !=null){
            tts.stop();
            tts.shutdown();
        }
    }

    public void playSilence(){
        tts.playSilentUtterance(300, TextToSpeech.QUEUE_ADD, null);
    }

    public boolean isSpeaking() {
        return tts.isSpeaking();
    }

    @Override
    protected void finalize() throws Throwable {
        if(tts !=null){
            tts.stop();
            tts.shutdown();
        }
        super.finalize();
    }

}