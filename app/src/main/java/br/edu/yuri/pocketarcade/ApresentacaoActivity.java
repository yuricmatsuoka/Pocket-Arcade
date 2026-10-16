package br.edu.yuri.pocketarcade;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import br.edu.yuri.pocketarcade.databinding.ActivityApresentacaoBinding;

public class ApresentacaoActivity extends AppCompatActivity {

    public static final String EXTRA_ULTIMO_MODULO = "br.edu.yuri.pocketarcade.ULTIMO_MODULO";
    private static final String TAG = "ApresentacaoActivity";
    private static final int TAMANHO_MINIMO_APELIDO = 3;

    private ActivityApresentacaoBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityApresentacaoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Log.d(TAG, "onCreate");

        binding.buttonEntrar.setOnClickListener(view -> validarEntrada());
    }

    private void validarEntrada() {
        String apelido = binding.editApelido.getText().toString().trim();

        if (apelido.isEmpty()) {
            binding.editApelido.setError(getString(R.string.erro_apelido_vazio));
            binding.editApelido.requestFocus();
            return;
        }
        if (apelido.length() < TAMANHO_MINIMO_APELIDO) {
            binding.editApelido.setError(getString(R.string.erro_apelido_curto));
            binding.editApelido.requestFocus();
            return;
        }

        animarBotaoEAbrirCentral(apelido);
    }

    // Encolhe o botão com ViewPropertyAnimator e, quando a animação termina,
    // abre a Central. O listener avisa quando a animação acabou.
    private void animarBotaoEAbrirCentral(String apelido) {
        binding.buttonEntrar.setEnabled(false);
        binding.buttonEntrar.animate()
                .scaleX(0.9f)
                .scaleY(0.9f)
                .setDuration(150)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        binding.buttonEntrar.animate().setListener(null);
                        binding.buttonEntrar.setScaleX(1f);
                        binding.buttonEntrar.setScaleY(1f);
                        abrirCentral(apelido);
                    }
                })
                .start();
    }

    private void abrirCentral(String apelido) {
        Intent intent = new Intent(this, CentralActivity.class);
        intent.putExtra(CentralActivity.EXTRA_APELIDO, apelido);
        startActivity(intent);
    }

    // Chamado quando alguma tela encerra a sessão (flags CLEAR_TOP | SINGLE_TOP):
    // esta mesma Activity é reaproveitada e mostra o último módulo usado.
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);

        String modulo = intent.getStringExtra(EXTRA_ULTIMO_MODULO);
        if (modulo == null || modulo.trim().isEmpty()) {
            modulo = getString(R.string.ultimo_modulo_nenhum);
        }
        binding.textUltimoModulo.setText(getString(R.string.ultimo_modulo, modulo));
        binding.textUltimoModulo.setVisibility(View.VISIBLE);
        Log.d(TAG, "onNewIntent: ultimo modulo = " + modulo);
    }

    @Override
    protected void onResume() {
        super.onResume();
        binding.buttonEntrar.setEnabled(true);
        Log.d(TAG, "onResume");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }
}
