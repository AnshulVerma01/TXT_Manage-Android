package com.example.txtmanage;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;

public class MainActivity extends AppCompatActivity {
    public static String abspath="";
    private static final int PERMISSION_REQUEST_STORAGE=1000;
    private static final int READ_REQUEST_CODE=42;
    private EditText editTextContent, editTextSearch, TextResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.MANAGE_EXTERNAL_STORAGE}, 1);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                Uri uri = Uri.fromParts("package", this.getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
            }
        }
        initView();
    }


    private void initView() {
        editTextContent = findViewById(R.id.editTextContent);
        editTextSearch = findViewById(R.id.editTextSearch);
        TextResult = findViewById(R.id.TextResult);
        Button buttonReadFile = findViewById(R.id.buttonReadFile);
        buttonReadFile.setOnClickListener(v -> performFileSearch());
        Button buttonUpdate = findViewById(R.id.buttonUpdate);
        buttonUpdate.setOnClickListener(v -> buttonUpdate_onClick());
        Button buttonSearch = findViewById(R.id.buttonSearch);
        buttonSearch.setOnClickListener(v -> buttonSearch_onClick());
        Button buttonClear = findViewById(R.id.buttonClear);
        buttonClear.setOnClickListener(v -> {
            editTextContent.setText("");
            TextResult.setText("");
            editTextSearch.setText("");
            abspath = "";
        });
    }

    private void buttonReadFile_onClick(String input) {
        try {
            StringBuilder result = new StringBuilder();
            String line;
            String folder = Environment.getExternalStorageDirectory().getAbsolutePath();
            BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(folder,input)));
            while ((line = bufferedReader.readLine()) != null) {
                result.append(line);
                result.append("\n");
            }
            editTextContent.setText(result.toString());
            editTextSearch.setText("");
            TextResult.setText("");
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void buttonUpdate_onClick() {
        try {
            String folder = Environment.getExternalStorageDirectory().getAbsolutePath();
            FileOutputStream outputStream = new FileOutputStream(new File(folder,abspath));
            outputStream.write(editTextContent.getText().toString().getBytes());
            outputStream.close();
            Toast.makeText(getApplicationContext(), "Done", Toast.LENGTH_LONG).show();
            TextResult.setText("");
        } catch (Exception e) {
            Toast.makeText(this, "Please Select A File", Toast.LENGTH_SHORT).show();
        }
    }

    private void buttonSearch_onClick(){
        if (editTextSearch.getText().toString().matches("")) {
            Toast.makeText(this, "Searchbar Empty", Toast.LENGTH_SHORT).show();
            return;
        }
        String word = editTextSearch.getText().toString();
        try{
            String line;
            String found1;
            StringBuilder found2 = new StringBuilder();
            String found3;
            String found4;
            int linecount=1;
            int count=0;
            String folder = Environment.getExternalStorageDirectory().getAbsolutePath();
            BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(folder,abspath)));
            while ((line = bufferedReader.readLine()) != null) {
                for(String element : line.split(" ")){
                    if(element.toLowerCase().contains(word.toLowerCase())){
                        found1 = "Matched Word: "+element+"\n";
                        count++;
                        found2.append(found1);
                        found3 = "Word Found At Line: "+linecount+"\n";
                        found2.append(found3);
                    }
                }
                linecount++;
            }
            found4 = "The Word \""+editTextSearch.getText().toString()+"\" appears "+count+" times.";
            found2.append(found4);
            TextResult.setText(found2);
        }catch (Exception e){
            Toast.makeText(this, "Please Select A File", Toast.LENGTH_SHORT).show();
        }
    }

    private void performFileSearch(){
        Intent intent = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        }
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/*");
        startActivityForResult(intent,READ_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                Uri uri = data.getData();
                String path = uri.getPath();
                path = path.substring(path.indexOf(":") + 1);
                if(path.contains("emulated")){
                    path=path.substring(path.indexOf("0")+1);
                }
                Toast.makeText(this, "" + path, Toast.LENGTH_SHORT).show();
                buttonReadFile_onClick(path);
                abspath = path;
            }
        }
    }
}