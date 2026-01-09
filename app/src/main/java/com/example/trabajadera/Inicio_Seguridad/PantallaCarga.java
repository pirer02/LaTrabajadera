package com.example.trabajadera.Inicio_Seguridad;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.trabajadera.PantallaSesionIniciada;
import com.example.trabajadera.R;

public class PantallaCarga extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.p_carga);

        // Obtener las credenciales guardadas (modo lectura)
        SharedPreferences prefs = getSharedPreferences(
                getString(R.string.prefs_file), // Nombre del archivo de preferencias
                Context.MODE_PRIVATE            // Modo privado
        );

        // Leer los valores guardados, si no existen devuelve "null"
        String idUsuario = prefs.getString("idUsuario", "null");
        String nombre = prefs.getString("nombre", "null");
        String correo = prefs.getString("email", "null");
        String propietario = prefs.getString("propietario", "null");


        // Ejemplo de uso
        if ("null".equals(idUsuario) || "null".equals(nombre) || "null".equals(correo)|| "null".equals(propietario)) {
            spashscreenstart();
        } else {
            spashscreenstartRecordado(idUsuario, correo, propietario, nombre);
        }
    }



    public void spashscreenstart() {
        new Handler().postDelayed(() -> {
            startActivity(new Intent(PantallaCarga.this, MainActivity.class));
            overridePendingTransition(
                    R.anim.fragment_zoom_in,
                    R.anim.fragment_zoom_out
            );
            finish();
        }, 2000);
    }

    public void spashscreenstartRecordado(String idUsuario, String email, String propietario,String nombre) {
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(PantallaCarga.this, PantallaSesionIniciada.class);
            //Enviar Datos aqui
            intent.putExtra("idUsuario", idUsuario);
            intent.putExtra("nombre", nombre);
            intent.putExtra("email", email);
            intent.putExtra("propietario", propietario.toString());
            startActivity(intent);

                       overridePendingTransition(
                    R.anim.fragment_zoom_in,
                    R.anim.fragment_zoom_out
            );
            finish();
        }, 2000);    }

}