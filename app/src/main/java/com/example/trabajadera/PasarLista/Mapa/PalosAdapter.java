package com.example.trabajadera.PasarLista.Mapa;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trabajadera.PasarLista.PositionCell;
import com.example.trabajadera.R;
import com.google.android.flexbox.FlexboxLayout;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.JustifyContent;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class PalosAdapter extends RecyclerView.Adapter<PalosAdapter.PaloViewHolder> {

    public interface OnMapaCellListener {
        boolean isSwapModeActive();
        PositionCell getPendingSwap();
        void onSwapConfirmed(PositionCell target);
        void onNormalClick(PositionCell cell);
    }

    public interface OnPaloMoveListener {
        void onPaloMoved(int fromPosition, int toPosition);
        void onPaloMoveComplete();
    }

    private final List<List<PositionCell>> grid;
    public final OnPaloMoveListener moveListener;
    private final OnMapaCellListener cellListener;

    public PalosAdapter(List<List<PositionCell>> grid, OnMapaCellListener cellListener, OnPaloMoveListener moveListener) {
        this.grid = grid;
        this.cellListener = cellListener;
        this.moveListener = moveListener;
    }

    @NonNull
    @Override
    public PaloViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_palo, parent, false);
        return new PaloViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PaloViewHolder holder, int position) {
        List<PositionCell> row = grid.get(position);
        holder.txtTituloPalo.setText((position + 1) + "º Trabajadera");

        holder.flexboxCostaleros.removeAllViews();
        holder.flexboxCostaleros.setFlexWrap(FlexWrap.NOWRAP);
        holder.flexboxCostaleros.setJustifyContent(JustifyContent.CENTER);

        float porcentajeAncho = 1.0f / row.size();

        for (PositionCell cell : row) {
            View cellView = LayoutInflater.from(holder.itemView.getContext())
                    .inflate(R.layout.item_posicion_mapa, holder.flexboxCostaleros, false);

            FlexboxLayout.LayoutParams lp = (FlexboxLayout.LayoutParams) cellView.getLayoutParams();
            lp.setFlexGrow(1f);
            lp.setFlexBasisPercent(porcentajeAncho);
            cellView.setLayoutParams(lp);

            TextView txtAltura = cellView.findViewById(R.id.txtAlturaCompacto);
            TextView txtNombre = cellView.findViewById(R.id.txtNombreCompacto);
            TextView txtApellido = cellView.findViewById(R.id.txtApellidoCompacto);
            Button btnAqui = cellView.findViewById(R.id.btnAqui);
            Button btnCancelarSwap = cellView.findViewById(R.id.btnCancelarSwap);

            if (cell.costalero != null) {
                // CAMBIO: Formateo a 2 decimales con punto
                txtAltura.setText(String.format(Locale.US, "%.1f cm", cell.costalero.getAlturaTotal()));
                txtNombre.setText(cell.costalero.getNombre());
                txtApellido.setText(cell.costalero.getApellido());
                cellView.setAlpha(1.0f);
            } else {
                txtAltura.setText("");
                txtNombre.setText("Vacío");
                txtApellido.setText("");
                cellView.setAlpha(0.4f);
            }

            if (cellListener.isSwapModeActive()) {
                PositionCell pending = cellListener.getPendingSwap();
                if (pending == cell) {
                    btnAqui.setVisibility(View.GONE);
                    btnCancelarSwap.setVisibility(View.VISIBLE);
                    btnCancelarSwap.setText("FIN");
                    View.OnClickListener cancelClick = v -> cellListener.onNormalClick(cell);
                    btnCancelarSwap.setOnClickListener(cancelClick);
                    cellView.setOnClickListener(cancelClick);
                } else {
                    btnAqui.setVisibility(View.VISIBLE);
                    btnCancelarSwap.setVisibility(View.GONE);
                    btnAqui.setText("AQUÍ");
                    View.OnClickListener swapClick = v -> cellListener.onSwapConfirmed(cell);
                    btnAqui.setOnClickListener(swapClick);
                    cellView.setOnClickListener(swapClick);
                }
            } else {
                btnAqui.setVisibility(View.GONE);
                btnCancelarSwap.setVisibility(View.GONE);
                cellView.setBackgroundResource(R.drawable.bg_card);
                if (cell.costalero != null) {
                    cellView.setOnClickListener(v -> cellListener.onNormalClick(cell));
                } else {
                    cellView.setOnClickListener(null);
                }
            }
            holder.flexboxCostaleros.addView(cellView);
        }
    }

    @Override
    public int getItemCount() { return grid.size(); }

    public void onItemMove(int from, int to) {
        if (from < to) {
            for (int i = from; i < to; i++) Collections.swap(grid, i, i + 1);
        } else {
            for (int i = from; i > to; i--) Collections.swap(grid, i, i - 1);
        }
        notifyItemMoved(from, to);
        if (moveListener != null) moveListener.onPaloMoved(from, to);
    }

    public static class PaloViewHolder extends RecyclerView.ViewHolder {
        TextView txtTituloPalo;
        FlexboxLayout flexboxCostaleros;
        public PaloViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTituloPalo = itemView.findViewById(R.id.txtTituloPalo);
            flexboxCostaleros = itemView.findViewById(R.id.flexboxCostaleros);
        }
    }
}