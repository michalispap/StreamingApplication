package com.example.streamingapplication;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class OptionsActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    String item;
    private static final int GALLERY_PICK_CODE = 1000;
    private static final int PERMISSION_CODE = 1001;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_VIDEO_CAPTURE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);
        Intent i = getIntent();
        Bundle b = i.getExtras();
        TextView portNumber = findViewById(R.id.channelString);
        portNumber.setText((String) b.get("port"));
        TextView channelName = findViewById(R.id.portNumber);
        channelName.setText((String) b.get("channelName"));
        Spinner spinner = findViewById(R.id.options_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.options_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
         item = parent.getItemAtPosition(position).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
    }

    public void openText(View view) {
        EditText message = (EditText) findViewById(R.id.message);
        Button textButton = (Button) findViewById(R.id.textBtn);
        String ButtonText = textButton.getText().toString();
        if (ButtonText.equals("Text")) {
            message.setVisibility(View.VISIBLE);
            textButton.setText("Send");
        }
        else if (ButtonText.equals("Send")) {
            Toast.makeText(this, message.getText().toString(), Toast.LENGTH_SHORT).show();
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
    }

    public void Choice(View view) {
        Button textButton = (Button) findViewById(R.id.textBtn);
        Button multimediaButton = (Button) findViewById(R.id.galleryBtn);
        Button cameraButton = (Button) findViewById(R.id.cameraBtn);
        Button cameraVideoButton = (Button) findViewById(R.id.cameraVideoBtn);
        if (item.equals("Publisher")) {
            textButton.setVisibility(View.VISIBLE);
            multimediaButton.setVisibility(View.VISIBLE);
            cameraButton.setVisibility(View.VISIBLE);
            cameraVideoButton.setVisibility(View.VISIBLE);
            //publisher stuff
        }
        else if (item.equals("Consumer")) {
            textButton.setVisibility(View.GONE);
            multimediaButton.setVisibility(View.GONE);
            cameraButton.setVisibility(View.GONE);
            cameraVideoButton.setVisibility(View.GONE);
            //publisher stuff
        }
        else if (item.equals("Update Broker Information")) {
            textButton.setVisibility(View.GONE);
            multimediaButton.setVisibility(View.GONE);
            cameraButton.setVisibility(View.GONE);
            cameraVideoButton.setVisibility(View.GONE);
            //update broker info
        }
    }

}