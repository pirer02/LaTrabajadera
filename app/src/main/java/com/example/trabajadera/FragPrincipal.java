package com.example.trabajadera;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trabajadera.CrearPaso.FragCrearPaso1;
import com.example.trabajadera.PasarLista.FragEditarPaso;
import com.example.trabajadera.PasarLista.Paso;
import com.example.trabajadera.PasarLista.PasoAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class FragPrincipal extends Fragment {

    private EditText buscador;
    private ImageButton crearPaso;
    private RecyclerView recyclerPasos;
    private PasoAdapter pasoAdapter;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.f_principal, container, false);

        crearPaso = view.findViewById(R.id.crearPaso);
        recyclerPasos = view.findViewById(R.id.recyclerPasos);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        recyclerPasos.setLayoutManager(new LinearLayoutManager(getContext()));
        pasoAdapter = new PasoAdapter(new ArrayList<>(), new PasoAdapter.OnPasoClickListener() {
            @Override
            public void onEditarClick(Paso paso) {
                // El paso.getId() NO debería ser null aquí.
                seleccionarCuadrilla(paso.getId());
            }

            @Override
            public void onBorrarClick(Paso paso, int position) {
                // Ya no se usa botón, pero mantenemos la lógica para el swipe
                confirmarBorradoPaso(paso, position);
            }
        });

        recyclerPasos.setAdapter(pasoAdapter);

        // 👉 Swipe-to-delete con degradado rojo y confirmación
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
                Paso paso = pasoAdapter.getItem(position);
                if (paso != null) {
                    confirmarBorradoPaso(paso, position);
                } else {
                    // Si el paso es null (por un error de sincronización), notificamos y salimos.
                    pasoAdapter.notifyItemChanged(position);
                }
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
                        Color.parseColor("#FF5252"),
                        Color.parseColor("#D32F2F"),
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
        new ItemTouchHelper(simpleCallback).attachToRecyclerView(recyclerPasos);


        cargarPasosUsuario();

        buscador = view.findViewById(R.id.buscadorPasos);
        buscador.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                pasoAdapter.filtrar(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        crearPaso.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.contenedorFragmentos, new FragCrearPaso1())
                    .addToBackStack(null)
                    .commit();
        });

        // Lógica para interceptar el botón de retroceso (BACK)
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Esto cierra la actividad que contiene este fragmento, saliendo de la app.
                requireActivity().finish();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), callback);

        return view;
    }

    private void confirmarBorradoPaso(Paso paso, int position) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Eliminar paso")
                .setMessage("¿Seguro que quieres borrar este paso permanentemente?")
                .setPositiveButton("Sí, borrar", (dialog, which) -> {
                    String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    FirebaseFirestore.getInstance()
                            .collection("usuarios").document(uid)
                            .collection("pasos").document(paso.getId())
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(getContext(), "Paso eliminado", Toast.LENGTH_SHORT).show();
                                pasoAdapter.removePaso(position);
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                            );
                })
                .setNegativeButton("Cancelar", (dialog, which) -> {
                    pasoAdapter.notifyItemChanged(position);
                })
                .show();
    }



    /**
     * Pregunta al usuario qué cuadrilla quiere editar si hay más de una
     */
    private void seleccionarCuadrilla(String idPaso) {
        if (auth.getCurrentUser() == null || idPaso == null) return; // 🔴 Validación extra de idPaso
        String uid = auth.getCurrentUser().getUid();

        db.collection("usuarios").document(uid)
                .collection("pasos").document(idPaso)
                .collection("cuadrillas")
                .get()
                .addOnSuccessListener(query -> {
                    List<DocumentSnapshot> docs = query.getDocuments();

                    java.util.Collections.reverse(docs);

                    int total = docs.size();
                    if (total == 0) {
                        Toast.makeText(getContext(), "Este paso no tiene cuadrillas", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (total == 1) {
                        String idCuadrilla = docs.get(0).getId();
                        abrirEditarCuadrilla(idPaso, idCuadrilla);
                    } else {
                        String[] opciones = new String[total];
                        for (int i = 0; i < total; i++) {
                            opciones[i] = "Cuadrilla " + (i + 1);
                        }

                        new AlertDialog.Builder(requireContext())
                                .setTitle("Selecciona cuadrilla")
                                .setItems(opciones, (dialog, which) -> {
                                    String idCuadrilla = docs.get(which).getId();
                                    abrirEditarCuadrilla(idPaso, idCuadrilla);
                                })
                                .show();
                    }
                });
    }

    private void abrirEditarCuadrilla(String idPaso, String idCuadrilla) {
        // 🔴 Corregido por robustez: Aseguramos que los IDs no son null antes de la navegación
        String finalIdPaso = idPaso != null ? idPaso : "";
        String finalIdCuadrilla = idCuadrilla != null ? idCuadrilla : "";

        FragEditarPaso fragEditar = FragEditarPaso.newInstance(finalIdPaso, finalIdCuadrilla);
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.contenedorFragmentos, fragEditar)
                .addToBackStack(null)
                .commit();
    }

    private void cargarPasosUsuario() {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(getContext(), "No hay usuario autenticado", Toast.LENGTH_SHORT).show();
            return;
        }
        String uid = auth.getCurrentUser().getUid();

        db.collection("usuarios").document(uid).collection("pasos")
                .get()
                .addOnSuccessListener(query -> {
                    List<Paso> lista = new ArrayList<>();
                    for (DocumentSnapshot doc : query) {
                        Paso p = doc.toObject(Paso.class);
                        if (p != null) {
                            p.setId(doc.getId());
                            lista.add(p);
                        }
                    }
                    pasoAdapter.setPasos(lista);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error al cargar pasos: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
}