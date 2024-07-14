package com.example.grabadora;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_PERMISSIONS = 1001;
    private String[] permissions = {Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private boolean isRecording = false;
    private MediaRecorder mediaRecorder;
    private String fileName;
    private ListView recordingsListView;
    private ArrayAdapter<String> recordingsAdapter;
    private List<String> recordingsList;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button startButton = findViewById(R.id.startButton);
        Button stopButton = findViewById(R.id.stopButton);
        recordingsListView = findViewById(R.id.recordingsListView);

        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE_PERMISSIONS);
        }

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isRecording) {
                    startRecording();
                } else {
                    Toast.makeText(MainActivity.this, "Already recording", Toast.LENGTH_SHORT).show();
                }
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isRecording) {
                    stopRecording();
                    loadRecordings();
                } else {
                    Toast.makeText(MainActivity.this, "Not recording", Toast.LENGTH_SHORT).show();
                }
            }
        });

        recordingsList = new ArrayList<>();
        recordingsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, recordingsList);
        recordingsListView.setAdapter(recordingsAdapter);
        recordingsListView.setOnItemClickListener((parent, view, position, id) -> {
            String recordingPath = recordingsList.get(position);
            playRecording(recordingPath);
        });

        loadRecordings();
    }

    private boolean allPermissionsGranted() {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (!allPermissionsGranted()) {
                Toast.makeText(this, "Permissions not granted", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void startRecording() {
        fileName = getExternalCacheDir().getAbsolutePath() + "/audio_record_" + System.currentTimeMillis() + ".3gp";
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mediaRecorder.setOutputFile(fileName);

        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
            isRecording = true;
            Toast.makeText(this, "Recording started", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Log.e("MainActivity", "startRecording: ", e);
        }
    }

    private void stopRecording() {
        mediaRecorder.stop();
        mediaRecorder.release();
        mediaRecorder = null;
        isRecording = false;
        Toast.makeText(this, "Recording stopped", Toast.LENGTH_SHORT).show();
    }

    private void loadRecordings() {
        recordingsList.clear();
        File[] files = getExternalCacheDir().listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.getName().endsWith(".3gp")) {
                    recordingsList.add(file.getAbsolutePath());
                }
            }
        }
        recordingsAdapter.notifyDataSetChanged();
    }

    private void playRecording(String filePath) {
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }

        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(filePath);
            mediaPlayer.prepare();
            mediaPlayer.start();
            Toast.makeText(this, "Playing recording", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Log.e("MainActivity", "playRecording: ", e);
        }
    }
}
