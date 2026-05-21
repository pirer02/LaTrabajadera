package com.example.trabajadera.CrearPaso.FragCrearPaso4;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trabajadera.CrearPaso.FragCrearPaso3;
import com.example.trabajadera.R;

import java.util.List;

public class CuadrillaAsignacionAdapter extends RecyclerView.Adapter<CuadrillaAsignacionAdapter.CuadrillaViewHolder> {

    private final List<List<FragCrearPaso3.Asignacion>> cuadrillas;

    public CuadrillaAsignacionAdapter(List<List<FragCrearPaso3.Asignacion>> cuadrillas) {
        this.cuadrillas = cuadrillas;
    }

    @NonNull
    @Override
    public CuadrillaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cuadrilla, parent, false);
        return new CuadrillaViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CuadrillaViewHolder holder, int position) {
        List<FragCrearPaso3.Asignacion> cuadrilla = cuadrillas.get(position);
        holder.txtTitulo.setText("Cuadrilla " + (position + 1));

        // Adapter interno para mostrar costaleros con posición
        CostaleroAsignacionAdapter adapter = new CostaleroAsignacionAdapter(cuadrilla);
        holder.recyclerCostaleros.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));
        holder.recyclerCostaleros.setAdapter(adapter);
    }

    @Override
    public int getItemCount() {
        return cuadrillas.size();
    }

    static class CuadrillaViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitulo;
        RecyclerView recyclerCostaleros;

        public CuadrillaViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTitulo = itemView.findViewById(R.id.txtTituloCuadrilla);
            recyclerCostaleros = itemView.findViewById(R.id.recyclerCostalerosCuadrilla);
        }
    }
}

