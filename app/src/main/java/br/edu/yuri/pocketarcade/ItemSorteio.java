package br.edu.yuri.pocketarcade;

/** Modelo de um item do Randomizador: uma palavra e a categoria a que pertence. */
public class ItemSorteio {

    private final String palavra;
    private final String categoria;

    public ItemSorteio(String palavra, String categoria) {
        this.palavra = palavra;
        this.categoria = categoria;
    }

    public String getPalavra() {
        return palavra;
    }

    public String getCategoria() {
        return categoria;
    }
}
