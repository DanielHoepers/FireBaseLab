package com.example.authfirebase;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.authfirebase.remoteconfig.RemoteConfigActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btnFuncaoNuvem).setOnClickListener(v ->
                startActivity(new Intent(this, CloudFunctionActivity.class)));
        findViewById(R.id.btnRemoteConfig).setOnClickListener(v ->
                startActivity(new Intent(this, RemoteConfigActivity.class)));
        findViewById(R.id.btnTarefas).setOnClickListener(v ->
                startActivity(new Intent(this, TarefasActivity.class)));
    }
}
