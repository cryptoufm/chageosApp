package com.example.julio.miprimerapp;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Instrumentation;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.Spinner;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    ImageView imgimagen;
    Button BtnTakePhoto;
    Button BtnSendInfo;
    static final int REQUEST_IMAGE_CAPTURE =1;
    public String path = Environment.getExternalStorageDirectory().getAbsolutePath()+"/aaTutorial";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        final Spinner mySpinner = (Spinner) findViewById(R.id.spinner1);
        final EditText CUIText =  (EditText)findViewById(R.id.editText2);
        ArrayAdapter<String> myAdapter = new ArrayAdapter<String>(MainActivity.this,
                android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.names));
        myAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mySpinner.setAdapter(myAdapter);

        BtnTakePhoto = (Button) findViewById(R.id.button2);
        imgimagen = (ImageView) findViewById(R.id.imageView);
        BtnSendInfo = (Button) findViewById(R.id.button4);
        File dir = new File(path);
        dir.mkdir();

        BtnTakePhoto.setOnClickListener(new View.OnClickListener(){
        @Override
            public void onClick(View v) {
            llamarIntent();
        }
    });
        BtnSendInfo.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                builder.setCancelable(true);
                builder.setTitle("Registro de voto");
                builder.setMessage("Voluntarix:  "+mySpinner.getSelectedItem().toString()+
                "\nCUI:  "+CUIText.getText().toString());
//                System.out.println("Voluntarix:  "+mySpinner.getSelectedItem().toString());
//                System.out.println("Prueba>"+imgimagen.getDrawable());
//                System.out.println("CUI:  "+CUIText.getText().toString());
//                Bitmap bm=((BitmapDrawable)imgimagen.getDrawable()).getBitmap();
//                System.out.println("Bitmap:  "+bm);
 //               ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//                bm.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
//                byte[] byteArray = byteArrayOutputStream .toByteArray();
   //             final String encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);
//                System.out.println("Encoded:  "+encoded);

                builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });

                builder.setPositiveButton("Enviar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Thread thread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {

                                    JSONObject jsonParam = new JSONObject();
                                    jsonParam.put("Voluntario",mySpinner.getSelectedItem().toString());
                                    jsonParam.put("CUI",CUIText.getText().toString());
                                    //jsonParam.put("Photo", encoded);

                                    Log.i("JSON: ", jsonParam.toString());

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });

                        thread.start();
                    }
                });
                builder.show();
            }
        });
    }

    private void llamarIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);

        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            imgimagen.setImageBitmap(imageBitmap);
        }
    }
    }
