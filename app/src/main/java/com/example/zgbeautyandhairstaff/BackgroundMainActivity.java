package com.example.zgbeautyandhairstaff;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

public class BackgroundMainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_background_main);

        new Handler().postDelayed(() -> {

            startActivity(new Intent(BackgroundMainActivity.this, MainActivity.class));
            finish();
        },3000);
    }
}
