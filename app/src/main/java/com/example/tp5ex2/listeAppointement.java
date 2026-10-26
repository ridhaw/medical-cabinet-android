package com.example.tp5ex2;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class listeAppointement extends AppCompatActivity {

    private static final String TAG = "listeAppointement";
    private static final String FILTER_ALL = "all";
    private static final String FILTER_PENDING = "pending";
    private static final String FILTER_CONFIRMED = "confirmed";
    private static final String FILTER_CANCELLED = "cancelled";

    private Toolbar toolbar;
    private RecyclerView recyclerViewAppointments;
    private ChipGroup chipGroup;
    private Chip chipAll, chipPending, chipConfirmed, chipCancelled;
    private TextView tvPendingCount, tvConfirmedCount, tvTotalAppointments, tvFilteredCount, tvEmptyMessage;
    private ProgressBar progressBar;
    private View llEmptyState;
    private FloatingActionButton fabNewAppointment;

    private DatabaseHelper dbHelper;
    private User currentUser;
    private List<Appointment> allAppointments = new ArrayList<>();
    private List<Appointment> filteredAppointments = new ArrayList<>();
    private AppointmentsPatientAdapter appointmentsAdapter;

    private String user_name;
    private String user_id;
    private String user_email;
    private String account_type;

    private String currentFilter = FILTER_ALL;
    private SimpleDateFormat dateDisplayFormat = new SimpleDateFormat("EEEE d MMMM yyyy", Locale.FRENCH);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_liste_appointement);

        initializeComponents();
        setupToolbar();
        setupListeners();
        loadAppointments();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAppointments();
    }

    private void initializeComponents() {
        dbHelper = DatabaseHelper.getInstance(this);
        extractUserDataFromIntent();
        initializeCurrentUser();
        initializeViews();
    }

    private void extractUserDataFromIntent() {
        Intent intent = getIntent();
        if (intent != null) {
            user_name = intent.getStringExtra("user_name");
            user_id = intent.getStringExtra("user_id");
            user_email = intent.getStringExtra("user_email");
            account_type = intent.getStringExtra("account_type");

            Log.d(TAG, "Type de compte: " + account_type);
            Log.d(TAG, "User ID: " + user_id);
            Log.d(TAG, "User Name: " + user_name);
        }
    }

    private void initializeCurrentUser() {
        currentUser = new User();

        if (!TextUtils.isEmpty(user_id)) {
            try {
                currentUser.setId(Integer.parseInt(user_id));
            } catch (NumberFormatException e) {
                currentUser.setId(0);
                Log.e(TAG, "Erreur de conversion de l'ID utilisateur", e);
            }
        }

        currentUser.setFullName(!TextUtils.isEmpty(user_name) ? user_name : "Utilisateur");
        currentUser.setEmail(!TextUtils.isEmpty(user_email) ? user_email : "");
        currentUser.setAccountType(!TextUtils.isEmpty(account_type) ? account_type : "patient");
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        tvPendingCount = findViewById(R.id.tvPendingCount);
        tvConfirmedCount = findViewById(R.id.tvConfirmedCount);
        tvTotalAppointments = findViewById(R.id.tvTotalAppointments);
        tvFilteredCount = findViewById(R.id.tvFilteredCount);
        tvEmptyMessage = findViewById(R.id.tvEmptyMessage);

        chipGroup = findViewById(R.id.chipGroup);
        chipAll = findViewById(R.id.chipAll);
        chipPending = findViewById(R.id.chipPending);
        chipConfirmed = findViewById(R.id.chipConfirmed);
        chipCancelled = findViewById(R.id.chipCancelled);

        recyclerViewAppointments = findViewById(R.id.recyclerViewAppointments);
        recyclerViewAppointments.setLayoutManager(new LinearLayoutManager(this));

        llEmptyState = findViewById(R.id.llEmptyState);
        progressBar = findViewById(R.id.progressBar);
        fabNewAppointment = findViewById(R.id.fabNewAppointment);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            if ("doctor".equalsIgnoreCase(account_type)) {
                getSupportActionBar().setTitle("Gestion des Rendez-vous");
            } else {
                getSupportActionBar().setTitle("Mes Rendez-vous");
            }
        }

        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupListeners() {
        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                chipAll.setChecked(true);
                applyFilter(FILTER_ALL);
            } else {
                handleFilterSelection(checkedIds.get(0));
            }
        });

        fabNewAppointment.setOnClickListener(v -> navigateToNewAppointment());
    }

    private void handleFilterSelection(int chipId) {
        if (chipId == R.id.chipAll) {
            applyFilter(FILTER_ALL);
        } else if (chipId == R.id.chipPending) {
            applyFilter(FILTER_PENDING);
        } else if (chipId == R.id.chipConfirmed) {
            applyFilter(FILTER_CONFIRMED);
        } else if (chipId == R.id.chipCancelled) {
            applyFilter(FILTER_CANCELLED);
        }
    }

    private void loadAppointments() {
        showLoadingState();

        new Thread(() -> {
            try {
                List<Appointment> appointments = fetchAppointments();
                sortAppointmentsByDate(appointments);

                runOnUiThread(() -> {
                    hideLoadingState();
                    handleAppointmentsResponse(appointments);
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    hideLoadingState();
                    handleLoadingError(e);
                });
            }
        }).start();
    }

    private List<Appointment> fetchAppointments() {
        List<Appointment> appointments = new ArrayList<>();

        try {
            if ("doctor".equalsIgnoreCase(account_type)) {
                appointments = fetchDoctorAppointments();
                Log.d(TAG, "Mode DOCTEUR - Rendez-vous chargés: " + appointments.size());

            } else {
                appointments = fetchPatientAppointments();
                Log.d(TAG, "Mode PATIENT - Rendez-vous chargés: " + appointments.size());
            }

        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de la récupération des rendez-vous", e);
        }

        return appointments;
    }

    private List<Appointment> fetchDoctorAppointments() {
        List<Appointment> appointments = new ArrayList<>();

        try {
            int doctorId = currentUser.getId();

            if (doctorId > 0) {
                appointments = dbHelper.getAppointmentsByDoctorId(doctorId);
                Log.d(TAG, "Rendez-vous trouvés pour docteur ID " + doctorId + ": " + appointments.size());
            } else {
                Log.w(TAG, "ID docteur invalide: " + doctorId);
            }

        } catch (Exception e) {
            Log.e(TAG, "Erreur fetchDoctorAppointments: " + e.getMessage(), e);
        }

        return appointments;
    }

    private List<Appointment> fetchPatientAppointments() {
        List<Appointment> appointments = new ArrayList<>();

        try {
            if (currentUser.getId() > 0) {
                appointments = dbHelper.getAppointmentsByPatientId(currentUser.getId());
                Log.d(TAG, "Recherche par patient_id: " + appointments.size() + " rendez-vous");
            }

            if (appointments.isEmpty() && !TextUtils.isEmpty(currentUser.getFullName())) {
                appointments = dbHelper.getAppointmentsByPatientName(currentUser.getFullName());
                Log.d(TAG, "Recherche par nom: " + appointments.size() + " rendez-vous");
            }

        } catch (Exception e) {
            Log.e(TAG, "Erreur fetchPatientAppointments: " + e.getMessage(), e);
        }

        return appointments;
    }

    private void showLoadingState() {
        progressBar.setVisibility(View.VISIBLE);
        llEmptyState.setVisibility(View.GONE);
        tvFilteredCount.setText("(0)");
    }

    private void hideLoadingState() {
        progressBar.setVisibility(View.GONE);
    }

    private void sortAppointmentsByDate(List<Appointment> appointments) {
        Collections.sort(appointments, new Comparator<Appointment>() {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

            @Override
            public int compare(Appointment a1, Appointment a2) {
                try {
                    Date date1 = dateFormat.parse(a1.getAppointmentDate());
                    Date date2 = dateFormat.parse(a2.getAppointmentDate());

                    int dateComparison = date2.compareTo(date1);

                    if (dateComparison == 0) {
                        return a2.getAppointmentTime().compareTo(a1.getAppointmentTime());
                    }

                    return dateComparison;
                } catch (ParseException e) {
                    return 0;
                }
            }
        });
    }

    private void handleAppointmentsResponse(List<Appointment> appointments) {
        if (appointments != null && !appointments.isEmpty()) {
            allAppointments = appointments;
            updateStatistics(appointments);
            applyFilter(currentFilter);
        } else {
            String emptyMessage = "doctor".equalsIgnoreCase(account_type)
                    ? "Aucun rendez-vous avec vos patients"
                    : "Vous n'avez pas encore de rendez-vous";
            showEmptyState(emptyMessage);
        }
    }

    private void handleLoadingError(Exception e) {
        Toast.makeText(this,
                "Erreur de chargement: " + e.getMessage(),
                Toast.LENGTH_SHORT).show();

        Log.e(TAG, "Erreur de chargement", e);
        showEmptyState("Erreur de chargement des rendez-vous");
    }

    private void applyFilter(String filterType) {
        currentFilter = filterType;
        filteredAppointments.clear();

        if (allAppointments.isEmpty()) {
            showEmptyState(getEmptyMessageForFilter(filterType));
            return;
        }

        for (Appointment appointment : allAppointments) {
            if (shouldIncludeAppointment(appointment, filterType)) {
                filteredAppointments.add(appointment);
            }
        }

        updateFilteredCount();

        if (filteredAppointments.isEmpty()) {
            showEmptyState(getEmptyMessageForFilter(filterType));
        } else {
            displayAppointments();
        }
    }

    private boolean shouldIncludeAppointment(Appointment appointment, String filterType) {
        String status = appointment.getStatus();
        if (TextUtils.isEmpty(status)) {
            return filterType.equals(FILTER_ALL);
        }

        status = status.toLowerCase();

        switch (filterType) {
            case FILTER_ALL:
                return true;

            case FILTER_PENDING:
                return status.contains("pending") ||
                        status.contains("en attente") ||
                        status.contains("en_attente");

            case FILTER_CONFIRMED:
                return status.contains("confirmé") ||
                        status.contains("confirmed") ||
                        status.contains("confirm");

            case FILTER_CANCELLED:
                return status.contains("annulé") ||
                        status.contains("cancelled") ||
                        status.contains("annule");

            default:
                return false;
        }
    }

    private String getEmptyMessageForFilter(String filterType) {
        switch (filterType) {
            case FILTER_PENDING:
                return "Aucun rendez-vous en attente";
            case FILTER_CONFIRMED:
                return "Aucun rendez-vous confirmé";
            case FILTER_CANCELLED:
                return "Aucun rendez-vous annulé";
            default:
                return "doctor".equalsIgnoreCase(account_type)
                        ? "Aucun rendez-vous avec vos patients"
                        : "Vous n'avez pas encore de rendez-vous";
        }
    }

    private void updateFilteredCount() {
        int filteredCount = filteredAppointments.size();
        tvFilteredCount.setText("(" + filteredCount + ")");
    }

    private void displayAppointments() {
        llEmptyState.setVisibility(View.GONE);
        recyclerViewAppointments.setVisibility(View.VISIBLE);

        if (appointmentsAdapter == null) {
            appointmentsAdapter = new AppointmentsPatientAdapter(filteredAppointments);
            recyclerViewAppointments.setAdapter(appointmentsAdapter);

            appointmentsAdapter.setOnItemClickListener(this::showAppointmentDetails);
        } else {
            appointmentsAdapter.updateData(filteredAppointments);
        }
    }

    private void showEmptyState(String message) {
        recyclerViewAppointments.setVisibility(View.GONE);
        llEmptyState.setVisibility(View.VISIBLE);
        tvEmptyMessage.setText(message);

        tvFilteredCount.setText("(0)");

        if (allAppointments.isEmpty()) {
            tvPendingCount.setText("0");
            tvConfirmedCount.setText("0");
            tvTotalAppointments.setText("Total: 0 rendez-vous");
        }
    }

    private void updateStatistics(List<Appointment> appointments) {
        int pendingCount = 0;
        int confirmedCount = 0;
        int totalCount = appointments.size();

        for (Appointment appointment : appointments) {
            String status = appointment.getStatus();
            if (!TextUtils.isEmpty(status)) {
                status = status.toLowerCase();

                if (status.contains("pending") || status.contains("en attente")) {
                    pendingCount++;
                } else if (status.contains("confirmé") || status.contains("confirmed")) {
                    confirmedCount++;
                }
            }
        }

        tvPendingCount.setText(String.valueOf(pendingCount));
        tvConfirmedCount.setText(String.valueOf(confirmedCount));
        tvTotalAppointments.setText("Total: " + totalCount + " rendez-vous");
    }

    private void showAppointmentDetails(Appointment appointment) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("📋 Détails du rendez-vous");
        builder.setMessage(buildAppointmentDetailsMessage(appointment));

        builder.setPositiveButton("👌 Fermer", null);

        // Actions spécifiques au docteur
        if ("doctor".equalsIgnoreCase(account_type)) {
            builder.setNeutralButton("🔄 Modifier", (dialog, which) ->
                    showDoctorActionsDialog(appointment));
        }

        if (isAppointmentCancellable(appointment) && !"doctor".equalsIgnoreCase(account_type)) {
            builder.setNegativeButton("❌ Annuler", (dialog, which) ->
                    showCancelConfirmation(appointment));
        }

        builder.create().show();
    }

    private String buildAppointmentDetailsMessage(Appointment appointment) {
        StringBuilder message = new StringBuilder();

        message.append("👤 Patient: ").append(appointment.getPatientName()).append("\n");

        if (!TextUtils.isEmpty(appointment.getPatientPhone())) {
            message.append("📞 Téléphone: ").append(appointment.getPatientPhone()).append("\n");
        }

        message.append("\n");

        String formattedDate = formatDateForDisplay(appointment.getAppointmentDate());
        message.append("📅 Date: ").append(formattedDate).append("\n");
        message.append("⏰ Heure: ").append(appointment.getAppointmentTime()).append("\n\n");

        MedicalCabinet cabinet = dbHelper.getCabinetById(appointment.getCabinetId());
        if (cabinet != null) {
            message.append("🏥 Cabinet: ").append(cabinet.getName()).append("\n");
            message.append("🎯 Spécialité: ").append(cabinet.getSpeciality()).append("\n");

            if (!TextUtils.isEmpty(cabinet.getAddress())) {
                message.append("📍 Adresse: ").append(cabinet.getAddress()).append("\n");
            }

            if (!TextUtils.isEmpty(cabinet.getPhone())) {
                message.append("📞 Cabinet: ").append(cabinet.getPhone()).append("\n");
            }

            message.append("\n");
        }

        if (!TextUtils.isEmpty(appointment.getNotes())) {
            message.append("📝 Notes médicales:\n").append(appointment.getNotes()).append("\n\n");
        }

        message.append(getStatusDisplayText(appointment.getStatus()));

        // Ajouter des informations pour le docteur
        if ("doctor".equalsIgnoreCase(account_type)) {
            message.append("\n\n");
            message.append("👨‍⚕️ Actions disponibles:\n");
            message.append("- Confirmer le rendez-vous\n");
            message.append("- Annuler le rendez-vous\n");
            message.append("- Modifier les notes\n");
        }

        return message.toString();
    }

    private String formatDateForDisplay(String dateStr) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date date = inputFormat.parse(dateStr);
            return dateDisplayFormat.format(date);
        } catch (ParseException e) {
            return dateStr;
        }
    }

    private String getStatusDisplayText(String status) {
        if (TextUtils.isEmpty(status)) {
            return "Statut: Non défini";
        }

        String lowerStatus = status.toLowerCase();

        if (lowerStatus.contains("pending") || lowerStatus.contains("en attente")) {
            return "⏳ Statut: En attente de confirmation";
        } else if (lowerStatus.contains("confirmé") || lowerStatus.contains("confirmed")) {
            return "✅ Statut: Rendez-vous confirmé";
        } else if (lowerStatus.contains("annulé") || lowerStatus.contains("cancelled")) {
            return "❌ Statut: Rendez-vous annulé";
        } else {
            return "📋 Statut: " + status;
        }
    }

    private boolean isAppointmentCancellable(Appointment appointment) {
        if (TextUtils.isEmpty(appointment.getStatus())) {
            return false;
        }

        String status = appointment.getStatus().toLowerCase();
        return status.contains("pending") ||
                status.contains("en attente") ||
                status.contains("confirmé") ||
                status.contains("confirmed");
    }

    private void showCancelConfirmation(Appointment appointment) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("❓ Confirmer l'annulation");
        builder.setMessage("Êtes-vous sûr de vouloir annuler ce rendez-vous ?\n\nCette action est irréversible.");

        builder.setPositiveButton("✅ Oui, annuler", (dialog, which) ->
                cancelAppointmentAsPatient(appointment));

        builder.setNegativeButton("❌ Non, garder", null);

        builder.create().show();
    }

    private void cancelAppointmentAsPatient(Appointment appointment) {
        try {
            boolean success = dbHelper.updateAppointmentStatus(appointment.getAppointmentId(), "annulé");

            if (success) {
                Toast.makeText(this, "✅ Rendez-vous annulé avec succès", Toast.LENGTH_SHORT).show();
                loadAppointments();
            } else {
                Toast.makeText(this, "❌ Échec de l'annulation", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Toast.makeText(this, "❌ Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Erreur lors de l'annulation", e);
        }
    }

    // ===== MÉTHODES POUR LES ACTIONS DU DOCTEUR =====

    private void showDoctorActionsDialog(Appointment appointment) {
        androidx.appcompat.app.AlertDialog.Builder builder =
                new androidx.appcompat.app.AlertDialog.Builder(this);

        builder.setTitle("Gérer le rendez-vous");

        String message = "Patient: " + appointment.getPatientName() + "\n" +
                "📅 Date: " + formatDateForDisplay(appointment.getAppointmentDate()) + "\n" +
                "⏰ Heure: " + appointment.getAppointmentTime() + "\n" +
                "📞 Téléphone: " + appointment.getPatientPhone() + "\n" +
                "📋 Statut actuel: " + appointment.getStatus() + "\n\n" +
                "Choisir une action:";

        builder.setMessage(message);

        String currentStatus = appointment.getStatus().toLowerCase();

        // Si le rendez-vous est en attente, proposer confirmation ou annulation
        if (currentStatus.contains("en attente") || currentStatus.contains("pending")) {
            builder.setPositiveButton("✅ Confirmer", (dialog, which) ->
                    confirmAppointmentAsDoctor(appointment));

            builder.setNegativeButton("❌ Annuler", (dialog, which) ->
                    cancelAppointmentAsDoctor(appointment));

            builder.setNeutralButton("📝 Modifier notes", (dialog, which) ->
                    showEditNotesDialog(appointment));
        }
        // Si le rendez-vous est déjà confirmé, proposer d'annuler
        else if (currentStatus.contains("confirmé") || currentStatus.contains("confirmed")) {
            builder.setPositiveButton("❌ Annuler", (dialog, which) ->
                    cancelAppointmentAsDoctor(appointment));

            builder.setNeutralButton("📝 Modifier notes", (dialog, which) ->
                    showEditNotesDialog(appointment));

            builder.setNegativeButton("⚠️ Reporter", (dialog, which) ->
                    showRescheduleDialog(appointment));
        }
        // Si le rendez-vous est annulé, proposer de réactiver
        else if (currentStatus.contains("annulé") || currentStatus.contains("cancelled")) {
            builder.setPositiveButton("🔄 Réactiver", (dialog, which) ->
                    confirmAppointmentAsDoctor(appointment));

            builder.setNegativeButton("📝 Modifier notes", (dialog, which) ->
                    showEditNotesDialog(appointment));
        }
        // Pour tout autre statut
        else {
            builder.setPositiveButton("✅ Confirmer", (dialog, which) ->
                    confirmAppointmentAsDoctor(appointment));

            builder.setNegativeButton("❌ Annuler", (dialog, which) ->
                    cancelAppointmentAsDoctor(appointment));

            builder.setNeutralButton("📝 Modifier notes", (dialog, which) ->
                    showEditNotesDialog(appointment));
        }

        builder.show();
    }

    private void confirmAppointmentAsDoctor(Appointment appointment) {
        androidx.appcompat.app.AlertDialog.Builder builder =
                new androidx.appcompat.app.AlertDialog.Builder(this);

        builder.setTitle("Confirmer le rendez-vous");
        builder.setMessage("Confirmer le rendez-vous avec " + appointment.getPatientName() + " ?\n\n" +
                "Date: " + formatDateForDisplay(appointment.getAppointmentDate()) + "\n" +
                "Heure: " + appointment.getAppointmentTime());

        builder.setPositiveButton("✅ Oui, confirmer", (dialog, which) -> {
            updateAppointmentStatus(appointment, "confirmé",
                    "✅ Rendez-vous confirmé avec succès");
        });

        builder.setNegativeButton("❌ Non", null);

        builder.show();
    }

    private void cancelAppointmentAsDoctor(Appointment appointment) {
        androidx.appcompat.app.AlertDialog.Builder builder =
                new androidx.appcompat.app.AlertDialog.Builder(this);

        builder.setTitle("Annuler le rendez-vous");
        builder.setMessage("Êtes-vous sûr de vouloir annuler ce rendez-vous ?\n\n" +
                "Patient: " + appointment.getPatientName() + "\n" +
                "Date: " + formatDateForDisplay(appointment.getAppointmentDate()) + "\n" +
                "Heure: " + appointment.getAppointmentTime() + "\n\n" +
                "Le patient sera notifié de l'annulation.");

        builder.setPositiveButton("✅ Oui, annuler", (dialog, which) -> {
            updateAppointmentStatus(appointment, "annulé",
                    "❌ Rendez-vous annulé avec succès");
        });

        builder.setNegativeButton("❌ Non", null);

        builder.show();
    }

    private void updateAppointmentStatus(Appointment appointment, String newStatus, String successMessage) {
        try {
            boolean success = dbHelper.updateAppointmentStatus(
                    appointment.getAppointmentId(),
                    newStatus
            );

            if (success) {
                Toast.makeText(this, successMessage, Toast.LENGTH_SHORT).show();

                // Envoyer une notification (optionnel)
                if (newStatus.equals("annulé")) {
                    sendCancellationNotification(appointment);
                } else if (newStatus.equals("confirmé")) {
                    sendConfirmationNotification(appointment);
                }

                // Recharger la liste
                loadAppointments();
            } else {
                Toast.makeText(this, "❌ Erreur lors de la mise à jour", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Toast.makeText(this, "❌ Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Erreur updateAppointmentStatus: " + e.getMessage(), e);
        }
    }

    private void sendConfirmationNotification(Appointment appointment) {
        // Implémenter l'envoi de notification de confirmation
        Log.d(TAG, "Notification de confirmation envoyée pour le RDV: " +
                appointment.getAppointmentId());

        // Exemple: Envoyer un email ou une notification push
        Toast.makeText(this, "📧 Notification envoyée au patient", Toast.LENGTH_SHORT).show();
    }

    private void sendCancellationNotification(Appointment appointment) {
        // Implémenter l'envoi de notification d'annulation
        Log.d(TAG, "Notification d'annulation envoyée pour le RDV: " +
                appointment.getAppointmentId());

        // Exemple: Envoyer un email ou une notification push
        Toast.makeText(this, "📧 Notification d'annulation envoyée", Toast.LENGTH_SHORT).show();
    }

    private void showEditNotesDialog(Appointment appointment) {
        androidx.appcompat.app.AlertDialog.Builder builder =
                new androidx.appcompat.app.AlertDialog.Builder(this);

        builder.setTitle("Modifier les notes médicales");

        // Créer un champ d'édition
        androidx.appcompat.widget.AppCompatEditText input =
                new androidx.appcompat.widget.AppCompatEditText(this);

        // Corrigé: utiliser directement ViewGroup.LayoutParams
        input.setLayoutParams(new android.view.ViewGroup.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT));

        input.setInputType(android.text.InputType.TYPE_CLASS_TEXT |
                android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        input.setMinLines(4);
        input.setMaxLines(8);
        input.setVerticalScrollBarEnabled(true);
        input.setText(appointment.getNotes());

        // Ajouter des marges pour un meilleur affichage
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        input.setPadding(padding, padding, padding, padding);

        builder.setView(input);

        builder.setPositiveButton("✅ Enregistrer", (dialog, which) -> {
            String newNotes = input.getText().toString().trim();
            updateAppointmentNotes(appointment, newNotes);
        });

        builder.setNegativeButton("❌ Annuler", null);

        builder.show();
    }

    private void updateAppointmentNotes(Appointment appointment, String newNotes) {
        try {
            boolean success = dbHelper.updateAppointmentNotes(
                    appointment.getAppointmentId(),
                    newNotes  // ← CORRECTION ICI : Utiliser la bonne méthode
            );

            if (success) {
                Toast.makeText(this, "✅ Notes médicales mises à jour", Toast.LENGTH_SHORT).show();
                loadAppointments();
            } else {
                Toast.makeText(this, "❌ Erreur lors de la mise à jour des notes",
                        Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Toast.makeText(this, "❌ Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Erreur updateAppointmentNotes: " + e.getMessage(), e);
        }
    }

    private void showRescheduleDialog(Appointment appointment) {
        Toast.makeText(this, "Fonctionnalité de report bientôt disponible",
                Toast.LENGTH_SHORT).show();
    }

    private void navigateToNewAppointment() {
        if ("doctor".equalsIgnoreCase(account_type)) {
            // Pour le docteur : aller à l'écran d'ajout de rendez-vous
            try {
                Intent intent = new Intent(this, AddAppointmentActivity.class);
                intent.putExtra("user_name", user_name);
                intent.putExtra("user_id", user_id);
                intent.putExtra("user_email", user_email);
                intent.putExtra("account_type", account_type);
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "Fonctionnalité non disponible", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Pour le patient : aller à la liste des cabinets
            Intent intent = new Intent(this, ListActivity2.class);
            intent.putExtra("user_name", user_name);
            intent.putExtra("user_id", user_id);
            intent.putExtra("user_email", user_email);
            intent.putExtra("account_type", account_type);
            startActivity(intent);
        }
    }
}