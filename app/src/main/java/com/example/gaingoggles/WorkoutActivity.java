package com.example.gaingoggles;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.VideoView;

import java.io.File;

public class WorkoutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_video);
        final VideoView view = findViewById(R.id.videoView);
        Intent intent = getIntent();
//        Uri uri = Uri.parse(intent.getStringExtra(MainActivity.EXTRA_FILE));
        view.setVideoPath(intent.getStringExtra(MainActivity.EXTRA_FILE));
        view.start();
        view.setOnCompletionListener ( new MediaPlayer.OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                view.start();
            }
        });
    }
}
