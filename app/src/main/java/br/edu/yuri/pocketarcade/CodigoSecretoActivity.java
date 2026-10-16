package br.edu.yuri.pocketarcade;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import br.edu.yuri.pocketarcade.databinding.ActivityCodigoSecretoBinding;
import br.edu.yuri.pocketarcade.databinding.ItemTentativaBinding;

public class CodigoSecretoActivity extends AppCompatActivity {

    public static final String EXTRA_APELIDO = "br.edu.yuri.pocketarcade.codigo.APELIDO";
    private static final String TAG = "CodigoSecretoActivity";

    private static final int TAMANHO_CODIGO = 4;
    private static final int MAXIMO_TENTATIVAS = 8;

    // Pistas de cada dígito do palpite.
    private static final int PISTA_AUSENTE = 0;
    private static final int PISTA_POSICAO_ERRADA = 1;
    private static final int PISTA_CERTA = 2;

    private ActivityCodigoSecretoBinding binding;
    private String apelido;

    private final Random sorteio = new Random();
    private final int[] codigo = new int[TAMANHO_CODIGO];
    private int tentativas;
    private boolean partidaEncerrada;
    private long inicioDaPartida;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCodigoSecretoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Log.d(TAG, "onCreate");

        if (!receberDados()) {
            return;
        }
        binding.textJogador.setText(getString(R.string.jogador_formato, apelido));

        configurarAcoes();
        iniciarPartida();
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
        binding.buttonTentar.setOnClickListener(view -> tentar());
        binding.editPalpite.setOnEditorActionListener((view, actionId, event) -> {
            tentar();
            return true;
        });
        binding.buttonNovaPartidaCodigo.setOnClickListener(view -> iniciarPartida());
        binding.buttonVoltar.setOnClickListener(view -> finish());
        binding.buttonEncerrarSessao.setOnClickListener(view -> encerrarSessao());
    }

    // Sorteia um código novo, limpa o histórico e marca o início do tempo.
    private void iniciarPartida() {
        sortearCodigo();
        tentativas = 0;
        partidaEncerrada = false;
        inicioDaPartida = System.currentTimeMillis();

        binding.textCofre.setText(R.string.codigo_cofre_fechado);
        binding.containerHistorico.removeAllViews();
        binding.textHistoricoVazio.setVisibility(View.VISIBLE);
        atualizarTentativas();

        binding.editPalpite.setText("");
        binding.editPalpite.setError(null);
        binding.editPalpite.setEnabled(true);
        binding.buttonTentar.setEnabled(true);

        binding.painelResultadoCodigo.clearAnimation();
        binding.painelResultadoCodigo.setVisibility(View.GONE);
    }

    // Embaralha os dígitos de 0 a 9 e fica com os quatro primeiros (nenhum se repete).
    private void sortearCodigo() {
        List<Integer> digitos = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            digitos.add(i);
        }
        Collections.shuffle(digitos, sorteio);
        for (int i = 0; i < TAMANHO_CODIGO; i++) {
            codigo[i] = digitos.get(i);
        }
        // Apenas para testes e demonstração: o código aparece no Logcat.
        Log.d(TAG, "codigo secreto: " + formatarCodigo());
    }

    private void tentar() {
        if (partidaEncerrada) {
            return;
        }
        String texto = binding.editPalpite.getText().toString().trim();

        if (texto.isEmpty()) {
            mostrarErro(R.string.erro_codigo_vazio);
            return;
        }
        if (!ehSomenteNumeros(texto) || texto.length() != TAMANHO_CODIGO) {
            mostrarErro(R.string.erro_codigo_formato);
            return;
        }
        if (temDigitoRepetido(texto)) {
            mostrarErro(R.string.erro_codigo_repetido);
            return;
        }

        int[] palpite = new int[TAMANHO_CODIGO];
        for (int i = 0; i < TAMANHO_CODIGO; i++) {
            palpite[i] = texto.charAt(i) - '0';
        }
        int[] pistas = calcularPistas(palpite);

        tentativas++;
        adicionarAoHistorico(palpite, pistas);
        atualizarTentativas();
        binding.editPalpite.setText("");
        binding.editPalpite.setError(null);

        if (acertouTudo(pistas)) {
            encerrarPartida(true);
        } else if (tentativas >= MAXIMO_TENTATIVAS) {
            encerrarPartida(false);
        }
    }

    private void mostrarErro(int mensagem) {
        binding.editPalpite.setError(getString(mensagem));
        binding.editPalpite.requestFocus();
    }

    private boolean ehSomenteNumeros(String texto) {
        for (int i = 0; i < texto.length(); i++) {
            if (!Character.isDigit(texto.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private boolean temDigitoRepetido(String texto) {
        boolean[] jaApareceu = new boolean[10];
        for (int i = 0; i < texto.length(); i++) {
            int digito = texto.charAt(i) - '0';
            if (jaApareceu[digito]) {
                return true;
            }
            jaApareceu[digito] = true;
        }
        return false;
    }

    // Verde: posição certa. Amarelo: existe no código, em outra posição. Cinza: não existe.
    private int[] calcularPistas(int[] palpite) {
        int[] pistas = new int[TAMANHO_CODIGO];
        for (int i = 0; i < TAMANHO_CODIGO; i++) {
            if (palpite[i] == codigo[i]) {
                pistas[i] = PISTA_CERTA;
            } else if (codigoContem(palpite[i])) {
                pistas[i] = PISTA_POSICAO_ERRADA;
            } else {
                pistas[i] = PISTA_AUSENTE;
            }
        }
        return pistas;
    }

    private boolean codigoContem(int digito) {
        for (int valor : codigo) {
            if (valor == digito) {
                return true;
            }
        }
        return false;
    }

    private boolean acertouTudo(int[] pistas) {
        for (int pista : pistas) {
            if (pista != PISTA_CERTA) {
                return false;
            }
        }
        return true;
    }

    // Cada tentativa vira uma linha (item_tentativa) com 4 dígitos coloridos.
    private void adicionarAoHistorico(int[] palpite, int[] pistas) {
        ItemTentativaBinding linha = ItemTentativaBinding.inflate(
                getLayoutInflater(), binding.containerHistorico, false);
        linha.textNumero.setText(tentativas + ".");

        TextView[] digitos = {
                linha.textDigito1, linha.textDigito2, linha.textDigito3, linha.textDigito4
        };
        for (int i = 0; i < TAMANHO_CODIGO; i++) {
            digitos[i].setText(String.valueOf(palpite[i]));
            digitos[i].setBackgroundColor(corDeFundoDaPista(pistas[i]));
            digitos[i].setTextColor(corDoTextoDaPista(pistas[i]));
        }

        binding.containerHistorico.addView(linha.getRoot());
        binding.textHistoricoVazio.setVisibility(View.GONE);
    }

    private int corDeFundoDaPista(int pista) {
        if (pista == PISTA_CERTA) {
            return ContextCompat.getColor(this, R.color.pista_certa);
        } else if (pista == PISTA_POSICAO_ERRADA) {
            return ContextCompat.getColor(this, R.color.pista_posicao);
        } else {
            return ContextCompat.getColor(this, R.color.pista_ausente);
        }
    }

    // Verde e amarelo pedem texto escuro; o cinza pede texto claro.
    private int corDoTextoDaPista(int pista) {
        if (pista == PISTA_AUSENTE) {
            return ContextCompat.getColor(this, R.color.texto);
        } else {
            return ContextCompat.getColor(this, R.color.fundo);
        }
    }

    private void atualizarTentativas() {
        binding.textTentativas.setText(
                getString(R.string.codigo_tentativas, tentativas, MAXIMO_TENTATIVAS));
    }

    private void encerrarPartida(boolean venceu) {
        partidaEncerrada = true;

        // Tempo da partida: diferença entre o fim e o início, em segundos.
        long fim = System.currentTimeMillis();
        long segundosTotais = (fim - inicioDaPartida) / 1000;
        String tempo = getString(R.string.tempo_formato, segundosTotais / 60, segundosTotais % 60);

        binding.editPalpite.setEnabled(false);
        binding.buttonTentar.setEnabled(false);
        esconderTeclado();

        binding.textCofre.setText(formatarCodigo());
        if (venceu) {
            binding.textResultadoCodigo.setText(R.string.codigo_vitoria);
        } else {
            binding.textResultadoCodigo.setText(R.string.codigo_derrota);
        }
        binding.textDetalhesCodigo.setText(
                getString(R.string.codigo_detalhes, tentativas, MAXIMO_TENTATIVAS, tempo));

        binding.painelResultadoCodigo.setVisibility(View.VISIBLE);
        int animacao = venceu ? R.anim.sucesso_cofre : R.anim.falha_cofre;
        binding.painelResultadoCodigo.startAnimation(AnimationUtils.loadAnimation(this, animacao));
    }

    private String formatarCodigo() {
        StringBuilder texto = new StringBuilder();
        for (int i = 0; i < TAMANHO_CODIGO; i++) {
            if (i > 0) {
                texto.append(" ");
            }
            texto.append(codigo[i]);
        }
        return texto.toString();
    }

    private void esconderTeclado() {
        InputMethodManager teclado = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        teclado.hideSoftInputFromWindow(binding.editPalpite.getWindowToken(), 0);
    }

    private void encerrarSessao() {
        Intent intent = new Intent(this, ApresentacaoActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra(ApresentacaoActivity.EXTRA_ULTIMO_MODULO, getString(R.string.modulo_codigo));
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
