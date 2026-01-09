package com.example.trabajadera.CrearPaso.FragCrearPaso3;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trabajadera.CrearPaso.PasoViewModel;
import com.example.trabajadera.CrearPaso.FragCrearPaso4.FragCrearPaso4;
import com.example.trabajadera.R;
import com.google.android.material.button.MaterialButton;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FragCrearPaso3 extends Fragment {
    private PasoViewModel pasoViewModel;

    private TextView txtResumenPaso;
    private RecyclerView recyclerFilas;
    private MaterialButton btnGuardar;

    private String capataz, tipoPaso, hermandad, ciudad, paso;
    private int trabajaderas;
    private int maxCostaleros;
    private ArrayList<com.example.trabajadera.CrearPaso.Costaleros.Costalero> listaCostaleros;

    private List<List<PositionCell>> grid; // [trabajaderas][5]
    private TrabajaderaRowAdapter rowAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.f_crear_paso3, container, false);

        txtResumenPaso = view.findViewById(R.id.txtResumenPaso);
        recyclerFilas = view.findViewById(R.id.recyclerFilas);
        btnGuardar = view.findViewById(R.id.btnGuardar);
        MaterialButton btnVolver = view.findViewById(R.id.btnVolver);

        pasoViewModel = new ViewModelProvider(requireActivity()).get(PasoViewModel.class);
        listaCostaleros = new ArrayList<>(pasoViewModel.getListaCostaleros());

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

        txtResumenPaso.setText(
                "Capataz: " + capataz + " • " +
                        "Tipo: " + tipoPaso + " (" + paso + ") • " +
                        "Hermandad: " + hermandad + " - " + ciudad + " • " +
                        "Trabajaderas: " + trabajaderas
        );

        prepararGridInicial();

        recyclerFilas.setLayoutManager(new LinearLayoutManager(getContext()));
        rowAdapter = new TrabajaderaRowAdapter(requireContext(), grid, new TrabajaderaRowAdapter.OnRowAction() {
            @Override
            public void moveRowUp(int rowIndex) {
                if (rowIndex > 0) {
                    Collections.swap(grid, rowIndex, rowIndex - 1);
                    reindexGrid();
                    rowAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void moveRowDown(int rowIndex) {
                if (rowIndex < grid.size() - 1) {
                    Collections.swap(grid, rowIndex, rowIndex + 1);
                    reindexGrid();
                    rowAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void swapRequest(int fromRow, int fromCol, int targetPosNumber) {
                int total = trabajaderas * 5;
                if (targetPosNumber < 1 || targetPosNumber > total) {
                    Toast.makeText(getContext(), "Posición debe estar entre 1 y " + total, Toast.LENGTH_SHORT).show();
                    return;
                }
                int targetRow = (targetPosNumber - 1) / 5;
                int targetCol = (targetPosNumber - 1) % 5;

                PositionCell a = grid.get(fromRow).get(fromCol);
                PositionCell b = grid.get(targetRow).get(targetCol);

                com.example.trabajadera.CrearPaso.Costaleros.Costalero tmp = a.costalero;
                a.costalero = b.costalero;
                b.costalero = tmp;

                rowAdapter.notifyDataSetChanged();
            }
        });
        recyclerFilas.setAdapter(rowAdapter);

        btnGuardar.setOnClickListener(v -> {
            ArrayList<Asignacion> asignaciones = generarAsignaciones();

            // Guardar en el ViewModel la cuadrilla como asignaciones (con posiciones exactas)
            pasoViewModel.addCuadrillaAsignaciones(asignaciones);
            pasoViewModel.clearCostaleros(); // limpiar temporal para próxima cuadrilla

            // Navegar a paso 4 con datos del paso (no hace falta pasar asignaciones por Bundle)
            Bundle b = new Bundle();
            b.putString("capataz", capataz);
            b.putString("tipoPaso", tipoPaso);
            b.putString("hermandad", hermandad);
            b.putString("ciudad", ciudad);
            b.putString("paso", paso);
            b.putInt("trabajaderas", trabajaderas);
            b.putInt("costaleros", maxCostaleros);

            FragCrearPaso4 frag = new FragCrearPaso4();
            frag.setArguments(b);

            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.contenedorFragmentos, frag)
                    .addToBackStack(null)
                    .commit();
        });

        btnVolver.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        return view;
    }

    private void prepararGridInicial() {
        int filas = Math.max(trabajaderas, 1);
        int cols = 5;
        grid = new ArrayList<>();

        List<com.example.trabajadera.CrearPaso.Costaleros.Costalero> fuente = new ArrayList<>(listaCostaleros);
        fuente.sort((c1, c2) -> Double.compare(c2.getAlturaTotal(), c1.getAlturaTotal()));
        int idx = 0;

        for (int r = 0; r < filas; r++) {
            List<PositionCell> fila = new ArrayList<>(cols);

            for (int c = 0; c < cols; c++) {
                fila.add(new PositionCell(r, c, null));
            }

            int[] orden = {0, 4, 1, 3, 2};

            for (int pos : orden) {
                if (idx < fuente.size()) {
                    fila.set(pos, new PositionCell(r, pos, fuente.get(idx)));
                    idx++;
                }
            }

            grid.add(fila);
        }
    }

    private void reindexGrid() {
        for (int r = 0; r < grid.size(); r++) {
            List<PositionCell> fila = grid.get(r);
            for (int c = 0; c < fila.size(); c++) {
                fila.get(c).row = r;
                fila.get(c).col = c;
            }
        }
    }

    public static class Asignacion implements Serializable {
        public int posicion; // 1..N
        public int fila;     // 0-based
        public int columna;  // 0-based
        public String nombre;
        public String apellido;
        public double altura;
        public double suplementos;

        public Asignacion(int pos, int fila, int col, com.example.trabajadera.CrearPaso.Costaleros.Costalero c) {
            this.posicion = pos;
            this.fila = fila;
            this.columna = col;
            if (c != null) {
                this.nombre = c.getNombre();
                this.apellido = c.getApellido();
                this.altura = c.getAltura();
                this.suplementos = c.getSuplementos();
            } else {
                this.nombre = "";
                this.apellido = "";
                this.altura = 0;
                this.suplementos = 0;
            }
        }
    }

    private ArrayList<Asignacion> generarAsignaciones() {
        ArrayList<Asignacion> res = new ArrayList<>();
        int pos = 1;
        for (int r = 0; r < grid.size(); r++) {
            for (int c = 0; c < grid.get(r).size(); c++) {
                PositionCell cell = grid.get(r).get(c);
                res.add(new Asignacion(pos, r, c, cell.costalero));
                pos++;
            }
        }
        return res;
    }
}
