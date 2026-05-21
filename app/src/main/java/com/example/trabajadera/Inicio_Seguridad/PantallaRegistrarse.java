package com.example.trabajadera.Inicio_Seguridad;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.trabajadera.R;

import java.util.Random;

import javax.mail.SendFailedException;

public class PantallaRegistrarse extends AppCompatActivity {
    EditText nombrePantalla;
    EditText correoPantalla;
    EditText contraseniaPantalla;
    EditText confirmarContraseniaPantalla;
    Button completar;
    Button atras;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.p_registrarse);

        nombrePantalla = findViewById(R.id.RegistrarseNombreCompleto);
        correoPantalla = findViewById(R.id.RegistrarseCorreoElectronico);
        contraseniaPantalla = findViewById(R.id.RegistrarseContraseña);
        confirmarContraseniaPantalla = findViewById(R.id.RegistrarseConfirmarContraseña);
        completar = findViewById(R.id.RegistrarseBotonCompletar);
        atras = findViewById(R.id.RegistrarseBotonAtras);


        atras.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


        completar.setOnClickListener(v -> {
            String nombre = nombrePantalla.getText().toString().trim();
            String correo = correoPantalla.getText().toString().trim();
            String contrasenia = contraseniaPantalla.getText().toString().trim();
            String confirmarContrasenia = confirmarContraseniaPantalla.getText().toString().trim();

            if (correo.isEmpty() || contrasenia.isEmpty() || confirmarContrasenia.isEmpty()) {
                Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
                Toast.makeText(this, "Correo electrónico no válido", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!contrasenia.equals(confirmarContrasenia)) {
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
                return;
            }

            if (contrasenia.length() < 6) {
                Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show();
                return;
            }

            enviarCorreo(correo,contrasenia, nombre);
        });

    }
    //Funciones y Metodos
    void showFirebaseError (Exception e){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Error de registro");
        builder.setMessage("Firebase dice: " + e.getMessage());
        builder.setPositiveButton("Aceptar", null);
        builder.show();
    }





    void enviarCorreo (String correo,String contrasenia, String nombre){
        String codigo = String.format("%05d", new Random().nextInt(999999)+1);
        String subject  = "Autenticación de cuenta";
        String template = getResources().getText(R.string.email_body).toString();
        String body     = String.format(template, codigo);

        MailSender.send(
                this, correo, subject, body,
                new MailSender.MailCallback() {
                    @Override
                    public void onSuccess() {
                        Intent pantallaConfirmar = new Intent(PantallaRegistrarse.this, PantallaConfirmacion.class);
                        pantallaConfirmar.putExtra("confirmation_code", codigo);
                        pantallaConfirmar.putExtra("nombre", nombre);
                        pantallaConfirmar.putExtra("email", correo);
                        pantallaConfirmar.putExtra("propietario", TipoPropietario.BASIC);
                        pantallaConfirmar.putExtra("contrasenia", contrasenia);

                        startActivity(pantallaConfirmar);
                        overridePendingTransition(
                                R.anim.fragment_zoom_in,  // animación de entrada
                                R.anim.fragment_zoom_out  // animación de salida
                        );

                    }
                    @Override
                    public void onError(Exception e) {

                        if (e instanceof SendFailedException) {
                            Toast.makeText(PantallaRegistrarse.this,
                                    "Correo no válido o no existente",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(PantallaRegistrarse.this,
                                    "Error al enviar correo: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                }
        );
    }
}






