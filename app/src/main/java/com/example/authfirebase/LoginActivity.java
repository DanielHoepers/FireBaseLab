package com.example.authfirebase;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;

public class LoginActivity extends AppCompatActivity {
    public static class LoginState extends ViewModel {
        Task<AuthResult> pending;
    }

    private EditText email;
    private EditText senha;
    private TextView status;
    private LoginState state;
    private boolean abrindoMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        state = new ViewModelProvider(this).get(LoginState.class);
        email = findViewById(R.id.loginEmail);
        senha = findViewById(R.id.loginSenha);
        status = findViewById(R.id.loginStatus);
        findViewById(R.id.btnEntrar).setOnClickListener(v -> autenticar(false));
        findViewById(R.id.btnCriarConta).setOnClickListener(v -> autenticar(true));
        findViewById(R.id.btnVoltarLogin).setOnClickListener(v -> finish());
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            abrirMenu();
        } else if (state.pending != null) {
            acompanhar();
        }
    }

    private void autenticar(boolean criarConta) {
        if (state.pending != null) return;
        String endereco = email.getText().toString().trim();
        String segredo = senha.getText().toString();
        if (!Patterns.EMAIL_ADDRESS.matcher(endereco).matches()) {
            email.setError("Digite um e-mail válido");
            email.requestFocus();
            return;
        }
        if (segredo.isEmpty() || (criarConta && segredo.length() < 6)) {
            senha.setError(criarConta ? "Use pelo menos 6 caracteres" : "Digite sua senha");
            senha.requestFocus();
            return;
        }
        FirebaseAuth auth = FirebaseAuth.getInstance();
        state.pending = criarConta
                ? auth.createUserWithEmailAndPassword(endereco, segredo)
                : auth.signInWithEmailAndPassword(endereco, segredo);
        acompanhar();
    }

    private void acompanhar() {
        carregando(true);
        status.setText("Autenticando…");
        Task<AuthResult> tarefa = state.pending;
        tarefa.addOnCompleteListener(this, resultado -> {
            if (state.pending != tarefa) return;
            state.pending = null;
            carregando(false);
            if (resultado.isSuccessful()) {
                senha.setText("");
                abrirMenu();
            } else {
                status.setText(mensagemErro(resultado.getException()));
            }
        });
    }

    private void carregando(boolean ativo) {
        email.setEnabled(!ativo);
        senha.setEnabled(!ativo);
        findViewById(R.id.btnEntrar).setEnabled(!ativo);
        findViewById(R.id.btnCriarConta).setEnabled(!ativo);
        findViewById(R.id.loginProgresso).setVisibility(ativo ? View.VISIBLE : View.GONE);
    }

    private void abrirMenu() {
        if (abrindoMenu || isFinishing()) return;
        abrindoMenu = true;
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private String mensagemErro(Exception erro) {
        if (erro instanceof FirebaseNetworkException) {
            return "Sem conexão. Verifique a internet e tente novamente.";
        }
        if (erro instanceof FirebaseTooManyRequestsException) {
            return "Muitas tentativas. Aguarde um pouco e tente novamente.";
        }
        if (erro instanceof FirebaseAuthException) {
            switch (((FirebaseAuthException) erro).getErrorCode()) {
                case "ERROR_EMAIL_ALREADY_IN_USE":
                    return "Este e-mail já tem uma conta. Use Entrar.";
                case "ERROR_WEAK_PASSWORD":
                    return "A senha não atende aos requisitos. Use uma senha mais forte.";
                case "ERROR_OPERATION_NOT_ALLOWED":
                    return "Ative E-mail/senha em Authentication no console do Firebase.";
                case "ERROR_USER_DISABLED":
                    return "Esta conta foi desativada.";
                case "ERROR_INVALID_EMAIL":
                    return "Digite um e-mail válido.";
                case "ERROR_INVALID_CREDENTIAL":
                case "ERROR_WRONG_PASSWORD":
                case "ERROR_USER_NOT_FOUND":
                case "ERROR_INVALID_LOGIN_CREDENTIALS":
                    return "E-mail ou senha incorretos. Confira os dados ou crie uma conta.";
            }
        }
        return "Não foi possível autenticar. Confira os dados e tente novamente.";
    }
}
