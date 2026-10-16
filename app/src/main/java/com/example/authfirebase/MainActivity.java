package com.example.authfirebase;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;


public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!verificarSessao()) return;
        setContentView(R.layout.activity_main);

        findViewById(R.id.btnFuncaoNuvem).setOnClickListener(v ->
                startActivity(new Intent(this, CloudFunctionActivity.class)));
        findViewById(R.id.btnRemoteConfig).setOnClickListener(v ->
                startActivity(new Intent(this, RemoteConfigActivity.class)));
        findViewById(R.id.btnTarefas).setOnClickListener(v ->
                startActivity(new Intent(this, TarefasActivity.class)));
    }
    @Override
    protected void onStart() {
        super.onStart();
        if (!isFinishing()) verificarSessao();
    }

    private boolean verificarSessao() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) return true;
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
        return false;
    }
}
