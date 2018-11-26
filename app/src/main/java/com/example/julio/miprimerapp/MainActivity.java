package com.example.julio.miprimerapp;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Instrumentation;
import android.app.NotificationManager;
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
import android.support.v4.app.NotificationCompat;
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
import java.net.URLEncoder;

import static java.lang.Integer.valueOf;

public class MainActivity extends AppCompatActivity {

    ImageView imgimagen;
    Button BtnTakePhoto;
    Button BtnSendInfo;
    Button BtnClearInfo;
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
       // BtnClearInfo = (Button) findViewById(R.id.button5);
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
                Bitmap bm=((BitmapDrawable)imgimagen.getDrawable()).getBitmap();
                System.out.println("Bitmap:  "+bm);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bm.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                byte[] byteArray = byteArrayOutputStream .toByteArray();
                final String encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);
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

                                    URL url = new URL("http://35.231.64.75");
                                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                                    conn.setRequestMethod("POST");
                                    conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                                    conn.setRequestProperty("Accept","application/json");
                                    conn.setDoOutput(true);
                                    conn.setDoInput(true);

                                    JSONObject jsonParam = new JSONObject();

                                    jsonParam.put("citizenuid",CUIText.getText().toString());
                                    jsonParam.put("volunteer_id",mySpinner.getSelectedItem().toString());
                                    jsonParam.put("image_hash", encoded);

                                    Log.i("JSON: ", jsonParam.toString());

                                    DataOutputStream os = new DataOutputStream(conn.getOutputStream());
                                    //os.writeBytes(URLEncoder.encode(jsonParam.toString(), "UTF-8"));
                                    os.writeBytes(jsonParam.toString());

                                    os.flush();
                                    os.close();

                                    Integer status = valueOf(conn.getResponseCode());

                                    Log.i("STATUS", String.valueOf(conn.getResponseCode()));
                                    Log.i("MSG" , conn.getResponseMessage());

                                    conn.disconnect();

                                    //System.out.println(status);
                                    if(status==200){
                                        okNoti();
                                    }
                                    if(status==500){
                                        notOkNoti();
                                    }

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

    public void okNoti(){
        NotificationCompat.Builder nBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setSmallIcon(R.drawable.imagensplash)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.drawable.imagensplash))
                .setContentTitle("Notificacion de ChangEOS")
                .setContentText("Voto registrado correctamente");

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1,nBuilder.build());
    }

    public void notOkNoti(){
        NotificationCompat.Builder nBuilder2 = (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setSmallIcon(R.drawable.imagensplash)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.drawable.imagensplash))
                .setContentTitle("Notificacion de ChangEOS")
                .setContentText("Voto no Registrado");

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1,nBuilder2.build());
    }

    }
