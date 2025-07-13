package com.example.sudokuzenith;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;

import java.util.HashMap;

public class SoundManager {

    private final SoundPool soundPool;
    private final HashMap<String, Integer> soundMap = new HashMap<>();

    public SoundManager(Context context) {
        AudioAttributes attrs = new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_GAME)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(4)
                .setAudioAttributes(attrs)
                .build();

        soundMap.put("click", soundPool.load(context, R.raw.click, 1));
        soundMap.put("error", soundPool.load(context, R.raw.error, 1));
        soundMap.put("hint", soundPool.load(context, R.raw.hint, 1));
        soundMap.put("win", soundPool.load(context, R.raw.win, 1));
    }

    public void play(String sound) {
        Integer soundId = soundMap.get(sound);
        if (soundId != null) {
            soundPool.play(soundId, 1f, 1f, 0, 0, 1f);
        }
    }

    public void release() {
        soundPool.release();
    }
}
