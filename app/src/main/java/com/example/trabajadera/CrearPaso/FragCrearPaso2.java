package com.example.trabajadera.CrearPaso;

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
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trabajadera.CrearPaso.Costaleros.Costalero;
import com.example.trabajadera.CrearPaso.Costaleros.CostaleroAdapter;
import com.example.trabajadera.R;
import com.google.android.material.button.MaterialButton;

public class FragCrearPaso2 extends Fragment {

    private TextView txtCapataz, txtTipoPaso, txtHermandad, txtMaxCostaleros;
    private RecyclerView recyclerView;
    private EditText edtBuscar;
    private MaterialButton btnAddCostalero, btnContinuar;
    private CostaleroAdapter adapter;
    private PasoViewModel pasoViewModel;

    private String capataz, ciudad, hermandad, paso, tipoPaso;
    private int trabajaderas, maxCostaleros;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.f_crear_paso2, container, false);

        txtCapataz = view.findViewById(R.id.txtCapataz);
        txtTipoPaso = view.findViewById(R.id.txtTipoPaso);
        txtHermandad = view.findViewById(R.id.txtHermandad);
        txtMaxCostaleros = view.findViewById(R.id.txtMaxCostaleros);
        recyclerView = view.findViewById(R.id.recyclerCostaleros);
        edtBuscar = view.findViewById(R.id.edtBuscarCostalero);
        btnAddCostalero = view.findViewById(R.id.btnAddCostalero);
        btnContinuar = view.findViewById(R.id.btnContinuar);

        pasoViewModel = new ViewModelProvider(requireActivity()).get(PasoViewModel.class);

        Bundle args = getArguments();
        if (args != null) {
            capataz = args.getString("capataz", "");
            ciudad = args.getString("ciudad", "");
            hermandad = args.getString("hermandad", "");
            paso = args.getString("paso", "");
            tipoPaso = args.getString("tipoPaso", "");
            trabajaderas = args.getInt("trabajaderas", 0);
            maxCostaleros = args.getInt("costaleros", 0);

            txtCapataz.setText("Capataz: " + capataz);
            txtTipoPaso.setText("Tipo de Paso: " + tipoPaso + " (" + paso + ")");
            txtHermandad.setText("Hermandad: " + hermandad + " - " + ciudad);
            txtMaxCostaleros.setText("Número máximo de costaleros: " + maxCostaleros);
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new CostaleroAdapter(pasoViewModel.getListaCostaleros());
        recyclerView.setAdapter(adapter);

        edtBuscar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filtrar(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        setupSwipeToDelete();

        btnAddCostalero.setOnClickListener(v -> abrirDialogoAgregarCostalero());

        btnContinuar.setOnClickListener(v -> {
            if (pasoViewModel.getListaCostaleros().isEmpty()) {
                Toast.makeText(getContext(), "Añade al menos un costalero para continuar", Toast.LENGTH_SHORT).show();
                return;
            }
            Bundle b = new Bundle();
            b.putString("capataz", capataz);
            b.putString("tipoPaso", tipoPaso);
            b.putString("hermandad", hermandad);
            b.putString("ciudad", ciudad);
            b.putString("paso", paso);
            b.putInt("trabajaderas", trabajaderas);
            b.putInt("costaleros", maxCostaleros);

            FragCrearPaso3 f3 = new FragCrearPaso3();
            f3.setArguments(b);

            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.contenedorFragmentos, f3)
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }

    private void setupSwipeToDelete() {
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView rv, @NonNull RecyclerView.ViewHolder vh, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Costalero c = adapter.getItem(position);

                new AlertDialog.Builder(getContext())
                        .setTitle("Eliminar costalero")
                        .setMessage("¿Estás seguro de que quieres quitar a " + c.getNombre() + " " + c.getApellido() + "?")
                        .setPositiveButton("Eliminar", (dialog, which) -> {
                            adapter.removeItem(position);
                            pasoViewModel.getListaCostaleros().remove(c);
                            Toast.makeText(getContext(), "Costalero eliminado", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Cancelar", (dialog, which) -> adapter.notifyItemChanged(position))
                        .setCancelable(false)
                        .show();
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView rv, @NonNull RecyclerView.ViewHolder vh, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    View itemView = vh.itemView;
                    Paint paint = new Paint();
                    RectF background = new RectF((float) itemView.getRight() + dX, (float) itemView.getTop(), (float) itemView.getRight(), (float) itemView.getBottom());

                    LinearGradient gradient = new LinearGradient(
                            (float) itemView.getRight() + dX, (float) itemView.getTop(),
                            (float) itemView.getRight(), (float) itemView.getTop(),
                            Color.parseColor("#FF6B6B"), Color.parseColor("#EE5253"),
                            Shader.TileMode.CLAMP
                    );
                    paint.setShader(gradient);
                    c.drawRect(background, paint);

                    paint.setShader(null);
                    paint.setColor(Color.WHITE);
                    paint.setTextSize(42f);
                    paint.setAntiAlias(true);
                    paint.setTextAlign(Paint.Align.RIGHT);

                    float textMargin = 48f;
                    float textX = (float) itemView.getRight() - textMargin;
                    float textY = (float) itemView.getTop() + ((float) itemView.getHeight() / 2) + (paint.getTextSize() / 3);

                    c.drawText("Eliminar", textX, textY, paint);
                }
                super.onChildDraw(c, rv, vh, dX, dY, actionState, isCurrentlyActive);
            }
        };

        new ItemTouchHelper(simpleCallback).attachToRecyclerView(recyclerView);
    }

    private void abrirDialogoAgregarCostalero() {
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
                        adapter.setData(pasoViewModel.getListaCostaleros());
                    } catch (NumberFormatException e) {
                        Toast.makeText(getContext(), "Introduce la altura en cm (ej: 175)", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}