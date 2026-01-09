package com.example.trabajadera.Inicio_Seguridad;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.trabajadera.PantallaSesionIniciada;
import com.example.trabajadera.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.mail.SendFailedException;

public class PantallaConfirmacion extends AppCompatActivity {

    boolean recordar;
    String nombre;
    String correo;
    String propietario;
    String contrasenia;
    Button confirmar;
    Button volver;
    String codigo;
    EditText etDigit1, etDigit2, etDigit3, etDigit4, etDigit5, etDigit6;
    TextView tvInfo; // TextView donde mostramos el mensaje con correo, tiempo y enlace

    CountDownTimer countDownTimer; // Temporizador de 3 minutos

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.p_confirmacion);
        FirebaseFirestore db = FirebaseFirestore.getInstance();


        // Referencias a los EditText
        etDigit1 = findViewById(R.id.etDigit1);
        etDigit2 = findViewById(R.id.etDigit2);
        etDigit3 = findViewById(R.id.etDigit3);
        etDigit4 = findViewById(R.id.etDigit4);
        etDigit5 = findViewById(R.id.etDigit5);
        etDigit6 = findViewById(R.id.etDigit6);

        // Referencia al TextView
        tvInfo = findViewById(R.id.tvInfo);
        configurarAutoFocus();


        // Recibimos los datos enviados desde la pantalla anterior
        nombre = getIntent().getStringExtra("nombre");
        correo = getIntent().getStringExtra("email");
        contrasenia = getIntent().getStringExtra("contrasenia");
        propietario = getIntent().getStringExtra("propietario");
        codigo = getIntent().getStringExtra("confirmation_code");

        confirmar = findViewById(R.id.btnConfirmar);
        volver = findViewById(R.id.btnVolver);

        // Iniciamos el temporizador de 3 minutos
        iniciarTemporizador();

        // Acción del botón Volver
        volver.setOnClickListener(view -> {
            finish();
        });

        // Acción del botón Confirmar
        confirmar.setOnClickListener(view -> {
            String inputCode = etDigit1.getText().toString().trim() +
                    etDigit2.getText().toString().trim() +
                    etDigit3.getText().toString().trim() +
                    etDigit4.getText().toString().trim() +
                    etDigit5.getText().toString().trim() +
                    etDigit6.getText().toString().trim();

            // Verificamos si el código coincide y no ha caducado
            if (codigo != null && inputCode.equals(codigo)) {
                    FirebaseAuth.getInstance()
                            .createUserWithEmailAndPassword(correo, contrasenia)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    FirebaseUser user = task.getResult().getUser();

                                    // Actualizamos el nombre en el perfil de Firebase Auth
                                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                            .setDisplayName(nombre) // nombre que recibiste del Intent
                                            .build();

                                    user.updateProfile(profileUpdates)
                                            .addOnCompleteListener(updateTask -> {
                                                if (updateTask.isSuccessful()) {
                                                    String userId = user.getUid();
                                                    String userEmail = user.getEmail();
                                                    String userName = user.getDisplayName();
                                                    showHome(userId != null ? userId : "", userName != null ? userName : "", userEmail != null ? userEmail : "", TipoPropietario.BASIC);
                                                } else {
                                                    showFirebaseError(updateTask.getException());
                                                }
                                            });
                                }

                            });

            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(PantallaConfirmacion.this);
                builder.setTitle("Código incorrecto o caducado");
                builder.setMessage("El código introducido no coincide o ha caducado. Inténtalo de nuevo.");
                builder.setPositiveButton("Aceptar", null);
                builder.show();
            }
        });
    }

    /**
     * Inicia un temporizador de 3 minutos que actualiza el texto cada segundo
     * y hace que el código caduque al finalizar.
     */
    private void iniciarTemporizador() {
        countDownTimer = new CountDownTimer(180000, 1000) { // 3 minutos en milisegundos
            public void onTick(long millisUntilFinished) {
                long seconds = millisUntilFinished / 1000;
                long minutes = seconds / 60;
                long remainingSeconds = seconds % 60;
                String tiempo = String.format("%02d:%02d", minutes, remainingSeconds);

                // Actualizamos el texto con el tiempo restante y el enlace clicable
                setTextoConEnlace("Se te ha enviado un correo a " + correo +
                        ", el mensaje caducará en " + tiempo +
                        ". Si no lo recibes, Pincha aquí para mandar otro código.");
            }

            public void onFinish() {
                codigo = null; // Invalida el código
                setTextoConEnlace("El código ha caducado. Pincha aquí para mandar otro código.");
            }
        }.start();
    }

    /**
     * Configura el texto del TextView y hace que "Pincha aquí" sea clicable.
     */
    private void setTextoConEnlace(String texto) {
        SpannableString spannable = new SpannableString(texto);
        int start = texto.indexOf("Pincha aquí");
        if (start >= 0) {
            int end = start + "Pincha aquí".length();
            spannable.setSpan(new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    // Genera y envía un nuevo código
                    enviarCorreo(correo, contrasenia);
                }
            }, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        tvInfo.setText(spannable);
        tvInfo.setMovementMethod(LinkMovementMethod.getInstance());
    }

    /**
     * Muestra un diálogo con el error de Firebase.
     */
    void showFirebaseError(Exception e) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Error de registro");
        builder.setMessage("Firebase dice: " + e.getMessage());
        builder.setPositiveButton("Aceptar", null);
        builder.show();
    }

    /**
     * Abre la pantalla de sesión iniciada.
     */
    void showHome(String idUsuario, String nombre, String email, TipoPropietario provider) {
        // Crear el mapa con los datos del usuario
        Map<String, Object> datosUsuario = new HashMap<>();
        datosUsuario.put("nombre", nombre);
        datosUsuario.put("correo", email);
        datosUsuario.put("idUsuario", idUsuario);
        datosUsuario.put("tipoPropietario", "BASIC");


        // Guardar en Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("usuarios")
                .document(idUsuario) // El documento tendrá como ID el UID del usuario
                .set(datosUsuario)
                .addOnSuccessListener(aVoid -> {
                    // Si se guarda correctamente, pasamos a la siguiente pantalla
                    Intent pantallaIniciada = new Intent(PantallaConfirmacion.this, PantallaSesionIniciada.class);
                    pantallaIniciada.putExtra("idUsuario", idUsuario);
                    pantallaIniciada.putExtra("nombre", nombre);
                    pantallaIniciada.putExtra("email", email);
                    pantallaIniciada.putExtra("propietario", provider.name());
                    startActivity(pantallaIniciada);
                })
                .addOnFailureListener(e -> {
                    // Si falla, mostramos el error
                    showFirebaseError(e);
                });
    }



    void enviarCorreo(String correo, String contrasenia) {
        String codigoEmail = String.format("%06d", new Random().nextInt(999999)); // 6 dígitos
        String subject  = "Autenticación de cuenta";
        String template = getResources().getText(R.string.email_body).toString();
        String body     = String.format(template, codigoEmail);
        codigo = codigoEmail;

        MailSender.send(
                this, correo, subject, body,
                new MailSender.MailCallback() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(PantallaConfirmacion.this,
                                "Se te acaba de enviar otro correo con un nuevo código",
                                Toast.LENGTH_LONG).show();

                        // Reinicia el temporizador y el texto
                        if (countDownTimer != null) {
                            countDownTimer.cancel();
                        }
                        iniciarTemporizador();
                    }

                    @Override
                    public void onError(Exception e) {
                        if (e instanceof SendFailedException) {
                            Toast.makeText(PantallaConfirmacion.this,
                                    "Correo no válido o no existente",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(PantallaConfirmacion.this,
                                    "Error al enviar correo: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                }
        );
    }


    private void configurarAutoFocus() {
        EditText[] campos = {etDigit1, etDigit2, etDigit3, etDigit4, etDigit5, etDigit6};

        for (int i = 0; i < campos.length; i++) {
            final int index = i;

            // TextWatcher para avanzar automáticamente al escribir
            campos[i].addTextChangedListener(new android.text.TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    // Avanzar si se escribe un dígito
                    if (s.length() == 1 && index < campos.length - 1) {
                        campos[index + 1].requestFocus();
                    }
                }

                @Override
                public void afterTextChanged(android.text.Editable s) {}
            });

            // OnKeyListener para retroceder y borrar el campo anterior
            campos[i].setOnKeyListener((v, keyCode, event) -> {
                if (keyCode == android.view.KeyEvent.KEYCODE_DEL && event.getAction() == android.view.KeyEvent.ACTION_DOWN) {
                    if (campos[index].getText().toString().isEmpty() && index > 0) {
                        // Borrar el campo anterior y mover el foco
                        campos[index - 1].setText("");
                        campos[index - 1].requestFocus();
                        return true; // Consumimos el evento
                    }
                }
                return false;
            });
        }
    }



}
