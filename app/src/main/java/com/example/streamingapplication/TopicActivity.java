package com.example.streamingapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.util.ArrayList;

public class TopicActivity extends AppCompatActivity {

    Consumer consumer;

    public ArrayList<String> fileArray = new ArrayList<String>();
    ArrayAdapter<String> fileAdapter;
    ListView filesView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topic);
        Intent i = getIntent();
        Bundle b = i.getExtras();
        consumer = i.getParcelableExtra("consumer");
        String topic = String.valueOf(b.get("topic"));
        Address addr = (Address) b.get("addr");
        consumer.addr = addr;
        TextView topicName = findViewById(R.id.topicName);
        topicName.setText(topic);
        consumer.showConversationData(topic);
        Log.d("myip" , String.valueOf(consumer.addr));

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        File folder = new File("/storage/emulated/0/Download/");
        File[] files = folder.listFiles();
        fileArray.clear();
        for (File file : files) {
            if (file.getPath().contains(topic)) {
                fileArray.add(file.getPath().replace("/storage/emulated/0/Download/", ""));
            }
        }
        fileAdapter = new ArrayAdapter<>(TopicActivity.this, R.layout.activity_listview, fileArray);
        filesView = findViewById(R.id.files_list);
        filesView.setAdapter(fileAdapter);

        filesView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                if (fileArray.get(i).endsWith("jpg") || fileArray.get(i).endsWith("png")){
                    intent.setDataAndType(Uri.parse("file://" + "/storage/emulated/0/Download/" + fileArray.get(i)), "image/*");
                }
                else if (fileArray.get(i).endsWith("mp4")) {
                    intent.setDataAndType(Uri.parse("file://" + "/storage/emulated/0/Download/" + fileArray.get(i)), "video/*");
                }
                else if (fileArray.get(i).endsWith("txt")) {
                    intent.setDataAndType(Uri.parse("file://" + "/storage/emulated/0/Download/" + fileArray.get(i)), "*/*");
                }
                startActivity(intent);
            }
        });

    }

}