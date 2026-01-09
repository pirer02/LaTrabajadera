package com.example.trabajadera.CrearPaso.FragCrearPaso3;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trabajadera.R;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class TrabajaderaRowAdapter extends RecyclerView.Adapter<TrabajaderaRowAdapter.FilaVH> {

    public interface OnRowAction {
        void moveRowUp(int rowIndex);
        void moveRowDown(int rowIndex);
        void swapRequest(int fromRow, int fromCol, int targetPosNumber);
    }

    private final Context context;
    // grid es una lista de filas, cada fila una lista de 5 PositionCell
    private final List<List<PositionCell>> grid;
    private final OnRowAction listener;

    public TrabajaderaRowAdapter(Context ctx, List<List<PositionCell>> grid, OnRowAction listener) {
        this.context = ctx;
        this.grid = grid;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FilaVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_trabajadera_row, parent, false);
        return new FilaVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull FilaVH h, int rowIndex) {
        List<PositionCell> fila = grid.get(rowIndex);

        h.txtTituloFila.setText("Trabajadera " + (rowIndex + 1));

        // base index para mostrar #posiciones: (rowIndex * 5) + 1
        int baseIndex = rowIndex * 5 + 1;

        // Configurar recycler horizontal de celdas
        h.recyclerCeldas.setLayoutManager(
                new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        );
        PosicionAdapter adapter = new PosicionAdapter(
                context,
                fila,
                baseIndex,
                (fromRow, fromCol, targetPos) -> listener.swapRequest(fromRow, fromCol, targetPos)
        );
        h.recyclerCeldas.setAdapter(adapter);

        // Botones mover fila
        h.btnSubirFila.setEnabled(rowIndex > 0);
        h.btnBajarFila.setEnabled(rowIndex < grid.size() - 1);

        h.btnSubirFila.setOnClickListener(v -> listener.moveRowUp(rowIndex));
        h.btnBajarFila.setOnClickListener(v -> listener.moveRowDown(rowIndex));
    }

    @Override
    public int getItemCount() {
        return grid.size();
    }

    static class FilaVH extends RecyclerView.ViewHolder {
        TextView txtTituloFila;
        RecyclerView recyclerCeldas;
        MaterialButton btnSubirFila, btnBajarFila;

        FilaVH(@NonNull View itemView) {
            super(itemView);
            txtTituloFila = itemView.findViewById(R.id.txtTituloFila);
            recyclerCeldas = itemView.findViewById(R.id.recyclerCeldas);
            btnSubirFila = itemView.findViewById(R.id.btnSubirFila);
            btnBajarFila = itemView.findViewById(R.id.btnBajarFila);
        }
    }
}
