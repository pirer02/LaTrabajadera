package com.example.trabajadera.CrearPaso;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trabajadera.CrearPaso.Costaleros.Costalero;
import com.example.trabajadera.CrearPaso.Costaleros.CostaleroAdapter;
import com.example.trabajadera.CrearPaso.FragCrearPaso3.FragCrearPaso3;
import com.example.trabajadera.R;
import com.google.android.material.button.MaterialButton;

public class FragCrearPaso2 extends Fragment {

    private TextView txtCapataz, txtTipoPaso, txtHermandad, txtMaxCostaleros;
    private RecyclerView recyclerView;
    private MaterialButton btnAddCostalero, btnContinuar;
    private CostaleroAdapter adapter;

    private PasoViewModel pasoViewModel;

    private String capataz, ciudad, hermandad, paso, tipoPaso;
    private int trabajaderas, maxCostaleros;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.f_crear_paso2, container, false);

        // ViewModel compartido
        pasoViewModel = new ViewModelProvider(requireActivity()).get(PasoViewModel.class);

        // Referencias UI
        txtCapataz = view.findViewById(R.id.txtCapataz);
        txtTipoPaso = view.findViewById(R.id.txtTipoPaso);
        txtHermandad = view.findViewById(R.id.txtHermandad);
        txtMaxCostaleros = view.findViewById(R.id.txtMaxCostaleros);
        recyclerView = view.findViewById(R.id.recyclerCostaleros);
        btnAddCostalero = view.findViewById(R.id.btnAddCostalero);
        btnContinuar = view.findViewById(R.id.btnContinuar);

        // Recuperar datos del paso
        Bundle args = getArguments();
        if (args != null) {
            capataz = args.getString("capataz");
            ciudad = args.getString("ciudad");
            hermandad = args.getString("hermandad");
            paso = args.getString("paso");
            tipoPaso = args.getString("tipoPaso");
            trabajaderas = args.getInt("trabajaderas");
            maxCostaleros = args.getInt("costaleros");

            // Guardar en el ViewModel
            pasoViewModel.setDatosPaso(capataz, ciudad, hermandad, paso, tipoPaso, trabajaderas, maxCostaleros);
        } else {
            // Recuperar del ViewModel
            capataz = pasoViewModel.getCapataz();
            ciudad = pasoViewModel.getCiudad();
            hermandad = pasoViewModel.getHermandad();
            paso = pasoViewModel.getPaso();
            tipoPaso = pasoViewModel.getTipoPaso();
            trabajaderas = pasoViewModel.getTrabajaderas();
            maxCostaleros = pasoViewModel.getMaxCostaleros();
        }

        // Mostrar cabecera
        txtCapataz.setText("Capataz: " + capataz);
        txtTipoPaso.setText("Tipo de Paso: " + tipoPaso + " (" + paso + ")");
        txtHermandad.setText("Hermandad: " + hermandad + " - " + ciudad);
        txtMaxCostaleros.setText("Número máximo de costaleros: " + maxCostaleros);

        // Configurar RecyclerView
        adapter = new CostaleroAdapter(pasoViewModel.getListaCostaleros());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        // Botón añadir costalero
        btnAddCostalero.setOnClickListener(v -> mostrarDialogoNuevoCostalero());

        // Botón continuar
        btnContinuar.setOnClickListener(v -> {
            if (pasoViewModel.getListaCostaleros().isEmpty()) {
                Toast.makeText(getContext(), "Debes añadir al menos un costalero", Toast.LENGTH_SHORT).show();
                return;
            }

            Bundle bundle = new Bundle();
            bundle.putString("capataz", capataz);
            bundle.putString("ciudad", ciudad);
            bundle.putString("hermandad", hermandad);
            bundle.putString("paso", paso);
            bundle.putString("tipoPaso", tipoPaso);
            bundle.putInt("trabajaderas", trabajaderas);
            bundle.putInt("costaleros", maxCostaleros);

            FragCrearPaso3 frag3 = new FragCrearPaso3();
            frag3.setArguments(bundle);

            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.contenedorFragmentos, frag3)
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }

    private void mostrarDialogoNuevoCostalero() {
        if (pasoViewModel.getListaCostaleros().size() >= maxCostaleros) {
            Toast.makeText(getContext(), "Has alcanzado el máximo de costaleros", Toast.LENGTH_SHORT).show();
            return;
        }

        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_costalero, null);

        EditText etNombre = dialogView.findViewById(R.id.etNombre);
        EditText etApellido = dialogView.findViewById(R.id.etApellido);
        EditText etAltura = dialogView.findViewById(R.id.etAltura);

        etAltura.setInputType(InputType.TYPE_CLASS_NUMBER);

        new AlertDialog.Builder(getContext())
                .setTitle("Añadir Costalero")
                .setView(dialogView)
                .setPositiveButton("Añadir", (dialog, which) -> {
                    String nombre = etNombre.getText().toString().trim();
                    String apellido = etApellido.getText().toString().trim();
                    String alturaStr = etAltura.getText().toString().trim();

                    if (nombre.isEmpty() || apellido.isEmpty() || alturaStr.isEmpty()) {
                        Toast.makeText(getContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    try {
                        int alturaCm = Integer.parseInt(alturaStr);
                        Costalero nuevo = new Costalero(nombre, apellido, alturaCm);

                        pasoViewModel.addCostalero(nuevo);
                        adapter.notifyItemInserted(pasoViewModel.getListaCostaleros().size() - 1);
                    } catch (NumberFormatException e) {
                        Toast.makeText(getContext(), "Introduce la altura en cm (ej: 175)", Toast.LENGTH_SHORT).show();
                    }

                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}
