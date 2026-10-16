package br.edu.yuri.pocketarcade;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import br.edu.yuri.pocketarcade.databinding.ActivityScannerBinding;

public class ScannerActivity extends AppCompatActivity {

    public static final String EXTRA_APELIDO = "br.edu.yuri.pocketarcade.scanner.APELIDO";
    private static final String TAG = "ScannerActivity";
    private static final int MINIMO_CARACTERES = 3;

    // Letras com acento e a letra correspondente sem acento (mesma posição nas duas strings).
    private static final String COM_ACENTO = "áàâãäéèêëíìîïóòôõöúùûüç";
    private static final String SEM_ACENTO = "aaaaaeeeeiiiiooooouuuuc";
    private static final String VOGAIS = "aeiouáàâãäéèêëíìîïóòôõöúùûü";

    private ActivityScannerBinding binding;
    private String apelido;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityScannerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Log.d(TAG, "onCreate");

        if (!receberDados()) {
            return;
        }
        binding.textJogador.setText(getString(R.string.jogador_formato, apelido));
        configurarAcoes();
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
        binding.buttonAnalisar.setOnClickListener(view -> analisarMensagem());
        binding.buttonLimpar.setOnClickListener(view -> limparTela());
        binding.buttonVoltar.setOnClickListener(view -> finish());
        binding.buttonEncerrarSessao.setOnClickListener(view -> encerrarSessao());
    }

    private void analisarMensagem() {
        String texto = binding.editMensagem.getText().toString().trim();

        if (texto.isEmpty()) {
            binding.editMensagem.setError(getString(R.string.erro_mensagem_vazia));
            binding.editMensagem.requestFocus();
            esconderPainel();
            return;
        }

        String semEspacos = removerEspacos(texto);
        if (semEspacos.length() < MINIMO_CARACTERES) {
            binding.editMensagem.setError(getString(R.string.erro_mensagem_curta));
            binding.editMensagem.requestFocus();
            esconderPainel();
            return;
        }

        binding.textValorCaracteres.setText(String.valueOf(semEspacos.length()));
        binding.textValorPalavras.setText(String.valueOf(contarPalavras(texto)));
        binding.textValorVogais.setText(String.valueOf(contarVogais(texto)));
        binding.textValorInvertido.setText(new StringBuilder(texto).reverse().toString());
        if (ehPalindromo(texto)) {
            binding.textValorPalindromo.setText(R.string.resposta_sim);
        } else {
            binding.textValorPalindromo.setText(R.string.resposta_nao);
        }

        esconderTeclado();
        mostrarPainelAnimado();
    }

    private void limparTela() {
        binding.editMensagem.setText("");
        binding.editMensagem.setError(null);
        binding.editMensagem.requestFocus();
        esconderPainel();
    }

    // Percorre o texto e guarda tudo que não for espaço em branco.
    private String removerEspacos(String texto) {
        StringBuilder resultado = new StringBuilder();
        for (int i = 0; i < texto.length(); i++) {
            char letra = texto.charAt(i);
            if (!Character.isWhitespace(letra)) {
                resultado.append(letra);
            }
        }
        return resultado.toString();
    }

    private int contarPalavras(String texto) {
        // split("\\s+") separa o texto a cada grupo de espaços
        String[] palavras = texto.split("\\s+");
        return palavras.length;
    }

    // Conta a, e, i, o, u (as vogais com acento também entram).
    private int contarVogais(String texto) {
        String minusculas = texto.toLowerCase();
        int total = 0;
        for (int i = 0; i < minusculas.length(); i++) {
            if (VOGAIS.indexOf(minusculas.charAt(i)) >= 0) {
                total++;
            }
        }
        return total;
    }

    // Palíndromo ignorando espaços, pontuação, acentos e maiúsculas/minúsculas:
    // guarda só letras e números (sem acento) e compara com o texto de trás para frente.
    private boolean ehPalindromo(String texto) {
        String minusculas = texto.toLowerCase();
        StringBuilder essencial = new StringBuilder();

        for (int i = 0; i < minusculas.length(); i++) {
            char letra = minusculas.charAt(i);

            int posicao = COM_ACENTO.indexOf(letra);
            if (posicao >= 0) {
                letra = SEM_ACENTO.charAt(posicao);
            }
            if (Character.isLetterOrDigit(letra)) {
                essencial.append(letra);
            }
        }

        if (essencial.length() == 0) {
            return false;
        }
        String invertido = new StringBuilder(essencial).reverse().toString();
        return essencial.toString().equals(invertido);
    }

    private void mostrarPainelAnimado() {
        binding.painelResultado.setVisibility(View.VISIBLE);
        Animation animacao = AnimationUtils.loadAnimation(this, R.anim.resultado_entrada);
        binding.painelResultado.startAnimation(animacao);
    }

    private void esconderPainel() {
        binding.painelResultado.clearAnimation();
        binding.painelResultado.setVisibility(View.GONE);
    }

    private void esconderTeclado() {
        InputMethodManager teclado = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        teclado.hideSoftInputFromWindow(binding.editMensagem.getWindowToken(), 0);
    }

    private void encerrarSessao() {
        Intent intent = new Intent(this, ApresentacaoActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra(ApresentacaoActivity.EXTRA_ULTIMO_MODULO, getString(R.string.modulo_scanner));
        startActivity(intent);
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
