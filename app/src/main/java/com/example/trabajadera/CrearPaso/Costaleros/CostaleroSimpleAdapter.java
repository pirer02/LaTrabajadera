package com.example.trabajadera.CrearPaso.Costaleros;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trabajadera.CrearPaso.Costaleros.Costalero;
import com.example.trabajadera.R;

import java.util.List;

public class CostaleroSimpleAdapter extends RecyclerView.Adapter<CostaleroSimpleAdapter.ViewHolder> {

    private final List<Costalero> costaleros;

    public CostaleroSimpleAdapter(List<Costalero> costaleros) {
        this.costaleros = costaleros;
    }

    @NonNull
    @Override
    public CostaleroSimpleAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_costalero_simple, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CostaleroSimpleAdapter.ViewHolder holder, int position) {
        Costalero c = costaleros.get(position);
        holder.txtNombre.setText(c.getNombre() + " " + c.getApellido());
        holder.txtAltura.setText(c.getAlturaTotal() + " cm" +
                (c.getSuplementos() > 0 ? " (" + c.getSuplementos() + " sup)" : ""));
    }

    @Override
    public int getItemCount() {
        return costaleros.size();
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
