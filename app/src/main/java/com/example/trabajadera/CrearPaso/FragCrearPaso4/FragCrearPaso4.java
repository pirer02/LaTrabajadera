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

import com.example.trabajadera.CrearPaso.FragCrearPaso2;
import com.example.trabajadera.CrearPaso.FragCrearPaso3.FragCrearPaso3;
import com.example.trabajadera.CrearPaso.PasoViewModel;
import com.example.trabajadera.FragPrincipal;
import com.example.trabajadera.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FragCrearPaso4 extends Fragment {

    private PasoViewModel pasoViewModel;
    private RecyclerView recyclerCuadrillas;
    private MaterialButton btnAddCuadrilla, btnFinalizar;

    private String capataz, tipoPaso, hermandad, ciudad, paso;
    private int trabajaderas, maxCostaleros;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.f_crear_paso4, container, false);

        pasoViewModel = new ViewModelProvider(requireActivity()).get(PasoViewModel.class);

        recyclerCuadrillas = view.findViewById(R.id.recyclerCuadrillas);
        btnAddCuadrilla = view.findViewById(R.id.btnAddCuadrilla);
        btnFinalizar = view.findViewById(R.id.btnFinalizar);

        Bundle args = getArguments();
        if (args != null) {
            capataz = args.getString("capataz", "");
            tipoPaso = args.getString("tipoPaso", "");
            hermandad = args.getString("hermandad", "");
            ciudad = args.getString("ciudad", "");
            paso = args.getString("paso", "");
            trabajaderas = args.getInt("trabajaderas", 0);
            maxCostaleros = args.getInt("costaleros", 0);
        }

        recyclerCuadrillas.setLayoutManager(new LinearLayoutManager(getContext()));
        CuadrillaAsignacionAdapter adapter = new CuadrillaAsignacionAdapter(pasoViewModel.getCuadrillasAsignaciones());
        recyclerCuadrillas.setAdapter(adapter);

        btnAddCuadrilla.setOnClickListener(v -> {
            if (pasoViewModel.getCuadrillasAsignaciones().size() >= 3) {
                Toast.makeText(getContext(), "Máximo 3 cuadrillas", Toast.LENGTH_SHORT).show();
                return;
            }
            pasoViewModel.clearCostaleros();
            FragCrearPaso2 frag2 = new FragCrearPaso2();
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.contenedorFragmentos, frag2)
                    .addToBackStack(null)
                    .commit();
        });

        btnFinalizar.setOnClickListener(v -> guardarJerarquicoEnFirestore());

        return view;
    }

    private void guardarJerarquicoEnFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            Toast.makeText(getContext(), "Usuario no autenticado", Toast.LENGTH_SHORT).show();
            return;
        }
        String uid = auth.getCurrentUser().getUid();

        Map<String, Object> pasoData = new HashMap<>();
        pasoData.put("capataz", pasoViewModel.getCapataz());
        pasoData.put("ciudad", pasoViewModel.getCiudad());
        pasoData.put("hermandad", pasoViewModel.getHermandad());
        pasoData.put("paso", pasoViewModel.getPaso());
        pasoData.put("tipoPaso", pasoViewModel.getTipoPaso());
        pasoData.put("trabajaderas", pasoViewModel.getTrabajaderas());
        pasoData.put("maxPersonas", pasoViewModel.getMaxCostaleros());
        pasoData.put("tamTrabajadera", 5);

        DocumentReference pasoRef = db.collection("usuarios")
                .document(uid)
                .collection("pasos")
                .document();

        WriteBatch batch = db.batch();
        batch.set(pasoRef, pasoData);

        List<List<FragCrearPaso3.Asignacion>> cuadrillas = pasoViewModel.getCuadrillasAsignaciones();

        for (int i = 0; i < cuadrillas.size(); i++) {
            List<FragCrearPaso3.Asignacion> asignaciones = cuadrillas.get(i);

            DocumentReference cuadrillaRef = pasoRef.collection("cuadrillas").document();
            Map<String, Object> cuadrillaData = new HashMap<>();
            cuadrillaData.put("orden", i);
            cuadrillaData.put("tamTrabajadera", 5);
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
                    Toast.makeText(getContext(), "Error al guardar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
