package com.recursivepenguin.bluetoothmanagerforglass;

import android.content.Context;
import android.media.MediaPlayer;

public class Util {

    public static void playSound(Context context, int resource) {
        MediaPlayer mp = MediaPlayer.create(context, resource);
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.release();
            }

        });
        mp.start();
    }

}
