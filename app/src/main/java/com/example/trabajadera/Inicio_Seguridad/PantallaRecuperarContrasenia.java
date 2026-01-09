package com.example.trabajadera.Inicio_Seguridad;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.trabajadera.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class PantallaRecuperarContrasenia extends AppCompatActivity {
    EditText correo;
    Button confirmar;
    Button volver;
    FirebaseAuth mAuth;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.p_recuperar_contrasenia);

        correo = findViewById(R.id.correoRecuperar);
        confirmar = findViewById(R.id.botonConfirmar);
        volver = findViewById(R.id.botonVolver);
        mAuth = FirebaseAuth.getInstance();

        volver.setOnClickListener(view -> finish());

        confirmar.setOnClickListener(view -> {
            String correoPantalla = correo.getText().toString().trim();

            if (correoPantalla.isEmpty()) {
                Toast.makeText(PantallaRecuperarContrasenia.this, "Introduce un correo", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseFirestore db = FirebaseFirestore.getInstance();

            db.collection("usuarios")
                    .whereEqualTo("correo", correoPantalla)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            if (!task.getResult().isEmpty()) {
                                // Obtenemos el primer documento (correo único)
                                String tipoPropietario = task.getResult()
                                        .getDocuments()
                                        .get(0)
                                        .getString("tipoPropietario");

                                Log.d("DEBUG", "tipoPropietario = " + tipoPropietario);

                                if (tipoPropietario != null && tipoPropietario.equalsIgnoreCase("GOOGLE")) {
                                    // Usuario con login de Google → no necesita reset de contraseña
                                    Toast.makeText(PantallaRecuperarContrasenia.this,
                                            "Este usuario inició sesión con Google. No es necesario cambiar la contraseña.",
                                            Toast.LENGTH_LONG).show();
                                } else {
                                    // Usuario BASIC u otro → enviamos correo de Firebase
                                    enviarCorreo(correoPantalla);
                                }

                            } else {
                                // No existe el correo en Firestore
                                Toast.makeText(PantallaRecuperarContrasenia.this,
                                        "El correo NO está registrado",
                                        Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(PantallaRecuperarContrasenia.this,
                                    "Error al verificar el correo",
                                    Toast.LENGTH_SHORT).show();
                            Log.e("FIRESTORE", "Error: ", task.getException());
                        }
                    });
        });
    }

    void enviarCorreo(String correo) {
        mAuth.sendPasswordResetEmail(correo)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(PantallaRecuperarContrasenia.this,
                                "Se te envio un mensaje al SPAM de tu correo electronico.",
                                Toast.LENGTH_LONG).show();

                        // Redirigir a otra pantalla si quieres
                        Intent pantallaConfirmar = new Intent(PantallaRecuperarContrasenia.this, MainActivity.class);
                        startActivity(pantallaConfirmar);
                        overridePendingTransition(
                                R.anim.fragment_zoom_in,
                                R.anim.fragment_zoom_out
                        );

                    } else {
                        Toast.makeText(PantallaRecuperarContrasenia.this,
                                "Error al enviar correo: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
}
