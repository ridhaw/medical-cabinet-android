package com.example.tp5ex2;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DoctorDashboardActivity extends AppCompatActivity {

    // ===== CONSTANTES =====
    private static final String TAG = "DoctorDashboard";
    private static final int MAX_PENDING_APPOINTMENTS = 5;
    private static final String STATUS_PENDING = "pending";
    private static final String STATUS_CONFIRMED = "confirmed";
    private static final String STATUS_CANCELLED = "cancelled";

    // ===== VIEWS =====
    private MaterialToolbar toolbar;
    private ImageView btnEditProfile;
    private TextView tvDoctorName, tvDoctorSpeciality, tvDoctorCabinet;
    private TextView tvPendingCount, tvConfirmedCount, tvPendingListCount, tvNoPendingAppointments;
    private RecyclerView recyclerViewPendingAppointments;
    private FloatingActionButton fabAddAppointment;
    private MaterialButton btnViewAllAppointments;
    private MaterialCardView cardProfile, cardSchedule, cardManageCabinet, cardPatients;

    // ===== DATA =====
    private DatabaseHelper dbHelper;
    private User currentUser;
    private List<MedicalCabinet> doctorCabinets = new ArrayList<>();
    private List<Appointment> pendingAppointments = new ArrayList<>();
    private List<Appointment> confirmedAppointments = new ArrayList<>();
    private AppointmentsPatientAdapter appointmentsAdapter;

    private String username;
    private String user_id;
    private String user_email;
    private String account_type;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    // ===== LIFECYCLE =====
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_dashboard);

        // Initialiser DatabaseHelper
        dbHelper = DatabaseHelper.getInstance(this);

        // Récupérer les informations utilisateur
        extractUserDataFromIntent();

        // Sauvegarder dans SessionManager
        saveUserToSession();

        // Initialiser currentUser
        initializeCurrentUser();

        // Initialiser les vues
        initializeViews();

        setupToolbar();
        setupListeners();
        loadDoctorData();
    }

    private void saveUserToSession() {
        try {
            SessionManager session = new SessionManager(this);
            if (user_id != null && !user_id.isEmpty()) {
                int userId = Integer.parseInt(user_id);
                session.createLoginSession(
                        userId,
                        username != null ? username : "Médecin",
                        user_email != null ? user_email : "",
                        account_type != null ? account_type : "médecin"
                );
                Log.d(TAG, "Session sauvegardée - ID: " + userId);
            }
        } catch (Exception e) {
            Log.e(TAG, "Erreur sauvegarde session: " + e.getMessage());
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        // Recharger les données quand on revient à l'activité
        Log.d(TAG, "=== onResume ===");
        refreshData();
    }

    // ===== INITIALISATION =====



    private void navigateToAllAppointments() {
        try {
            Log.d(TAG, "Navigation vers listeAppointement");
            Log.d(TAG, "Doctor ID: " + currentUser.getId());
            Log.d(TAG, "Doctor Name: " + username);

            Intent intent = new Intent(this, listeAppointement.class);

            // ⚠️ IMPORTANT: Envoyer account_type = "doctor"
            intent.putExtra("user_id", String.valueOf(currentUser.getId()));
            intent.putExtra("user_name", username);
            intent.putExtra("user_email", user_email);
            intent.putExtra("account_type", "doctor"); // ← CRUCIAL !

            // Logs de débogage
            Bundle extras = intent.getExtras();
            if (extras != null) {
                for (String key : extras.keySet()) {
                    Log.d(TAG, "Extra - " + key + ": " + extras.get(key));
                }
            }

            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Erreur navigation: " + e.getMessage(), e);
            Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }



    private void extractUserDataFromIntent() {
        Intent intent = getIntent();
        if (intent != null) {
            username = intent.getStringExtra("user_name");
            user_id = intent.getStringExtra("user_id");
            user_email = intent.getStringExtra("user_email");
            account_type = intent.getStringExtra("account_type");

            Log.d(TAG, "Données reçues - Nom: " + username + ", ID: " + user_id);
            showWelcomeMessage();
        } else {
            Log.w(TAG, "Aucune donnée reçue de l'Intent");
        }
    }

    private void showWelcomeMessage() {
        if (username != null && !username.isEmpty()) {
            Toast.makeText(this, "Bienvenue Dr. " + username, Toast.LENGTH_SHORT).show();
        }
    }

    private void initializeCurrentUser() {
        currentUser = new User();
        currentUser.setFullName(username != null ? username : "Médecin");
        currentUser.setEmail(user_email != null ? user_email : "email@exemple.com");

        try {
            if (user_id != null && !user_id.isEmpty()) {
                currentUser.setId(Integer.parseInt(user_id));
                Log.d(TAG, "CurrentUser ID: " + currentUser.getId());
            } else {
                currentUser.setId(9999);
                Log.w(TAG, "ID utilisateur non fourni, utilisation de la valeur par défaut");
            }
        } catch (NumberFormatException e) {
            currentUser.setId(9999);
            Log.e(TAG, "Erreur de conversion de l'ID", e);
        }

        currentUser.setAccountType(account_type != null && !account_type.isEmpty() ? account_type : "médecin");
    }

    private void initializeViews() {
        // Toolbar
        toolbar = findViewById(R.id.toolbar);

        // Views du profil du docteur
        btnEditProfile = findViewById(R.id.btnEditProfile);
        tvDoctorName = findViewById(R.id.tvDoctorName);
        tvDoctorSpeciality = findViewById(R.id.tvDoctorSpeciality);
        tvDoctorCabinet = findViewById(R.id.tvDoctorCabinet);

        // Statistiques
        tvPendingCount = findViewById(R.id.tvPendingCount);
        tvConfirmedCount = findViewById(R.id.tvConfirmedCount);
        tvPendingListCount = findViewById(R.id.tvPendingListCount);
        tvNoPendingAppointments = findViewById(R.id.tvNoPendingAppointments);

        // RecyclerView pour les rendez-vous en attente
        recyclerViewPendingAppointments = findViewById(R.id.recyclerViewPendingAppointments);
        recyclerViewPendingAppointments.setLayoutManager(new LinearLayoutManager(this));

        // Boutons FAB
        fabAddAppointment = findViewById(R.id.fabAddAppointment);
        btnViewAllAppointments = findViewById(R.id.btnViewAllAppointments);

        // Cartes d'accès rapide
        cardProfile = findViewById(R.id.cardProfile);
        cardSchedule = findViewById(R.id.cardSchedule);
        cardManageCabinet = findViewById(R.id.cardManageCabinet);
        cardPatients = findViewById(R.id.cardPatients);

        // Mettre à jour le nom du médecin
        updateDoctorName();
    }

    private void updateDoctorName() {
        if (username != null && !username.isEmpty()) {
            String displayName = formatDoctorName(username);
            tvDoctorName.setText(displayName);
        } else {
            tvDoctorName.setText("Dr. Médecin");
        }
    }

    private String formatDoctorName(String name) {
        if (name == null || name.isEmpty()) {
            return "Dr. Médecin";
        }

        String formattedName = name.substring(0, 1).toUpperCase() +
                (name.length() > 1 ? name.substring(1) : "");
        return "Dr. " + formattedName;
    }

    // ===== SETUP =====
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Tableau de bord");
        }
    }

    private void setupListeners() {
        // Bouton d'édition du profil
        btnEditProfile.setOnClickListener(v -> navigateToDoctorProfile());

        // Cartes d'accès rapide
        cardProfile.setOnClickListener(v -> navigateToDoctorProfile());
        cardSchedule.setOnClickListener(v -> navigateToSchedule());
        cardManageCabinet.setOnClickListener(v -> navigateToCabinetManagement());
        cardPatients.setOnClickListener(v -> navigateToPatients());

        // Cartes de statistiques
        findViewById(R.id.cardPendingAppointments).setOnClickListener(v ->
                navigateToAppointments(STATUS_PENDING));
        findViewById(R.id.cardConfirmedAppointments).setOnClickListener(v ->
                navigateToAppointments(STATUS_CONFIRMED));

        // Boutons
        btnViewAllAppointments.setOnClickListener(v -> navigateToAllAppointments());
        fabAddAppointment.setOnClickListener(v -> navigateToAddAppointment());
    }

    // ===== NAVIGATION =====
    private void navigateToDoctorProfile() {
        Intent intent = new Intent(this, DoctorProfileActivity.class);
        addUserDataToIntent(intent);
        startActivity(intent);
    }

    private void navigateToSchedule() {
        try {
            Intent intent = new Intent(this, DoctorScheduleActivity.class);
            intent.putExtra("user_id", String.valueOf(currentUser.getId()));
            intent.putExtra("user_name", username);
            startActivity(intent);
        } catch (Exception e) {
            showFeatureNotAvailable("Module planning");
        }
    }

    private void navigateToCabinetManagement() {
        try {
            if (!doctorCabinets.isEmpty()) {
                MedicalCabinet cabinet = doctorCabinets.get(0);
                Intent intent = new Intent(this, ManageCabinetActivity.class);
                intent.putExtra("cabinet_id", String.valueOf(cabinet.getId()));
                addUserDataToIntent(intent);
                startActivity(intent);
            } else {
                Intent intent = new Intent(this, CreateCabinetActivity.class);
                addUserDataToIntent(intent);
                startActivity(intent);
            }
        } catch (Exception e) {
            showFeatureNotAvailable("Module cabinet");
        }
    }

    private void navigateToPatients() {
        try {
            Intent intent = new Intent(this, DoctorPatientsActivity.class);
            addUserDataToIntent(intent);
            startActivity(intent);
        } catch (Exception e) {
            showFeatureNotAvailable("Module patients");
        }
    }

    private void navigateToAppointments(String filter) {
        try {
            Intent intent = new Intent(this, listeAppointement.class);

            // Ajouter toutes les informations nécessaires
            intent.putExtra("user_id", String.valueOf(currentUser.getId()));
            intent.putExtra("user_name", username);
            intent.putExtra("user_email", user_email);
            intent.putExtra("account_type", "doctor"); // ← CRUCIAL !
            intent.putExtra("filter", filter);

            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Erreur navigation avec filtre: " + e.getMessage());
            showFeatureNotAvailable("Module rendez-vous");
        }
    }



    private void navigateToAddAppointment() {
        try {
            if (!doctorCabinets.isEmpty()) {
                MedicalCabinet cabinet = doctorCabinets.get(0);
                Intent intent = new Intent(this, AddAppointmentActivity.class);
                intent.putExtra("cabinet_id", String.valueOf(cabinet.getId()));
                intent.putExtra("doctor_id", String.valueOf(currentUser.getId()));
                intent.putExtra("user_name", username);
                intent.putExtra("user_email", user_email);
                startActivity(intent);
            }

        } catch (Exception e) {
            showFeatureNotAvailable("Module ajout de rendez-vous");
        }
    }

    private void addUserDataToIntent(Intent intent) {
        intent.putExtra("user_name", username);
        intent.putExtra("user_id", String.valueOf(currentUser.getId()));
        intent.putExtra("user_email", user_email);
        intent.putExtra("account_type", "doctor"); // ← Toujours "doctor" pour cette activité
    }

    private void showFeatureNotAvailable(String featureName) {
        Toast.makeText(this, featureName + " - Bientôt disponible", Toast.LENGTH_SHORT).show();
    }

    // ===== CHARGEMENT DES DONNÉES =====
    private void loadDoctorData() {
        Log.d(TAG, "=== DÉBUT loadDoctorData ===");

        if (currentUser == null) {
            showErrorMessage("Utilisateur non trouvé");
            return;
        }

        try {
            // Charger les cabinets du médecin
            loadDoctorCabinets();

            if (!doctorCabinets.isEmpty()) {
                MedicalCabinet cabinet = doctorCabinets.get(0);
                updateCabinetInfo(cabinet);
                loadAppointmentData(cabinet.getId());
            } else {
                showNoCabinetState();
            }
        } catch (Exception e) {
            handleDataLoadingError(e);
        }
    }

    private void loadDoctorCabinets() {
        doctorCabinets = dbHelper.getCabinetsByDoctorId(currentUser.getId());
        Log.d(TAG, "Cabinets trouvés: " + doctorCabinets.size());
    }

    private void updateCabinetInfo(MedicalCabinet cabinet) {
        tvDoctorCabinet.setText(cabinet.getName());
        tvDoctorSpeciality.setText(cabinet.getSpeciality());
        Log.d(TAG, "Cabinet info mis à jour: " + cabinet.getName());
    }

    private void loadAppointmentData(int cabinetId) {
        loadAppointmentStats(cabinetId);
        loadPendingAppointments(cabinetId);
    }

    private void showNoCabinetState() {
        tvDoctorCabinet.setText("Aucun cabinet configuré");
        tvDoctorSpeciality.setText("Spécialité non définie");



        resetAppointmentUI();
    }

    private void resetAppointmentUI() {
        tvPendingCount.setText("0");
        tvConfirmedCount.setText("0");
        tvPendingListCount.setText("(0)");
        tvNoPendingAppointments.setVisibility(View.VISIBLE);
        recyclerViewPendingAppointments.setVisibility(View.GONE);
    }

    private void handleDataLoadingError(Exception e) {
        Log.e(TAG, "Erreur de chargement: " + e.getMessage(), e);
        Toast.makeText(this, "Erreur de chargement des données", Toast.LENGTH_SHORT).show();
        resetAppointmentUI();
    }

    // ===== STATISTIQUES DES RENDEZ-VOUS =====
    private void loadAppointmentStats(int cabinetId) {
        Log.d(TAG, "Chargement des stats pour cabinet: " + cabinetId);

        try {
            List<Appointment> allAppointments = dbHelper.getAppointmentsByCabinet(cabinetId);
            Log.d(TAG, "Total rendez-vous: " + (allAppointments != null ? allAppointments.size() : 0));

            if (allAppointments != null && !allAppointments.isEmpty()) {
                updateAppointmentCounters(allAppointments);
            } else {
                setZeroAppointmentCounters();
            }
        } catch (Exception e) {
            Log.e(TAG, "Erreur dans loadAppointmentStats", e);
            setZeroAppointmentCounters();
        }
    }

    private void updateAppointmentCounters(List<Appointment> appointments) {
        int pendingCount = 0;
        int confirmedCount = 0;

        for (Appointment appointment : appointments) {
            String status = appointment.getStatus();
            Log.d(TAG, String.format("RDV %d - Statut: %s",
                    appointment.getAppointmentId(), status));

            if (isStatusPending(status)) {
                pendingCount++;
            } else if (isStatusConfirmed(status)) {
                confirmedCount++;
            }
        }

        tvPendingCount.setText(String.valueOf(pendingCount));
        tvConfirmedCount.setText(String.valueOf(confirmedCount));

        Log.d(TAG, String.format("Statistiques - En attente: %d, Confirmés: %d",
                pendingCount, confirmedCount));
    }

    private boolean isStatusPending(String status) {
        if (status == null) return false;
        String lowerStatus = status.toLowerCase();
        return lowerStatus.contains("pending") ||
                lowerStatus.contains("en attente") ||
                lowerStatus.contains("en_attente");
    }

    private boolean isStatusConfirmed(String status) {
        if (status == null) return false;
        String lowerStatus = status.toLowerCase();
        return lowerStatus.contains("confirmed") ||
                lowerStatus.contains("confirmé") ||
                lowerStatus.contains("confirm");
    }

    private void setZeroAppointmentCounters() {
        tvPendingCount.setText("0");
        tvConfirmedCount.setText("0");
    }

    // ===== CHARGEMENT DES RENDEZ-VOUS EN ATTENTE =====
    private void loadPendingAppointments(int cabinetId) {
        Log.d(TAG, "Chargement RDV en attente pour cabinet: " + cabinetId);

        pendingAppointments.clear();

        try {
            List<Appointment> allAppointments = dbHelper.getAppointmentsByCabinet(cabinetId);

            if (allAppointments != null && !allAppointments.isEmpty()) {
                List<Appointment> pendingList = filterPendingAppointments(allAppointments);
                sortAppointmentsByDate(pendingList);

                if (!pendingList.isEmpty()) {
                    displayPendingAppointments(pendingList);
                } else {
                    showNoPendingAppointments();
                }
            } else {
                showNoPendingAppointments();
            }
        } catch (Exception e) {
            Log.e(TAG, "Erreur dans loadPendingAppointments", e);
            showNoPendingAppointments();
        }
    }

    private List<Appointment> filterPendingAppointments(List<Appointment> appointments) {
        List<Appointment> pendingList = new ArrayList<>();

        for (Appointment appointment : appointments) {
            if (isStatusPending(appointment.getStatus())) {
                pendingList.add(appointment);
            }
        }

        Log.d(TAG, "RDV en attente filtrés: " + pendingList.size());
        return pendingList;
    }

    private void sortAppointmentsByDate(List<Appointment> appointments) {
        Collections.sort(appointments, new Comparator<Appointment>() {
            @Override
            public int compare(Appointment a1, Appointment a2) {
                try {
                    Date date1 = dateFormat.parse(a1.getAppointmentDate());
                    Date date2 = dateFormat.parse(a2.getAppointmentDate());

                    int dateComparison = date1.compareTo(date2);
                    if (dateComparison == 0) {
                        return a1.getAppointmentTime().compareTo(a2.getAppointmentTime());
                    }
                    return dateComparison;
                } catch (ParseException e) {
                    return 0;
                }
            }
        });
    }

    private void displayPendingAppointments(List<Appointment> pendingList) {
        // Limiter le nombre d'affichage
        int displayCount = Math.min(pendingList.size(), MAX_PENDING_APPOINTMENTS);
        pendingAppointments = pendingList.subList(0, displayCount);

        // Mettre à jour l'interface
        tvPendingListCount.setText("(" + pendingList.size() + ")");
        tvNoPendingAppointments.setVisibility(View.GONE);
        recyclerViewPendingAppointments.setVisibility(View.VISIBLE);

        // Configurer l'adaptateur AppointmentsPatientAdapter
        setupAppointmentsAdapter();

        Log.d(TAG, String.format("%d RDV en attente affichés sur %d",
                displayCount, pendingList.size()));
    }

    private void setupAppointmentsAdapter() {
        if (appointmentsAdapter == null) {
            appointmentsAdapter = new AppointmentsPatientAdapter(pendingAppointments);
            recyclerViewPendingAppointments.setAdapter(appointmentsAdapter);

            // Gestion du clic
            appointmentsAdapter.setOnItemClickListener(appointment -> {
                Log.d(TAG, "Clic sur RDV: " + appointment.getAppointmentId());
                showAppointmentActions(appointment);
            });
        } else {
            appointmentsAdapter.updateData(pendingAppointments);
        }
    }





    private void showAppointmentActions(Appointment appointment) {
        androidx.appcompat.app.AlertDialog.Builder builder =
                new androidx.appcompat.app.AlertDialog.Builder(this);

        builder.setTitle("Gérer le rendez-vous");

        String message = "Patient: " + appointment.getPatientName() + "\n" +
                "📅 Date: " + appointment.getAppointmentDate() + "\n" +
                "⏰ Heure: " + appointment.getAppointmentTime() + "\n" +
                "📞 Téléphone: " + appointment.getPatientPhone() + "\n" +
                "📋 Statut: " + appointment.getStatus();

        builder.setMessage(message);

        // Boutons d'action
        if (isStatusPending(appointment.getStatus())) {
            builder.setPositiveButton("✅ Confirmer", (dialog, which) ->
                    confirmAppointment(appointment));

            builder.setNegativeButton("❌ Refuser", (dialog, which) ->
                    cancelAppointment(appointment));
        } else if (isStatusConfirmed(appointment.getStatus())) {
            builder.setPositiveButton("✅ Déjà confirmé", null);
            builder.setNegativeButton("❌ Annuler", (dialog, which) ->
                    cancelAppointment(appointment));
        } else {
            builder.setPositiveButton("✅ Re-confirmer", (dialog, which) ->
                    confirmAppointment(appointment));
            builder.setNegativeButton("❌ Déjà annulé", null);
        }

        builder.setNeutralButton("👁️ Détails", (dialog, which) ->
                openAppointmentDetails(appointment));

        builder.show();
    }


    private void confirmAppointment(Appointment appointment) {
        try {
            boolean success = dbHelper.updateAppointmentStatus(
                    appointment.getAppointmentId(),
                    "confirmé" // Utiliser "confirmé" au lieu de "confirmed"
            );

            if (success) {
                Toast.makeText(this, "✅ Rendez-vous confirmé", Toast.LENGTH_SHORT).show();
                refreshData();
            } else {
                Toast.makeText(this, "❌ Erreur de confirmation", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Erreur confirmation: " + e.getMessage());
            Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    private void cancelAppointment(Appointment appointment) {
        androidx.appcompat.app.AlertDialog.Builder builder =
                new androidx.appcompat.app.AlertDialog.Builder(this);

        builder.setTitle("Confirmer l'annulation");
        builder.setMessage("Êtes-vous sûr de vouloir refuser ce rendez-vous ?");

        builder.setPositiveButton("Oui", (dialog, which) -> {
            try {
                boolean success = dbHelper.updateAppointmentStatus(
                        appointment.getAppointmentId(),
                        "annulé" // Utiliser "annulé" au lieu de "cancelled"
                );

                if (success) {
                    Toast.makeText(this, "❌ Rendez-vous annulé", Toast.LENGTH_SHORT).show();
                    refreshData();
                } else {
                    Toast.makeText(this, "Erreur d'annulation", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Log.e(TAG, "Erreur annulation: " + e.getMessage());
                Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Non", null);
        builder.show();
    }

    private void showNoPendingAppointments() {
        tvPendingListCount.setText("(0)");
        tvNoPendingAppointments.setVisibility(View.VISIBLE);
        recyclerViewPendingAppointments.setVisibility(View.GONE);
        Log.d(TAG, "Aucun RDV en attente à afficher");
    }

    // ===== DÉTAILS DU RENDEZ-VOUS =====
    private void openAppointmentDetails(Appointment appointment) {
        try {
            Intent intent = new Intent(this, AppointmentDetailActivity.class);
            intent.putExtra("appointment_id", appointment.getAppointmentId());
            intent.putExtra("user_name", username);
            intent.putExtra("user_id", String.valueOf(currentUser.getId()));
            intent.putExtra("user_email", user_email);
            intent.putExtra("account_type", "doctor"); // ← Important
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Erreur ouverture détails: " + e.getMessage());
            showErrorMessage("Erreur lors de l'ouverture du détail");
        }
    }

    // ===== RAFRAÎCHISSEMENT =====
    private void refreshData() {
        if (currentUser != null) {
            loadDoctorData();
        }
    }

    // ===== UTILITAIRES =====
    private void showErrorMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        Log.e(TAG, message);
    }

    // ===== MENU =====
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_doctor_dashboard, menu);
        return true;
    }


    private void performLogout() {
        // Effacer les données de session
        getSharedPreferences("session", MODE_PRIVATE).edit().clear().apply();

        // Fermer la base de données
        if (dbHelper != null) {
            dbHelper.close();
        }

        // Rediriger vers l'écran de connexion
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();

        // Animation de transition
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}