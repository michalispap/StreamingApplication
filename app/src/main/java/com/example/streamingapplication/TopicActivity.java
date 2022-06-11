package com.example.streamingapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class TopicActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topic);
        Intent i = getIntent();
        Bundle b = i.getExtras();
        String topic = String.valueOf(b.get("topic"));
        TextView topicName = findViewById(R.id.topicName);
        topicName.setText(topic);
    }
}