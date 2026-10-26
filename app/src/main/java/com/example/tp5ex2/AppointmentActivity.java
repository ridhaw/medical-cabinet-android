package com.example.tp5ex2;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AppointmentActivity extends AppCompatActivity {

    private EditText etPatientName, etPhone, etNotes;
    private TextView tvSelectedDate, tvSelectedTime;
    private Button btnSelectDate, btnSelectTime, btnConfirm;

    private DatabaseHelper dbHelper;
    private int cabinetId;
    private String cabinetName, doctorName, speciality;

    private Calendar selectedDateTime = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment);

        try {
            // Initialiser DatabaseHelper
            dbHelper = new DatabaseHelper(this);

            // Récupérer les informations du cabinet
            getIntentData();

            // Initialiser les vues
            initializeViews();

            // Afficher les infos du médecin
            displayDoctorInfo();

            // Configurer les listeners
            setupListeners();

        } catch (Exception e) {
            Log.e("AppointmentActivity", "Erreur dans onCreate: " + e.getMessage());
            Toast.makeText(this, "Erreur d'initialisation", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void getIntentData() {
        cabinetId = getIntent().getIntExtra("cabinet_id", -1);
        cabinetName = getIntent().getStringExtra("cabinet_name");
        doctorName = getIntent().getStringExtra("doctor_name");
        speciality = getIntent().getStringExtra("speciality");

        if (cabinetId == -1 || doctorName == null) {
            Toast.makeText(this, "Informations du cabinet manquantes", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initializeViews() {
        etPatientName = findViewById(R.id.etPatientName);
        etPhone = findViewById(R.id.etPhone);
        etNotes = findViewById(R.id.etNotes);
        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        tvSelectedTime = findViewById(R.id.tvSelectedTime);
        btnSelectDate = findViewById(R.id.btnSelectDate);
        btnSelectTime = findViewById(R.id.btnSelectTime);
        btnConfirm = findViewById(R.id.btnConfirm);
    }

    private void displayDoctorInfo() {
        TextView tvAppointmentTitle = findViewById(R.id.tvAppointmentTitle);
        tvAppointmentTitle.setText("Rendez-vous avec Dr. " + doctorName);

        TextView tvAppointmentSubtitle = findViewById(R.id.tvAppointmentSubtitle);
        tvAppointmentSubtitle.setText(speciality + " - " + cabinetName);
    }

    private void setupListeners() {
        btnSelectDate.setOnClickListener(v -> showDatePicker());
        btnSelectTime.setOnClickListener(v -> showTimePicker());
        btnConfirm.setOnClickListener(v -> confirmAppointment());
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePicker = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedDateTime.set(year, month, dayOfMonth);
                    String date = String.format(Locale.getDefault(),
                            "%02d/%02d/%04d", dayOfMonth, month + 1, year);
                    tvSelectedDate.setText(date);

                    // Si l'heure est déjà sélectionnée, afficher la date et l'heure complètes
                    if (!tvSelectedTime.getText().toString().equals("Non sélectionnée")) {
                        updateDateTimeDisplay();
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        // Définir la date minimale à aujourd'hui
        datePicker.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);

        // Définir la date maximale à 3 mois
        Calendar maxDate = Calendar.getInstance();
        maxDate.add(Calendar.MONTH, 3);
        datePicker.getDatePicker().setMaxDate(maxDate.getTimeInMillis());

        datePicker.setTitle("Choisir une date");
        datePicker.show();
    }

    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();
        TimePickerDialog timePicker = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    selectedDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    selectedDateTime.set(Calendar.MINUTE, minute);
                    String time = String.format(Locale.getDefault(),
                            "%02d:%02d", hourOfDay, minute);
                    tvSelectedTime.setText(time);
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
        );
        timePicker.setTitle("Choisir une heure");
        timePicker.show();
    }

    private void updateDateTimeDisplay() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        String fullDateTime = dateFormat.format(selectedDateTime.getTime());
        // Vous pouvez utiliser cette chaîne pour afficher ou enregistrer
    }

    private void confirmAppointment() {
        // Validation des champs
        String patientName = etPatientName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String notes = etNotes.getText().toString().trim();
        String date = tvSelectedDate.getText().toString();
        String time = tvSelectedTime.getText().toString();

        // Validation
        if (patientName.isEmpty()) {
            etPatientName.setError("Le nom du patient est requis");
            etPatientName.requestFocus();
            return;
        }

        if (phone.isEmpty()) {
            etPhone.setError("Le numéro de téléphone est requis");
            etPhone.requestFocus();
            return;
        }

        if (date.equals("Non sélectionnée")) {
            Toast.makeText(this, "Veuillez choisir une date", Toast.LENGTH_SHORT).show();
            return;
        }

        if (time.equals("Non sélectionnée")) {
            Toast.makeText(this, "Veuillez choisir une heure", Toast.LENGTH_SHORT).show();
            return;
        }

        // Formater la date et l'heure pour la base de données
        String appointmentDateTime = formatDateTimeForDB(date, time);

        try {
            // Récupérer l'ID de l'utilisateur connecté (patient)
            int patientId = getCurrentPatientId(); // À implémenter selon votre système

            // Insérer le rendez-vous dans la base de données
            long appointmentId = dbHelper.addAppointment(
                    patientId,
                    cabinetId,
                    patientName,
                    phone,
                    appointmentDateTime,
                    notes,
                    "en attente"
            );

            if (appointmentId != -1) {
                Toast.makeText(this, "Rendez-vous confirmé avec succès !", Toast.LENGTH_LONG).show();

                // Afficher les détails du rendez-vous
                showAppointmentConfirmation(patientName, date, time, cabinetName, doctorName);

                // Optionnel: retourner à l'écran précédent après un délai
                new android.os.Handler().postDelayed(() -> finish(), 3000);
            } else {
                Toast.makeText(this, "Erreur lors de la confirmation", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Log.e("AppointmentActivity", "Erreur confirmation: " + e.getMessage());
            Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private String formatDateTimeForDB(String date, String time) {
        try {
            // Convertir "dd/MM/yyyy" et "HH:mm" en format SQL
            String[] dateParts = date.split("/");
            String[] timeParts = time.split(":");

            if (dateParts.length == 3 && timeParts.length == 2) {
                return String.format(Locale.getDefault(),
                        "%s-%s-%s %s:%s:00",
                        dateParts[2], // année
                        dateParts[1], // mois
                        dateParts[0], // jour
                        timeParts[0], // heure
                        timeParts[1]  // minute
                );
            }
        } catch (Exception e) {
            Log.e("AppointmentActivity", "Erreur format date: " + e.getMessage());
        }
        return "";
    }

    private int getCurrentPatientId() {
        // À adapter selon votre système d'authentification
        // Pour l'instant, retourner un ID temporaire
        return 1;
    }

    private void showAppointmentConfirmation(String patientName, String date, String time,
                                             String cabinet, String doctor) {
        String confirmationMessage =
                "✅ Demande de rendez-vous envoyée !\n\n" +
                        "Patient: " + patientName + "\n" +
                        "Médecin: Dr. " + doctor + "\n" +
                        "Cabinet: " + cabinet + "\n" +
                        "Date: " + date + " à " + time + "\n\n" +
                        "État: En attente de confirmation\n" +
                        "Vous recevrez une confirmation par email.";

        Toast.makeText(this, confirmationMessage, Toast.LENGTH_LONG).show();
    }
}