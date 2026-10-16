package br.edu.yuri.pocketarcade;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import br.edu.yuri.pocketarcade.databinding.ActivityRandomizadorBinding;

public class RandomizadorActivity extends AppCompatActivity {

    public static final String EXTRA_APELIDO = "br.edu.yuri.pocketarcade.randomizador.APELIDO";
    private static final String TAG = "RandomizadorActivity";
    private static final int COLUNAS_DA_GRADE = 2;

    private ActivityRandomizadorBinding binding;
    private String apelido;

    private String[] categorias;
    private int categoriaAtual;
    private boolean modoGrade;

    // todosItens guarda tudo o que foi cadastrado; itensVisiveis é a lista
    // da categoria selecionada, que o Adapter mostra na tela.
    private final List<ItemSorteio> todosItens = new ArrayList<>();
    private final List<ItemSorteio> itensVisiveis = new ArrayList<>();
    private ItemAdapter adapter;

    private final Random sorteio = new Random();
    private MediaPlayer somSorteio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRandomizadorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Log.d(TAG, "onCreate");

        if (!receberDados()) {
            return;
        }
        binding.textJogador.setText(getString(R.string.jogador_formato, apelido));

        categorias = getResources().getStringArray(R.array.categorias);
        cadastrarExemplos();
        configurarLista();
        configurarAcoes();
        somSorteio = MediaPlayer.create(this, R.raw.som_sorteio);
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

    private void cadastrarExemplos() {
        adicionarExemplos(categorias[0], getResources().getStringArray(R.array.exemplos_filmes));
        adicionarExemplos(categorias[1], getResources().getStringArray(R.array.exemplos_comidas));
        adicionarExemplos(categorias[2], getResources().getStringArray(R.array.exemplos_lugares));
        adicionarExemplos(categorias[3], getResources().getStringArray(R.array.exemplos_desafios));
    }

    private void adicionarExemplos(String categoria, String[] palavras) {
        for (String palavra : palavras) {
            todosItens.add(new ItemSorteio(palavra, categoria));
        }
    }

    private void configurarLista() {
        adapter = new ItemAdapter(itensVisiveis, posicao -> removerItem(posicao));
        binding.recyclerItens.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerItens.setAdapter(adapter);

        categoriaAtual = 0;
        recarregarItensVisiveis();
    }

    private void configurarAcoes() {
        binding.spinnerCategoria.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view,
                                               int posicao, long id) {
                        if (posicao != categoriaAtual) {
                            categoriaAtual = posicao;
                            recarregarItensVisiveis();
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });

        binding.buttonAdicionar.setOnClickListener(view -> adicionarItem());
        binding.buttonSortear.setOnClickListener(view -> sortearItem());
        binding.buttonAlternar.setOnClickListener(view -> alternarLayout());
        binding.editValor.setOnEditorActionListener((view, actionId, event) -> {
            adicionarItem();
            return true;
        });
        binding.buttonVoltar.setOnClickListener(view -> finish());
        binding.buttonEncerrarSessao.setOnClickListener(view -> encerrarSessao());
    }

    // Mostra na lista apenas os itens da categoria selecionada.
    private void recarregarItensVisiveis() {
        int quantidadeAnterior = itensVisiveis.size();
        itensVisiveis.clear();
        adapter.notifyItemRangeRemoved(0, quantidadeAnterior);

        String categoria = categorias[categoriaAtual];
        for (ItemSorteio item : todosItens) {
            if (item.getCategoria().equals(categoria)) {
                itensVisiveis.add(item);
            }
        }
        adapter.notifyItemRangeInserted(0, itensVisiveis.size());
        binding.painelSorteio.setVisibility(View.GONE);
    }

    private void adicionarItem() {
        String valor = binding.editValor.getText().toString().trim();
        if (valor.isEmpty()) {
            binding.editValor.setError(getString(R.string.erro_valor_vazio));
            binding.editValor.requestFocus();
            return;
        }

        ItemSorteio novo = new ItemSorteio(valor, categorias[categoriaAtual]);
        todosItens.add(novo);
        itensVisiveis.add(novo);

        int posicao = itensVisiveis.size() - 1;
        adapter.notifyItemInserted(posicao);
        binding.recyclerItens.scrollToPosition(posicao);

        binding.editValor.setText("");
        binding.editValor.setError(null);
        esconderTeclado();
    }

    // Clique longo no item: remove da lista da tela e do cadastro geral.
    private void removerItem(int posicao) {
        ItemSorteio item = itensVisiveis.get(posicao);
        itensVisiveis.remove(posicao);
        todosItens.remove(item);
        adapter.notifyItemRemoved(posicao);

        Toast.makeText(this, getString(R.string.item_removido, item.getPalavra()),
                Toast.LENGTH_SHORT).show();
    }

    private void sortearItem() {
        String categoria = categorias[categoriaAtual];
        if (itensVisiveis.isEmpty()) {
            Toast.makeText(this, getString(R.string.sorteio_vazio, categoria),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        int indice = sorteio.nextInt(itensVisiveis.size());
        ItemSorteio sorteado = itensVisiveis.get(indice);
        binding.recyclerItens.scrollToPosition(indice);

        binding.textSorteado.setText(
                getString(R.string.sorteio_resultado, categoria, sorteado.getPalavra()));
        binding.painelSorteio.setVisibility(View.VISIBLE);
        binding.painelSorteio.startAnimation(
                AnimationUtils.loadAnimation(this, R.anim.mensagem_resultado));
        tocarSom();
    }

    // Troca entre lista (uma coluna) e grade (duas colunas).
    private void alternarLayout() {
        modoGrade = !modoGrade;
        if (modoGrade) {
            binding.recyclerItens.setLayoutManager(new GridLayoutManager(this, COLUNAS_DA_GRADE));
            binding.buttonAlternar.setText(R.string.botao_modo_lista);
        } else {
            binding.recyclerItens.setLayoutManager(new LinearLayoutManager(this));
            binding.buttonAlternar.setText(R.string.botao_modo_grade);
        }
    }

    private void tocarSom() {
        if (somSorteio == null) {
            return;
        }
        if (somSorteio.isPlaying()) {
            somSorteio.pause();
        }
        somSorteio.seekTo(0);
        somSorteio.start();
    }

    private void esconderTeclado() {
        InputMethodManager teclado = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        teclado.hideSoftInputFromWindow(binding.editValor.getWindowToken(), 0);
    }

    private void encerrarSessao() {
        Intent intent = new Intent(this, ApresentacaoActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra(ApresentacaoActivity.EXTRA_ULTIMO_MODULO, getString(R.string.modulo_randomizador));
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
        if (somSorteio != null && somSorteio.isPlaying()) {
            somSorteio.pause();
        }
        Log.d(TAG, "onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
    }

    // release() libera os recursos e leva o player ao estado End.
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (somSorteio != null) {
            somSorteio.release();
            somSorteio = null;
        }
        Log.d(TAG, "onDestroy");
    }
}
