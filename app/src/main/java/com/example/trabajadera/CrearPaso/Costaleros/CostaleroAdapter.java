package com.example.trabajadera.CrearPaso.Costaleros;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trabajadera.R;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class CostaleroAdapter extends RecyclerView.Adapter<CostaleroAdapter.CostaleroViewHolder> {

    private List<Costalero> lista;

    public CostaleroAdapter(List<Costalero> lista) {
        this.lista = lista;
    }

    @NonNull
    @Override
    public CostaleroViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_costalero, parent, false);
        return new CostaleroViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CostaleroViewHolder holder, int position) {
        Costalero c = lista.get(position);
        holder.txtNombre.setText(c.getNombre() + " " + c.getApellido());
        holder.txtAltura.setText("Altura: " + c.getAlturaTotal() + " cm");
        holder.txtSuplementos.setText("Suplementos: " + c.getSuplementos());

        // Botón + suplemento
        holder.btnAddSuplemento.setOnClickListener(v -> {
            c.addSuplemento();
            notifyItemChanged(position);
        });

        // Botón - suplemento
        holder.btnRemoveSuplemento.setOnClickListener(v -> {
            c.removeSuplemento();
            notifyItemChanged(position);
        });

        // Botón eliminar costalero
        holder.btnEliminar.setOnClickListener(v -> {
            lista.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, lista.size());
        });
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    static class CostaleroViewHolder extends RecyclerView.ViewHolder {
        TextView txtNombre, txtAltura, txtSuplementos;
        MaterialButton btnAddSuplemento, btnRemoveSuplemento, btnEliminar;

        public CostaleroViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNombre = itemView.findViewById(R.id.txtNombreCostalero);
            txtAltura = itemView.findViewById(R.id.txtAlturaCostalero);
            txtSuplementos = itemView.findViewById(R.id.txtSuplementos);
            btnAddSuplemento = itemView.findViewById(R.id.btnAddSuplemento);
            btnRemoveSuplemento = itemView.findViewById(R.id.btnRemoveSuplemento);
            btnEliminar = itemView.findViewById(R.id.btnEliminarCostalero);
        }

    }


}

