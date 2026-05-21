package com.example.trabajadera.CrearPaso.FragCrearPaso3;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trabajadera.CrearPaso.PasoViewModel;
import com.example.trabajadera.CrearPaso.Costaleros.Costalero;
import com.example.trabajadera.CrearPaso.FragCrearPaso4.FragCrearPaso4;
import com.example.trabajadera.PasarLista.PositionCell;
import com.example.trabajadera.PasarLista.Mapa.PalosAdapter;
import com.example.trabajadera.R;
import com.google.android.material.button.MaterialButton;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FragCrearPaso3 extends Fragment implements PalosAdapter.OnMapaCellListener, PalosAdapter.OnPaloMoveListener {

    private PasoViewModel pasoViewModel;
    private TextView txtResumenPaso;
    private RecyclerView recyclerFilas;
    private MaterialButton btnGuardar, btnVolver;

    private String capataz, tipoPaso, hermandad, ciudad, paso;
    private int trabajaderas;
    private int maxCostaleros;
    private ArrayList<Costalero> listaCostaleros;

    private List<List<PositionCell>> grid; // Estructura unificada [filas][5 columnas]
    private PalosAdapter palosAdapter;
    private PositionCell pendingSwapCell = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.f_crear_paso3, container, false);

        txtResumenPaso = view.findViewById(R.id.txtResumenPaso);
        recyclerFilas = view.findViewById(R.id.recyclerFilas);
        btnGuardar = view.findViewById(R.id.btnGuardar);
        btnVolver = view.findViewById(R.id.btnVolver);

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

        txtResumenPaso.setText("Capataz: " + capataz + " • F Hermandad: " + hermandad + " • Trabajaderas: " + trabajaderas);

        prepararGridEstructural();

        recyclerFilas.setLayoutManager(new LinearLayoutManager(getContext()));
        palosAdapter = new PalosAdapter(grid, this, this);
        recyclerFilas.setAdapter(palosAdapter);

        // Permitimos arrastrar e intercambiar la posición de las filas completas exactamente igual que en edición
        ItemTouchHelper touchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                int from = viewHolder.getAdapterPosition();
                int to = target.getAdapterPosition();
                Collections.swap(grid, from, to);
                reindexarGrid();
                palosAdapter.notifyItemMoved(from, to);
                return true;
            }
            @Override public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {}
        });
        touchHelper.attachToRecyclerView(recyclerFilas);

        btnGuardar.setOnClickListener(v -> {
            ArrayList<Asignacion> asignaciones = generarListaAsignaciones();
            pasoViewModel.addCuadrillaAsignaciones(asignaciones);
            pasoViewModel.clearCostaleros();

            Bundle b = new Bundle();
            b.putString("capataz", capataz);
            b.putString("tipoPaso", tipoPaso);
            b.putString("hermandad", hermandad);
            b.putString("ciudad", ciudad);
            b.putString("paso", paso);
            b.putInt("trabajaderas", trabajaderas);
            b.putInt("costaleros", maxCostaleros);

            FragCrearPaso4 f4 = new FragCrearPaso4();
            f4.setArguments(b);

            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.contenedorFragmentos, f4)
                    .addToBackStack(null)
                    .commit();
        });

        btnVolver.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        return view;
    }

    private void prepararGridEstructural() {
        int filas = Math.max(trabajaderas, 1);
        grid = new ArrayList<>();

        List<Costalero> ordenados = new ArrayList<>(listaCostaleros);
        ordenados.sort((c1, c2) -> Double.compare(c2.getAlturaTotal(), c1.getAlturaTotal()));
        int index = 0;

        for (int r = 0; r < filas; r++) {
            List<PositionCell> filaCeldas = new ArrayList<>();
            for (int c = 0; c < 5; c++) {
                // Generamos celdas con su identificador absoluto inicial
                filaCeldas.add(new PositionCell(r, c, (r * 5) + c + 1, null));
            }

            // Patrón clásico (Reparto equilibrado de palos)
            int[] distribucionCeldas = {0, 4, 1, 3, 2};
            for (int slot : distribucionCeldas) {
                if (index < ordenados.size()) {
                    filaCeldas.get(slot).costalero = ordenados.get(index);
                    filaCeldas.get(slot).costalero.setFila(r);
                    filaCeldas.get(slot).costalero.setColumna(slot);
                    filaCeldas.get(slot).costalero.setPosicionAbs((r * 5) + slot + 1);
                    index++;
                }
            }
            grid.add(filaCeldas);
        }
    }

    private void reindexarGrid() {
        for (int r = 0; r < grid.size(); r++) {
            for (int c = 0; c < grid.get(r).size(); c++) {
                PositionCell cell = grid.get(r).get(c);
                cell.fila = r;
                cell.columna = c;
                cell.posicionAbs = (r * 5) + c + 1;
                if (cell.costalero != null) {
                    cell.costalero.setFila(r);
                    cell.costalero.setColumna(c);
                    cell.costalero.setPosicionAbs(cell.posicionAbs);
                }
            }
        }
    }

    @Override public boolean isSwapModeActive() { return pendingSwapCell != null; }
    @Override public PositionCell getPendingSwap() { return pendingSwapCell; }

    @Override
    public void onNormalClick(PositionCell cell) {
        // Al hacer click normal activamos el modo Swap dinámico mostrando los botones AQUÍ
        pendingSwapCell = cell;
        palosAdapter.notifyDataSetChanged();
        Toast.makeText(getContext(), "Selecciona el hueco de destino", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSwapConfirmed(PositionCell target) {
        if (pendingSwapCell != null) {
            // Ejecutamos el swap nativo en memoria de forma inmediata
            Costalero temporal = pendingSwapCell.costalero;
            pendingSwapCell.costalero = target.costalero;
            target.costalero = temporal;

            reindexarGrid();
            pendingSwapCell = null;
            palosAdapter.notifyDataSetChanged();
            Toast.makeText(getContext(), "Posiciones cambiadas", Toast.LENGTH_SHORT).show();
        }
    }

    @Override public void onPaloMoved(int from, int to) { reindexarGrid(); }
    @Override public void onPaloMoveComplete() { reindexarGrid(); palosAdapter.notifyDataSetChanged(); }

    public static class Asignacion implements Serializable {
        public int posicion;
        public int fila;
        public int columna;
        public String nombre;
        public String apellido;
        public double altura;
        public double suplementos;

        public Asignacion(int pos, int fila, int col, Costalero c) {
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

    private ArrayList<Asignacion> generarListaAsignaciones() {
        ArrayList<Asignacion> result = new ArrayList<>();
        int absoluteCounter = 1;
        for (int r = 0; r < grid.size(); r++) {
            for (int c = 0; c < grid.get(r).size(); c++) {
                PositionCell cell = grid.get(r).get(c);
                result.add(new Asignacion(absoluteCounter, r, c, cell.costalero));
                absoluteCounter++;
            }
        }
        return result;
    }
}