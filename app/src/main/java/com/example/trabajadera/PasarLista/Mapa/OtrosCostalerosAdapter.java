package com.example.trabajadera.PasarLista.Mapa;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trabajadera.CrearPaso.Costaleros.Costalero;
import com.example.trabajadera.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_costalero_otro, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Costalero c = listaFiltrada.get(position);

        holder.txtNombre.setText(c.getNombre() + " " + c.getApellido());
        holder.txtAltura.setText("Altura base: " + c.getAltura() + " cm");

        actualizarTextosSuplemento(holder, c);

        // Botón para editar suplemento con el nuevo BottomSheet
        holder.btnEditarSuplemento.setOnClickListener(v -> {
            mostrarDialogoEdicionSuplemento(v.getContext(), c, holder);
        });

        // Botón para confirmar selección (el de la derecha)
        holder.btnAceptar.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCostaleroSeleccionado(c);
            }
        });
    }

    private void actualizarTextosSuplemento(ViewHolder holder, Costalero c) {
        holder.txtSuplementos.setText(String.format(Locale.getDefault(), "Suplemento: %.2f cm", (double) c.getSuplementos()));
        holder.txtAlturaTotal.setText(String.format(Locale.getDefault(), "Altura Final: %.2f cm", (double) c.getAlturaTotal()));
    }

    private void mostrarDialogoEdicionSuplemento(Context context, Costalero costalero, ViewHolder holder) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_editar_suplemento, null);
        bottomSheetDialog.setContentView(view);

        TextView tvNombre = view.findViewById(R.id.tvNombreCostaleroDialogo);
        TextInputEditText etSuplemento = view.findViewById(R.id.etValorSuplemento);
        MaterialButton btnGuardar = view.findViewById(R.id.btnGuardarSuplemento);

        tvNombre.setText(costalero.getNombre() + " " + costalero.getApellido());
        String actual = String.valueOf(costalero.getSuplementos()).replace('.', ',');
        etSuplemento.setText(actual);

        btnGuardar.setOnClickListener(v -> {
            String valorStr = etSuplemento.getText().toString().trim();
            if (!valorStr.isEmpty()) {
                try {
                    double nuevoSup = Double.parseDouble(valorStr.replace(',', '.'));
                    costalero.setSuplementos((int) nuevoSup);

                    // Actualizamos el item en la lista del buscador
                    actualizarTextosSuplemento(holder, costalero);

                    bottomSheetDialog.dismiss();
                } catch (NumberFormatException e) {
                    Toast.makeText(context, "Formato incorrecto", Toast.LENGTH_SHORT).show();
                }
            }
        });

        bottomSheetDialog.show();
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
        MaterialButton btnEditarSuplemento, btnAceptar;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNombre = itemView.findViewById(R.id.txtNombre);
            txtAltura = itemView.findViewById(R.id.txtAltura);
            txtSuplementos = itemView.findViewById(R.id.txtSuplementos);
            txtAlturaTotal = itemView.findViewById(R.id.txtAlturaTotal);
            btnEditarSuplemento = itemView.findViewById(R.id.btnEditarSuplementoOtro);
            btnAceptar = itemView.findViewById(R.id.btnAceptar);
        }
    }
}