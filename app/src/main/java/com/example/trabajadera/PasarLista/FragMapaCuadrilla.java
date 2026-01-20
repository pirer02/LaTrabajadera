package com.example.trabajadera.PasarLista;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trabajadera.CrearPaso.Costaleros.Costalero;
import com.example.trabajadera.FragPrincipal;
import com.example.trabajadera.PasarLista.Mapa.DetalleCostaleroDialog;
import com.example.trabajadera.PasarLista.Mapa.OtrosCostalerosDialog;
import com.example.trabajadera.PasarLista.Mapa.PalosAdapter;
import com.example.trabajadera.PasarLista.Mapa.ZoomLayout;
import com.example.trabajadera.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class FragMapaCuadrilla extends Fragment
        implements DetalleCostaleroDialog.OnAccionListener,
        PalosAdapter.OnMapaCellListener,
        PalosAdapter.OnPaloMoveListener,
        OtrosCostalerosDialog.OnOtrosCostaleroListener {

    private static final String ARG_ID_PASO = "idPaso";
    private static final String ARG_ID_CUADRILLA = "idCuadrilla";

    // --- Variables de UI ---
    private String idPaso, idCuadrilla;
    private TextView txtCapatazMapa, txtHermandadMapa, txtTipoPasoMapa;
    private RecyclerView layoutContenedorPalos;
    private LinearLayout layoutBotonOtros;
    private MaterialButton btnOtros;
    private ProgressBar progressBarMapa;
    private NestedScrollView scrollLayoutItemPosicion;
    private ZoomLayout zoomLayoutMapa; // Nuevo componente de Zoom

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private int trabajaderas = 0;
    private int tamTrabajadera = 5;

    // --- Lógica del Mapa ---
    private final List<List<PositionCell>> grid = new ArrayList<>();
    private final List<Costalero> presentes = new ArrayList<>();
    private PositionCell pendingSwap = null;
    private boolean modoSwapActivo = false;
    private PalosAdapter palosAdapter;
    private ItemTouchHelper itemTouchHelper;

    public static FragMapaCuadrilla newInstance(String idPaso, String idCuadrilla) {
        FragMapaCuadrilla frag = new FragMapaCuadrilla();
        Bundle b = new Bundle();
        b.putString(ARG_ID_PASO, idPaso != null ? idPaso : "");
        b.putString(ARG_ID_CUADRILLA, idCuadrilla != null ? idCuadrilla : "");
        frag.setArguments(b);
        return frag;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.f_mapa_cuadrilla, container, false);

        // 1. Inicialización de Vistas
        txtCapatazMapa = view.findViewById(R.id.txtCapatazMapa);
        txtHermandadMapa = view.findViewById(R.id.txtHermandadMapa);
        txtTipoPasoMapa = view.findViewById(R.id.txtTipoPasoMapa);
        layoutContenedorPalos = view.findViewById(R.id.layoutContenedorPalos);
        layoutBotonOtros = view.findViewById(R.id.layoutBotonOtros);
        btnOtros = view.findViewById(R.id.btnOtros);
        progressBarMapa = view.findViewById(R.id.progressBarMapa);
        scrollLayoutItemPosicion = view.findViewById(R.id.scrollLayoutItemPosicion);
        zoomLayoutMapa = view.findViewById(R.id.zoomLayoutMapa); // Referencia al ZoomLayout

        MaterialButton btnActualizarMapa = view.findViewById(R.id.btnActualizarMapa);
        MaterialButton btnOrdenDefecto = view.findViewById(R.id.btnOrdenDefecto);

        // 2. Inicialización de Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        if (getArguments() != null) {
            idPaso = getArguments().getString(ARG_ID_PASO);
            idCuadrilla = getArguments().getString(ARG_ID_CUADRILLA);
        }

        // Estado inicial de carga
        progressBarMapa.setVisibility(View.VISIBLE);
        scrollLayoutItemPosicion.setVisibility(View.GONE);

        // 3. Configuración de RecyclerView
        palosAdapter = new PalosAdapter(grid, this, this);
        layoutContenedorPalos.setLayoutManager(new LinearLayoutManager(getContext()));
        layoutContenedorPalos.setNestedScrollingEnabled(false);
        layoutContenedorPalos.setAdapter(palosAdapter);

        // Habilitar arrastrar trabajaderas (reordenar palos)
        itemTouchHelper = new ItemTouchHelper(new ItemTouchHelperCallback(palosAdapter));
        itemTouchHelper.attachToRecyclerView(layoutContenedorPalos);

        // 4. Carga de Datos
        cargarMetaYCostaleros();

        // 5. Listeners de Botones
        btnActualizarMapa.setOnClickListener(v -> guardarMapaYVolver());
        btnOrdenDefecto.setOnClickListener(v -> mostrarDialogoConfirmacionOrden());
        btnOtros.setOnClickListener(v -> abrirDialogoSobrantes());

        return view;
    }

    // --- LÓGICA DE CARGA ---

    private void cargarMetaYCostaleros() {
        if (auth.getCurrentUser() == null) { finalizarCarga(); return; }
        String uid = auth.getCurrentUser().getUid();

        db.collection("usuarios").document(uid).collection("pasos").document(idPaso).get()
                .addOnSuccessListener(pasoDoc -> {
                    if (pasoDoc.exists()) {
                        txtCapatazMapa.setText("Capataz: " + pasoDoc.getString("capataz"));
                        txtHermandadMapa.setText("Hermandad: " + pasoDoc.getString("hermandad") + " - " + pasoDoc.getString("ciudad"));
                        txtTipoPasoMapa.setText("Tipo: " + pasoDoc.getString("tipoPaso"));
                        trabajaderas = pasoDoc.getLong("trabajaderas") != null ? pasoDoc.getLong("trabajaderas").intValue() : 0;
                    }

                    db.collection("usuarios").document(uid).collection("pasos").document(idPaso)
                            .collection("cuadrillas").document(idCuadrilla).get()
                            .addOnSuccessListener(cuadrillaDoc -> {
                                Integer t = cuadrillaDoc.getLong("tamTrabajadera") != null ? cuadrillaDoc.getLong("tamTrabajadera").intValue() : null;
                                if (t != null && t > 0) tamTrabajadera = t;
                                cargarPresentesYConstruirGrid(uid);
                            })
                            .addOnFailureListener(e -> finalizarCarga());
                });
    }

    private void cargarPresentesYConstruirGrid(String uid) {
        db.collection("usuarios").document(uid).collection("pasos").document(idPaso)
                .collection("cuadrillas").document(idCuadrilla).collection("costaleros").get()
                .addOnSuccessListener(query -> {
                    presentes.clear();
                    for (DocumentSnapshot doc : query) {
                        Costalero c = doc.toObject(Costalero.class);
                        if (c != null && c.isAsistencia()) {
                            c.setId(doc.getId());
                            presentes.add(c);
                        }
                    }

                    // Construcción inicial del grid vacío
                    grid.clear();
                    int posAbs = 1;
                    for (int f = 0; f < trabajaderas; f++) {
                        List<PositionCell> row = new ArrayList<>();
                        for (int col = 0; col < tamTrabajadera; col++) {
                            row.add(new PositionCell(f, col, posAbs++, null));
                        }
                        grid.add(row);
                    }

                    // Ubicar costaleros según su posición guardada
                    presentes.sort(Comparator.comparingInt(c -> c.getPosicionAbs() >= 0 ? c.getPosicionAbs() : Integer.MAX_VALUE));
                    for (Costalero c : presentes) {
                        PositionCell cell = findByPosicionAbs(c.getPosicionAbs());
                        if (cell != null && cell.costalero == null) cell.costalero = c;
                    }

                    // Ubicar a los que no tienen posición en los huecos libres
                    for (Costalero c : presentes) {
                        if (c.getPosicionAbs() <= 0 || findByPosicionAbs(c.getPosicionAbs()) == null || findByPosicionAbs(c.getPosicionAbs()).costalero != c) {
                            PositionCell hole = findFirstEmpty();
                            if (hole != null) hole.costalero = c;
                        }
                    }

                    recalcularPosicionesAbsolutas();
                    palosAdapter.notifyDataSetChanged();
                    finalizarCarga();
                });
    }

    private void finalizarCarga() {
        if (progressBarMapa != null) progressBarMapa.setVisibility(View.GONE);
        if (scrollLayoutItemPosicion != null) scrollLayoutItemPosicion.setVisibility(View.VISIBLE);
        if (zoomLayoutMapa != null) zoomLayoutMapa.resetZoom(); // Reiniciar zoom al cargar
    }

    // --- GESTIÓN DE INTERCAMBIOS (SWAP) ---

    @Override
    public void onNormalClick(PositionCell cell) {
        if (modoSwapActivo) {
            if (pendingSwap == cell) cancelarSwap();
            return;
        }

        if (cell.costalero != null) {
            DetalleCostaleroDialog dialog = DetalleCostaleroDialog.newInstance(cell, true);
            dialog.setTargetFragment(this, 0);
            dialog.show(getParentFragmentManager(), "DetalleCostaleroDialog");
        }
    }

    @Override
    public void onIniciarCambioPosicion(int posAbs) {
        PositionCell cell = findByPosicionAbs(posAbs);
        if (cell == null || cell.costalero == null) return;

        pendingSwap = cell;
        modoSwapActivo = true;

        if (layoutBotonOtros != null) {
            layoutBotonOtros.setVisibility(obtenerCostalerosSobrantes().isEmpty() ? View.GONE : View.VISIBLE);
        }

        palosAdapter.notifyDataSetChanged();
        Toast.makeText(getContext(), "Selecciona el destino", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSwapConfirmed(PositionCell target) {
        if (pendingSwap != null) {
            Costalero a = pendingSwap.costalero;
            pendingSwap.costalero = target.costalero;
            target.costalero = a;

            pendingSwap = null;
            modoSwapActivo = false;
            if (layoutBotonOtros != null) layoutBotonOtros.setVisibility(View.GONE);

            recalcularPosicionesAbsolutas();
            palosAdapter.notifyDataSetChanged();
        }
    }

    private void cancelarSwap() {
        pendingSwap = null;
        modoSwapActivo = false;
        if (layoutBotonOtros != null) layoutBotonOtros.setVisibility(View.GONE);
        palosAdapter.notifyDataSetChanged();
    }

    // --- AUXILIARES ---

    private void recalcularPosicionesAbsolutas() {
        int newPosAbs = 1;
        for (int f = 0; f < grid.size(); f++) {
            List<PositionCell> row = grid.get(f);
            for (int col = 0; col < row.size(); col++) {
                PositionCell cell = row.get(col);
                cell.fila = f;
                cell.columna = col;
                cell.posicionAbs = newPosAbs++;
                if (cell.costalero != null) {
                    cell.costalero.setFila(f);
                    cell.costalero.setColumna(col);
                    cell.costalero.setPosicionAbs(cell.posicionAbs);
                }
            }
        }
    }

    private PositionCell findByPosicionAbs(int posAbs) {
        for (List<PositionCell> row : grid) {
            for (PositionCell cell : row) {
                if (cell.posicionAbs == posAbs) return cell;
            }
        }
        return null;
    }

    private PositionCell findFirstEmpty() {
        for (List<PositionCell> row : grid) {
            for (PositionCell cell : row) {
                if (cell.costalero == null) return cell;
            }
        }
        return null;
    }

    private List<Costalero> obtenerCostalerosSobrantes() {
        return presentes.stream().filter(c -> c.getPosicionAbs() <= 0).collect(Collectors.toList());
    }

    private void abrirDialogoSobrantes() {
        List<Costalero> sobrantes = obtenerCostalerosSobrantes();
        if (!sobrantes.isEmpty()) {
            OtrosCostalerosDialog dialog = OtrosCostalerosDialog.newInstance(sobrantes);
            dialog.setTargetFragment(this, 0);
            dialog.show(getParentFragmentManager(), "OtrosCostalerosDialog");
        } else {
            Toast.makeText(getContext(), "No hay costaleros fuera del mapa", Toast.LENGTH_SHORT).show();
            if (layoutBotonOtros != null) layoutBotonOtros.setVisibility(View.GONE);
        }
    }

    // --- PERSISTENCIA Y NAVEGACIÓN ---

    private void guardarMapaYVolver() {
        if (auth.getCurrentUser() == null) return;
        String uid = auth.getCurrentUser().getUid();

        recalcularPosicionesAbsolutas();

        List<Costalero> enGrid = grid.stream()
                .flatMap(List::stream)
                .filter(cell -> cell.costalero != null)
                .map(cell -> cell.costalero)
                .collect(Collectors.toList());

        List<String> idsEnGrid = enGrid.stream().map(Costalero::getId).collect(Collectors.toList());

        db.collection("usuarios").document(uid).collection("pasos").document(idPaso)
                .collection("cuadrillas").document(idCuadrilla).collection("costaleros").get()
                .addOnSuccessListener(query -> {
                    for (DocumentSnapshot doc : query) {
                        Costalero c = doc.toObject(Costalero.class);
                        if (c == null) continue;
                        c.setId(doc.getId());

                        if (idsEnGrid.contains(c.getId())) {
                            Costalero act = enGrid.stream().filter(p -> p.getId().equals(c.getId())).findFirst().get();
                            db.collection("usuarios").document(uid).collection("pasos").document(idPaso)
                                    .collection("cuadrillas").document(idCuadrilla).collection("costaleros").document(c.getId())
                                    .update("posicionAbs", act.getPosicionAbs(), "fila", act.getFila(), "columna", act.getColumna());
                        } else if (presentes.stream().anyMatch(p -> p.getId().equals(c.getId()))) {
                            // Si está presente pero no en el grid, lo marcamos como fuera (-1)
                            db.collection("usuarios").document(uid).collection("pasos").document(idPaso)
                                    .collection("cuadrillas").document(idCuadrilla).collection("costaleros").document(c.getId())
                                    .update("posicionAbs", -1, "fila", -1, "columna", -1);
                        }
                    }
                    Toast.makeText(getContext(), "Mapa guardado", Toast.LENGTH_SHORT).show();
                    navegarAlPrincipal();
                });
    }

    private void navegarAlPrincipal() {
        if (getParentFragmentManager() != null && getView() != null) {
            getParentFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            Fragment principal = new FragPrincipal();
            int containerId = ((ViewGroup) getView().getParent()).getId();
            getParentFragmentManager().beginTransaction().replace(containerId, principal).commit();
        }
    }

    private void mostrarDialogoConfirmacionOrden() {
        new AlertDialog.Builder(getContext())
                .setTitle("Reiniciar Orden")
                .setMessage("¿Reorganizar por altura? (Cambios no guardados se perderán)")
                .setPositiveButton("Sí", (dialog, which) -> aplicarOrdenPorAltura())
                .setNegativeButton("Cancelar", null).show();
    }

    private void aplicarOrdenPorAltura() {
        List<Costalero> ordenados = presentes.stream()
                .sorted(Comparator.comparingDouble(Costalero::getAlturaTotal).reversed())
                .collect(Collectors.toList());

        int[] ordenPuestos = {1, 3, 4, 2, 0}; // Orden típico de carga por altura
        int index = 0;

        // Limpiar grid
        for (List<PositionCell> row : grid) for (PositionCell cell : row) cell.costalero = null;

        for (int f = 0; f < trabajaderas; f++) {
            for (int col : ordenPuestos) {
                if (index < ordenados.size() && col < tamTrabajadera) {
                    grid.get(f).get(col).costalero = ordenados.get(index++);
                }
            }
        }
        recalcularPosicionesAbsolutas();
        palosAdapter.notifyDataSetChanged();
    }

    // --- INTERFACES ---

    @Override public boolean isSwapModeActive() { return modoSwapActivo; }
    @Override public PositionCell getPendingSwap() { return pendingSwap; }
    @Override public void onDatosCostaleroEditados() { palosAdapter.notifyDataSetChanged(); }
    @Override public void onSwapWithOther(Costalero otro) {
        if (pendingSwap != null) {
            Costalero desplazado = pendingSwap.costalero;
            pendingSwap.costalero = otro;
            if (desplazado != null) desplazado.setPosicionAbs(-1);
            cancelarSwap();
        }
    }

    @Override public void onPaloMoved(int f, int t) {}
    @Override public void onPaloMoveComplete() { recalcularPosicionesAbsolutas(); palosAdapter.notifyDataSetChanged(); }

    // --- ITEM TOUCH HELPER PARA REORDENAR TRABAJADERAS ---

    private class ItemTouchHelperCallback extends ItemTouchHelper.Callback {
        private final PalosAdapter adapter;
        public ItemTouchHelperCallback(PalosAdapter a) { adapter = a; }
        @Override public int getMovementFlags(@NonNull RecyclerView rv, @NonNull RecyclerView.ViewHolder vh) {
            return makeMovementFlags(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0);
        }
        @Override public boolean onMove(@NonNull RecyclerView rv, @NonNull RecyclerView.ViewHolder vh, @NonNull RecyclerView.ViewHolder t) {
            adapter.onItemMove(vh.getAdapterPosition(), t.getAdapterPosition());
            return true;
        }
        @Override public void onSwiped(@NonNull RecyclerView.ViewHolder vh, int d) {}
        @Override public void onSelectedChanged(@Nullable RecyclerView.ViewHolder vh, int as) {
            super.onSelectedChanged(vh, as);
            if (as == ItemTouchHelper.ACTION_STATE_IDLE) adapter.moveListener.onPaloMoveComplete();
        }
    }
}