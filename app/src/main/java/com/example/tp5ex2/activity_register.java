package com.example.tp5ex2;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class activity_register extends AppCompatActivity {

    // Views
    private RadioGroup rgAccountType;
    private RadioButton rbPatient, rbDoctor;
    private EditText editFullName, editEmail, editPhone, editPassword, editConfirmPassword;
    private EditText editCabinetName, editCabinetAddress;
    private AutoCompleteTextView editSpeciality;
    private Button btnRegister;
    private TextView txtLogin, txtMessage;
    private ProgressBar progressBar;
    private LinearLayout sectionCabinet;

    // Database helper
    private DatabaseHelper dbHelper;

    // Data
    private static final String TAG = "RegisterActivity";
    private String accountType = "patient";

    // Specialities list
    private String[] specialities = {
            "Cardiologie", "Dermatologie", "Pédiatrie", "Gynécologie",
            "Ophtalmologie", "Dentiste", "Généraliste", "Neurologie",
            "Orthopédie", "Psychiatrie", "Urologie", "Radiologie", "Chirurgie"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Log.d(TAG, "RegisterActivity started");

        try {
            // Initialiser DatabaseHelper avec Singleton
            dbHelper = DatabaseHelper.getInstance(this);

            // Initialisation des vues
            initializeViews();

            // Configuration des listeners
            setupListeners();

            // Configurer l'auto-complétion des spécialités
            setupSpecialityAutocomplete();

            Log.d(TAG, "RegisterActivity initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing RegisterActivity: " + e.getMessage());
            showErrorMessage("Erreur d'initialisation du formulaire");
        }
    }

    private void initializeViews() {
        try {
            // Radio buttons pour le type de compte
            rgAccountType = findViewById(R.id.rgAccountType);
            rbPatient = findViewById(R.id.rbPatient);
            rbDoctor = findViewById(R.id.rbDoctor);

            // Champs communs
            editFullName = findViewById(R.id.editFullName);
            editEmail = findViewById(R.id.editEmail);
            editPhone = findViewById(R.id.editPhone);
            editPassword = findViewById(R.id.editPassword);
            editConfirmPassword = findViewById(R.id.editConfirmPassword);

            // Champs spécifiques aux médecins
            editCabinetName = findViewById(R.id.editCabinetName);
            editCabinetAddress = findViewById(R.id.editCabinetAddress);
            editSpeciality = findViewById(R.id.editSpeciality);

            // Autres vues
            btnRegister = findViewById(R.id.btnRegister);
            txtLogin = findViewById(R.id.txtLogin);
            txtMessage = findViewById(R.id.txtMessage);
            progressBar = findViewById(R.id.progressBar);
            sectionCabinet = findViewById(R.id.sectionCabinet);

        } catch (Exception e) {
            Log.e(TAG, "Error initializing views: " + e.getMessage());
            showErrorMessage("Erreur d'initialisation des vues");
        }
    }

    private void setupListeners() {
        // Changement de type de compte
        rgAccountType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbPatient) {
                accountType = "patient";
                sectionCabinet.setVisibility(View.GONE);
                btnRegister.setText("Créer mon compte patient");
            } else if (checkedId == R.id.rbDoctor) {
                accountType = "doctor";
                sectionCabinet.setVisibility(View.VISIBLE);
                btnRegister.setText("Créer mon compte médecin");
            }
        });

        // Bouton d'inscription
        btnRegister.setOnClickListener(v -> {
            Log.d(TAG, "Register button clicked - Account type: " + accountType);
            handleRegistration();
        });

        // Lien vers la connexion
        txtLogin.setOnClickListener(v -> navigateToLogin());
    }

    private void setupSpecialityAutocomplete() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                specialities
        );
        editSpeciality.setAdapter(adapter);
    }

    private void handleRegistration() {
        // Récupération des valeurs
        String fullName = editFullName.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String phone = editPhone.getText().toString().trim();
        String password = editPassword.getText().toString().trim();
        String confirmPassword = editConfirmPassword.getText().toString().trim();
        String cabinetName = accountType.equals("doctor") ? editCabinetName.getText().toString().trim() : "";
        String cabinetAddress = accountType.equals("doctor") ? editCabinetAddress.getText().toString().trim() : "";
        String speciality = accountType.equals("doctor") ? editSpeciality.getText().toString().trim() : "";

        // Validation
        if (!validateAllFields(fullName, email, phone, password, confirmPassword, cabinetName, cabinetAddress, speciality)) {
            return;
        }

        // Vérification si l'utilisateur existe déjà
        checkUserExistence(email, fullName, password, cabinetName, cabinetAddress, speciality, phone);
    }

    private boolean validateAllFields(String fullName, String email, String phone,
                                      String password, String confirmPassword,
                                      String cabinetName, String cabinetAddress, String speciality) {

        boolean isValid = validateFullName(fullName) &&
                validateEmail(email) &&
                validatePhone(phone) &&
                validatePassword(password) &&
                validateConfirmPassword(confirmPassword);

        if (accountType.equals("doctor")) {
            isValid = isValid && validateCabinetFields(cabinetName, cabinetAddress, speciality);
        }

        return isValid;
    }

    private boolean validateFullName(String fullName) {
        if (fullName.isEmpty()) {
            editFullName.setError("Le nom complet est requis");
            editFullName.requestFocus();
            return false;
        }
        return true;
    }

    private boolean validateEmail(String email) {
        if (email.isEmpty()) {
            editEmail.setError("L'email est requis");
            editEmail.requestFocus();
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editEmail.setError("Format d'email invalide");
            editEmail.requestFocus();
            return false;
        }
        return true;
    }

    private boolean validatePhone(String phone) {
        if (phone.isEmpty()) {
            editPhone.setError("Le téléphone est requis");
            editPhone.requestFocus();
            return false;
        }
        return true;
    }

    private boolean validatePassword(String password) {
        if (password.isEmpty()) {
            editPassword.setError("Le mot de passe est requis");
            editPassword.requestFocus();
            return false;
        }
        if (password.length() < 6) {
            editPassword.setError("Minimum 6 caractères");
            editPassword.requestFocus();
            return false;
        }
        return true;
    }

    private boolean validateConfirmPassword(String confirmPassword) {
        String password = editPassword.getText().toString().trim();

        if (confirmPassword.isEmpty()) {
            editConfirmPassword.setError("Confirmation requise");
            editConfirmPassword.requestFocus();
            return false;
        }
        if (!confirmPassword.equals(password)) {
            editConfirmPassword.setError("Les mots de passe ne correspondent pas");
            editConfirmPassword.requestFocus();
            return false;
        }
        return true;
    }

    private boolean validateCabinetFields(String cabinetName, String cabinetAddress, String speciality) {
        boolean isValid = true;

        if (cabinetName.isEmpty()) {
            editCabinetName.setError("Le nom du cabinet est requis");
            editCabinetName.requestFocus();
            isValid = false;
        }
        if (cabinetAddress.isEmpty()) {
            editCabinetAddress.setError("L'adresse du cabinet est requise");
            editCabinetAddress.requestFocus();
            isValid = false;
        }
        if (speciality.isEmpty()) {
            editSpeciality.setError("La spécialité est requise");
            editSpeciality.requestFocus();
            isValid = false;
        }

        return isValid;
    }

    private void checkUserExistence(String email, String fullName, String password,
                                    String cabinetName, String cabinetAddress, String speciality, String phone) {

        showLoading(true);

        new Handler().postDelayed(() -> {
            try {
                boolean emailExists = dbHelper.checkUserExists(email);
                if (emailExists) {
                    showLoading(false);
                    showErrorMessage("Cette adresse email est déjà utilisée");
                    editEmail.setError("Email déjà enregistré");
                    editEmail.requestFocus();
                    return;
                }

                createUserAccount(email, fullName, password, phone, cabinetName, cabinetAddress, speciality);

            } catch (Exception e) {
                Log.e(TAG, "Error checking user existence: " + e.getMessage());
                showLoading(false);
                showErrorMessage("Erreur lors de la vérification");
            }
        }, 1000);
    }

    private void createUserAccount(String email, String fullName, String password,
                                   String phone, String cabinetName, String cabinetAddress, String speciality) {

        try {
            long userId = dbHelper.addUser(fullName, email, password, accountType, phone);

            if (userId == -1) {
                showErrorMessage("Échec de la création du compte");
                showLoading(false);
                return;
            }

            if (accountType.equals("doctor")) {
                createMedicalCabinet(fullName, cabinetName, cabinetAddress, speciality, phone, email, (int) userId);
            } else {
                showSuccessMessage("Compte patient créé avec succès !");
                navigateToLoginWithEmail(email);
            }

        } catch (Exception e) {
            Log.e(TAG, "Error creating user account: " + e.getMessage());
            showErrorMessage("Erreur de création du compte");
        } finally {
            showLoading(false);
        }
    }

    private void createMedicalCabinet(String doctorName, String cabinetName,
                                      String cabinetAddress, String speciality, String phone, String email, int doctorId) {

        try {
            long cabinetId = dbHelper.insertCabinetForDoctor(
                    doctorId, cabinetName, cabinetAddress, speciality, phone, email, doctorName
            );

            if (cabinetId != -1) {
                showSuccessMessage("Compte médecin et cabinet créés avec succès !");
                navigateToLoginWithEmail(email);
            } else {
                showErrorMessage("Échec de la création du cabinet");
            }

        } catch (Exception e) {
            Log.e(TAG, "Error creating medical cabinet: " + e.getMessage());
            showErrorMessage("Erreur lors de la création du cabinet");
        }
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnRegister.setEnabled(!show);
        btnRegister.setText(show ? "Création en cours..." :
                (accountType.equals("doctor") ? "Créer mon compte médecin" : "Créer mon compte patient"));
    }

    private void showSuccessMessage(String message) {
        txtMessage.setText(message);
        txtMessage.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        txtMessage.setVisibility(View.VISIBLE);

        new Handler().postDelayed(() -> {
            txtMessage.setVisibility(View.GONE);
        }, 3000);
    }

    private void showErrorMessage(String message) {
        txtMessage.setText(message);
        txtMessage.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        txtMessage.setVisibility(View.VISIBLE);

        new Handler().postDelayed(() -> {
            txtMessage.setVisibility(View.GONE);
        }, 5000);
    }

    private void navigateToLogin() {
        Intent intent = new Intent(activity_register.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void navigateToLoginWithEmail(String email) {
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(activity_register.this, MainActivity.class);
            intent.putExtra("registered_email", email);
            startActivity(intent);
            finish();
        }, 2000);
    }
}