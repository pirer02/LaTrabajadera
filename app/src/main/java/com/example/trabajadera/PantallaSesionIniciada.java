package com.example.trabajadera;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.trabajadera.Inicio_Seguridad.MainActivity;
import com.google.firebase.auth.FirebaseAuth;

public class PantallaSesionIniciada extends AppCompatActivity {

    TextView textoBienvenido;
    ImageButton cerrarSesion, ajuste;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.p_sesion_iniciada);

        textoBienvenido = findViewById(R.id.textoBienvenido);
        cerrarSesion = findViewById(R.id.cerrarSesion);
        ajuste = findViewById(R.id.ajuste);

        // Recuperar datos del intent
        String idUsuario = getIntent().getStringExtra("idUsuario");
        String nombre = getIntent().getStringExtra("nombre");
        String correo = getIntent().getStringExtra("email");
        String propietario = getIntent().getStringExtra("propietario");

        // Guardar en SharedPreferences
        SharedPreferences.Editor prefs = getSharedPreferences(
                getString(R.string.prefs_file),
                Context.MODE_PRIVATE
        ).edit();

        prefs.putString("nombre", nombre);
        prefs.putString("idUsuario", idUsuario);
        prefs.putString("email", correo);
        prefs.putString("propietario", propietario);
        prefs.apply();

        textoBienvenido.setText("Bienvenido " + nombre + "!");

        //Toast.makeText(this, "usuario: " + correo, Toast.LENGTH_SHORT).show();
        //Toast.makeText(this, "propietario: " + propietario, Toast.LENGTH_SHORT).show();
        //Toast.makeText(this, "nombre: " + nombre, Toast.LENGTH_SHORT).show();
        //Toast.makeText(this, "idUsuario: " + idUsuario, Toast.LENGTH_SHORT).show();

        // Botón cerrar sesión
        cerrarSesion.setOnClickListener(view -> {
            SharedPreferences.Editor editor = getSharedPreferences(
                    getString(R.string.prefs_file),
                    Context.MODE_PRIVATE
            ).edit();
            editor.clear();
            editor.apply();

            FirebaseAuth.getInstance().signOut();

            Intent intent = new Intent(PantallaSesionIniciada.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        ajuste.setOnClickListener(view -> {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.contenedorFragmentos, new FragAjustes())
                    .addToBackStack(null) // así puedes volver al FragPrincipal con "atrás"
                    .commit();
        });



        // Cargar fragmento inicial con los botones (OpcionesFragment)
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.contenedorFragmentos, new FragPrincipal())
                    .commit();
        }
    }
}
