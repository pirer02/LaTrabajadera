package com.example.trabajadera.PasarLista;

import android.app.AlertDialog;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trabajadera.CrearPaso.Costaleros.Costalero;
import com.example.trabajadera.CrearPaso.Costaleros.CostaleroEditarAdapter;
import com.example.trabajadera.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class FragEditarPaso extends Fragment {

    private static final String ARG_ID_PASO = "idPaso";
    private static final String ARG_ID_CUADRILLA = "idCuadrilla";

    private String idPaso;
    private String idCuadrilla;

    private TextView txtCapataz, txtHermandad, txtTipoPaso;
    private RecyclerView recyclerCostaleros;
    private MaterialButton btnAddCostalero, btnGuardar;
    private EditText edtBuscarCostalero;
    private MaterialButton btnSeleccionarTodo;
    private MaterialButton btnDeseleccionarTodo;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private CostaleroEditarAdapter adapter;
    private final List<Costalero> listaCostaleros = new ArrayList<>();
    private final List<Costalero> eliminados = new ArrayList<>();

    private int trabajaderas = 0;
    private int tamTrabajadera = 5;

    public static FragEditarPaso newInstance(String idPaso, String idCuadrilla) {
        FragEditarPaso frag = new FragEditarPaso();
        Bundle b = new Bundle();
        b.putString(ARG_ID_PASO, idPaso);
        b.putString(ARG_ID_CUADRILLA, idCuadrilla);
        frag.setArguments(b);
        return frag;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.f_editar_paso, container, false);

        txtCapataz = view.findViewById(R.id.txtCapataz);
        txtHermandad = view.findViewById(R.id.txtHermandad);
        txtTipoPaso = view.findViewById(R.id.txtTipoPaso);
        recyclerCostaleros = view.findViewById(R.id.recyclerCostaleros);
        btnAddCostalero = view.findViewById(R.id.btnAddCostalero);
        btnGuardar = view.findViewById(R.id.btnGuardar);
        edtBuscarCostalero = view.findViewById(R.id.edtBuscarCostalero);
        btnSeleccionarTodo = view.findViewById(R.id.btnSeleccionarTodo);
        btnDeseleccionarTodo = view.findViewById(R.id.btnDeseleccionarTodo);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        if (getArguments() != null) {
            idPaso = getArguments().getString(ARG_ID_PASO);
            idCuadrilla = getArguments().getString(ARG_ID_CUADRILLA);
        }

        recyclerCostaleros.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new CostaleroEditarAdapter(listaCostaleros, c -> eliminados.add(c));
        recyclerCostaleros.setAdapter(adapter);

        // 👉 Swipe-to-delete con degradado rojo
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Costalero eliminado = adapter.getItem(position);
                adapter.removeItem(position);
                eliminados.add(eliminado);
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                                    @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY,
                                    int actionState, boolean isCurrentlyActive) {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

                View itemView = viewHolder.itemView;
                Paint paint = new Paint();
                LinearGradient gradient = new LinearGradient(
                        itemView.getRight() + dX, itemView.getTop(),
                        itemView.getRight(), itemView.getBottom(),
                        Color.parseColor("#FF5252"), // rojo claro
                        Color.parseColor("#D32F2F"), // rojo oscuro
                        Shader.TileMode.CLAMP
                );
                paint.setShader(gradient);

                if (dX < 0) { // swipe hacia la izquierda
                    RectF background = new RectF(
                            itemView.getRight() + dX,
                            itemView.getTop(),
                            itemView.getRight(),
                            itemView.getBottom()
                    );
                    c.drawRect(background, paint);

                    // Texto "Eliminar"
                    paint.setShader(null);
                    paint.setColor(Color.WHITE);
                    paint.setTextSize(40f);
                    paint.setTextAlign(Paint.Align.RIGHT);
                    c.drawText("Eliminar",
                            itemView.getRight() - 50f,
                            itemView.getTop() + (itemView.getHeight() / 2f) + 15f,
                            paint);
                }
            }
        };
        new ItemTouchHelper(simpleCallback).attachToRecyclerView(recyclerCostaleros);

        cargarDatosPaso();
        cargarCostaleros();

        btnAddCostalero.setOnClickListener(v -> mostrarDialogoNuevoCostalero());
        btnGuardar.setOnClickListener(v -> onGuardarCambios());

        // Buscar: filtra y si está vacío muestra todos
        edtBuscarCostalero.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filtrar(s.toString());
            }
        });

        // Seleccionar todo: marca asistencia de todos
        btnSeleccionarTodo.setOnClickListener(v -> adapter.seleccionarTodos());
        btnDeseleccionarTodo.setOnClickListener(v -> adapter.deseleccionarTodos());

        return view;
    }




private void cargarDatosPaso() {
        if (auth.getCurrentUser() == null) return;
        String uid = auth.getCurrentUser().getUid();

        db.collection("usuarios").document(uid)
                .collection("pasos").document(idPaso)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        txtCapataz.setText("Capataz: " + doc.getString("capataz"));
                        txtHermandad.setText("Hermandad: " + doc.getString("hermandad") + " - " + doc.getString("ciudad"));
                        txtTipoPaso.setText("Tipo: " + doc.getString("tipoPaso"));
                        trabajaderas = doc.getLong("trabajaderas") != null ? doc.getLong("trabajaderas").intValue() : 0;
                        cargarTamTrabajaderaCuadrilla();
                    }
                });
    }

    private void cargarTamTrabajaderaCuadrilla() {
        if (auth.getCurrentUser() == null) return;
        String uid = auth.getCurrentUser().getUid();

        db.collection("usuarios").document(uid)
                .collection("pasos").document(idPaso)
                .collection("cuadrillas").document(idCuadrilla)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Integer t = doc.getLong("tamTrabajadera") != null ? doc.getLong("tamTrabajadera").intValue() : null;
                        if (t != null && t > 0) tamTrabajadera = t;
                    }
                });
    }

    private void cargarCostaleros() {
        if (auth.getCurrentUser() == null) return;
        String uid = auth.getCurrentUser().getUid();

        db.collection("usuarios").document(uid)
                .collection("pasos").document(idPaso)
                .collection("cuadrillas").document(idCuadrilla)
                .collection("costaleros")
                .get()
                .addOnSuccessListener(query -> {
                    listaCostaleros.clear();
                    for (DocumentSnapshot doc : query) {
                        Costalero c = doc.toObject(Costalero.class);
                        if (c != null) {
                            c.setId(doc.getId());
                            if (doc.getLong("fila") != null) c.setFila(doc.getLong("fila").intValue());
                            if (doc.getLong("columna") != null) c.setColumna(doc.getLong("columna").intValue());
                            if (doc.getLong("posicionAbs") != null) c.setPosicionAbs(doc.getLong("posicionAbs").intValue());
                            listaCostaleros.add(c);
                        }
                    }
                    // clave: sincroniza adapter con todos los costaleros
                    adapter.setData(listaCostaleros);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error al cargar costaleros: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void mostrarDialogoNuevoCostalero() {
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
                        int altura = Integer.parseInt(alturaStr);
                        Costalero nuevo = new Costalero(nombre, apellido, altura);

                        listaCostaleros.add(nuevo);
                        adapter.setData(listaCostaleros); // refresca original y filtrada con todos

                        if (auth.getCurrentUser() != null) {
                            String uid = auth.getCurrentUser().getUid();
                            db.collection("usuarios").document(uid)
                                    .collection("pasos").document(idPaso)
                                    .collection("cuadrillas").document(idCuadrilla)
                                    .collection("costaleros")
                                    .add(nuevo)
                                    .addOnSuccessListener(ref -> {
                                        nuevo.setId(ref.getId());
                                        Toast.makeText(getContext(), "Costalero añadido", Toast.LENGTH_SHORT).show();
                                        adapter.setData(listaCostaleros); // asegurar sync tras ID asignada
                                    })
                                    .addOnFailureListener(e ->
                                            Toast.makeText(getContext(), "Error al añadir: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                                    );
                        }

                    } catch (NumberFormatException e) {
                        Toast.makeText(getContext(), "Introduce la altura en cm (ej: 175)", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void onGuardarCambios() {
        if (auth.getCurrentUser() == null) return;
        String uid = auth.getCurrentUser().getUid();

        // 1) Borrar eliminados (de Firestore)
        for (Costalero c : eliminados) {
            if (c.getId() == null) continue;
            db.collection("usuarios").document(uid)
                    .collection("pasos").document(idPaso)
                    .collection("cuadrillas").document(idCuadrilla)
                    .collection("costaleros").document(c.getId())
                    .delete();
        }

        // 2) Actualizar suplementos/asistencia/altura (sin recolocar)
        for (Costalero c : listaCostaleros) {
            if (c.getId() == null) continue;
            db.collection("usuarios").document(uid)
                    .collection("pasos").document(idPaso)
                    .collection("cuadrillas").document(idCuadrilla)
                    .collection("costaleros").document(c.getId())
                    .update(
                            "nombre", c.getNombre(),
                            "apellido", c.getApellido(),
                            "altura", c.getAltura(),
                            "suplementos", c.getSuplementos(),
                            "asistencia", c.isAsistencia()
                    );
        }

        // 🔴 Eliminamos la validación de exceso de presentes

        // 3) Abrir el mapa de la cuadrilla para organizar
        FragMapaCuadrilla fragMapa = FragMapaCuadrilla.newInstance(idPaso, idCuadrilla);
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.contenedorFragmentos, fragMapa)
                .addToBackStack(null)
                .commit();
    }

}
