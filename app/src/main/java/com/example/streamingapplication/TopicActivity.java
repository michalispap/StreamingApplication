package com.example.streamingapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class TopicActivity extends AppCompatActivity {

    Consumer consumer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topic);
        Intent i = getIntent();
        Bundle b = i.getExtras();
        consumer = i.getParcelableExtra("consumer");
        String topic = String.valueOf(b.get("topic"));
        TextView topicName = findViewById(R.id.topicName);
        topicName.setText(topic);
        consumer.showConversationData(topic); //error in Broker (address is null)
    }
}