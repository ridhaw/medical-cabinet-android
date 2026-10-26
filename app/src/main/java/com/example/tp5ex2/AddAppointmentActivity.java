package com.example.tp5ex2;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddAppointmentActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private int cabinetId;
    private int doctorId;

    private EditText editPatientName, editPatientPhone, editNotes;
    private TextView tvDate, tvTime;
    private Button btnSelectDate, btnSelectTime, btnSave;
    private Calendar selectedDateTime = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        dbHelper = DatabaseHelper.getInstance(this);
        cabinetId = getIntent().getIntExtra("cabinet_id", -1);
        doctorId = getIntent().getIntExtra("doctor_id", -1);

        initializeViews();
        updateDateTimeDisplay();
    }

    private void initializeViews() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Nouveau Rendez-vous");
        }
        toolbar.setNavigationOnClickListener(v -> finish());


        tvDate = findViewById(R.id.tvDate);
        tvTime = findViewById(R.id.tvTime);
        btnSelectDate = findViewById(R.id.btnSelectDate);
        btnSelectTime = findViewById(R.id.btnSelectTime);
        btnSave = findViewById(R.id.btnSave);

        btnSelectDate.setOnClickListener(v -> showDatePicker());
        btnSelectTime.setOnClickListener(v -> showTimePicker());
        btnSave.setOnClickListener(v -> saveAppointment());
    }

    private void showDatePicker() {
        DatePickerDialog datePicker = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedDateTime.set(Calendar.YEAR, year);
                    selectedDateTime.set(Calendar.MONTH, month);
                    selectedDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    updateDateTimeDisplay();
                },
                selectedDateTime.get(Calendar.YEAR),
                selectedDateTime.get(Calendar.MONTH),
                selectedDateTime.get(Calendar.DAY_OF_MONTH)
        );
        datePicker.show();
    }

    private void showTimePicker() {
        TimePickerDialog timePicker = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    selectedDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    selectedDateTime.set(Calendar.MINUTE, minute);
                    updateDateTimeDisplay();
                },
                selectedDateTime.get(Calendar.HOUR_OF_DAY),
                selectedDateTime.get(Calendar.MINUTE),
                true
        );
        timePicker.show();
    }

    private void updateDateTimeDisplay() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE);
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.FRANCE);

        tvDate.setText(dateFormat.format(selectedDateTime.getTime()));
        tvTime.setText(timeFormat.format(selectedDateTime.getTime()));
    }

    private void saveAppointment() {
        String patientName = editPatientName.getText().toString().trim();
        String patientPhone = editPatientPhone.getText().toString().trim();
        String notes = editNotes.getText().toString().trim();

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE);
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.FRANCE);

        String date = dateFormat.format(selectedDateTime.getTime());
        String time = timeFormat.format(selectedDateTime.getTime());

        if (patientName.isEmpty() || patientPhone.isEmpty()) {
            Toast.makeText(this, "Nom et téléphone du patient sont requis", Toast.LENGTH_SHORT).show();
            return;
        }

        // Créer le rendez-vous
        Appointment appointment = new Appointment(
                0, // ID auto-généré
                cabinetId,
                patientName,
                patientPhone,
                // patientId (à implémenter)
                date,
                time,
                notes,
                "confirmé"
        );

        long appointmentId = dbHelper.insertAppointment(appointment);

        if (appointmentId != -1) {
            Toast.makeText(this, "Rendez-vous ajouté avec succès", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Erreur lors de l'ajout", Toast.LENGTH_SHORT).show();
        }
    }
}