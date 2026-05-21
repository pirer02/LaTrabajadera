package com.example.trabajadera.Inicio_Seguridad;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.trabajadera.PantallaSesionIniciada;
import com.example.trabajadera.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

enum TipoPropietario {
    BASIC,
    GOOGLE
}


public class MainActivity extends AppCompatActivity {
    int GOOGLE_SIGN_IN = 100;
    EditText correoPantalla;
    EditText contraseniaPantalla;
    Button iniciar;
    Button registrarse;
    ImageButton google;
    TextView contraseniaOlvidada;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        correoPantalla = findViewById(R.id.IniciarSesionCorreo);
        contraseniaPantalla = findViewById(R.id.IniciarSesionContraseña);
        iniciar = findViewById(R.id.IniciarSesionBotonIniciar);
        registrarse = findViewById(R.id.IniciarSesionBotonRegistrarse);
        google = findViewById(R.id.botonGoogle);


        //Contrasenia olvidada
        contraseniaOlvidada = findViewById(R.id.textoContraseniaOlvidada);
        SpannableString spannableString = new SpannableString(contraseniaOlvidada.getText());
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                Intent intent = new Intent(MainActivity.this, PantallaRecuperarContrasenia.class);
                startActivity(intent);
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(false); // Opcional: quitar subrayado
                ds.setColor(Color.BLACK);
            }
        };

        // Encuentra la posición de “Pincha aquí” en el texto
        String textoCompleto = contraseniaOlvidada.getText().toString();
        int inicio = textoCompleto.indexOf("Pincha aquí");
        int fin = inicio + "Pincha aquí".length();

        spannableString.setSpan(clickableSpan, inicio, fin, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        contraseniaOlvidada.setText(spannableString);
        contraseniaOlvidada.setMovementMethod(LinkMovementMethod.getInstance());



        //Entrar con GOOGLE
        google.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GoogleSignInOptions googlConf = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build();

                // Aquí usamos el contexto correcto de la Activity
                GoogleSignInClient googleClient = GoogleSignIn.getClient(MainActivity.this, googlConf);

                googleClient.signOut();

                startActivityForResult(googleClient.getSignInIntent(), GOOGLE_SIGN_IN);


            }
        });


        registrarse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, PantallaRegistrarse.class);
                startActivity(intent);
            }
        });


        iniciar.setOnClickListener(view -> {
            String correo = correoPantalla.getText().toString().trim();
            String contrasenia = contraseniaPantalla.getText().toString().trim();

            // Validar campos vacíos
            if (correo.isEmpty() || contrasenia.isEmpty()) {
                Toast.makeText(MainActivity.this, "Por favor, rellena todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            //Iniciar sesión con Firebase
            FirebaseAuth.getInstance()
                    .signInWithEmailAndPassword(correo, contrasenia)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            //Inicio de sesión correcto
                            String idUsuario = task.getResult().getUser().getUid();
                            String usuarioGmail = task.getResult().getUser().getEmail();
                            String usuarioNombre = task.getResult().getUser().getDisplayName();

                            IniciarSesion(
                                    idUsuario != null ? idUsuario : "",
                                    usuarioGmail != null ? usuarioGmail : "",
                                    TipoPropietario.BASIC,
                                    usuarioNombre != null ? usuarioNombre : ""
                            );

                        } else {
                            //Error en el inicio de sesión → traducir mensaje
                            String mensajeError = "Error desconocido";

                            Exception exception = task.getException();
                            if (exception != null) {
                                if (exception instanceof FirebaseAuthInvalidUserException) {
                                    mensajeError = "No existe una cuenta con ese correo";
                                } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
                                    // Puede ser contraseña incorrecta o email mal formateado
                                    if (exception.getMessage() != null && exception.getMessage().toLowerCase().contains("badly formatted")) {
                                        mensajeError = "El formato del correo no es válido";
                                    } else {
                                        mensajeError = "Campos incorrectos";
                                    }
                                } else if (exception instanceof FirebaseAuthUserCollisionException) {
                                    mensajeError = "Este correo ya está en uso";
                                } else {
                                    mensajeError = exception.getMessage(); // Mensaje genérico
                                }
                            }

                            Toast.makeText(MainActivity.this, mensajeError, Toast.LENGTH_LONG).show();
                        }
                    });
        });



    }

    public void IniciarSesion(String idUsuario, String email, TipoPropietario propietario, String nombre) {
        Intent intent = new Intent(MainActivity.this, PantallaSesionIniciada.class);
        //Enviar Datos aqui
        intent.putExtra("idUsuario", idUsuario);
        intent.putExtra("email", email);
        intent.putExtra("nombre", nombre);
        intent.putExtra("propietario", propietario.toString());

        startActivity(intent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GOOGLE_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);

                if (account != null) {
                    // Crear credencial para Firebase con el token de Google
                    AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);

                    FirebaseAuth.getInstance().signInWithCredential(credential)
                            .addOnCompleteListener(this, signInTask -> {
                                if (signInTask.isSuccessful()) {
                                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                    if (user != null) {
                                        String idUsuario = user.getUid();
                                        String email = user.getEmail();
                                        String nombre = account.getGivenName(); // Nombre de pila

                                        FirebaseFirestore db = FirebaseFirestore.getInstance();

                                        // Comprobar si el documento ya existe
                                        db.collection("usuarios").document(idUsuario).get()
                                                .addOnSuccessListener(documentSnapshot -> {
                                                    if (!documentSnapshot.exists()) {
                                                        // No existe → lo creamos
                                                        Map<String, Object> datosUsuario = new HashMap<>();
                                                        datosUsuario.put("nombre", nombre);
                                                        datosUsuario.put("correo", email);
                                                        datosUsuario.put("idUsuario", idUsuario);
                                                        datosUsuario.put("tipoPropietario", "GOOGLE");

                                                        db.collection("usuarios").document(idUsuario)
                                                                .set(datosUsuario)
                                                                .addOnSuccessListener(aVoid -> {
                                                                    // Guardar en SharedPreferences
                                                                    SharedPreferences prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE);
                                                                    prefs.edit()
                                                                            .putString("idUsuario", idUsuario)
                                                                            .putString("email", email)
                                                                            .putString("propietario", TipoPropietario.GOOGLE.toString())
                                                                            .apply();

                                                                    // Ir a la siguiente pantalla
                                                                    IniciarSesion(idUsuario, email, TipoPropietario.GOOGLE, nombre);
                                                                })
                                                                .addOnFailureListener(e -> {
                                                                    Toast.makeText(MainActivity.this, "Error al guardar usuario en Firestore: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                                                });
                                                    } else {
                                                        // Ya existe → solo continuar
                                                        SharedPreferences prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE);
                                                        prefs.edit()
                                                                .putString("idUsuario", idUsuario)
                                                                .putString("email", email)
                                                                .putString("propietario", TipoPropietario.GOOGLE.toString())
                                                                .apply();

                                                        IniciarSesion(idUsuario, email, TipoPropietario.GOOGLE, nombre);
                                                    }
                                                })
                                                .addOnFailureListener(e -> {
                                                    Toast.makeText(MainActivity.this, "Error al comprobar usuario en Firestore: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                                });
                                    }
                                } else {
                                    Toast.makeText(MainActivity.this, "Fallo al inicio de sesión con Google", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            } catch (ApiException e) {
                e.printStackTrace();
                Toast.makeText(MainActivity.this, "Datos incorrectos", Toast.LENGTH_SHORT).show();
            }
        }
    }



}