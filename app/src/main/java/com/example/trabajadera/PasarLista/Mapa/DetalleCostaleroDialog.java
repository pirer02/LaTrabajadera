package com.example.trabajadera.PasarLista.Mapa;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.trabajadera.CrearPaso.Costaleros.Costalero;
import com.example.trabajadera.PasarLista.PositionCell;
import com.example.trabajadera.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Locale;

public class DetalleCostaleroDialog extends DialogFragment {

    public interface OnAccionListener {
        void onIniciarCambioPosicion(int posAbs);
        void onDatosCostaleroEditados();
    }

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

        if (cell != null && cell.costalero != null) {
            actualizarTextos(cell.costalero, txtNombre, txtApellido, txtAltura, txtPosicion);
        }

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
        if (n != null) n.setText("Nombre: " + c.getNombre());
        if (a != null) a.setText("Apellido: " + c.getApellido());
        if (alt != null) alt.setText("Altura: " + c.getAltura() + " cm");

        txtSuplemento.setText(String.format(Locale.getDefault(), "Suplemento: %.2f cm", (double) c.getSuplementos()));
        txtAlturaTotal.setText(String.format(Locale.getDefault(), "Altura Final: %.2f cm", (double) c.getAlturaTotal()));
        if (pos != null) pos.setText("Posición: " + cell.posicionAbs);
    }

    private void mostrarDialogoInputSuplemento() {
        if (cell == null || cell.costalero == null) return;

        // 1. Usamos BottomSheetDialog para que aparezca desde abajo
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());

        View customView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_editar_suplemento, null);
        bottomSheetDialog.setContentView(customView);

        TextView tvNombre = customView.findViewById(R.id.tvNombreCostaleroDialogo);
        TextInputEditText etSuplemento = customView.findViewById(R.id.etValorSuplemento);
        MaterialButton btnGuardar = customView.findViewById(R.id.btnGuardarSuplemento);

        tvNombre.setText(cell.costalero.getNombre() + " " + cell.costalero.getApellido());

        // Cargar suplemento actual (cambiando punto por coma)
        String actual = String.valueOf(cell.costalero.getSuplementos()).replace('.', ',');
        etSuplemento.setText(actual);

        btnGuardar.setOnClickListener(v -> {
            String valorStr = etSuplemento.getText().toString().trim();

            if (!valorStr.isEmpty()) {
                try {
                    double nuevoSuplemento = Double.parseDouble(valorStr.replace(',', '.'));

                    // Guardamos en memoria (mantenemos el cast a int si tu modelo es int)
                    cell.costalero.setSuplementos((int) nuevoSuplemento);

                    // Actualizamos la tarjeta de detalle (la que está detrás)
                    actualizarTextos(cell.costalero, null, null, null, null);

                    // Refrescamos el Fragment principal
                    OnAccionListener listener = getListener();
                    if (listener != null) {
                        listener.onDatosCostaleroEditados();
                    }

                    bottomSheetDialog.dismiss();

                } catch (NumberFormatException e) {
                    Toast.makeText(getContext(), "Formato de número incorrecto", Toast.LENGTH_SHORT).show();
                }
            } else {
                etSuplemento.setError("Introduce un valor");
            }
        });

        bottomSheetDialog.show();
    }

    private OnAccionListener getListener() {
        if (getTargetFragment() instanceof OnAccionListener) return (OnAccionListener) getTargetFragment();
        if (getParentFragment() instanceof OnAccionListener) return (OnAccionListener) getParentFragment();
        if (getActivity() instanceof OnAccionListener) return (OnAccionListener) getActivity();
        return null;
    }
}