package com.example.streamingapplication;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onEnter(View view) {
        EditText editText = findViewById(R.id.portNum);
        int portNum = Integer.parseInt(String.valueOf(editText.getText()));
        new Task().execute(portNum);
    }

    public class Task extends AsyncTask<Integer, Void, Void> {
        int port;

        @Override
        protected Void doInBackground(Integer... ints) {
            try {
                port = ints[0];
            }
            catch(Exception e) {
                e.getStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            Intent intent = new Intent(MainActivity.this, ChannelNameActivity.class);
            intent.putExtra("port", port);
            startActivity(intent);
        }

    }

}
