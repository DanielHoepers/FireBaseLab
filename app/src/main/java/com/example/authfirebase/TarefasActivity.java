package com.example.authfirebase;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TarefasActivity extends AppCompatActivity {

    private static final String TAG = "Tarefas";

    private CollectionReference tarefasRef;
    private ListenerRegistration listener;

    private final List<String> ids = new ArrayList<>();
    private final List<Boolean> estados = new ArrayList<>();
    private final List<String> textos = new ArrayList<>();
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tarefas);

        findViewById(R.id.btnVoltar).setOnClickListener(v -> finish());

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // usuarios/{uid}/tarefas
        tarefasRef = FirebaseFirestore.getInstance()
                .collection("usuarios")
                .document(user.getUid())
                .collection("tarefas");

        EditText campoTitulo = findViewById(R.id.campoTitulo);
        ListView lista = findViewById(R.id.listaTarefas);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, textos);
        lista.setAdapter(adapter);

        // RF02 e RF06: criar tarefa
        findViewById(R.id.btnAdicionar).setOnClickListener(v -> {
            String titulo = campoTitulo.getText().toString().trim();
            if (titulo.isEmpty()) {
                campoTitulo.setError("Digite um título");
                return;
            }

            Map<String, Object> tarefa = new HashMap<>();
            tarefa.put("titulo", titulo);
            tarefa.put("concluida", false);
            tarefa.put("criadaEm", FieldValue.serverTimestamp());

            tarefasRef.add(tarefa)
                    .addOnFailureListener(e -> mostrarErro("Erro ao salvar", e));
            campoTitulo.setText("");
        });

        // RF04: alternar concluída
        lista.setOnItemClickListener((parent, view, pos, id) ->
                tarefasRef.document(ids.get(pos))
                        .update("concluida", !estados.get(pos))
                        .addOnFailureListener(e -> mostrarErro("Erro ao atualizar", e)));

        // RF05: excluir
        lista.setOnItemLongClickListener((parent, view, pos, id) -> {
            tarefasRef.document(ids.get(pos))
                    .delete()
                    .addOnFailureListener(e -> mostrarErro("Erro ao excluir", e));
            return true;
        });
    }

    // RF03: ouvir em tempo real
    @Override
    protected void onStart() {
        super.onStart();
        if (tarefasRef == null) {
            return;
        }

        listener = tarefasRef
                .orderBy("criadaEm", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshot, erro) -> {
                    if (erro != null || snapshot == null) {
                        mostrarErro("Erro ao ler", erro);
                        return;
                    }

                    ids.clear();
                    estados.clear();
                    textos.clear();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        boolean concluida = Boolean.TRUE.equals(doc.getBoolean("concluida"));
                        String titulo = doc.getString("titulo");
                        ids.add(doc.getId());
                        estados.add(concluida);
                        textos.add((concluida ? "✔ " : "") + (titulo == null ? "(sem título)" : titulo));
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (listener != null) {
            listener.remove();
            listener = null;
        }
    }

    private void mostrarErro(String mensagem, Exception e) {
        Log.e(TAG, mensagem, e);
        String detalhe = e == null ? "" : ": " + e.getMessage();
        Toast.makeText(this, mensagem + detalhe, Toast.LENGTH_LONG).show();
    }
}
