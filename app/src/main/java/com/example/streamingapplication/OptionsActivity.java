package com.example.streamingapplication;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.FileObserver;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

public class OptionsActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    String item;
    private static final int GALLERY_PICK_CODE = 1000;
    private static final int PERMISSION_CODE = 1001;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_VIDEO_CAPTURE = 2;
    int port;
    String channel;
    ArrayAdapter<String> listAdapter;
    ListView listView;

    Publisher pub;
    Consumer cons;

    FileObserver fileObserver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);
        Intent i = getIntent();
        Bundle b = i.getExtras();
        TextView channelName = findViewById(R.id.channelString);
        port = Integer.parseInt((String) b.get("port"));
        TextView portNumber = findViewById(R.id.portNumber);
        portNumber.setText((String) b.get("port"));
        channelName.setText((String) b.get("channelName"));
        channel = (String) b.get("channelName");
        Spinner spinner = findViewById(R.id.options_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.options_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
        new InitTask().execute();

        fileObserver = new FileObserver("/storage/emulated/0/Download/") {
            @Override
            public void onEvent(int i, @Nullable String path) {
                if((path != null) && (i == 256)) {
                    runOnUiThread(new Runnable() {
                        public void run()
                        {
                            Toast.makeText(OptionsActivity.this, "New file added to your registered topics!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        };
        fileObserver.startWatching();

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
         item = parent.getItemAtPosition(position).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
    }

    public void openText(View view) {
        EditText message = findViewById(R.id.message);
        Button textButton = findViewById(R.id.textBtn);
        String ButtonText = textButton.getText().toString();
        if (ButtonText.equals("Text")) {
            message.setVisibility(View.VISIBLE);
            textButton.setText("Send");
        }
        else if (ButtonText.equals("Send")) {
            Toast.makeText(this, message.getText().toString(), Toast.LENGTH_SHORT).show();

            //send text
            Date dateCreated = new Date();
            ArrayList<String> hashTags = new ArrayList();
            hashTags.add("android_topic"); //temporary (testing)
            pub.setFileCollection(message.getText().toString(), hashTags);
            pub.sendFile(message.getText().toString(), hashTags, dateCreated);

            message.setVisibility(View.GONE);
            textButton.setText("Text");
        }
    }

    public void openGallery(View view) {
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE};
            requestPermissions(permissions, PERMISSION_CODE);
        }
        else {
            pickFromGallery();
        }
    }

    public void openCamera(View view) {
        if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
            String[] permissions = {Manifest.permission.CAMERA};
            requestPermissions(permissions, REQUEST_IMAGE_CAPTURE);
        }
        else {
            takePhoto();
        }
    }

    public void openVideoCamera(View view) {
        if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
            String[] permissions = {Manifest.permission.CAMERA};
            requestPermissions(permissions, REQUEST_VIDEO_CAPTURE);
        }
        else {
            takeVideo();
        }
    }

    public void takePhoto() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
    }

    public void takeVideo() {
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
    }

    public void pickFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("*/*");
        startActivityForResult(intent, GALLERY_PICK_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    pickFromGallery();
                }
                else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                }
                break;
            }
            case REQUEST_IMAGE_CAPTURE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    takePhoto();
                }
                else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                }
                break;
            }
            case REQUEST_VIDEO_CAPTURE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    takeVideo();
                }
                else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && (requestCode == GALLERY_PICK_CODE || requestCode == REQUEST_IMAGE_CAPTURE || requestCode == REQUEST_VIDEO_CAPTURE)) {
            Toast.makeText(this, "Success!", Toast.LENGTH_SHORT).show();
        }

        if (resultCode == RESULT_OK && (requestCode == GALLERY_PICK_CODE)) {
            //store in File
            Uri uri = data.getData();
            String path = getPath(uri);
            File file = new File(path);
            Toast.makeText(this, path, Toast.LENGTH_SHORT).show();
        }
    }

    public String getPath(Uri uri) {
        Cursor cursor = null;
        try {
            String[] projection = {MediaStore.Images.Media.DATA};
            cursor = getContentResolver().query(uri, projection, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(projection[0]);
            String filePath = cursor.getString(columnIndex);
            BitmapFactory.decodeFile(filePath);
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public void Choice(View view) {
        Button textButton = findViewById(R.id.textBtn);
        Button multimediaButton = findViewById(R.id.galleryBtn);
        Button cameraButton = findViewById(R.id.cameraBtn);
        Button cameraVideoButton = findViewById(R.id.cameraVideoBtn);
        Button topicsButton = findViewById(R.id.topicsBtn);
        ListView topicsList = findViewById(R.id.topics_list);
        Button registerButton = findViewById(R.id.registerBtn);
        Button viewDataButton = findViewById(R.id.viewDataBtn);
        Button myTopics = findViewById(R.id.myTopicsBtn);
        if (item.equals("Publisher")) {
            textButton.setVisibility(View.VISIBLE);
            multimediaButton.setVisibility(View.VISIBLE);
            cameraButton.setVisibility(View.VISIBLE);
            cameraVideoButton.setVisibility(View.VISIBLE);
            topicsButton.setVisibility(View.GONE);
            topicsList.setVisibility(View.GONE);
            registerButton.setVisibility(View.GONE);
            viewDataButton.setVisibility(View.GONE);
            myTopics.setVisibility(View.GONE);
            //publisher stuff
        }
        else if (item.equals("Consumer")) {
            textButton.setVisibility(View.GONE);
            multimediaButton.setVisibility(View.GONE);
            cameraButton.setVisibility(View.GONE);
            cameraVideoButton.setVisibility(View.GONE);
            topicsButton.setVisibility(View.VISIBLE);
            //publisher stuff
        }
    }

    public void updateBrokerInfo(View view) {
        Button updateBrokers = findViewById(R.id.updateBrokers);
        new UpdateBrokerInfoTask().execute();
    }

    public void viewTopics(View view) {
        Button myTopics = findViewById(R.id.myTopicsBtn);
        myTopics.setVisibility(View.VISIBLE);
        new Task().execute(channel);
    }

    public void myTopics(View view) {
        for (int i = 0; i < listView.getCount(); i++) {
            if (cons.topics.contains((String) listView.getItemAtPosition(i))) {
                listView.getChildAt(i).setBackgroundColor(Color.CYAN);
            }
        }
    }

    public class Task extends AsyncTask<String, Void, Void> {

        Address address;
        String channelName;
        ArrayList<String> topicsArray = new ArrayList<>();
        Intent intent;
        Button registerButton = findViewById(R.id.registerBtn);
        Button viewDataButton = findViewById(R.id.viewDataBtn);
        String entry;
        View previousSelectedItem;
        View itemAtPosition;

        @Override
        protected Void doInBackground(String... strings) {
            channelName = strings[0];
            Broker.getBrokerList().forEach((k, v) -> topicsArray.addAll(v));
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            listAdapter = new ArrayAdapter<>(OptionsActivity.this, R.layout.activity_listview, topicsArray);
            listView = findViewById(R.id.topics_list);
            listView.setAdapter(listAdapter);
            listView.setVisibility(View.VISIBLE);
            listView.setOnItemClickListener((parent, view, position, id) -> {
                entry = (String) parent.getAdapter().getItem(position);
                itemAtPosition = view;
                intent = new Intent(OptionsActivity.this, TopicActivity.class);
                intent.putExtra("topic", entry);
                intent.putExtra("consumer", cons);
                intent.putExtra("addr" , cons.addr);
                Log.d("myipcons.addr" , String.valueOf(cons.addr));
                registerButton.setVisibility(View.VISIBLE);
                viewDataButton.setVisibility(View.VISIBLE);
                if (previousSelectedItem != null) {
                    previousSelectedItem.setBackgroundColor(Color.WHITE);
                }
                previousSelectedItem = view;
                view.setBackgroundColor(Color.LTGRAY);
            });

            viewDataButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    startActivity(intent);
                }
            });

            registerButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    if (!cons.topics.contains(entry)) {
                        cons.register(entry);
                        Toast.makeText(OptionsActivity.this, "Registered on topic: " + entry, Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(OptionsActivity.this, "Already registered on topic: " + entry, Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }

    }

    public class InitTask extends AsyncTask<Void, Void, Void> {

        Address address;
        String channelName;

        @Override
        protected Void doInBackground(Void... voids) {
//            final DatagramSocket dSocket;
//            try {
//                dSocket = new DatagramSocket();
//                dSocket.connect(InetAddress.getByName("8.8.8.8"), 10002);
//                address = new Address(dSocket.getLocalAddress().getHostAddress(), port);
//                Log.d("ip address", dSocket.getLocalAddress().getHostAddress());
//            } catch (SocketException | UnknownHostException e) {
//                e.printStackTrace();
//            }
            address = new Address("192.168.1.5" , port);
            pub = new Publisher(address, channelName);
            cons = new Consumer(address);

            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
        }
    }

    public class UpdateBrokerInfoTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            pub.getBrokerList();
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            Toast.makeText(OptionsActivity.this, "Broker Information Updated", Toast.LENGTH_SHORT).show();
        }
    }

}