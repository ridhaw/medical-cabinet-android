package com.example.tp5ex2;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;

public class CreateCabinetActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private int doctorId;
    private String doctorName, doctorEmail;

    private EditText editName, editAddress, editPhone, editSpeciality;
    private Button btnCreate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_cabinet);

        dbHelper = DatabaseHelper.getInstance(this);
        doctorId = getIntent().getIntExtra("user_id", -1);

        // Dans une vraie app, vous récupéreriez le nom et email depuis la base
        doctorName = "Dr. Inconnu";
        doctorEmail = "doctor@example.com";

        initializeViews();
    }

    private void initializeViews() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Créer mon Cabinet");
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        editName = findViewById(R.id.editName);
        editAddress = findViewById(R.id.editAddress);
        editPhone = findViewById(R.id.editPhone);
        editSpeciality = findViewById(R.id.editSpeciality);
        btnCreate = findViewById(R.id.btnCreate);

        btnCreate.setOnClickListener(v -> createCabinet());
    }

    private void createCabinet() {
        String name = editName.getText().toString().trim();
        String address = editAddress.getText().toString().trim();
        String phone = editPhone.getText().toString().trim();
        String speciality = editSpeciality.getText().toString().trim();

        if (name.isEmpty() || address.isEmpty() || speciality.isEmpty()) {
            Toast.makeText(this, "Tous les champs sont requis", Toast.LENGTH_SHORT).show();
            return;
        }

        long cabinetId = dbHelper.insertCabinetForDoctor(
                doctorId, name, address, speciality, phone, doctorEmail, doctorName
        );

        if (cabinetId != -1) {
            Toast.makeText(this, "Cabinet créé avec succès", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Erreur lors de la création", Toast.LENGTH_SHORT).show();
        }
    }
}