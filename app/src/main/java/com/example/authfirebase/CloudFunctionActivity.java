package com.example.authfirebase;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.functions.FirebaseFunctions;

import java.util.Map;

public class CloudFunctionActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cloud_function);

        findViewById(R.id.btnVoltar).setOnClickListener(v -> finish());

        Button btnExecutar = findViewById(R.id.btnExecutarFuncao);
        FirebaseFunctions functions;
        if (BuildConfig.DEBUG) {
            FirebaseApp localApp;
            try {
                localApp = FirebaseApp.getInstance("local-functions");
            } catch (IllegalStateException exception) {
                localApp = FirebaseApp.initializeApp(getApplicationContext(),
                        FirebaseApp.getInstance().getOptions(), "local-functions");
            }
            functions = FirebaseFunctions.getInstance(localApp, "us-central1");
            functions.useEmulator("127.0.0.1", 5001);
        } else {
            functions = FirebaseFunctions.getInstance("us-central1");
        }
        btnExecutar.setOnClickListener(v -> {
            btnExecutar.setEnabled(false);
            functions
                    .getHttpsCallable("saudacao")
                    .call()
                    .addOnCompleteListener(task -> {
                        btnExecutar.setEnabled(true);
                        if (!task.isSuccessful()) {
                            Log.w("CloudFunctionActivity", "Falha ao executar saudacao", task.getException());
                            Toast.makeText(this,
                                    BuildConfig.DEBUG
                                            ? "Não foi possível acessar a função local. Execute start-local-firebase.sh no Mac com o emulador Android aberto."
                                            : "Não foi possível executar a função. Tente novamente.",
                                    Toast.LENGTH_LONG).show();
                            return;
                        }

                        Object data = task.getResult().getData();
                        if (!(data instanceof Map)
                                || !(((Map<?, ?>) data).get("message") instanceof String)) {
                            Toast.makeText(this, "A função retornou uma resposta inválida.",
                                    Toast.LENGTH_LONG).show();
                            return;
                        }

                        String message = (String) ((Map<?, ?>) data).get("message");
                        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                    });
        });
    }
}
