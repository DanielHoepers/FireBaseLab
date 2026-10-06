package com.example.authfirebase.remoteconfig;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.authfirebase.BuildConfig;
import com.example.authfirebase.R;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

public class RemoteConfigActivity extends AppCompatActivity {
    private static final String TITULO_APP = "titulo_app";
    private static final String MENSAGEM_APP = "mensagem_app";
    private static final String COR_DESTAQUE = "cor_destaque";

    private FirebaseRemoteConfig remoteConfig;
    private TextView titulo;
    private TextView mensagem;
    private TextView origem;
    private View faixaDestaque;
    private com.google.android.material.button.MaterialButton buscar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.remote_config_demo);

        findViewById(R.id.btnVoltar).setOnClickListener(v -> finish());
        titulo = findViewById(R.id.textTituloConfig);
        mensagem = findViewById(R.id.textMensagemConfig);
        origem = findViewById(R.id.textOrigemConfig);
        faixaDestaque = findViewById(R.id.faixaDestaque);
        buscar = findViewById(R.id.btnBuscarConfiguracoes);
        buscar.setOnClickListener(v -> buscarConfiguracoes());

        remoteConfig = FirebaseRemoteConfig.getInstance();
        long intervalo = BuildConfig.DEBUG ? 0L : 12L * 60L * 60L;
        FirebaseRemoteConfigSettings settings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(intervalo)
                .build();

        Tasks.whenAll(remoteConfig.setConfigSettingsAsync(settings),
                        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults))
                .addOnCompleteListener(this, task -> {
                    if (!task.isSuccessful()) {
                        origem.setText("Não foi possível preparar o Remote Config. Os valores locais continuam visíveis.");
                        Toast.makeText(this, "Falha ao iniciar Remote Config.", Toast.LENGTH_LONG).show();
                        return;
                    }
                    aplicarValores();
                    buscar.setEnabled(true);
                });
    }

    private void buscarConfiguracoes() {
        buscar.setEnabled(false);
        origem.setText("Buscando configurações no Firebase...");

        remoteConfig.fetchAndActivate().addOnCompleteListener(this, task -> {
            buscar.setEnabled(true);
            if (!task.isSuccessful()) {
                origem.setText("Falha na busca. Os valores atuais continuam visíveis.");
                Toast.makeText(this, "Não foi possível buscar: "
                        + (task.getException() == null ? "verifique a conexão"
                        : task.getException().getMessage()), Toast.LENGTH_LONG).show();
                return;
            }

            boolean corValida = aplicarValores();
            if (!corValida) {
                Toast.makeText(this, "cor_destaque inválida; usando a cor padrão.",
                        Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Configurações buscadas e ativadas com sucesso.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean aplicarValores() {
        titulo.setText(remoteConfig.getString(TITULO_APP));
        mensagem.setText(remoteConfig.getString(MENSAGEM_APP));

        int cor;
        boolean corValida = true;
        try {
            cor = Color.parseColor(remoteConfig.getString(COR_DESTAQUE).trim());
        } catch (IllegalArgumentException exception) {
            cor = getColor(R.color.firebase_blue);
            corValida = false;
        }

        titulo.setTextColor(cor);
        faixaDestaque.setBackgroundColor(cor);
        buscar.setBackgroundTintList(ColorStateList.valueOf(cor));

        boolean remoto = remoteConfig.getValue(TITULO_APP).getSource()
                == FirebaseRemoteConfig.VALUE_SOURCE_REMOTE;
        origem.setText(remoto ? "Exibindo valores publicados no Firebase"
                : "Exibindo valores padrão do aplicativo");
        return corValida;
    }
}
