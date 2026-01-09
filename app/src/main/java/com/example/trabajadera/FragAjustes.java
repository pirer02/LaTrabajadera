package com.example.trabajadera;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.trabajadera.CrearPaso.FragCrearPaso1;
import com.example.trabajadera.Inicio_Seguridad.MainActivity;
import com.example.trabajadera.PasarLista.Paso;
import com.example.trabajadera.PasarLista.PasoAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class FragAjustes extends Fragment {
    private Button borrarCuenta;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.f_ajustes, container, false);

        borrarCuenta = view.findViewById(R.id.borrarCuenta);
        borrarCuenta.setOnClickListener(v -> borrarCuenta());

        return view;
    }


    private void borrarCuenta() {
        if (auth.getCurrentUser() != null) {
            String uid = auth.getCurrentUser().getUid();

            db.collection("usuarios").document(uid)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        auth.getCurrentUser().delete()
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(getContext(),
                                                "Cuenta eliminada correctamente",
                                                Toast.LENGTH_SHORT).show();

                                        Intent intent = new Intent(getActivity(), MainActivity.class);
                                        startActivity(intent);
                                        requireActivity().finish();
                                    } else {
                                        Toast.makeText(getContext(),
                                                "Error al eliminar la cuenta: " + task.getException().getMessage(),
                                                Toast.LENGTH_LONG).show();
                                    }
                                });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(),
                                "Error al eliminar datos de Firestore: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    });
        } else {
            Toast.makeText(getContext(),
                    "No hay usuario autenticado",
                    Toast.LENGTH_SHORT).show();
        }
    }
}

