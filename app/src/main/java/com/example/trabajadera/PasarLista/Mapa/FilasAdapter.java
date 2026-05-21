package com.example.trabajadera.PasarLista.Mapa;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trabajadera.PasarLista.PositionCell;
import com.example.trabajadera.R;

import java.util.List;

public class FilasAdapter {

    private final List<List<PositionCell>> grid;
    private final PosicionesAdapter.OnCellClickListener onCellClick;

    public FilasAdapter(List<List<PositionCell>> grid,
                        PosicionesAdapter.OnCellClickListener onCellClick) {
        this.grid = grid;
        this.onCellClick = onCellClick;
    }

    /**
     * Reemplaza a onCreateViewHolder. Este método simplemente infla y devuelve la vista de la fila.
     * @param parent El ViewGroup padre (layoutContenedorFilas).
     * @return El ViewHolder con la vista inflada.
     */
    public FilaViewHolder createViewHolder(@NonNull ViewGroup parent) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_fila_mapa, parent, false);
        return new FilaViewHolder(v);
    }

    /**
     * Mantiene la lógica original de onBindViewHolder para enlazar datos a la vista.
     * @param h El ViewHolder que contiene el RecyclerView horizontal.
     * @param position El índice de la fila en el grid.
     */
    public void bindViewHolder(@NonNull FilaViewHolder h, int position) {
        List<PositionCell> fila = grid.get(position);

        // Aquí se crea el adaptador de posiciones para el RecyclerView horizontal interno.
        PosicionesAdapter adapter = new PosicionesAdapter(fila, onCellClick);
        h.recyclerPosiciones.setAdapter(adapter);
    }

    // Eliminamos getItemCount() ya que se controla en FragMapaCuadrilla con grid.size()

    // El ViewHolder es ahora una clase simple, pero MANTIENE la herencia de RecyclerView.ViewHolder
    // porque contiene y configura un RecyclerView interno.
    public static class FilaViewHolder extends RecyclerView.ViewHolder {
        public RecyclerView recyclerPosiciones; // Lo hacemos público para que FragMapaCuadrilla lo pueda acceder si fuera necesario

        public FilaViewHolder(@NonNull View itemView) {
            super(itemView);
            recyclerPosiciones = itemView.findViewById(R.id.recyclerPosiciones);

            // La configuración del LayoutManager para el RecyclerView horizontal interno se mantiene
            recyclerPosiciones.setLayoutManager(
                    new LinearLayoutManager(itemView.getContext(), LinearLayoutManager.HORIZONTAL, false)
            );
        }
    }
}