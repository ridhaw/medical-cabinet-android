package com.example.tp5ex2;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DoctorAllAppointmentsActivity extends AppCompatActivity {

    // ===== CONSTANTES =====
    private static final String TAG = "DoctorAllAppointments";
    private static final String FILTER_ALL = "all";
    private static final String FILTER_PENDING = "pending";
    private static final String FILTER_CONFIRMED = "confirmed";
    private static final String FILTER_TODAY = "today";
    private static final String FILTER_UPCOMING = "upcoming";

    // ===== VIEWS =====
    private Toolbar toolbar;
    private RecyclerView recyclerViewAppointments;
    private ChipGroup chipGroup;
    private ProgressBar progressBar;
    private View llEmptyState;
    private TextView tvEmptyMessage, tvAppointmentsCount;

    // ===== DATA =====
    private DatabaseHelper dbHelper;
    private User currentUser;
    private List<MedicalCabinet> doctorCabinets = new ArrayList<>();
    private List<Appointment> allAppointments = new ArrayList<>();
    private List<Appointment> filteredAppointments = new ArrayList<>();
    private AppointmentsPatientAdapter appointmentsAdapter;

    private String username;
    private String user_id;
    private String user_email;
    private String account_type;

    private String currentFilter = FILTER_ALL;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_all_appointments);

        initializeComponents();
        setupToolbar();
        setupListeners();
        loadAppointments();
    }

    private void initializeComponents() {
        dbHelper = DatabaseHelper.getInstance(this);

        // Récupérer les données
        Intent intent = getIntent();
        if (intent != null) {
            username = intent.getStringExtra("user_name");
            user_id = intent.getStringExtra("user_id");
            user_email = intent.getStringExtra("user_email");
            account_type = intent.getStringExtra("account_type");
        }

        // Initialiser l'utilisateur
        currentUser = new User();
        if (user_id != null && !user_id.isEmpty()) {
            try {
                currentUser.setId(Integer.parseInt(user_id));
            } catch (NumberFormatException e) {
                currentUser.setId(0);
            }
        }

        // Initialiser les vues
        toolbar = findViewById(R.id.toolbar);
        recyclerViewAppointments = findViewById(R.id.recyclerViewAppointments);
        recyclerViewAppointments.setLayoutManager(new LinearLayoutManager(this));

        chipGroup = findViewById(R.id.chipGroup);
        progressBar = findViewById(R.id.progressBar);
        llEmptyState = findViewById(R.id.llEmptyState);
        tvEmptyMessage = findViewById(R.id.tvEmptyMessage);
        tvAppointmentsCount = findViewById(R.id.tvAppointmentsCount);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Tous mes rendez-vous");
        }

        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupListeners() {
        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                applyFilter(FILTER_ALL);
            } else {
                int chipId = checkedIds.get(0);
                if (chipId == R.id.chipAll) {
                    applyFilter(FILTER_ALL);
                } else if (chipId == R.id.chipPending) {
                    applyFilter(FILTER_PENDING);
                } else if (chipId == R.id.chipConfirmed) {
                    applyFilter(FILTER_CONFIRMED);
                } else if (chipId == R.id.chipToday) {
                    applyFilter(FILTER_TODAY);
                } else if (chipId == R.id.chipUpcoming) {
                    applyFilter(FILTER_UPCOMING);
                }
            }
        });
    }

    private void loadAppointments() {
        progressBar.setVisibility(View.VISIBLE);
        llEmptyState.setVisibility(View.GONE);

        new Thread(() -> {
            try {
                // Récupérer tous les rendez-vous du médecin
              //  allAppointments = dbHelper.getAppointmentsByDoctor(currentUser.getId());

                // Trier par date
                sortAppointmentsByDate(allAppointments);

                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    if (!allAppointments.isEmpty()) {
                        applyFilter(currentFilter);
                    } else {
                        showEmptyState("Aucun rendez-vous trouvé");
                    }
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Erreur de chargement", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Erreur: " + e.getMessage());
                    showEmptyState("Erreur de chargement");
                });
            }
        }).start();
    }

    private void sortAppointmentsByDate(List<Appointment> appointments) {
        Collections.sort(appointments, new Comparator<Appointment>() {
            @Override
            public int compare(Appointment a1, Appointment a2) {
                try {
                    Date date1 = dateFormat.parse(a1.getAppointmentDate());
                    Date date2 = dateFormat.parse(a2.getAppointmentDate());
                    return date2.compareTo(date1); // Plus récent en premier
                } catch (ParseException e) {
                    return 0;
                }
            }
        });
    }

    private void applyFilter(String filterType) {
        currentFilter = filterType;
        filteredAppointments.clear();

        if (allAppointments.isEmpty()) {
            showEmptyState("Aucun rendez-vous");
            return;
        }

        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        for (Appointment appointment : allAppointments) {
            boolean shouldAdd = false;
            String status = appointment.getStatus() != null ?
                    appointment.getStatus().toLowerCase() : "";

            switch (filterType) {
                case FILTER_ALL:
                    shouldAdd = true;
                    break;

                case FILTER_PENDING:
                    shouldAdd = status.contains("pending") ||
                            status.contains("en attente");
                    break;

                case FILTER_CONFIRMED:
                    shouldAdd = status.contains("confirmé") ||
                            status.contains("confirmed");
                    break;

                case FILTER_TODAY:
                    shouldAdd = appointment.getAppointmentDate().equals(today);
                    break;

                case FILTER_UPCOMING:
                    try {
                        Date appDate = dateFormat.parse(appointment.getAppointmentDate());
                        Date todayDate = dateFormat.parse(today);
                        shouldAdd = !appDate.before(todayDate); // Aujourd'hui et futur
                    } catch (ParseException e) {
                        shouldAdd = false;
                    }
                    break;
            }

            if (shouldAdd) {
                filteredAppointments.add(appointment);
            }
        }

        updateAppointmentsCount();

        if (filteredAppointments.isEmpty()) {
            showEmptyState(getEmptyMessageForFilter(filterType));
        } else {
            displayAppointments();
        }
    }

    private String getEmptyMessageForFilter(String filterType) {
        switch (filterType) {
            case FILTER_PENDING:
                return "Aucun rendez-vous en attente";
            case FILTER_CONFIRMED:
                return "Aucun rendez-vous confirmé";
            case FILTER_TODAY:
                return "Aucun rendez-vous aujourd'hui";
            case FILTER_UPCOMING:
                return "Aucun rendez-vous à venir";
            default:
                return "Aucun rendez-vous";
        }
    }

    private void updateAppointmentsCount() {
        int total = allAppointments.size();
        int filtered = filteredAppointments.size();

        if (currentFilter.equals(FILTER_ALL)) {
            tvAppointmentsCount.setText(String.format("Total: %d rendez-vous", total));
        } else {
            tvAppointmentsCount.setText(String.format("%d/%d rendez-vous", filtered, total));
        }
    }

    private void displayAppointments() {
        llEmptyState.setVisibility(View.GONE);
        recyclerViewAppointments.setVisibility(View.VISIBLE);

        if (appointmentsAdapter == null) {
            //appointmentsAdapter = new AppointmentsPatientAdapter(filteredAppointments);
            recyclerViewAppointments.setAdapter(appointmentsAdapter);

           // appointmentsAdapter.setOnItemClickListener(appointment ->
             //       openAppointmentDetails(appointment));
        } else {
            appointmentsAdapter.updateData(filteredAppointments);
        }
    }

    private void showEmptyState(String message) {
        recyclerViewAppointments.setVisibility(View.GONE);
        llEmptyState.setVisibility(View.VISIBLE);
        tvEmptyMessage.setText(message);
        tvAppointmentsCount.setText("0 rendez-vous");
    }

    private void openAppointmentDetails(Appointment appointment) {
        try {
            Intent intent = new Intent(this, AppointmentDetailActivity.class);
            intent.putExtra("appointment_id", String.valueOf(appointment.getAppointmentId()));
            intent.putExtra("user_name", username);
            intent.putExtra("user_id", String.valueOf(currentUser.getId()));
            intent.putExtra("user_email", user_email);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Erreur", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAppointments();
    }
}