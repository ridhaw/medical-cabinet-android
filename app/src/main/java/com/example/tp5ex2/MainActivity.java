package com.example.tp5ex2;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private EditText editEmail, editPassword;
    private Button btnLogin;
    private TextView txtRegister, txtForgetPassword;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialiser DatabaseHelper avec Singleton
        dbHelper = DatabaseHelper.getInstance(this);

        // Initialisation des vues
        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        btnLogin = findViewById(R.id.btnLogin);
        txtRegister = findViewById(R.id.txtRegister);
        txtForgetPassword = findViewById(R.id.txtForgetPassword);

        // Pré-remplir l'email si venant de l'inscription
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("registered_email")) {
            String email = intent.getStringExtra("registered_email");
            editEmail.setText(email);
        }

        // Bouton Login
        btnLogin.setOnClickListener(v -> handleLogin());

        // Lien vers Register
        txtRegister.setOnClickListener(v -> {
            Intent registerIntent = new Intent(MainActivity.this, activity_register.class);
            startActivity(registerIntent);
        });

        // Lien Forgot Password - Version simple avec Toast
        txtForgetPassword.setOnClickListener(v -> {
            showRelaxToast();
        });
    }

    private void handleLogin() {
        String email = editEmail.getText().toString().trim();
        String password = editPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        // Authentification
        User user = dbHelper.authenticateUser(email, password);

        if (user != null) {
            Toast.makeText(this, "Connexion réussie !", Toast.LENGTH_SHORT).show();

            // Rediriger selon le type de compte
            Intent intent;
            if (user.isDoctor() || user.isAdmin()) {
                // Pour les médecins
                intent = new Intent(MainActivity.this, DoctorDashboardActivity.class);
            } else {
                // Pour les patients
                intent = new Intent(MainActivity.this, ListActivity2.class);
            }

            intent.putExtra("user_id", String.valueOf(user.getId()));
            intent.putExtra("user_name", user.getFullName());
            intent.putExtra("user_email", user.getEmail());
            intent.putExtra("user_tel", user.getPhone());
            intent.putExtra("account_type", user.getAccountType());
            intent.putExtra("account_type", user.getAccountType()); // "patient" ou "doctor"
            intent.putExtra("user_id", String.valueOf(user.getId()));

            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Email ou mot de passe incorrect", Toast.LENGTH_SHORT).show();
        }
    }

    private void showRelaxToast() {
        // Toast personnalisé avec emoji
        String message = "😌 Relax, take your time, and remember your password";
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();

        // Optionnel : Petite vibration pour attirer l'attention
        try {
            android.os.Vibrator vibrator = (android.os.Vibrator) getSystemService(VIBRATOR_SERVICE);
            if (vibrator != null && vibrator.hasVibrator()) {
                vibrator.vibrate(100); // Vibration de 100ms
            }
        } catch (Exception e) {
            // Ignorer si la vibration n'est pas disponible
        }
    }
}