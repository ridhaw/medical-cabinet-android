package com.example.tp5ex2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;

public class DoctorProfileActivity extends AppCompatActivity {

    // Données reçues de l'Intent
    private String user_name, user_id, user_email, account_type;

    // Views
    private TextView tvProfileName, tvProfileType, tvProfileInitial;
    private TextView tvUserId, tvEmail, tvPhone;
    private MaterialButton btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_doctor_profile);

            // Récupérer les données depuis l'Intent
            Intent intent = getIntent();
            user_name = intent.getStringExtra("user_name");
            user_id = intent.getStringExtra("user_id");
            user_email = intent.getStringExtra("user_email");
            account_type = intent.getStringExtra("account_type");

            // Log des données reçues (pour déboguer)
            Log.d("DoctorProfile", "Données reçues:");
            Log.d("DoctorProfile", "user_name: " + user_name);
            Log.d("DoctorProfile", "user_id: " + user_id);
            Log.d("DoctorProfile", "user_email: " + user_email);
            Log.d("DoctorProfile", "account_type: " + account_type);

            // Si certaines données manquent, essayer depuis SharedPreferences
            if (user_name == null) {
                SharedPreferences prefs = getSharedPreferences("session", MODE_PRIVATE);
                user_name = prefs.getString("username", null);
                user_id = prefs.getString("user_id", null);
                user_email = prefs.getString("user_email", null);
                account_type = prefs.getString("account_type", null);
            }

            if (user_name == null) {
                Toast.makeText(this, "Utilisateur non identifié", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            initializeViews();
            loadProfileData();

        } catch (Exception e) {
            Log.e("DoctorProfile", "Erreur lors de la création", e);
            Toast.makeText(this, "Erreur de chargement du profil", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initializeViews() {
        try {
            tvProfileName = findViewById(R.id.tvProfileName);
            tvProfileType = findViewById(R.id.tvProfileType);
            tvProfileInitial = findViewById(R.id.tvProfileInitial);
            tvUserId = findViewById(R.id.tvUserId);
            tvEmail = findViewById(R.id.tvEmail);
            tvPhone = findViewById(R.id.tvPhone);
            btnLogout = findViewById(R.id.btnLogout);

            btnLogout.setOnClickListener(v -> showLogoutConfirmation());

        } catch (Exception e) {
            Log.e("DoctorProfile", "Erreur d'initialisation des vues", e);
            Toast.makeText(this, "Erreur d'interface", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadProfileData() {
        try {
            Log.d("DoctorProfile", "Chargement des données depuis l'Intent...");

            // 1. Initiales pour l'avatar
            if (user_name != null && !user_name.isEmpty()) {
                String initial = user_name.substring(0, 1).toUpperCase();
                tvProfileInitial.setText(initial);
                Log.d("DoctorProfile", "Initiale: " + initial);
            } else {
                tvProfileInitial.setText("?");
            }

            // 2. Nom complet
            String displayName;
            if (user_name != null && !user_name.isEmpty()) {
                // Capitaliser la première lettre
                displayName = user_name.substring(0, 1).toUpperCase() +
                        (user_name.length() > 1 ? user_name.substring(1) : "");
                // Ajouter "Dr." si c'est un médecin
                if ("doctor".equalsIgnoreCase(account_type) || "médecin".equalsIgnoreCase(account_type)) {
                    displayName = "Dr. " + displayName;
                }
            } else {
                displayName = "Utilisateur";
            }
            tvProfileName.setText(displayName);
            Log.d("DoctorProfile", "Nom affiché: " + displayName);

            // 3. Type d'utilisateur
            String displayType;
            if (account_type != null && !account_type.isEmpty()) {
                // Capitaliser la première lettre
                displayType = account_type.substring(0, 1).toUpperCase() +
                        (account_type.length() > 1 ? account_type.substring(1) : "");
            } else {
                displayType = "Utilisateur";
            }
            tvProfileType.setText(displayType);
            Log.d("DoctorProfile", "Type: " + displayType);

            // 4. ID utilisateur
            String displayId;
            if (user_id != null && !user_id.isEmpty()) {
                // Enlever "ID: " si déjà présent
                if (user_id.startsWith("ID: ")) {
                    displayId = user_id.substring(4);
                } else {
                    displayId = user_id;
                }
            } else {
                displayId = "N/A";
            }
            tvUserId.setText(displayId);
            Log.d("DoctorProfile", "ID: " + displayId);

            // 5. Email
            String displayEmail;
            if (user_email != null && !user_email.isEmpty()) {
                displayEmail = user_email;
            } else {
                // Générer un email basé sur le nom d'utilisateur
                displayEmail = (user_name != null ? user_name.toLowerCase() : "utilisateur") + "@example.com";
            }
            tvEmail.setText(displayEmail);
            Log.d("DoctorProfile", "Email: " + displayEmail);

            // 6. Téléphone (non fourni dans l'Intent, mettre par défaut)
            // Vous pouvez ajouter cette donnée dans l'Intent si disponible
            String displayPhone = "+212 6 21 15 63 63 "; // Valeur par défaut
            tvPhone.setText(displayPhone);
            Log.d("DoctorProfile", "Téléphone: " + displayPhone);

            Log.d("DoctorProfile", "Données chargées avec succès depuis l'Intent!");

        } catch (Exception e) {
            Log.e("DoctorProfile", "Erreur de chargement des données", e);
            Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            showDefaultData();
        }
    }

    private void showDefaultData() {
        try {
            tvProfileInitial.setText("?");
            tvProfileName.setText("Utilisateur");
            tvProfileType.setText("Médecin");
            tvUserId.setText("Non disponible");
            tvEmail.setText("exemple@email.com");
            tvPhone.setText("+212 21 15 36 63");
        } catch (Exception e) {
            Log.e("DoctorProfile", "Erreur dans showDefaultData", e);
        }
    }

    private void showLogoutConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Déconnexion")
                .setMessage("Êtes-vous sûr de vouloir vous déconnecter ?")
                .setPositiveButton("Oui", (dialog, which) -> performLogout())
                .setNegativeButton("Non", null)
                .show();
    }

    private void performLogout() {
        // Effacer la session
        getSharedPreferences("session", MODE_PRIVATE).edit().clear().apply();

        // Retour à l'écran de connexion
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();

        Toast.makeText(this, "Déconnexion réussie", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Pour s'assurer que les données sont toujours à jour
        loadProfileData();
    }
}