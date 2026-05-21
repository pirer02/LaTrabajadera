package com.example.trabajadera.CrearPaso.FragCrearPaso3;

import android.app.AlertDialog;
import android.content.Context;
import android.text.InputFilter;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trabajadera.R;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class PosicionAdapter extends RecyclerView.Adapter<PosicionAdapter.CeldaVH> {

    public interface OnSwapRequest {
        void requestSwap(int fromRow, int fromCol, int targetPositionNumber);
    }

    private final Context context;
    private final List<PositionCell> celdasDeFila; // 5 celdas
    private final int baseIndex; // número de posición inicial de la fila
    private final OnSwapRequest swapListener;

    public PosicionAdapter(Context ctx, List<PositionCell> celdasDeFila, int baseIndex, OnSwapRequest swapListener) {
        this.context = ctx;
        this.celdasDeFila = celdasDeFila;
        this.baseIndex = baseIndex;
        this.swapListener = swapListener;
    }

    @NonNull
    @Override
    public CeldaVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_posicion_costalero, parent, false);
        return new CeldaVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CeldaVH h, int position) {
        PositionCell cell = celdasDeFila.get(position);
        int numero = baseIndex + position; // 1..N

        h.txtNumero.setText("#" + numero);

        if (cell.costalero != null) {
            h.txtNombre.setText(cell.costalero.getNombre() + " " + cell.costalero.getApellido());
            if (cell.costalero.getSuplementos() > 0) {
                h.txtAltura.setText(
                        "Altura: " + cell.costalero.getAlturaTotal() + " cm (" + cell.costalero.getAltura() + " + " + cell.costalero.getSuplementos() + " sup)"
                );
            } else {
                h.txtAltura.setText("Altura: " + cell.costalero.getAltura() + " cm");
            }
            h.btnCambiar.setEnabled(true);
        } else {
            h.txtNombre.setText("(Vacío)");
            h.txtAltura.setText("--");
            h.btnCambiar.setEnabled(true); // permitir intercambiar con vacíos
        }

        h.btnCambiar.setOnClickListener(v -> {
            // Diálogo: introducir número de posición destino
            EditText input = new EditText(context);
            input.setInputType(InputType.TYPE_CLASS_NUMBER);
            input.setFilters(new InputFilter[]{ new InputFilter.LengthFilter(3) });

            new AlertDialog.Builder(context)
                    .setTitle("Cambiar posición")
                    .setMessage("Introduce el número de posición destino:")
                    .setView(input)
                    .setPositiveButton("Cambiar", (d, w) -> {
                        String s = input.getText().toString().trim();
                        if (!s.isEmpty()) {
                            int target = Integer.parseInt(s);
                            swapListener.requestSwap(cell.row, cell.col, target);
                        }
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return celdasDeFila.size();
    }

    static class CeldaVH extends RecyclerView.ViewHolder {
        TextView txtNumero, txtNombre, txtAltura;
        MaterialButton btnCambiar;

        CeldaVH(@NonNull View itemView) {
            super(itemView);
            txtNumero = itemView.findViewById(R.id.txtNumeroPosicion);
            txtNombre = itemView.findViewById(R.id.txtNombreApellidos);
            txtAltura = itemView.findViewById(R.id.txtAltura);
            btnCambiar = itemView.findViewById(R.id.btnCambiarPosicion);
        }
    }
}

