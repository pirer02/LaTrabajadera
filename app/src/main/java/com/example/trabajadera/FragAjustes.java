package com.example.trabajadera;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.trabajadera.Inicio_Seguridad.MainActivity;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class FragAjustes extends Fragment {

    private TextView txtNombrePerfil, txtEmailPerfil, txtTipoRegistro;
    private MaterialButton borrarCuenta;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.f_ajustes, container, false);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        txtNombrePerfil = view.findViewById(R.id.txtNombrePerfil);
        txtEmailPerfil = view.findViewById(R.id.txtEmailPerfil);
        txtTipoRegistro = view.findViewById(R.id.txtTipoRegistro);
        borrarCuenta = view.findViewById(R.id.borrarCuenta);

        // Cargamos la información automáticamente al entrar en el fragmento
        cargarDatosUsuario();

        // Asignamos el click para borrar la cuenta pidiendo confirmación primero
        borrarCuenta.setOnClickListener(v -> mostrarDialogoBorrarCuenta());

        return view;
    }

    private void cargarDatosUsuario() {
        if (getActivity() != null) {
            SharedPreferences prefs = getActivity().getSharedPreferences(
                    getString(R.string.prefs_file), Context.MODE_PRIVATE);

            // Rescatamos los valores guardados al hacer login
            String nombre = prefs.getString("nombre", "Usuario");
            String email = prefs.getString("email", "Sin correo");
            String propietario = prefs.getString("propietario", "Desconocido");

            // Rellenamos la interfaz
            txtNombrePerfil.setText(nombre);
            txtEmailPerfil.setText(email);

            // Le damos formato amigable al texto del tipo de registro
            if (propietario.equalsIgnoreCase("GOOGLE")) {
                txtTipoRegistro.setText("Cuenta de Google");
            } else if (propietario.equalsIgnoreCase("BASIC")) {
                txtTipoRegistro.setText("Correo y Contraseña");
            } else {
                txtTipoRegistro.setText(propietario);
            }
        }
    }

    private void mostrarDialogoBorrarCuenta() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Eliminar Cuenta")
                .setMessage("¿Estás completamente seguro de que deseas eliminar tu cuenta? Esta acción es irreversible y perderás el acceso a todos los datos de tus pasos y cuadrillas.")
                .setPositiveButton("Sí, eliminar", (dialog, which) -> eliminarCuentaFirebase())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void eliminarCuentaFirebase() {
        if (auth.getCurrentUser() != null) {
            String uid = auth.getCurrentUser().getUid();

            // 1. Borrar datos de Firestore
            db.collection("usuarios").document(uid).delete()
                    .addOnSuccessListener(aVoid -> {
                        // 2. Eliminar el usuario de Firebase Authentication
                        auth.getCurrentUser().delete()
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        // 3. Limpiar los datos guardados en el teléfono para que no intente autologearse
                                        if (getActivity() != null) {
                                            getActivity().getSharedPreferences(
                                                            getString(R.string.prefs_file), Context.MODE_PRIVATE)
                                                    .edit().clear().apply();
                                        }

                                        Toast.makeText(getContext(), "Cuenta eliminada correctamente", Toast.LENGTH_SHORT).show();

                                        Intent intent = new Intent(getActivity(), MainActivity.class);
                                        startActivity(intent);
                                        requireActivity().finish();
                                    } else {
                                        Toast.makeText(getContext(), "Error al eliminar la cuenta: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Error al eliminar datos de Firestore: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        } else {
            Toast.makeText(getContext(), "No hay usuario autenticado", Toast.LENGTH_SHORT).show();
        }
    }
}