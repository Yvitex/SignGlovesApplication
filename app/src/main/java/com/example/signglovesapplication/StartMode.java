package com.example.signglovesapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.List;
import java.util.Locale;
import java.util.Set;

public class StartMode extends AppCompatActivity {

    private TextToSpeech textToSpeech;
    private Button speakButton;
    private TextView dataLog;
    private EditText dataInput;


    int result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_mode);

        dataInput = findViewById(R.id.processedWord);
        dataLog = findViewById(R.id.dataStreamText);
        speakButton = findViewById(R.id.speakButton);


        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if (i != TextToSpeech.ERROR) {
                    result = textToSpeech.setLanguage(Locale.ENGLISH);
                } else {
                    Toast.makeText(StartMode.this, "Failed to initialize Text to Speech", Toast.LENGTH_LONG).show();
                }

                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Toast.makeText(StartMode.this, "Tagalog language not supported", Toast.LENGTH_SHORT).show();
                }
            }
        });

        speakButton.setOnClickListener(view -> {
            String text = dataInput.getText().toString().trim();
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        });


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Shutdown TextToSpeech engine
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }
}