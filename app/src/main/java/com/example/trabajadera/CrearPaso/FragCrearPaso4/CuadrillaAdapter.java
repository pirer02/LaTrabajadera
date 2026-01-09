package com.example.trabajadera.CrearPaso.FragCrearPaso4;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trabajadera.CrearPaso.Costaleros.Costalero;
import com.example.trabajadera.CrearPaso.Costaleros.CostaleroSimpleAdapter;
import com.example.trabajadera.R;

import java.util.List;

public class CuadrillaAdapter extends RecyclerView.Adapter<CuadrillaAdapter.CuadrillaViewHolder> {

    private final List<List<Costalero>> cuadrillas;
    private final Context context;

    public CuadrillaAdapter(List<List<Costalero>> cuadrillas) {
        this.cuadrillas = cuadrillas;
        this.context = null;
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
        List<Costalero> cuadrilla = cuadrillas.get(position);
        holder.txtTitulo.setText("Cuadrilla " + (position + 1));

        // Configurar el Recycler interno para mostrar costaleros
        CostaleroSimpleAdapter adapter = new CostaleroSimpleAdapter(cuadrilla);
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
