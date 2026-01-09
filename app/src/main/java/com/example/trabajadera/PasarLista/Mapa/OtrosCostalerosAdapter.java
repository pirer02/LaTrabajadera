package com.example.trabajadera.PasarLista.Mapa;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trabajadera.CrearPaso.Costaleros.Costalero;
import com.example.trabajadera.R;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class OtrosCostalerosAdapter extends RecyclerView.Adapter<OtrosCostalerosAdapter.ViewHolder> {

    public interface OnCostaleroSeleccionadoListener {
        void onCostaleroSeleccionado(Costalero costalero);
    }

    private final List<Costalero> listaOriginal;
    private final List<Costalero> listaFiltrada;
    private final OnCostaleroSeleccionadoListener listener;

    public OtrosCostalerosAdapter(List<Costalero> costaleros,
                                  OnCostaleroSeleccionadoListener listener) {
        this.listaOriginal = new ArrayList<>(costaleros);
        this.listaFiltrada = new ArrayList<>(costaleros);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_costalero_otro, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Costalero c = listaFiltrada.get(position);

        h.txtNombre.setText(c.getNombre() + " " + c.getApellido());
        h.txtAltura.setText("Altura: " + c.getAltura() + " cm");
        h.txtSuplementos.setText("Suplementos: " + c.getSuplementos());
        h.txtAlturaTotal.setText("Altura Final: " + c.getAlturaTotal() + " cm");

        // Botón + suplemento
        h.btnAddSup.setOnClickListener(v -> {
            c.addSuplemento();
            notifyItemChanged(position);
        });

        // Botón - suplemento
        h.btnRemoveSup.setOnClickListener(v -> {
            c.removeSuplemento();
            notifyItemChanged(position);
        });

        // Botón Aceptar
        h.btnAceptar.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCostaleroSeleccionado(c);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaFiltrada.size();
    }

    public void filtrar(String query) {
        listaFiltrada.clear();
        if (query == null || query.trim().isEmpty()) {
            listaFiltrada.addAll(listaOriginal);
        } else {
            String lower = query.toLowerCase();
            for (Costalero c : listaOriginal) {
                if ((c.getNombre() != null && c.getNombre().toLowerCase().contains(lower)) ||
                        (c.getApellido() != null && c.getApellido().toLowerCase().contains(lower))) {
                    listaFiltrada.add(c);
                }
            }
        }
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtNombre, txtAltura, txtSuplementos, txtAlturaTotal;
        MaterialButton btnAddSup, btnRemoveSup, btnAceptar;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNombre = itemView.findViewById(R.id.txtNombre);
            txtAltura = itemView.findViewById(R.id.txtAltura);
            txtSuplementos = itemView.findViewById(R.id.txtSuplementos);
            txtAlturaTotal = itemView.findViewById(R.id.txtAlturaTotal);
            btnAddSup = itemView.findViewById(R.id.btnAddSup);
            btnRemoveSup = itemView.findViewById(R.id.btnRemoveSup);
            btnAceptar = itemView.findViewById(R.id.btnAceptar);
        }
    }
}
