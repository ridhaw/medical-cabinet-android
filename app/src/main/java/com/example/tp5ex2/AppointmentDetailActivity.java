package com.example.tp5ex2;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;

public class AppointmentDetailActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private int appointmentId;
    private Appointment appointment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment_detail);

        dbHelper = DatabaseHelper.getInstance(this);
        appointmentId = getIntent().getIntExtra("appointment_id", -1);

        initializeViews();
        loadAppointmentDetails();
    }

    private void initializeViews() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Détails du Rendez-vous");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void loadAppointmentDetails() {
        // Note: Vous devez ajouter une méthode getAppointmentById() dans DatabaseHelper
        // Pour l'instant, on utilise des données fictives

        if (appointmentId != -1) {
            // Récupérer le rendez-vous depuis la base
            // appointment = dbHelper.getAppointmentById(appointmentId);

            // Données fictives pour l'exemple
            appointment = new Appointment(
                    appointmentId,
                    1, // cabinetId
                    "Patient Test",
                    "0123456789",
                    "01/01/2024",
                    "10:30",
                    "Première consultation",
                    "confirmé"
            );

            displayAppointmentDetails();
        }
    }

    private void displayAppointmentDetails() {
        if (appointment != null) {
            TextView tvPatientName = findViewById(R.id.tvPatientName);
            TextView tvPatientPhone = findViewById(R.id.tvPatientPhone);
            TextView tvDate = findViewById(R.id.tvDate);
            TextView tvTime = findViewById(R.id.tvTime);
            TextView tvStatus = findViewById(R.id.tvStatus);
            TextView tvNotes = findViewById(R.id.tvNotes);

            tvPatientName.setText(appointment.getPatientName());
            tvPatientPhone.setText(appointment.getPatientPhone());
            tvDate.setText(appointment.getAppointmentDate());
            tvTime.setText(appointment.getAppointmentTime());
            tvStatus.setText(appointment.getStatus());
            tvNotes.setText(appointment.getNotes());
        }
    }

}