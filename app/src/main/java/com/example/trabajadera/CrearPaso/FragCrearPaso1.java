package com.example.trabajadera.CrearPaso;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.trabajadera.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class FragCrearPaso1 extends Fragment {

    private TextInputEditText nombreCapataz, inputCiudad, inputHermandad, inputPaso, inputTrabajaderas, inputCostaleros;
    private Spinner spinnerTipoPaso;
    private MaterialButton btnGuardar, btnVolver;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.f_crear_paso1, container, false);

        // Referencias
        nombreCapataz = view.findViewById(R.id.nombreCapataz);
        inputCiudad = view.findViewById(R.id.inputCiudad);
        inputHermandad = view.findViewById(R.id.inputHermandad);
        inputPaso = view.findViewById(R.id.inputPaso);
        inputTrabajaderas = view.findViewById(R.id.inputTrabajaderas);
        inputCostaleros = view.findViewById(R.id.inputCostaleros);
        spinnerTipoPaso = view.findViewById(R.id.spinnerTipoPaso);

        btnGuardar = view.findViewById(R.id.btnGuardarPaso);
        btnVolver = view.findViewById(R.id.btnVolverAtras);

        // Volver atrás
        btnVolver.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        // Guardar y pasar al siguiente fragmento
        btnGuardar.setOnClickListener(v -> {
            if (validarCampos()) {
                // Recoger valores
                String capataz = nombreCapataz.getText().toString().trim();
                String ciudad = inputCiudad.getText().toString().trim();
                String hermandad = inputHermandad.getText().toString().trim();
                String paso = inputPaso.getText().toString().trim();
                String tipoPaso = spinnerTipoPaso.getSelectedItem().toString();
                int trabajaderas = Integer.parseInt(inputTrabajaderas.getText().toString().trim());
                int costaleros = Integer.parseInt(inputCostaleros.getText().toString().trim());

                // Bundle con los datos
                Bundle bundle = new Bundle();
                bundle.putString("capataz", capataz);
                bundle.putString("ciudad", ciudad);
                bundle.putString("hermandad", hermandad);
                bundle.putString("paso", paso);
                bundle.putString("tipoPaso", tipoPaso);
                bundle.putInt("trabajaderas", trabajaderas);
                bundle.putInt("costaleros", costaleros);

                // Crear siguiente fragmento y pasarle los datos
                FragCrearPaso2 frag2 = new FragCrearPaso2();
                frag2.setArguments(bundle);

                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.contenedorFragmentos, frag2)
                        .addToBackStack(null)
                        .commit();
            }
        });

        return view;
    }

    private boolean validarCampos() {
        if (TextUtils.isEmpty(nombreCapataz.getText())) {
            nombreCapataz.setError("Introduce el nombre del capataz");
            return false;
        }
        if (TextUtils.isEmpty(inputCiudad.getText())) {
            inputCiudad.setError("Introduce la ciudad/pueblo");
            return false;
        }
        if (TextUtils.isEmpty(inputHermandad.getText())) {
            inputHermandad.setError("Introduce la hermandad");
            return false;
        }
        if (TextUtils.isEmpty(inputPaso.getText())) {
            inputPaso.setError("Introduce el nombre del paso");
            return false;
        }
        if (TextUtils.isEmpty(inputTrabajaderas.getText())) {
            inputTrabajaderas.setError("Introduce el número de trabajaderas");
            return false;
        }
        if (TextUtils.isEmpty(inputCostaleros.getText())) {
            inputCostaleros.setError("Introduce el número de costaleros");
            return false;
        }

        int trabajaderas = Integer.parseInt(inputTrabajaderas.getText().toString().trim());
        int costaleros = Integer.parseInt(inputCostaleros.getText().toString().trim());

        if (trabajaderas > 8) {
            inputTrabajaderas.setError("Máximo 8 trabajaderas");
            return false;
        }
        if (costaleros > 100) {
            inputCostaleros.setError("Máximo 100 costaleros");
            return false;
        }

        return true;
    }
}
