package com.example.streamingapplication;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class ChannelNameActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channel_name);
    }

    public void onEnter(View view) {
        EditText channelName = findViewById(R.id.channelName);
        Intent i = getIntent();
        Bundle b = i.getExtras();
        String port = String.valueOf(b.get("port"));
        Intent ii = new Intent(ChannelNameActivity.this, OptionsActivity.class);
        ii.putExtra("port", port);
        ii.putExtra("channelName", channelName.getText().toString());
        startActivity(ii);
    }

}