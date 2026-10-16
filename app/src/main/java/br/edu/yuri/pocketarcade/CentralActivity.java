package br.edu.yuri.pocketarcade;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import br.edu.yuri.pocketarcade.databinding.ActivityCentralBinding;

public class CentralActivity extends AppCompatActivity {

    public static final String EXTRA_APELIDO = "br.edu.yuri.pocketarcade.central.APELIDO";
    private static final String TAG = "CentralActivity";

    private ActivityCentralBinding binding;
    private String apelido;
    private String ultimoModulo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCentralBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Log.d(TAG, "onCreate");

        if (!receberDados()) {
            return;
        }

        binding.textSaudacao.setText(getString(R.string.saudacao, apelido));
        configurarAcoes();
        animarEntradaDosPaineis();
    }

    private boolean receberDados() {
        apelido = getIntent().getStringExtra(EXTRA_APELIDO);
        if (apelido == null || apelido.trim().isEmpty()) {
            Toast.makeText(this, R.string.erro_dados_invalidos, Toast.LENGTH_SHORT).show();
            finish();
            return false;
        }
        return true;
    }

    private void configurarAcoes() {
        binding.buttonAbrirScanner.setOnClickListener(view -> abrirScanner());
        binding.buttonAbrirArena.setOnClickListener(view -> abrirArena());
        binding.buttonAbrirCofre.setOnClickListener(view -> abrirCodigoSecreto());
        binding.buttonAbrirRandomizador.setOnClickListener(view -> abrirRandomizador());
        binding.buttonEncerrarSessao.setOnClickListener(view -> encerrarSessao());
    }

    private void abrirScanner() {
        ultimoModulo = getString(R.string.modulo_scanner);
        Intent intent = new Intent(this, ScannerActivity.class);
        intent.putExtra(ScannerActivity.EXTRA_APELIDO, apelido);
        startActivity(intent);
    }

    private void abrirArena() {
        ultimoModulo = getString(R.string.modulo_arena);
        Intent intent = new Intent(this, ArenaActivity.class);
        intent.putExtra(ArenaActivity.EXTRA_APELIDO, apelido);
        startActivity(intent);
    }

    private void abrirCodigoSecreto() {
        ultimoModulo = getString(R.string.modulo_codigo);
        Intent intent = new Intent(this, CodigoSecretoActivity.class);
        intent.putExtra(CodigoSecretoActivity.EXTRA_APELIDO, apelido);
        startActivity(intent);
    }

    private void abrirRandomizador() {
        ultimoModulo = getString(R.string.modulo_randomizador);
        Intent intent = new Intent(this, RandomizadorActivity.class);
        intent.putExtra(RandomizadorActivity.EXTRA_APELIDO, apelido);
        startActivity(intent);
    }

    private void encerrarSessao() {
        Intent intent = new Intent(this, ApresentacaoActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        if (ultimoModulo != null) {
            intent.putExtra(ApresentacaoActivity.EXTRA_ULTIMO_MODULO, ultimoModulo);
        }
        startActivity(intent);
    }

    private void animarEntradaDosPaineis() {
        View[] paineis = {
                binding.painelScanner,
                binding.painelArena,
                binding.painelCofre,
                binding.painelRandomizador
        };
        for (int i = 0; i < paineis.length; i++) {
            Animation animacao = AnimationUtils.loadAnimation(this, R.anim.entrada_painel);
            paineis[i].startAnimation(animacao);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
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
