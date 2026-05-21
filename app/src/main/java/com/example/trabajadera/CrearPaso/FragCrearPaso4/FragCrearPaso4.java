package com.example.trabajadera.CrearPaso.FragCrearPaso4;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trabajadera.CrearPaso.FragCrearPaso3.FragCrearPaso3;
import com.example.trabajadera.CrearPaso.PasoViewModel;
import com.example.trabajadera.FragPrincipal;
import com.example.trabajadera.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FragCrearPaso4 extends Fragment {

    private PasoViewModel pasoViewModel;
    private RecyclerView recyclerCuadrillas;
    private MaterialButton btnAddCuadrilla, btnFinalizar;
    private CuadrillaAsignacionAdapter adapter;

    private String capataz, tipoPaso, hermandad, ciudad, paso;
    private int trabajaderas, maxCostaleros;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.f_crear_paso4, container, false);

        recyclerCuadrillas = view.findViewById(R.id.recyclerCuadrillas);
        btnAddCuadrilla = view.findViewById(R.id.btnAddCuadrilla);
        btnFinalizar = view.findViewById(R.id.btnFinalizar);

        pasoViewModel = new ViewModelProvider(requireActivity()).get(PasoViewModel.class);

        Bundle args = getArguments();
        if (args != null) {
            capataz = args.getString("capataz", pasoViewModel.getCapataz());
            tipoPaso = args.getString("tipoPaso", pasoViewModel.getTipoPaso());
            hermandad = args.getString("hermandad", pasoViewModel.getHermandad());
            ciudad = args.getString("ciudad", pasoViewModel.getCiudad());
            paso = args.getString("paso", pasoViewModel.getPaso());
            trabajaderas = args.getInt("trabajaderas", pasoViewModel.getTrabajaderas());
            maxCostaleros = args.getInt("costaleros", pasoViewModel.getMaxCostaleros());

            pasoViewModel.setDatosPaso(capataz, ciudad, hermandad, paso, tipoPaso, trabajaderas, maxCostaleros);
        } else {
            capataz = pasoViewModel.getCapataz();
            tipoPaso = pasoViewModel.getTipoPaso();
            hermandad = pasoViewModel.getHermandad();
            ciudad = pasoViewModel.getCiudad();
            paso = pasoViewModel.getPaso();
            trabajaderas = pasoViewModel.getTrabajaderas();
            maxCostaleros = pasoViewModel.getMaxCostaleros();
        }

        recyclerCuadrillas.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new CuadrillaAsignacionAdapter(pasoViewModel.getCuadrillasAsignaciones());
        recyclerCuadrillas.setAdapter(adapter);

        btnAddCuadrilla.setOnClickListener(v -> {
            List<List<FragCrearPaso3.Asignacion>> current = pasoViewModel.getCuadrillasAsignaciones();
            if (current.size() >= 3) {
                Toast.makeText(getContext(), "Máximo 3 cuadrillas", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!current.isEmpty()) {
                List<FragCrearPaso3.Asignacion> base = current.get(0);
                List<FragCrearPaso3.Asignacion> nueva = new ArrayList<>();
                for (FragCrearPaso3.Asignacion a : base) {
                    nueva.add(new FragCrearPaso3.Asignacion(a.posicion, a.fila, a.columna, null));
                }
                pasoViewModel.addCuadrillaAsignaciones(nueva);
                adapter.notifyItemInserted(current.size() - 1);
            } else {
                Toast.makeText(getContext(), "No hay una cuadrilla base creada", Toast.LENGTH_SHORT).show();
            }
        });

        btnFinalizar.setOnClickListener(v -> guardarTodoEnFirestore());

        return view;
    }

    private void guardarTodoEnFirestore() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (auth.getCurrentUser() == null) {
            Toast.makeText(getContext(), "Usuario no autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = auth.getCurrentUser().getUid();
        WriteBatch batch = db.batch();

        DocumentReference pasoRef = db.collection("usuarios").document(uid).collection("pasos").document();

        Map<String, Object> pasoData = new HashMap<>();
        pasoData.put("id", pasoRef.getId());
        pasoData.put("capataz", capataz != null ? capataz : "");
        pasoData.put("ciudad", ciudad != null ? ciudad : "");
        pasoData.put("hermandad", hermandad != null ? hermandad : "");
        pasoData.put("paso", paso != null ? paso : "");
        pasoData.put("tipoPaso", tipoPaso != null ? tipoPaso : "");

        // CORRECCIÓN INTERNA: Registramos el volumen de trabajaderas y costaleros en la raíz
        pasoData.put("trabajaderas", trabajaderas);
        pasoData.put("costaleros", maxCostaleros);

        batch.set(pasoRef, pasoData);

        List<List<FragCrearPaso3.Asignacion>> cuadrillas = pasoViewModel.getCuadrillasAsignaciones();
        for (int i = 0; i < cuadrillas.size(); i++) {
            List<FragCrearPaso3.Asignacion> asignaciones = cuadrillas.get(i);
            DocumentReference cuadrillaRef = pasoRef.collection("cuadrillas").document("cuadrilla_" + (i + 1));

            Map<String, Object> cuadrillaData = new HashMap<>();
            cuadrillaData.put("nombre", "Cuadrilla " + (i + 1));
            batch.set(cuadrillaRef, cuadrillaData);

            for (FragCrearPaso3.Asignacion a : asignaciones) {
                if (a.nombre == null || a.nombre.isEmpty()) continue;
                DocumentReference costaleroRef = cuadrillaRef.collection("costaleros").document();
                Map<String, Object> cMap = new HashMap<>();
                cMap.put("nombre", a.nombre);
                cMap.put("apellido", a.apellido);
                cMap.put("altura", a.altura);
                cMap.put("suplementos", a.suplementos);
                cMap.put("posicionAbs", a.posicion);
                cMap.put("fila", a.fila);
                cMap.put("columna", a.columna);
                batch.set(costaleroRef, cMap);
            }
        }

        batch.commit()
                .addOnSuccessListener(unused -> {
                    Toast.makeText(getContext(), "Paso guardado correctamente", Toast.LENGTH_SHORT).show();
                    requireActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.contenedorFragmentos, new FragPrincipal())
                            .commit();
                    pasoViewModel.clearCuadrillas();
                    pasoViewModel.clearCostaleros();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error al guardar en la base de datos: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}