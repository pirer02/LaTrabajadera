package com.example.trabajadera.CrearPaso.FragCrearPaso4;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trabajadera.CrearPaso.FragCrearPaso3.FragCrearPaso3;
import com.example.trabajadera.R;

import java.util.List;

public class CostaleroAsignacionAdapter extends RecyclerView.Adapter<CostaleroAsignacionAdapter.ViewHolder> {

    private final List<FragCrearPaso3.Asignacion> asignaciones;

    public CostaleroAsignacionAdapter(List<FragCrearPaso3.Asignacion> asignaciones) {
        this.asignaciones = asignaciones;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_costalero_simple, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FragCrearPaso3.Asignacion a = asignaciones.get(position);

        if (a.nombre == null || a.nombre.isEmpty()) {
            holder.txtNombre.setText("(Vacío)");
            holder.txtAltura.setText("--");
        } else {
            holder.txtNombre.setText(a.nombre + " " + a.apellido);
            holder.txtAltura.setText(
                    "Pos: " + a.posicion +
                            " | Fila: " + (a.fila + 1) +
                            " Col: " + (a.columna + 1) +
                            " | Altura: " + a.altura + "cm" +
                            (a.suplementos > 0 ? " (+" + a.suplementos + " sup)" : "")
            );
        }
    }

    @Override
    public int getItemCount() {
        return asignaciones.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtNombre, txtAltura;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNombre = itemView.findViewById(R.id.txtNombreCostalero);
            txtAltura = itemView.findViewById(R.id.txtAlturaCostalero);
        }
    }
}
