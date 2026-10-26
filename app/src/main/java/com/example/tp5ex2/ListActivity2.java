package com.example.tp5ex2;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;
import java.util.List;

public class ListActivity2 extends AppCompatActivity {

    // Views
    private LinearLayout cabinetsContainer;
    private TextInputEditText etSearchAll;
    private MaterialButton btnClearSearch;
    private LinearLayout cardEmptyState;
    private TextView tvWelcome, tvStats, tvEmptyMessage;
    private BottomNavigationView bottomNavigationView;
    private LinearProgressIndicator progressSearch;

    // Database helper
    private DatabaseHelper dbHelper;

    // Data
    private String user_name;
    private String user_id;
    private String account_type;
    private String user_email;

    private List<MedicalCabinet> allCabinets = new ArrayList<>();
    private List<MedicalCabinet> filteredCabinets = new ArrayList<>();

    // Search query
    private String searchQuery = "";

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_doctor_dashboard, menu);
        return true;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list2);

        // Récupérer les informations utilisateur
        Intent intent = getIntent();
        if (intent != null) {
            user_id = intent.getStringExtra("user_id");
            user_name = intent.getStringExtra("user_name");
            user_email = intent.getStringExtra("user_email");
            account_type = intent.getStringExtra("account_type");

        }

        // Initialiser DatabaseHelper
        dbHelper = new DatabaseHelper(this);

        initializeViews();
        setupToolbar();
        setupBottomNavigation();
        setupSearchFunctionality();
        loadAllCabinets();
    }

    @SuppressLint("WrongViewCast")
    private void initializeViews() {
        cabinetsContainer = findViewById(R.id.companiesContainer);
        etSearchAll = findViewById(R.id.etSearchAll);
        btnClearSearch = findViewById(R.id.btnClearSearch);
        cardEmptyState = findViewById(R.id.cardEmptyState);
        tvWelcome = findViewById(R.id.tvWelcome);
        tvStats = findViewById(R.id.tvStats);
        tvEmptyMessage = findViewById(R.id.tvEmptyMessage);
       bottomNavigationView = findViewById(R.id.bottomNavigationView);
       progressSearch = findViewById(R.id.progressSearch);

        // Message de bienvenue
        if (user_name != null && !user_name.isEmpty()) {
            tvWelcome.setText("Bonjour " + user_name + "!");
        }
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_list) {
                // On est déjà sur la liste, ne rien faire
                return true;
            } else if (itemId == R.id.nav_appointments) {
                // Ouvrir l'activité des rendez-vous
                Intent appointmentsIntent = new Intent(ListActivity2.this, listeAppointement.class);
                appointmentsIntent.putExtra("user_name", user_name);
                startActivity(appointmentsIntent);
                return true;
            } else if (itemId == R.id.nav_profile) {
                // Ouvrir l'activité du profil AVEC GESTION DES ERREURS
                try {
                    Intent profileIntent = new Intent(ListActivity2.this, DoctorProfileActivity.class);
                    profileIntent.putExtra("user_name", user_name);
                    profileIntent.putExtra("user_id", user_id);
                    profileIntent.putExtra("user_email", user_email);
                    profileIntent.putExtra("account_type", account_type);
                    startActivity(profileIntent);
                } catch (Exception e) {
                    Log.e("ListActivity2", "Erreur lors de l'ouverture du profil", e);
                    Toast.makeText(this, "Impossible d'ouvrir le profil", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
            return false;
        });

        // Marquer "Liste" comme sélectionné
        bottomNavigationView.setSelectedItemId(R.id.nav_list);
    }


    private void setupSearchFunctionality() {
        // Recherche en temps réel
        etSearchAll.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                progressSearch.setVisibility(View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {
                searchQuery = s.toString().trim().toLowerCase();
                applyFilters();
                progressSearch.setVisibility(View.GONE);
            }
        });

        // Bouton Effacer recherche
        btnClearSearch.setOnClickListener(v -> clearSearch());
    }

    private void loadAllCabinets() {
        allCabinets = dbHelper.getAllMedicalCabinets();
        filteredCabinets = new ArrayList<>(allCabinets);
        displayCabinets(filteredCabinets);
        updateStats();
    }

    private void applyFilters() {
        filteredCabinets.clear();

        if (searchQuery.isEmpty()) {
            filteredCabinets.addAll(allCabinets);
        } else {
            for (MedicalCabinet cabinet : allCabinets) {
                boolean matchesSearch =
                        cabinet.getDoctorName().toLowerCase().contains(searchQuery) ||
                                cabinet.getSpeciality().toLowerCase().contains(searchQuery) ||
                                cabinet.getAddress().toLowerCase().contains(searchQuery) ||
                                cabinet.getName().toLowerCase().contains(searchQuery);

                if (matchesSearch) {
                    filteredCabinets.add(cabinet);
                }
            }
        }

        displayCabinets(filteredCabinets);
        updateStats();
    }

    private void displayCabinets(List<MedicalCabinet> cabinets) {
        cabinetsContainer.removeAllViews();

        if (cabinets.isEmpty()) {
            showEmptyState();
            return;
        }

        cardEmptyState.setVisibility(View.GONE);
        cabinetsContainer.setVisibility(View.VISIBLE);

        for (MedicalCabinet cabinet : cabinets) {
            CardView cabinetCard = createCabinetCard(cabinet);
            cabinetsContainer.addView(cabinetCard);
        }
    }

    private CardView createCabinetCard(MedicalCabinet cabinet) {
        LayoutInflater inflater = LayoutInflater.from(this);
        CardView cabinetCard = (CardView) inflater.inflate(R.layout.activity_item_company_card, cabinetsContainer, false);

        TextView tvCabinetName = cabinetCard.findViewById(R.id.tvCabinetName);
        TextView tvDoctorName = cabinetCard.findViewById(R.id.tvDoctorName);
        TextView tvSpeciality = cabinetCard.findViewById(R.id.tvSpeciality);
        TextView tvAddress = cabinetCard.findViewById(R.id.tvAddress);
        TextView tvAvailability = cabinetCard.findViewById(R.id.tvAvailability);
        MaterialButton btnBook = cabinetCard.findViewById(R.id.btnBook);
        View availabilityIndicator = cabinetCard.findViewById(R.id.availabilityIndicator);

        tvCabinetName.setText(cabinet.getName());
        tvDoctorName.setText("Dr. " + cabinet.getDoctorName());
        tvSpeciality.setText(cabinet.getSpeciality());
        tvAddress.setText(cabinet.getAddress());

        if (cabinet.isAvailable()) {
            tvAvailability.setText("✅ Disponible aujourd'hui");
            availabilityIndicator.setBackgroundColor(getColor(android.R.color.holo_green_light));
            btnBook.setEnabled(true);
            btnBook.setBackgroundColor(getColor(android.R.color.holo_green_dark));
        } else {
            tvAvailability.setText("❌ Non disponible");
            availabilityIndicator.setBackgroundColor(getColor(android.R.color.holo_red_light));
            btnBook.setEnabled(false);
            btnBook.setBackgroundColor(getColor(android.R.color.darker_gray));
        }

        btnBook.setOnClickListener(v -> {
            Intent intent = new Intent(ListActivity2.this, AppointmentActivity.class);
            intent.putExtra("cabinet_id", cabinet.getId());
            intent.putExtra("cabinet_name", cabinet.getName());
            intent.putExtra("doctor_name", cabinet.getDoctorName());
            intent.putExtra("speciality", cabinet.getSpeciality());
            startActivity(intent);
        });

        cabinetCard.setOnClickListener(v -> {
            Intent intent = new Intent(ListActivity2.this, CabinetDetailActivity.class);
            intent.putExtra("cabinet_id", cabinet.getId());
            startActivity(intent);
        });

        return cabinetCard;
    }

    private void showEmptyState() {
        cabinetsContainer.setVisibility(View.GONE);
        cardEmptyState.setVisibility(View.VISIBLE);

        if (searchQuery.isEmpty()) {
            tvEmptyMessage.setText("Aucun cabinet médical disponible");
        } else {
            tvEmptyMessage.setText("Aucun résultat pour \"" + searchQuery + "\"");
        }
    }

    private void updateStats() {
        String statsText = filteredCabinets.size() + " cabinet" +
                (filteredCabinets.size() > 1 ? "s" : "") + " trouvé" +
                (filteredCabinets.size() > 1 ? "s" : "");

        tvStats.setText(statsText);
    }

    private void clearSearch() {
        searchQuery = "";
        etSearchAll.setText("");
        applyFilters();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recharger les données si nécessaire
        bottomNavigationView.setSelectedItemId(R.id.nav_list);
    }
}