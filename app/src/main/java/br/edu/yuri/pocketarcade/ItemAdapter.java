package br.edu.yuri.pocketarcade;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import br.edu.yuri.pocketarcade.databinding.ItemPalavraBinding;

// Entrega ao RecyclerView as views dos itens da categoria selecionada.
public class ItemAdapter extends RecyclerView.Adapter<ItemViewHolder> {

    public interface OnItemLongClickListener {
        void aoSegurarItem(int posicao);
    }

    private final List<ItemSorteio> itens;
    private final OnItemLongClickListener listener;

    public ItemAdapter(List<ItemSorteio> itens, OnItemLongClickListener listener) {
        this.itens = itens;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPalavraBinding binding = ItemPalavraBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ItemViewHolder(binding, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        holder.bind(itens.get(position));
    }

    @Override
    public int getItemCount() {
        return itens.size();
    }
}
