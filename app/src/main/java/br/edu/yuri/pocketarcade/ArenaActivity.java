package br.edu.yuri.pocketarcade;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

import br.edu.yuri.pocketarcade.databinding.ActivityArenaBinding;

public class ArenaActivity extends AppCompatActivity {

    public static final String EXTRA_APELIDO = "br.edu.yuri.pocketarcade.arena.APELIDO";
    private static final String TAG = "ArenaActivity";

    private static final int PEDRA = 0;
    private static final int PAPEL = 1;
    private static final int TESOURA = 2;
    private static final int VITORIAS_PARA_GANHAR = 2;

    private ActivityArenaBinding binding;
    private String apelido;

    private final Random sorteio = new Random();
    private int vitoriasJogador;
    private int vitoriasDispositivo;
    private boolean partidaEncerrada;

    // Um MediaPlayer para cada efeito: são criados uma vez no onCreate e reutilizados.
    private MediaPlayer somVitoria;
    private MediaPlayer somDerrota;
    private MediaPlayer somEmpate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityArenaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Log.d(TAG, "onCreate");

        if (!receberDados()) {
            return;
        }
        binding.textJogador.setText(getString(R.string.jogador_formato, apelido));

        criarSons();
        configurarAcoes();
        reiniciarPartida();
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

    private void criarSons() {
        somVitoria = MediaPlayer.create(this, R.raw.som_vitoria);
        somDerrota = MediaPlayer.create(this, R.raw.som_derrota);
        somEmpate = MediaPlayer.create(this, R.raw.som_empate);
    }

    private void configurarAcoes() {
        binding.buttonPedra.setOnClickListener(view -> jogar(PEDRA));
        binding.buttonPapel.setOnClickListener(view -> jogar(PAPEL));
        binding.buttonTesoura.setOnClickListener(view -> jogar(TESOURA));
        binding.buttonNovaPartida.setOnClickListener(view -> reiniciarPartida());
        binding.buttonVoltar.setOnClickListener(view -> finish());
        binding.buttonEncerrarSessao.setOnClickListener(view -> encerrarSessao());
    }

    private void jogar(int escolhaJogador) {
        if (partidaEncerrada) {
            return;
        }
        int escolhaDispositivo = sorteio.nextInt(3);
        mostrarEscolhas(escolhaJogador, escolhaDispositivo);

        int mensagemRodada;
        MediaPlayer som;

        if (escolhaJogador == escolhaDispositivo) {
            mensagemRodada = R.string.rodada_empate;
            som = somEmpate;
        } else if ((escolhaJogador == PEDRA && escolhaDispositivo == TESOURA)
                || (escolhaJogador == PAPEL && escolhaDispositivo == PEDRA)
                || (escolhaJogador == TESOURA && escolhaDispositivo == PAPEL)) {
            // Pedra ganha de tesoura, papel ganha de pedra, tesoura ganha de papel.
            vitoriasJogador++;
            mensagemRodada = R.string.rodada_vitoria;
            som = somVitoria;
        } else {
            vitoriasDispositivo++;
            mensagemRodada = R.string.rodada_derrota;
            som = somDerrota;
        }

        atualizarPlacar();
        String mensagem = getString(mensagemRodada);

        // Quem chegar a duas vitórias primeiro ganha a partida.
        if (vitoriasJogador == VITORIAS_PARA_GANHAR) {
            partidaEncerrada = true;
            habilitarJogadas(false);
            mensagem += "\n" + getString(R.string.final_vitoria, vitoriasJogador, vitoriasDispositivo);
        } else if (vitoriasDispositivo == VITORIAS_PARA_GANHAR) {
            partidaEncerrada = true;
            habilitarJogadas(false);
            mensagem += "\n" + getString(R.string.final_derrota, vitoriasJogador, vitoriasDispositivo);
        }

        binding.textResultado.setText(mensagem);
        binding.textResultado.startAnimation(
                AnimationUtils.loadAnimation(this, R.anim.mensagem_resultado));
        tocarSom(som);
    }

    private void mostrarEscolhas(int escolhaJogador, int escolhaDispositivo) {
        binding.imageEscolhaJogador.setImageResource(imagemDaJogada(escolhaJogador));
        binding.imageEscolhaDispositivo.setImageResource(imagemDaJogada(escolhaDispositivo));
        binding.imageEscolhaJogador.setVisibility(View.VISIBLE);
        binding.imageEscolhaDispositivo.setVisibility(View.VISIBLE);

        binding.imageEscolhaJogador.startAnimation(
                AnimationUtils.loadAnimation(this, R.anim.jogada_jogador));
        binding.imageEscolhaDispositivo.startAnimation(
                AnimationUtils.loadAnimation(this, R.anim.jogada_dispositivo));
    }

    private int imagemDaJogada(int jogada) {
        if (jogada == PEDRA) {
            return R.drawable.img_pedra;
        } else if (jogada == PAPEL) {
            return R.drawable.img_papel;
        } else {
            return R.drawable.img_tesoura;
        }
    }

    private void atualizarPlacar() {
        binding.textPlacar.setText(
                getString(R.string.placar_formato, vitoriasJogador, vitoriasDispositivo));
    }

    // Zera o placar e deixa a Arena pronta para uma nova partida.
    private void reiniciarPartida() {
        vitoriasJogador = 0;
        vitoriasDispositivo = 0;
        partidaEncerrada = false;

        atualizarPlacar();
        habilitarJogadas(true);

        binding.imageEscolhaJogador.clearAnimation();
        binding.imageEscolhaDispositivo.clearAnimation();
        binding.imageEscolhaJogador.setVisibility(View.INVISIBLE);
        binding.imageEscolhaDispositivo.setVisibility(View.INVISIBLE);

        binding.textResultado.clearAnimation();
        binding.textResultado.setText(R.string.arena_escolha_msg);
    }

    private void habilitarJogadas(boolean habilitar) {
        float opacidade = habilitar ? 1f : 0.4f;
        binding.buttonPedra.setEnabled(habilitar);
        binding.buttonPapel.setEnabled(habilitar);
        binding.buttonTesoura.setEnabled(habilitar);
        binding.buttonPedra.setAlpha(opacidade);
        binding.buttonPapel.setAlpha(opacidade);
        binding.buttonTesoura.setAlpha(opacidade);
    }

    private void tocarSom(MediaPlayer som) {
        if (som == null) {
            return;
        }
        if (som.isPlaying()) {
            som.pause();
        }
        som.seekTo(0);
        som.start();
    }

    private void pausarSom(MediaPlayer som) {
        if (som != null && som.isPlaying()) {
            som.pause();
        }
    }

    private void encerrarSessao() {
        Intent intent = new Intent(this, ApresentacaoActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra(ApresentacaoActivity.EXTRA_ULTIMO_MODULO, getString(R.string.modulo_arena));
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
    }

    // Ao sair da tela o áudio é pausado (sem stop()).
    @Override
    protected void onPause() {
        super.onPause();
        pausarSom(somVitoria);
        pausarSom(somDerrota);
        pausarSom(somEmpate);
        Log.d(TAG, "onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
    }

    // release() libera os recursos e leva cada player ao estado End.
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (somVitoria != null) {
            somVitoria.release();
            somVitoria = null;
        }
        if (somDerrota != null) {
            somDerrota.release();
            somDerrota = null;
        }
        if (somEmpate != null) {
            somEmpate.release();
            somEmpate = null;
        }
        Log.d(TAG, "onDestroy");
    }
}
