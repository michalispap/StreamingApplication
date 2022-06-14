package com.example.streamingapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class TopicActivity extends AppCompatActivity {

    Consumer consumer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("bug", "mpika kanonika");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topic);
        Intent i = getIntent();
        //consumer = (Consumer) i.getSerializableExtra("consumer");
        Bundle b = i.getExtras();
        String topic = String.valueOf(b.get("topic"));
        TextView topicName = findViewById(R.id.topicName);
        topicName.setText(topic);
        //Toast.makeText(this, consumer.addr.getIp(), Toast.LENGTH_SHORT).show();
    }
}