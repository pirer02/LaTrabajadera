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

    @SuppressWarnings("unchecked")
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
        View view = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_otros_costaleros, null);

        EditText edtBuscar = view.findViewById(R.id.edtBuscarCostalero);
        RecyclerView recycler = view.findViewById(R.id.recyclerOtrosCostaleros);
        MaterialButton btnAtras = view.findViewById(R.id.btnAtrasOtros);

        recycler.setLayoutManager(new LinearLayoutManager(getContext()));

        OtrosCostalerosAdapter adapter = new OtrosCostalerosAdapter(costalerosSobrantes, costalero -> {
            OnOtrosCostaleroListener listener = null;

            if (getTargetFragment() instanceof OnOtrosCostaleroListener) {
                listener = (OnOtrosCostaleroListener) getTargetFragment();
            } else if (getParentFragment() instanceof OnOtrosCostaleroListener) {
                listener = (OnOtrosCostaleroListener) getParentFragment();
            } else if (getActivity() instanceof OnOtrosCostaleroListener) {
                listener = (OnOtrosCostaleroListener) getActivity();
            }

            if (listener != null) {
                listener.onSwapWithOther(costalero);
            }

            dismiss();
        });

        recycler.setAdapter(adapter);

        // Filtro de búsqueda
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
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        return dialog;
    }
}

