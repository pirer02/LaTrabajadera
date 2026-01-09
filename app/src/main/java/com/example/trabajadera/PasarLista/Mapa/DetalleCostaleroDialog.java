package com.example.trabajadera.PasarLista.Mapa;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.trabajadera.CrearPaso.Costaleros.Costalero;
import com.example.trabajadera.PasarLista.PositionCell;
import com.example.trabajadera.R;
import com.google.android.material.button.MaterialButton;

public class DetalleCostaleroDialog extends DialogFragment {

    public interface OnAccionListener {
        void onIniciarCambioPosicion(int posAbs);
        // Nuevo método para avisar que los datos del costalero han cambiado en memoria
        void onDatosCostaleroEditados();
    }

    private static final String TAG = "DetalleCostaleroDialog";
    private static final String ARG_POS_ABS = "posAbs";
    private static final String ARG_SHOW_SWAP = "showSwap";

    private PositionCell cell;
    private TextView txtSuplemento, txtAlturaTotal;

    public static DetalleCostaleroDialog newInstance(PositionCell cell, boolean showSwapButton) {
        DetalleCostaleroDialog dialog = new DetalleCostaleroDialog();
        Bundle args = new Bundle();
        args.putInt(ARG_POS_ABS, cell.posicionAbs);
        args.putBoolean(ARG_SHOW_SWAP, showSwapButton);
        dialog.setArguments(args);
        dialog.cell = cell;
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_detalle_costalero, null);

        TextView txtNombre = view.findViewById(R.id.txtNombreDetalle);
        TextView txtApellido = view.findViewById(R.id.txtApellidoDetalle);
        TextView txtAltura = view.findViewById(R.id.txtAlturaDetalle);
        txtSuplemento = view.findViewById(R.id.txtSuplementoDetalle);
        txtAlturaTotal = view.findViewById(R.id.txtAlturaTotalDetalle);
        TextView txtPosicion = view.findViewById(R.id.txtPosicionDetalle);

        MaterialButton btnAtras = view.findViewById(R.id.botonAtras);
        MaterialButton btnCambiar = view.findViewById(R.id.btnCambiarPosicion);
        MaterialButton btnEditarSuplemento = view.findViewById(R.id.btnEditarSuplemento);

        // Cargar datos iniciales
        if (cell != null && cell.costalero != null) {
            actualizarTextos(cell.costalero, txtNombre, txtApellido, txtAltura, txtPosicion);
        }

        // Lógica para cambiar suplemento solo en memoria
        btnEditarSuplemento.setOnClickListener(v -> mostrarDialogoInputSuplemento());

        btnAtras.setOnClickListener(v -> dismiss());

        boolean showSwap = getArguments() != null && getArguments().getBoolean(ARG_SHOW_SWAP, true);
        if (showSwap) {
            btnCambiar.setVisibility(View.VISIBLE);
            btnCambiar.setOnClickListener(v -> {
                OnAccionListener listener = getListener();
                if (listener != null && cell != null) {
                    listener.onIniciarCambioPosicion(cell.posicionAbs);
                }
                dismiss();
            });
        } else {
            btnCambiar.setVisibility(View.GONE);
        }

        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(view);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        return dialog;
    }

    private void actualizarTextos(Costalero c, TextView n, TextView a, TextView alt, TextView pos) {
        n.setText("Nombre: " + c.getNombre());
        a.setText("Apellido: " + c.getApellido());
        alt.setText("Altura: " + c.getAltura() + " cm");
        txtSuplemento.setText("Suplemento: " + c.getSuplementos() + " cm");
        txtAlturaTotal.setText("Altura Final: " + c.getAlturaTotal() + " cm");
        pos.setText("Posición: " + cell.posicionAbs);
    }

    private void mostrarDialogoInputSuplemento() {
        if (cell == null || cell.costalero == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Nuevo Suplemento (cm)");

        final EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setText(String.valueOf(cell.costalero.getSuplementos()));
        builder.setView(input);

        builder.setPositiveButton("Terminar", (dialog, which) -> {
            String valor = input.getText().toString();
            if (!valor.isEmpty()) {
                int nuevoSuplemento = Integer.parseInt(valor);

                // 1. Modificamos el objeto en MEMORIA
                cell.costalero.setSuplementos(nuevoSuplemento);

                // 2. Actualizamos la interfaz del diálogo actual
                txtSuplemento.setText("Suplemento: " + nuevoSuplemento + " cm");
                txtAlturaTotal.setText("Altura Final: " + cell.costalero.getAlturaTotal() + " cm");

                // 3. Avisamos al Fragment para que refresque el mapa (notifyDataSetChanged)
                OnAccionListener listener = getListener();
                if (listener != null) {
                    listener.onDatosCostaleroEditados();
                }
            }
        });
        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    private OnAccionListener getListener() {
        if (getTargetFragment() instanceof OnAccionListener) return (OnAccionListener) getTargetFragment();
        if (getParentFragment() instanceof OnAccionListener) return (OnAccionListener) getParentFragment();
        if (getActivity() instanceof OnAccionListener) return (OnAccionListener) getActivity();
        return null;
    }
}