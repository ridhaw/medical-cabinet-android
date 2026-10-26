package com.example.tp5ex2;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static DatabaseHelper instance;
    private static final String DATABASE_NAME = "medical_cabinets.db";
    private static final int DATABASE_VERSION = 3;
    private static final String TAG = "DatabaseHelper";

    private static final String TABLE_USERS = "users";
    private static final String TABLE_CABINETS = "medical_cabinets";
    private static final String TABLE_APPOINTMENTS = "appointments";
    public static final String TABLE_CABINET = "cabinet";

    private static final String COLUMN_USER_ID = "user_id";
    private static final String COLUMN_FULL_NAME = "full_name";
    private static final String COLUMN_USER_EMAIL = "user_email";
    private static final String COLUMN_USER_PASSWORD = "password";
    private static final String COLUMN_ACCOUNT_TYPE = "account_type";
    private static final String COLUMN_USER_PHONE = "user_phone";
    private static final String COLUMN_CREATED_AT = "created_at";

    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_IMAGE_RESOURCE = "image_resource";
    private static final String COLUMN_PHONE = "phone";
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_WEBSITE = "website";
    private static final String COLUMN_ADDRESS = "address";
    private static final String COLUMN_SPECIALITY = "speciality";
    private static final String COLUMN_SERVICES = "services";
    private static final String COLUMN_LATITUDE = "latitude";
    private static final String COLUMN_LONGITUDE = "longitude";
    private static final String COLUMN_RATING = "rating";
    private static final String COLUMN_AVAILABILITY = "availability";
    private static final String COLUMN_DOCTOR_NAME = "doctor_name";
    private static final String COLUMN_IS_AVAILABLE = "is_available";
    private static final String COLUMN_DOCTOR_ID = "doctor_id";
    public static final String COLUMN_CABINET_ID = "_id";
    public static final String COLUMN_CABINET_NAME = "name";
    public static final String COLUMN_CABINET_ADDRESS = "address";
    public static final String COLUMN_CABINET_PHONE = "phone";
    public static final String COLUMN_CABINET_EMAIL = "email";
    public static final String COLUMN_CABINET_WEBSITE = "website";
    public static final String COLUMN_CABINET_SPECIALITY = "speciality";
    public static final String COLUMN_CABINET_SERVICES = "services";
    public static final String COLUMN_CABINET_AVAILABILITY = "availability";
    public static final String COLUMN_CABINET_IS_AVAILABLE = "is_available";


    private static final String COLUMN_APPOINTMENT_ID = "appointment_id";
    private static final String COLUMN_PATIENT_NAME = "patient_name";
    private static final String COLUMN_PATIENT_PHONE = "patient_phone";
    private static final String COLUMN_PATIENT_ID = "patient_id";
    private static final String COLUMN_APPOINTMENT_DATE = "appointment_date";
    private static final String COLUMN_APPOINTMENT_TIME = "appointment_time";
    private static final String COLUMN_NOTES = "notes";
    private static final String COLUMN_STATUS = "status";

    // =============================
    // CONSTRUCTEUR & SINGLETON
    // =============================

    DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        Log.d(TAG, "DatabaseHelper initialisé - Version: " + DATABASE_VERSION);
    }

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    // =============================
    //  MÉTHODES DU CYCLE DE VIE
    // =============================

    @Override
    public void onCreate(SQLiteDatabase db) {
        createUsersTable(db);
        createCabinetsTable(db);
        createAppointmentsTable(db);
        insertInitialMedicalCabinets(db);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Logique de mise à jour de la base de données
    }

    // =============================
    // CRÉATION DES TABLES
    // =============================

    private void createUsersTable(SQLiteDatabase db) {
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + " ("
                + COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_FULL_NAME + " TEXT NOT NULL, "
                + COLUMN_USER_EMAIL + " TEXT UNIQUE NOT NULL, "
                + COLUMN_USER_PASSWORD + " TEXT NOT NULL, "
                + COLUMN_ACCOUNT_TYPE + " TEXT DEFAULT 'patient', "
                + COLUMN_USER_PHONE + " TEXT, "
                + COLUMN_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP"
                + ")";
        db.execSQL(CREATE_USERS_TABLE);
    }

    private void createCabinetsTable(SQLiteDatabase db) {
        String CREATE_CABINETS_TABLE = "CREATE TABLE " + TABLE_CABINETS + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_NAME + " TEXT NOT NULL, "
                + COLUMN_IMAGE_RESOURCE + " INTEGER, "
                + COLUMN_PHONE + " TEXT, "
                + COLUMN_EMAIL + " TEXT, "
                + COLUMN_WEBSITE + " TEXT, "
                + COLUMN_ADDRESS + " TEXT, "
                + COLUMN_SPECIALITY + " TEXT NOT NULL, "
                + COLUMN_SERVICES + " TEXT, "
                + COLUMN_LATITUDE + " REAL, "
                + COLUMN_LONGITUDE + " REAL, "
                + COLUMN_RATING + " REAL DEFAULT 0, "
                + COLUMN_AVAILABILITY + " TEXT, "
                + COLUMN_DOCTOR_NAME + " TEXT, "
                + COLUMN_IS_AVAILABLE + " INTEGER DEFAULT 1, "
                + COLUMN_DOCTOR_ID + " INTEGER"
                + ")";
        db.execSQL(CREATE_CABINETS_TABLE);
    }

    private void createAppointmentsTable(SQLiteDatabase db) {
        String CREATE_APPOINTMENTS_TABLE = "CREATE TABLE " + TABLE_APPOINTMENTS + " ("
                + COLUMN_APPOINTMENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_ID + " INTEGER, "
                + COLUMN_PATIENT_NAME + " TEXT NOT NULL, "
                + COLUMN_PATIENT_PHONE + " TEXT NOT NULL, "
                + COLUMN_PATIENT_ID + " INTEGER, "
                + COLUMN_APPOINTMENT_DATE + " TEXT NOT NULL, "
                + COLUMN_APPOINTMENT_TIME + " TEXT NOT NULL, "
                + COLUMN_NOTES + " TEXT, "
                + COLUMN_STATUS + " TEXT DEFAULT 'En attente'"
                + ")";
        db.execSQL(CREATE_APPOINTMENTS_TABLE);
    }

    // =============================
    //  DONNÉES INITIALES
    // =============================

    private void insertInitialMedicalCabinets(SQLiteDatabase db) {
        List<MedicalCabinet> cabinets = new ArrayList<>();
        int defaultIcon = R.drawable.isims;

        cabinets.add(new MedicalCabinet(1, "Cabinet de Cardiologie du Dr. Ahmed", defaultIcon,
                "+216 74 111 111", "cardio.ahmed@medical.tn", "www.cardiologie-sfax.tn",
                "45 Avenue Habib Bourguiba, Sfax", "Cardiologie",
                34.7406, 10.7603, 4.8f, "08:00-12:00,14:00-18:00",
                "Dr. Ahmed Ben Salah", true, 0));

        cabinets.add(new MedicalCabinet(2, "Centre Dermatologique Sfax", defaultIcon,
                "+216 74 222 222", "dermato.sfax@medical.tn", "www.dermatologie-sfax.tn",
                "123 Rue de la République, Sfax", "Dermatologie",
                34.7418, 10.7619, 4.6f, "09:00-13:00,15:00-19:00",
                "Dr. Leila Trabelsi", true, 0));

        cabinets.add(new MedicalCabinet(3, "Cabinet Pédiatrique Les Enfants", defaultIcon,
                "+216 74 333 333", "pediatrie.enfants@medical.tn", "www.pediatrie-sfax.tn",
                "78 Rue Hédi Chaker, Sfax", "Pédiatrie",
                34.7395, 10.7590, 4.9f, "08:30-12:30,14:30-17:30",
                "Dr. Mohamed Karray", true, 0));

        cabinets.add(new MedicalCabinet(4, "Centre Dentaire du Sourire", defaultIcon,
                "+216 74 444 444", "sourire.dentaire@medical.tn", "www.dentiste-sfax.tn",
                "56 Avenue Farhat Hached, Sfax", "Dentiste",
                34.7425, 10.7615, 4.7f, "08:00-17:00",
                "Dr. Sami Guedria", false, 0));

        cabinets.add(new MedicalCabinet(5, "Cabinet Médical Général", defaultIcon,
                "+216 74 555 555", "general.medical@medical.tn", "www.medecin-generaliste.tn",
                "34 Rue Taieb Mhiri, Sfax", "Généraliste",
                34.7380, 10.7580, 4.5f, "07:30-13:00,16:00-20:00",
                "Dr. Fatma Bouaziz", true, 0));

        for (MedicalCabinet cabinet : cabinets) {
            insertMedicalCabinet(db, cabinet);
        }
    }

    // =============================
    //  MÉTHODES UTILISATEURS
    // =============================

    public long addUser(String fullName, String email, String password, String accountType, String phone) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_FULL_NAME, fullName);
        values.put(COLUMN_USER_EMAIL, email);
        values.put(COLUMN_USER_PASSWORD, password);
        values.put(COLUMN_ACCOUNT_TYPE, accountType);
        values.put(COLUMN_USER_PHONE, phone);

        return db.insert(TABLE_USERS, null, values);
    }

    public boolean checkUserExists(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS,
                new String[]{COLUMN_USER_ID},
                COLUMN_USER_EMAIL + " = ?",
                new String[]{email},
                null, null, null);

        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public User authenticateUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        User user = null;

        Cursor cursor = db.query(TABLE_USERS,
                new String[]{COLUMN_USER_ID, COLUMN_FULL_NAME, COLUMN_USER_EMAIL,
                        COLUMN_ACCOUNT_TYPE, COLUMN_USER_PHONE},
                COLUMN_USER_EMAIL + " = ? AND " + COLUMN_USER_PASSWORD + " = ?",
                new String[]{email, password},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            user = new User(
                    cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USER_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FULL_NAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_EMAIL)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ACCOUNT_TYPE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_PHONE))
            );
            cursor.close();
        }

        return user;
    }

    // =============================
    //  MÉTHODES CABINETS MÉDICAUX
    // =============================

    private void insertMedicalCabinet(SQLiteDatabase db, MedicalCabinet cabinet) {
        ContentValues values = createCabinetContentValues(cabinet);
        db.insert(TABLE_CABINETS, null, values);
    }

    public long insertMedicalCabinet(MedicalCabinet cabinet) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = createCabinetContentValues(cabinet);
        return db.insert(TABLE_CABINETS, null, values);
    }

    public long insertCabinetForDoctor(int doctorUserId, String cabinetName, String address,
                                       String speciality, String phone, String email, String doctorName) {
        MedicalCabinet cabinet = new MedicalCabinet(
                0,
                cabinetName,
                R.drawable.isims,
                phone,
                email,
                "",
                address,
                speciality,
                34.7406, 10.7603,
                4.5f,
                "08:00-12:00,14:00-18:00",
                doctorName,
                true,
                doctorUserId
        );

        return insertMedicalCabinet(cabinet);
    }

    public List<MedicalCabinet> getAllMedicalCabinets() {
        List<MedicalCabinet> cabinets = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_CABINETS + " ORDER BY " + COLUMN_SPECIALITY, null);

        if (cursor.moveToFirst()) {
            do {
                cabinets.add(extractMedicalCabinetFromCursor(cursor));
            } while (cursor.moveToNext());
        }

        cursor.close();
        return cabinets;
    }

    public MedicalCabinet getCabinetById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        MedicalCabinet cabinet = null;

        Cursor cursor = db.query(TABLE_CABINETS, null,
                COLUMN_ID + "=?", new String[]{String.valueOf(id)},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            cabinet = extractMedicalCabinetFromCursor(cursor);
            cursor.close();
        }

        return cabinet;
    }

    public List<MedicalCabinet> getCabinetsByDoctorId(int doctorId) {
        List<MedicalCabinet> cabinets = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(
                "medical_cabinets",
                new String[]{"id", "name", "address", "phone", "email", "speciality", "doctor_id"},
                "doctor_id = ?",
                new String[]{String.valueOf(doctorId)},
                null, null, null
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                MedicalCabinet cabinet = new MedicalCabinet();
                cabinet.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                cabinet.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
                cabinet.setAddress(cursor.getString(cursor.getColumnIndexOrThrow("address")));
                cabinet.setPhone(cursor.getString(cursor.getColumnIndexOrThrow("phone")));
                cabinet.setEmail(cursor.getString(cursor.getColumnIndexOrThrow("email")));
                cabinet.setSpeciality(cursor.getString(cursor.getColumnIndexOrThrow("speciality")));
                cabinet.setDoctorId(cursor.getInt(cursor.getColumnIndexOrThrow("doctor_id")));

                cabinets.add(cabinet);
            } while (cursor.moveToNext());
            cursor.close();
        }

        return cabinets;
    }

    // =============================
    //  MÉTHODES RENDEZ-VOUS
    // =============================

    public long addAppointment(int patientId, int cabinetId, String patientName,
                               String phone, String appointmentDateTime,
                               String notes, String status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        try {
            if (appointmentDateTime == null || appointmentDateTime.isEmpty()) {
                Log.e(TAG, "Date/heure du rendez-vous vide");
                return -1;
            }

            String[] dateTimeParts = appointmentDateTime.split(" ");
            String date = "";
            String time = "";

            if (dateTimeParts.length >= 2) {
                date = dateTimeParts[0].trim();
                time = dateTimeParts[1].trim();

                if (time.length() > 5) {
                    time = time.substring(0, 5);
                }
            } else {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                String currentDateTime = sdf.format(new Date());
                String[] currentParts = currentDateTime.split(" ");
                date = currentParts[0];
                time = currentParts[1];
            }

            Log.d(TAG, "Ajout rendez-vous - Date: " + date + ", Heure: " + time);

            values.put(COLUMN_ID, cabinetId);
            values.put(COLUMN_PATIENT_NAME, patientName);
            values.put(COLUMN_PATIENT_PHONE, phone);
            values.put(COLUMN_PATIENT_ID, patientId);
            values.put(COLUMN_APPOINTMENT_DATE, date);
            values.put(COLUMN_APPOINTMENT_TIME, time);
            values.put(COLUMN_NOTES, notes != null ? notes : "");
            values.put(COLUMN_STATUS, status != null ? status : "en attente");

            long result = db.insert(TABLE_APPOINTMENTS, null, values);

            if (result != -1) {
                Log.d(TAG, "✅ Rendez-vous ajouté avec ID: " + result +
                        " - Patient: " + patientName +
                        " - Date: " + date + " " + time);
            } else {
                Log.e(TAG, "❌ Erreur lors de l'ajout du rendez-vous");
            }

            return result;

        } catch (Exception e) {
            Log.e(TAG, "Erreur addAppointment: " + e.getMessage(), e);
            return -1;
        }
    }

    public long insertAppointment(Appointment appointment) {
        return addAppointment(
                appointment.getPatientId(),
                appointment.getCabinetId(),
                appointment.getPatientName(),
                appointment.getPatientPhone(),
                appointment.getAppointmentDate() + " " + appointment.getAppointmentTime(),
                appointment.getNotes(),
                appointment.getStatus()
        );
    }

    public List<Appointment> getAppointmentsByCabinet(int cabinetId) {
        List<Appointment> appointments = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_APPOINTMENTS + " WHERE " + COLUMN_ID + " = ? " +
                        "ORDER BY " + COLUMN_APPOINTMENT_DATE + ", " + COLUMN_APPOINTMENT_TIME,
                new String[]{String.valueOf(cabinetId)}
        );

        if (cursor.moveToFirst()) {
            do {
                appointments.add(extractAppointmentFromCursor(cursor));
            } while (cursor.moveToNext());
        }

        cursor.close();
        return appointments;
    }

    public List<Appointment> getAppointmentsByPatientId(int patientId) {
        List<Appointment> appointments = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_APPOINTMENTS +
                " WHERE " + COLUMN_PATIENT_ID + " = ? " +
                "ORDER BY " + COLUMN_APPOINTMENT_DATE + " DESC, " +
                COLUMN_APPOINTMENT_TIME + " DESC";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(patientId)});

        if (cursor.moveToFirst()) {
            do {
                appointments.add(extractAppointmentFromCursor(cursor));
            } while (cursor.moveToNext());
        }

        cursor.close();
        Log.d(TAG, "Rendez-vous trouvés pour patient " + patientId + ": " + appointments.size());

        return appointments;
    }

    public List<Appointment> getAppointmentsByPatientName(String patientName) {
        List<Appointment> appointments = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_APPOINTMENTS +
                " WHERE LOWER(" + COLUMN_PATIENT_NAME + ") LIKE LOWER(?) " +
                "ORDER BY " + COLUMN_APPOINTMENT_DATE + " DESC, " +
                COLUMN_APPOINTMENT_TIME + " DESC";

        Cursor cursor = db.rawQuery(query, new String[]{"%" + patientName + "%"});

        if (cursor.moveToFirst()) {
            do {
                appointments.add(extractAppointmentFromCursor(cursor));
            } while (cursor.moveToNext());
        }

        cursor.close();
        Log.d(TAG, "Rendez-vous trouvés pour patient '" + patientName + "': " + appointments.size());

        return appointments;
    }

    public List<Appointment> getAppointmentsByDoctorId(int doctorId) {
        List<Appointment> appointments = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT a.* FROM " + TABLE_APPOINTMENTS + " a " +
                "INNER JOIN " + TABLE_CABINETS + " c ON a." + COLUMN_ID + " = c." + COLUMN_ID + " " +
                "WHERE c." + COLUMN_DOCTOR_ID + " = ? " +
                "ORDER BY a." + COLUMN_APPOINTMENT_DATE + " DESC, " +
                "a." + COLUMN_APPOINTMENT_TIME + " DESC";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(doctorId)});

        if (cursor.moveToFirst()) {
            do {
                appointments.add(extractAppointmentFromCursor(cursor));
            } while (cursor.moveToNext());
        }

        cursor.close();
        Log.d(TAG, "Rendez-vous trouvés pour docteur " + doctorId + ": " + appointments.size());

        return appointments;
    }

    public boolean updateAppointmentStatus(int appointmentId, String status) {
        SQLiteDatabase db = this.getWritableDatabase();

        if (db == null) {
            Log.e(TAG, "Database non disponible pour updateAppointmentStatus");
            return false;
        }

        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_STATUS, status);

            int rowsAffected = db.update(
                    TABLE_APPOINTMENTS,
                    values,
                    COLUMN_APPOINTMENT_ID + " = ?",
                    new String[]{String.valueOf(appointmentId)}
            );

            Log.d(TAG, "updateAppointmentStatus - ID: " + appointmentId +
                    ", Nouveau statut: " + status + ", Lignes affectées: " + rowsAffected);

            return rowsAffected > 0;

        } catch (Exception e) {
            Log.e(TAG, "Erreur updateAppointmentStatus: " + e.getMessage(), e);
            return false;
        }
    }

    public boolean updateAppointmentNotes(int appointmentId, String notes) {
        SQLiteDatabase db = this.getWritableDatabase();

        if (db == null) {
            Log.e(TAG, "Database non disponible pour updateAppointmentNotes");
            return false;
        }

        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_NOTES, notes != null ? notes : "");

            int rowsAffected = db.update(
                    TABLE_APPOINTMENTS,
                    values,
                    COLUMN_APPOINTMENT_ID + " = ?",
                    new String[]{String.valueOf(appointmentId)}
            );

            Log.d(TAG, "updateAppointmentNotes - ID: " + appointmentId +
                    ", Longueur notes: " + (notes != null ? notes.length() : 0) +
                    ", Lignes affectées: " + rowsAffected);

            return rowsAffected > 0;

        } catch (Exception e) {
            Log.e(TAG, "Erreur updateAppointmentNotes: " + e.getMessage(), e);
            return false;
        }
    }



    private MedicalCabinet extractMedicalCabinetFromCursor(Cursor cursor) {
        int doctorIdIndex = cursor.getColumnIndex(COLUMN_DOCTOR_ID);
        int doctorId = doctorIdIndex != -1 ? cursor.getInt(doctorIdIndex) : 0;

        return new MedicalCabinet(
                cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)),
                cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_RESOURCE)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHONE)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_WEBSITE)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ADDRESS)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SPECIALITY)),
                cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_LATITUDE)),
                cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_LONGITUDE)),
                cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_RATING)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_AVAILABILITY)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DOCTOR_NAME)),
                cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_AVAILABLE)) == 1,
                doctorId
        );
    }

    private Appointment extractAppointmentFromCursor(Cursor cursor) {
        int patientIdIndex = cursor.getColumnIndex(COLUMN_PATIENT_ID);
        int patientId = patientIdIndex != -1 ? cursor.getInt(patientIdIndex) : 0;

        return new Appointment(
                cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_APPOINTMENT_ID)),
                cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PATIENT_NAME)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PATIENT_PHONE)),
                patientId,
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_APPOINTMENT_DATE)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_APPOINTMENT_TIME)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOTES)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STATUS))
        );
    }

    // =============================
    // S MÉTHODES UTILITAIRES
    // =============================

    private ContentValues createCabinetContentValues(MedicalCabinet cabinet) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, cabinet.getName());
        values.put(COLUMN_IMAGE_RESOURCE, cabinet.getImageResource());
        values.put(COLUMN_PHONE, cabinet.getPhone());
        values.put(COLUMN_EMAIL, cabinet.getEmail());
        values.put(COLUMN_WEBSITE, cabinet.getWebsite());
        values.put(COLUMN_ADDRESS, cabinet.getAddress());
        values.put(COLUMN_SPECIALITY, cabinet.getSpeciality());
        values.put(COLUMN_SERVICES, cabinet.getServices());
        values.put(COLUMN_LATITUDE, cabinet.getLatitude());
        values.put(COLUMN_LONGITUDE, cabinet.getLongitude());
        values.put(COLUMN_RATING, cabinet.getRating());
        values.put(COLUMN_AVAILABILITY, cabinet.getAvailability());
        values.put(COLUMN_DOCTOR_NAME, cabinet.getDoctorName());
        values.put(COLUMN_IS_AVAILABLE, cabinet.isAvailable() ? 1 : 0);
        values.put(COLUMN_DOCTOR_ID, cabinet.getDoctorId());

        return values;
    }
}