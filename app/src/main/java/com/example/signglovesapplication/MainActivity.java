package com.example.signglovesapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private Button startButton;
    private Button learnButton;
    private TextView debuggingText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startButton = findViewById(R.id.startPatternMode);
        learnButton = findViewById(R.id.learningMode);
        debuggingText = findViewById(R.id.debuggingText);

        startButton.setOnClickListener(view -> {
            Intent intent = new Intent(this, StartMode.class);
            startActivity(intent);
        });

    }
}