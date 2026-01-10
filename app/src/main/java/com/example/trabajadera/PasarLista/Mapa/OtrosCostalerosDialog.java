package com.example.trabajadera.PasarLista.Mapa;

import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trabajadera.CrearPaso.Costaleros.Costalero;
import com.example.trabajadera.R;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class OtrosCostalerosDialog extends DialogFragment {

    public interface OnOtrosCostaleroListener {
        void onSwapWithOther(Costalero costalero);
    }

    private static final String ARG_COSTALEROS = "arg_costaleros";
    private List<Costalero> costalerosSobrantes;

    public static OtrosCostalerosDialog newInstance(List<Costalero> sobrantes) {
        OtrosCostalerosDialog dialog = new OtrosCostalerosDialog();
        Bundle args = new Bundle();
        args.putSerializable(ARG_COSTALEROS, new java.util.ArrayList<>(sobrantes));
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            costalerosSobrantes = (List<Costalero>) getArguments().getSerializable(ARG_COSTALEROS);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // Inflamos el XML que me acabas de pasar
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_otros_costaleros, null);

        // CORRECCIÓN DE IDs: Ahora coinciden exactamente con tu XML
        EditText edtBuscar = view.findViewById(R.id.edtBuscarCostalero);
        RecyclerView recycler = view.findViewById(R.id.recyclerOtrosCostaleros);
        MaterialButton btnAtras = view.findViewById(R.id.btnAtrasOtros);

        recycler.setLayoutManager(new LinearLayoutManager(getContext()));

        OtrosCostalerosAdapter adapter = new OtrosCostalerosAdapter(costalerosSobrantes, costalero -> {
            OnOtrosCostaleroListener listener = getListener();
            if (listener != null) {
                listener.onSwapWithOther(costalero);
            }
            dismiss();
        });

        recycler.setAdapter(adapter);

        // Filtro de búsqueda usando el ID correcto
        edtBuscar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filtrar(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        btnAtras.setOnClickListener(v -> dismiss());

        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(view);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        return dialog;
    }

    private OnOtrosCostaleroListener getListener() {
        if (getTargetFragment() instanceof OnOtrosCostaleroListener) return (OnOtrosCostaleroListener) getTargetFragment();
        if (getParentFragment() instanceof OnOtrosCostaleroListener) return (OnOtrosCostaleroListener) getParentFragment();
        if (getActivity() instanceof OnOtrosCostaleroListener) return (OnOtrosCostaleroListener) getActivity();
        return null;
    }
}