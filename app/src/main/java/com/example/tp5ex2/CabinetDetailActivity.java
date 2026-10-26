package com.example.tp5ex2;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;

public class CabinetDetailActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private int cabinetId;
    private MedicalCabinet cabinet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cabinet_detail);

        // Initialiser DatabaseHelper
        dbHelper = DatabaseHelper.getInstance(this);

        // Récupérer l'ID du cabinet
        cabinetId = getIntent().getIntExtra("cabinet_id", -1);

        if (cabinetId != -1) {
            loadCabinetDetails();
            setupClickListeners();
        } else {
            Toast.makeText(this, "Erreur: Cabinet non trouvé", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadCabinetDetails() {
        cabinet = dbHelper.getCabinetById(cabinetId);

        if (cabinet != null) {
            // Mettre à jour les TextView (garder votre code existant)
            android.widget.TextView tvCabinetName = findViewById(R.id.tvCabinetName);
            android.widget.TextView tvDoctorName = findViewById(R.id.tvDoctorName);
            android.widget.TextView tvSpeciality = findViewById(R.id.tvSpeciality);
            android.widget.TextView tvAddress = findViewById(R.id.tvAddress);
            android.widget.TextView tvPhone = findViewById(R.id.tvPhone);
            android.widget.TextView tvEmail = findViewById(R.id.tvEmail);
            android.widget.TextView tvAvailability = findViewById(R.id.tvAvailability);

            if (tvCabinetName != null) tvCabinetName.setText(cabinet.getName());
            if (tvDoctorName != null) tvDoctorName.setText("Dr. " + cabinet.getDoctorName());
            if (tvSpeciality != null) tvSpeciality.setText(cabinet.getSpeciality());
            if (tvAddress != null) tvAddress.setText(cabinet.getAddress());
            if (tvPhone != null) tvPhone.setText(cabinet.getPhone());
            if (tvEmail != null) tvEmail.setText(cabinet.getEmail());
            if (tvAvailability != null) tvAvailability.setText(formatAvailability(cabinet.getAvailability()));
        } else {
            Toast.makeText(this, "Cabinet non trouvé", Toast.LENGTH_SHORT).show();
        }
    }

    private String formatAvailability(String availability) {
        if (availability == null || availability.isEmpty()) {
            return "🕐 Lundi - Vendredi: 08:00-12:00, 14:00-18:00\n" +
                    "🕐 Samedi: 08:00-13:00\n" +
                    "❌ Dimanche: Fermé";
        }
        return availability;
    }

    private void setupClickListeners() {
        // Bouton retour
        ImageButton btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // Bouton Prendre Rendez-vous
        MaterialButton btnBookAppointment = findViewById(R.id.btnBookAppointment);
        if (btnBookAppointment != null) {
            btnBookAppointment.setOnClickListener(v -> {
                if (cabinet != null) {
                    navigateToAppointmentActivity();
                } else {
                    Toast.makeText(this, "Erreur: Informations du cabinet non disponibles", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Bouton Appeler
        MaterialButton btnCall = findViewById(R.id.btnCall);
        if (btnCall != null) {
            btnCall.setOnClickListener(v -> {
                if (cabinet != null && cabinet.getPhone() != null && !cabinet.getPhone().isEmpty()) {
                    makePhoneCall(cabinet.getPhone());
                } else {
                    Toast.makeText(this, "Numéro de téléphone non disponible", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Bouton Itinéraire
        MaterialButton btnDirections = findViewById(R.id.btnDirections);
        if (btnDirections != null) {
            btnDirections.setOnClickListener(v -> {
                if (cabinet != null && cabinet.getAddress() != null && !cabinet.getAddress().isEmpty()) {
                    openMapWithDirections(cabinet.getAddress());
                } else {
                    Toast.makeText(this, "Adresse non disponible", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Bouton Email (optionnel - si vous voulez ajouter un bouton email)
        // Vous pouvez ajouter un bouton pour envoyer un email
    }

    private void navigateToAppointmentActivity() {
        try {
            Intent intent = new Intent(this, AppointmentActivity.class);
            // Passer les informations nécessaires
            intent.putExtra("cabinet_id", cabinetId);
            intent.putExtra("cabinet_name", cabinet.getName());
            intent.putExtra("doctor_name", cabinet.getDoctorName());
            intent.putExtra("speciality", cabinet.getSpeciality());
            intent.putExtra("address", cabinet.getAddress());
            intent.putExtra("phone", cabinet.getPhone());

            // Ajouter les informations utilisateur si disponibles
            intent.putExtra("user_name", getIntent().getStringExtra("user_name"));
            intent.putExtra("user_id", getIntent().getStringExtra("user_id"));
            intent.putExtra("user_email", getIntent().getStringExtra("user_email"));
            intent.putExtra("account_type", getIntent().getStringExtra("account_type"));

            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void makePhoneCall(String phoneNumber) {
        try {
            // Nettoyer le numéro de téléphone
            String cleanedNumber = phoneNumber.replaceAll("[^0-9+]", "");

            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + cleanedNumber));

            // Vérifier si une application de téléphonie est disponible
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            } else {
                Toast.makeText(this, "Aucune application de téléphonie disponible", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Impossible d'appeler ce numéro", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void openMapWithDirections(String address) {
        try {
            // Nettoyer l'adresse pour l'URL
            String encodedAddress = Uri.encode(address);

            // Créer l'URI pour Google Maps
            Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + encodedAddress);

            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");

            // Vérifier si Google Maps est installé
            if (mapIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(mapIntent);
            } else {
                // Fallback: ouvrir dans le navigateur ou une autre application de carte
                Uri fallbackUri = Uri.parse("https://maps.google.com/?q=" + encodedAddress);
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, fallbackUri);
                startActivity(browserIntent);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Impossible d'ouvrir la carte", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}