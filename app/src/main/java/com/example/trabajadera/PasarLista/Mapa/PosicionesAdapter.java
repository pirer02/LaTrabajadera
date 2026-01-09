package com.example.trabajadera.PasarLista.Mapa;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trabajadera.CrearPaso.Costaleros.Costalero;
import com.example.trabajadera.PasarLista.PositionCell;
import com.example.trabajadera.R;

import java.util.List;

public class PosicionesAdapter extends RecyclerView.Adapter<PosicionesAdapter.ViewHolder> {

    public interface OnCellClickListener {
        boolean isSwapModeActive();
        PositionCell getPendingSwap();
        void onSwapConfirmed(PositionCell target);
        void onNormalClick(PositionCell cell);
    }

    private final List<PositionCell> posiciones;
    private final OnCellClickListener onCellClick;

    public PosicionesAdapter(List<PositionCell> posiciones,
                             OnCellClickListener onCellClick) {
        this.posiciones = posiciones;
        this.onCellClick = onCellClick;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_posicion_mapa, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        PositionCell cell = posiciones.get(position);

        // --- 1. CONFIGURACIÓN INICIAL DE CONTENIDO ---
        if (cell.costalero == null) {
            h.txtAlturaCompacto.setText("");
            h.txtNombreCompacto.setText("Vacío");
            h.txtApellidoCompacto.setText("");
        } else {
            Costalero c = cell.costalero;
            h.txtAlturaCompacto.setText(c.getAlturaTotal() + " cm");
            h.txtNombreCompacto.setText(c.getNombre() != null ? c.getNombre() : "");
            h.txtApellidoCompacto.setText(c.getApellido() != null ? c.getApellido() : "");
        }

        // --- 2. MANEJO DE ESTADOS VISUALES ---

        // Reset
        h.btnAqui.setVisibility(View.GONE);
        h.itemView.setOnClickListener(null);
        h.txtAlturaCompacto.setVisibility(View.VISIBLE);
        h.txtNombreCompacto.setVisibility(View.VISIBLE);
        h.txtApellidoCompacto.setVisibility(View.VISIBLE);


        if (onCellClick != null && onCellClick.isSwapModeActive()) {
            // **MODO SWAP ACTIVO**

            PositionCell pendingCell = onCellClick.getPendingSwap();

            if (pendingCell == cell) {
                // ESTADO 1: CELDA SELECCIONADA (Origen del Swap - Moviendo)

                h.btnAqui.setVisibility(View.VISIBLE);
                h.btnAqui.setText("Moviendo");
                h.btnAqui.setBackgroundTintList(
                        h.btnAqui.getContext().getResources().getColorStateList(R.color.black)
                );

                // El click en esta celda cancela el swap
                h.itemView.setOnClickListener(v -> onCellClick.onNormalClick(cell));

            } else {
                // ESTADO 2: CELDA DESTINO (Aquí)

                h.btnAqui.setVisibility(View.VISIBLE);

                // Si la celda está vacía, ocultamos los textos para dar protagonismo al botón
                if (cell.costalero == null) {
                    h.txtAlturaCompacto.setText("");
                    h.txtNombreCompacto.setText("");
                    h.txtApellidoCompacto.setText("");
                }

                h.btnAqui.setText("Aquí");
                h.btnAqui.setBackgroundTintList(
                        h.btnAqui.getContext().getResources().getColorStateList(R.color.orangeApp)
                );

                // El click en esta celda confirma el swap
                h.itemView.setOnClickListener(v -> onCellClick.onSwapConfirmed(cell));
            }

        } else {
            // **MODO NORMAL**
            h.btnAqui.setVisibility(View.GONE);

            // Si la celda está ocupada, el clic abre el diálogo (a través de onNormalClick del Fragmento).
            if (cell.costalero != null) {
                h.itemView.setOnClickListener(v -> onCellClick.onNormalClick(cell));
            }
            // Si está vacía en modo normal, no hay listener.
        }
    }

    @Override
    public int getItemCount() {
        return posiciones.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView txtNombreCompacto, txtApellidoCompacto, txtAlturaCompacto;
        Button btnAqui;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNombreCompacto = itemView.findViewById(R.id.txtNombreCompacto);
            txtApellidoCompacto = itemView.findViewById(R.id.txtApellidoCompacto);
            txtAlturaCompacto = itemView.findViewById(R.id.txtAlturaCompacto);
            btnAqui = itemView.findViewById(R.id.btnAqui);

            btnAqui.setOnClickListener(null);
        }
    }
}