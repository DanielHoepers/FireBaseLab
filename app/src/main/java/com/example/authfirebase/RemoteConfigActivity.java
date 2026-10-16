package com.example.authfirebase;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import java.text.DateFormat;
import java.util.Date;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Tasks;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigFetchThrottledException;

public class RemoteConfigActivity extends AppCompatActivity {
    private static final String TAG = "RemoteConfigActivity";
    private static final String TITULO_APP = "TITULO_APP";
    private static final String MENSAGEM_APP = "MENSAGEM_APP";
    private static final String COR_DESTAQUE = "COR_DESTAQUE";

    private FirebaseRemoteConfig remoteConfig;
    private TextView titulo;
    private TextView mensagem;
    private TextView status;
    private View faixaDestaque;
    private MaterialButton btnBuscar;
    private Toast aviso;

    // Prepara a tela, configura o Remote Config e carrega os valores disponíveis.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remote_config);

        findViewById(R.id.btnVoltar).setOnClickListener(v -> finish());
        titulo = findViewById(R.id.textTituloConfig);
        mensagem = findViewById(R.id.textMensagemConfig);
        status = findViewById(R.id.textStatusConfig);
        faixaDestaque = findViewById(R.id.faixaDestaque);
        btnBuscar = findViewById(R.id.btnBuscarConfiguracoes);
        btnBuscar.setOnClickListener(v -> buscarConfiguracoes());

        mostrarStatus(R.string.rc_initializing);
        remoteConfig = FirebaseRemoteConfig.getInstance();

        long intervalo = BuildConfig.DEBUG ? 0L : 12L * 60L * 60L;

        FirebaseRemoteConfigSettings configuracoes = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(intervalo)
                .build();

        Tasks.whenAll(remoteConfig.setConfigSettingsAsync(configuracoes),
                      remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults))
                      .addOnCompleteListener(task -> {
                    if (isFinishing() || isDestroyed()) {
                        return;
                    }
                    if (!task.isSuccessful()) {
                        Log.e(TAG, "Falha ao preparar Remote Config", task.getException());
                        mostrarStatus(R.string.rc_init_error);
                        return;
                    }
                    aplicarValores();
                    btnBuscar.setEnabled(true);
                });
    }

    // Busca e ativa os valores do Firebase, atualizando a tela quando a busca termina.
    private void buscarConfiguracoes() {
        btnBuscar.setEnabled(false);
        mostrarStatus(R.string.rc_loading);

        // O resultado também deve liberar o botão se o app estiver em segundo plano.
        Log.i(TAG, "Iniciando busca; intervalo mínimo: "
                + remoteConfig.getInfo().getConfigSettings().getMinimumFetchIntervalInSeconds());
        remoteConfig.fetchAndActivate().addOnCompleteListener(task -> {
            if (isFinishing() || isDestroyed()) {
                return;
            }
            btnBuscar.setEnabled(true);
            if (task.isSuccessful()) {
                Log.i(TAG, "Busca concluída; novos valores ativados: " + task.getResult());
                aplicarValores();
                if (!Boolean.TRUE.equals(task.getResult())) {
                    mostrarStatus("Busca concluída. Nenhuma configuração nova para ativar.");
                }
            } else {
                Exception erro = task.getException();
                Log.e(TAG, "Falha ao buscar Remote Config", erro);
                if (erro instanceof FirebaseRemoteConfigFetchThrottledException) {
                    long fim = ((FirebaseRemoteConfigFetchThrottledException) erro)
                            .getThrottleEndTimeMillis();
                    String horario = DateFormat.getTimeInstance(DateFormat.MEDIUM)
                            .format(new Date(fim));
                    mostrarStatus("Buscas temporariamente limitadas pelo Firebase. Tente após "
                            + horario + ". Os valores atuais foram mantidos.");
                } else {
                    mostrarStatus(R.string.rc_fetch_error);
                }
            }
        });
    }

    // Exibe o título, a mensagem e a cor de destaque, usando o azul padrão se a cor for inválida.
    private void aplicarValores() {
        titulo.setText(remoteConfig.getString(TITULO_APP));
        mensagem.setText(remoteConfig.getString(MENSAGEM_APP));
        int textoStatus = R.string.rc_applied;

        int cor;
        try {
            cor = Color.parseColor(remoteConfig.getString(COR_DESTAQUE).trim());
        } catch (IllegalArgumentException exception) {
            cor = getColor(R.color.firebase_blue);
            textoStatus = R.string.rc_invalid_color;
        }

        faixaDestaque.setBackgroundColor(cor);
        mostrarStatus(textoStatus);
    }

    // Atualiza o texto da tela e mostra um Toast com a etapa atual, substituindo o aviso anterior.
    private void mostrarStatus(int texto) {
        mostrarStatus(getString(texto));
    }

    private void mostrarStatus(String texto) {
        status.setText(texto);
        if (aviso != null) {
            aviso.cancel();
        }
        aviso = Toast.makeText(this, texto, Toast.LENGTH_LONG);
        aviso.show();
    }
}
