package br.edu.yuri.pocketarcade;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import br.edu.yuri.pocketarcade.databinding.ItemPalavraBinding;

// Guarda as views de um item para o RecyclerView poder reaproveitá-las.
public class ItemViewHolder extends RecyclerView.ViewHolder {

    private final ItemPalavraBinding binding;

    public ItemViewHolder(@NonNull ItemPalavraBinding binding,
                          ItemAdapter.OnItemLongClickListener listener) {
        super(binding.getRoot());
        this.binding = binding;

        // Clique longo: avisa a tela qual posição foi tocada.
        binding.getRoot().setOnLongClickListener(view -> {
            int posicao = getBindingAdapterPosition();
            if (posicao != RecyclerView.NO_POSITION) {
                listener.aoSegurarItem(posicao);
            }
            return true;
        });
    }

    public void bind(ItemSorteio item) {
        binding.textPalavra.setText(item.getPalavra());
        binding.textCategoria.setText(item.getCategoria());
    }
}
