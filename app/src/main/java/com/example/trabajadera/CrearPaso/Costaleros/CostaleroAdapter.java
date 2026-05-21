package com.example.trabajadera.CrearPaso.Costaleros;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trabajadera.R;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class CostaleroAdapter extends RecyclerView.Adapter<CostaleroAdapter.CostaleroViewHolder> {

    private final List<Costalero> listaOriginal = new ArrayList<>();
    private final List<Costalero> listaFiltrada = new ArrayList<>();

    public CostaleroAdapter(List<Costalero> lista) {
        setData(lista);
    }

    public void setData(List<Costalero> nuevaLista) {
        listaOriginal.clear();
        if (nuevaLista != null) {
            listaOriginal.addAll(nuevaLista);
        }
        listaFiltrada.clear();
        listaFiltrada.addAll(listaOriginal);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CostaleroViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_costalero_editar, parent, false);
        return new CostaleroViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CostaleroViewHolder holder, int position) {
        Costalero c = listaFiltrada.get(position);

        holder.txtNombre.setText((c.getNombre() != null ? c.getNombre() : "") + " " + (c.getApellido() != null ? c.getApellido() : ""));
        holder.txtAlturaBase.setText("Altura base: " + ((int) c.getAltura()) + " cm");
        holder.txtSuplementos.setText("Suplemento: " + c.getSuplementos() + " cm");
        holder.txtAlturaFinal.setText("Altura Final: " + String.format(java.util.Locale.US, "%.1f", c.getAlturaTotal()) + " cm");

        // Ocultamos el bloque de asistencia en la fase de creación pura por consistencia
        View parentCheck = (View) holder.chkAsistencia.getParent();
        if (parentCheck != null) {
            parentCheck.setVisibility(View.INVISIBLE);
        }

        holder.btnCambiarSuplemento.setOnClickListener(v -> {
            com.google.android.material.bottomsheet.BottomSheetDialog dialog =
                    new com.google.android.material.bottomsheet.BottomSheetDialog(v.getContext());

            View view = LayoutInflater.from(v.getContext()).inflate(R.layout.dialog_editar_suplemento, null);
            dialog.setContentView(view);

            TextView tvNombre = view.findViewById(R.id.tvNombreCostaleroDialogo);
            com.google.android.material.textfield.TextInputEditText etValor = view.findViewById(R.id.etValorSuplemento);
            MaterialButton btnGuardar = view.findViewById(R.id.btnGuardarSuplemento);

            tvNombre.setText(c.getNombre() + " " + c.getApellido());
            etValor.setText(String.valueOf(c.getSuplementos()));

            etValor.setFilters(new android.text.InputFilter[] { (source, start, end, dest, dstart, dend) -> {
                StringBuilder sb = new StringBuilder(dest);
                sb.replace(dstart, dend, source.subSequence(start, end).toString());
                String resultado = sb.toString();

                if (resultado.isEmpty()) return null;
                if (!resultado.matches("^\\d*(\\.\\d{0,3})?$")) {
                    return "";
                }
                return null;
            }});

            btnGuardar.setOnClickListener(v1 -> {
                String input = etValor.getText().toString().trim();
                if (input.isEmpty()) input = "0";

                try {
                    float nuevoSuplemento = Float.parseFloat(input);
                    c.setSuplementos(nuevoSuplemento);

                    holder.txtSuplementos.setText("Suplemento: " + input + " cm");
                    holder.txtAlturaFinal.setText("Altura Final: " + String.format(java.util.Locale.US, "%.1f", c.getAlturaTotal()) + " cm");

                    dialog.dismiss();
                } catch (NumberFormatException e) {
                    etValor.setError("Formato incorrecto");
                }
            });

            dialog.show();
        });
    }

    @Override
    public int getItemCount() {
        return listaFiltrada.size();
    }

    public void filtrar(String texto) {
        String t = texto == null ? "" : texto.trim().toLowerCase();
        listaFiltrada.clear();
        if (t.isEmpty()) {
            listaFiltrada.addAll(listaOriginal);
        } else {
            for (Costalero c : listaOriginal) {
                if ((c.getNombre() != null && c.getNombre().toLowerCase().contains(t)) ||
                        (c.getApellido() != null && c.getApellido().toLowerCase().contains(t))) {
                    listaFiltrada.add(c);
                }
            }
        }
        notifyDataSetChanged();
    }

    public Costalero getItem(int position) {
        return listaFiltrada.get(position);
    }

    public void removeItem(int position) {
        Costalero eliminado = listaFiltrada.remove(position);
        listaOriginal.remove(eliminado);
        notifyItemRemoved(position);
    }

    static class CostaleroViewHolder extends RecyclerView.ViewHolder {
        TextView txtNombre, txtAlturaBase, txtSuplementos, txtAlturaFinal;
        CheckBox chkAsistencia;
        MaterialButton btnCambiarSuplemento;

        public CostaleroViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNombre = itemView.findViewById(R.id.txtNombre);
            txtAlturaBase = itemView.findViewById(R.id.txtAlturaBase);
            txtSuplementos = itemView.findViewById(R.id.txtSuplementos);
            txtAlturaFinal = itemView.findViewById(R.id.txtAlturaFinal);
            chkAsistencia = itemView.findViewById(R.id.chkAsistencia);
            btnCambiarSuplemento = itemView.findViewById(R.id.btnCambiarSuplemento);
        }
    }
}