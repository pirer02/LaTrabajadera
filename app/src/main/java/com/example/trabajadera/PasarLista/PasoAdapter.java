package com.example.trabajadera.PasarLista;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trabajadera.R;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class PasoAdapter extends RecyclerView.Adapter<PasoAdapter.ViewHolder> {

    public interface OnPasoClickListener {
        void onEditarClick(Paso paso);
        void onBorrarClick(Paso paso, int position); // se mantiene para que el swipe lo use
    }

    private List<Paso> lista;
    private List<Paso> listaOriginal;
    private final OnPasoClickListener listener;

    public PasoAdapter(List<Paso> lista, OnPasoClickListener listener) {
        this.lista = lista;
        this.listaOriginal = new ArrayList<>(lista);
        this.listener = listener;
    }

    public void setPasos(List<Paso> pasos) {
        this.lista = pasos;
        this.listaOriginal = new ArrayList<>(pasos);
        notifyDataSetChanged();
    }

    public void filtrar(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            lista = new ArrayList<>(listaOriginal);
        } else {
            String filtro = texto.toLowerCase();
            List<Paso> filtrados = new ArrayList<>();
            for (Paso p : listaOriginal) {
                if ((p.getPaso() != null && p.getPaso().toLowerCase().contains(filtro)) ||
                        (p.getHermandad() != null && p.getHermandad().toLowerCase().contains(filtro)) ||
                        (p.getCiudad() != null && p.getCiudad().toLowerCase().contains(filtro))) {
                    filtrados.add(p);
                }
            }
            lista = filtrados;
        }
        notifyDataSetChanged();
    }

    public void removePaso(int position) {
        if (position >= 0 && position < lista.size()) {
            Paso eliminado = lista.remove(position);
            listaOriginal.remove(eliminado);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, lista.size());
        }
    }

    // 👉 Nuevo método para swipe-to-delete
    public Paso getItem(int position) {
        if (position >= 0 && position < lista.size()) {
            return lista.get(position);
        }
        return null;
    }

    @NonNull
    @Override
    public PasoAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_paso, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PasoAdapter.ViewHolder h, int position) {
        Paso p = lista.get(position);

        h.txtNombrePaso.setText(p.getPaso());
        h.txtHermandadCiudad.setText(p.getHermandad() + " - " + p.getCiudad());
        h.txtTipoPaso.setText("Tipo: " + p.getTipoPaso());

        h.btnEditar.setOnClickListener(v -> {
            if (listener != null) listener.onEditarClick(p);
        });

        // 🔴 Eliminado el botón borrar, ahora se maneja con swipe
    }

    @Override
    public int getItemCount() {
        return lista != null ? lista.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtNombrePaso, txtHermandadCiudad, txtTipoPaso;
        MaterialButton btnEditar; // 🔴 btnBorrar eliminado

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNombrePaso = itemView.findViewById(R.id.txtNombrePaso);
            txtHermandadCiudad = itemView.findViewById(R.id.txtHermandadCiudad);
            txtTipoPaso = itemView.findViewById(R.id.txtTipoPaso);
            btnEditar = itemView.findViewById(R.id.btnEditarPaso);
            // 🔴 btnBorrar eliminado
        }
    }
}
