package com.example.tp5ex2;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;

public class ManageCabinetActivity extends AppCompatActivity {

    private static final String TAG = "ManageCabinet";

    private DatabaseHelper dbHelper;
    private int cabinetId;
    private MedicalCabinet cabinet;

    private EditText editName, editAddress, editPhone, editEmail, editWebsite;
    private EditText editSpeciality, editServices, editAvailability;
    private Switch switchAvailable; // Switch standard
    private Button btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            setContentView(R.layout.activity_manage_cabinet);
            Log.d(TAG, "Layout chargé avec succès");
        } catch (Exception e) {
            Log.e(TAG, "Erreur chargement layout: " + e.getMessage());
            Toast.makeText(this, "Erreur d'interface", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        try {
            dbHelper = DatabaseHelper.getInstance(this);
            cabinetId = getIntent().getIntExtra("cabinet_id", -1);
            Log.d(TAG, "cabinetId: " + cabinetId);

            initializeViews();
            loadCabinetData();
        } catch (Exception e) {
            Log.e(TAG, "Erreur initialisation: " + e.getMessage());
            Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void initializeViews() {
        try {
            MaterialToolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle("Gérer mon Cabinet");
            }
            toolbar.setNavigationOnClickListener(v -> finish());

            editName = findViewById(R.id.editName);
            editAddress = findViewById(R.id.editAddress);
            editPhone = findViewById(R.id.editPhone);
            editEmail = findViewById(R.id.editEmail);
            editWebsite = findViewById(R.id.editWebsite);
            editSpeciality = findViewById(R.id.editSpeciality);
            editServices = findViewById(R.id.editServices);
            editAvailability = findViewById(R.id.editAvailability);
            switchAvailable = findViewById(R.id.switchAvailable);
            btnSave = findViewById(R.id.btnSave);

            Log.d(TAG, "Toutes les vues initialisées");

            btnSave.setOnClickListener(v -> saveCabinet());
        } catch (Exception e) {
            Log.e(TAG, "Erreur initialisation vues: " + e.getMessage());
            Toast.makeText(this, "Problème d'interface: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void loadCabinetData() {
        if (cabinetId != -1) {
            try {
                cabinet = dbHelper.getCabinetById(cabinetId);
                if (cabinet != null) {
                    editName.setText(cabinet.getName() != null ? cabinet.getName() : "");
                    editAddress.setText(cabinet.getAddress() != null ? cabinet.getAddress() : "");
                    editPhone.setText(cabinet.getPhone() != null ? cabinet.getPhone() : "");
                    editEmail.setText(cabinet.getEmail() != null ? cabinet.getEmail() : "");
                    editWebsite.setText(cabinet.getWebsite() != null ? cabinet.getWebsite() : "");
                    editSpeciality.setText(cabinet.getSpeciality() != null ? cabinet.getSpeciality() : "");
                    editServices.setText(cabinet.getServices() != null ? cabinet.getServices() : "");
                    editAvailability.setText(cabinet.getAvailability() != null ? cabinet.getAvailability() : "");
                    switchAvailable.setChecked(cabinet.isAvailable());
                    Log.d(TAG, "Données chargées avec succès pour cabinet ID: " + cabinet.getId());
                } else {
                    Log.d(TAG, "Aucun cabinet trouvé avec ID: " + cabinetId);
                    Toast.makeText(this, "Cabinet non trouvé. Créez d'abord un cabinet.", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Log.e(TAG, "Erreur chargement données: " + e.getMessage());
                Toast.makeText(this, "Erreur chargement données", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.d(TAG, "Aucun ID de cabinet reçu");
            Toast.makeText(this, "Aucun cabinet sélectionné", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveCabinet() {
        try {
            // Récupérer les valeurs
            String name = editName.getText().toString().trim();
            String address = editAddress.getText().toString().trim();
            String phone = editPhone.getText().toString().trim();
            String email = editEmail.getText().toString().trim();
            String website = editWebsite.getText().toString().trim();
            String speciality = editSpeciality.getText().toString().trim();
            String services = editServices.getText().toString().trim();
            //String horaires = editHoraires.getText().toString().trim(); // Note: changé de "availability" à "horaires"
            boolean isAvailable = switchAvailable.isChecked();

            if (name.isEmpty() || address.isEmpty() || speciality.isEmpty()) {
                Toast.makeText(this, "Nom, adresse et spécialité sont requis", Toast.LENGTH_SHORT).show();
                return;
            }

            // SOLUTION : Utiliser SQLiteDatabase directement
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            try {
                // 1. Supprimer tous les cabinets existants (s'il y en a)
                db.delete("cabinet", null, null);
                Log.d(TAG, "Anciens cabinets supprimés");

                // 2. Créer un ContentValues avec les nouvelles données
                // CORRECTION : Utiliser les noms de colonnes qui correspondent à la table SQL
                ContentValues values = new ContentValues();
                values.put("nom", name); // Changé de "name" à "nom"
                values.put("adresse", address); // Changé de "address" à "adresse"
                values.put("telephone", phone); // Changé de "phone" à "telephone"
                values.put("email", email);
                values.put("site_web", website); // Changé de "website" à "site_web"
                values.put("specialite", speciality); // Changé de "speciality" à "specialite"
                values.put("services", services);
              //  values.put("horaires", horaires); // Changé de "availability" à "horaires"
                values.put("disponible", isAvailable ? 1 : 0); // Changé de "is_available" à "disponible"

                // 3. Insérer le nouveau cabinet
                long newId = db.insert("cabinet", null, values);

                if (newId != -1) {
                    Toast.makeText(this, "Cabinet enregistré avec succès!", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Nouveau cabinet créé avec ID: " + newId);
                    finish();
                } else {
                    Toast.makeText(this, "Erreur lors de l'enregistrement", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Échec de l'insertion dans la base de données");
                }

            } catch (Exception e) {
                Log.e(TAG, "Erreur base de données: " + e.getMessage(), e);
                Toast.makeText(this, "Erreur base de données: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            } finally {
                db.close();
            }

        } catch (Exception e) {
            Log.e(TAG, "Erreur générale: " + e.getMessage(), e);
            Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }}