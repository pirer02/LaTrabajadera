package com.example.trabajadera.CrearPaso.Costaleros;

import android.app.AlertDialog;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trabajadera.R;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class CostaleroEditarAdapter extends RecyclerView.Adapter<CostaleroEditarAdapter.ViewHolder> {

    private final List<Costalero> listaOriginal = new ArrayList<>();
    private final List<Costalero> listaFiltrada = new ArrayList<>();
    private final Consumer<Costalero> onEliminar;

    public CostaleroEditarAdapter(List<Costalero> lista, Consumer<Costalero> onEliminar) {
        this.onEliminar = onEliminar;
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
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_costalero_editar, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Costalero c = listaFiltrada.get(position);

        h.txtNombre.setText((c.getNombre() != null ? c.getNombre() : "") + " " + (c.getApellido() != null ? c.getApellido() : ""));

        h.txtAlturaBase.setText("Altura base: " + c.getAltura() + " cm");
        h.txtSuplementos.setText("Suplemento: " + c.getSuplementos() + " cm");
        h.txtAlturaFinal.setText("Altura Final: " + c.getAlturaTotal() + " cm");

        h.chkAsistencia.setOnCheckedChangeListener(null);
        h.chkAsistencia.setChecked(c.isAsistencia());
        h.chkAsistencia.setOnCheckedChangeListener((btn, checked) -> c.setAsistencia(checked));

        h.btnCambiarSuplemento.setOnClickListener(v -> {
            com.google.android.material.bottomsheet.BottomSheetDialog dialog =
                    new com.google.android.material.bottomsheet.BottomSheetDialog(v.getContext());

            View view = LayoutInflater.from(v.getContext()).inflate(R.layout.dialog_editar_suplemento, null);
            dialog.setContentView(view);

            TextView tvNombre = view.findViewById(R.id.tvNombreCostaleroDialogo);
            com.google.android.material.textfield.TextInputEditText etValor = view.findViewById(R.id.etValorSuplemento);
            MaterialButton btnGuardar = view.findViewById(R.id.btnGuardarSuplemento);

            tvNombre.setText(c.getNombre() + " " + c.getApellido());

            // 1. Mostrar valor actual con punto
            etValor.setText(String.valueOf(c.getSuplementos()));

            // 2. Filtro para limitar a 3 decimales (ahora con punto)
            etValor.setFilters(new android.text.InputFilter[] { (source, start, end, dest, dstart, dend) -> {
                StringBuilder sb = new StringBuilder(dest);
                sb.replace(dstart, dend, source.subSequence(start, end).toString());
                String resultado = sb.toString();

                if (resultado.isEmpty()) return null;

                // Regla: Solo números y un punto. Máximo 3 decimales.
                if (!resultado.matches("^\\d*(\\.\\d{0,1})?$")) {
                    return "";
                }
                return null;
            }});

            btnGuardar.setOnClickListener(v1 -> {
                String input = etValor.getText().toString().trim();
                if (input.isEmpty()) input = "0";

                try {
                    // Conversión directa y limpia
                    float nuevoSuplemento = Float.parseFloat(input);

                    // Guardar en el objeto
                    c.setSuplementos(nuevoSuplemento);

                    // Actualizar textos en la lista
                    h.txtSuplementos.setText("Suplemento: " + input + " cm");

                    // Formatear altura final con punto
                    String alturaTotal = String.format("%.1f", c.getAlturaTotal());
                    h.txtAlturaFinal.setText("Altura Final: " + alturaTotal + " cm");

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
                        (c.getApellido() != null && c.getApellido().toLowerCase().contains(t)) ||
                        String.valueOf(c.getAltura()).contains(t)) {
                    listaFiltrada.add(c);
                }
            }
        }
        notifyDataSetChanged();
    }

    public void seleccionarTodos() {
        for (Costalero c : listaOriginal) c.setAsistencia(true);
        notifyDataSetChanged();
    }

    public void deseleccionarTodos() {
        for (Costalero c : listaOriginal) c.setAsistencia(false);
        notifyDataSetChanged();
    }

    public Costalero getItem(int position) {
        return listaFiltrada.get(position);
    }

    public void removeItem(int position) {
        Costalero eliminado = listaFiltrada.remove(position);
        listaOriginal.remove(eliminado);
        notifyItemRemoved(position);
        if (onEliminar != null) onEliminar.accept(eliminado);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtNombre, txtAlturaBase, txtSuplementos, txtAlturaFinal;
        CheckBox chkAsistencia;
        MaterialButton btnCambiarSuplemento;

        ViewHolder(@NonNull View itemView) {
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