package arachnID_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.lang.Math;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Callback;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void colorClick(View v) {
        int Red = (int) Math.floor(Math.random()*(256));
        int Green = (int) Math.floor(Math.random()*(256));
        int Blue = (int) Math.floor(Math.random()*(256));
        v.setBackgroundColor(android.graphics.Color.rgb(Red, Green, Blue));
    }

    public void moveToCamera(View v) {
        Intent camera = new Intent(this, CameraActivity.class);
        startActivity(camera);
    }

}