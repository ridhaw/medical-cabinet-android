package com.example.tp5ex2;

public class Appointment {
    private int appointmentId;
    private int cabinetId;
    private String patientName;
    private String patientPhone;
    private int patientId;
    private String appointmentDate;
    private String appointmentTime;
    private String notes;
    private String status;

    // Constructeur complet
    public Appointment(int appointmentId, int cabinetId, String patientName,
                       String patientPhone, int patientId, String appointmentDate,
                       String appointmentTime, String notes, String status) {
        this.appointmentId = appointmentId;
        this.cabinetId = cabinetId;
        this.patientName = patientName;
        this.patientPhone = patientPhone;
        this.patientId = patientId;
        this.appointmentDate = appointmentDate;
        this.appointmentTime = appointmentTime;
        this.notes = notes;
        this.status = status;
    }

    public Appointment() {

    }

    // Constructeur simplifié (pour compatibilité)
    public Appointment(int appointmentId, int cabinetId, String patientName,
                       String patientPhone, String appointmentDate,
                       String appointmentTime, String notes, String status) {
        this(appointmentId, cabinetId, patientName, patientPhone, 0,
                appointmentDate, appointmentTime, notes, status);
    }

    // Getters
    public int getAppointmentId() { return appointmentId; }
    public int getCabinetId() { return cabinetId; }
    public String getPatientName() { return patientName; }
    public String getPatientPhone() { return patientPhone; }
    public int getPatientId() { return patientId; }
    public String getAppointmentDate() { return appointmentDate; }
    public String getAppointmentTime() { return appointmentTime; }
    public String getNotes() { return notes; }
    public String getStatus() { return status; }

    // Setters
    public void setAppointmentId(int appointmentId) { this.appointmentId = appointmentId; }
    public void setCabinetId(int cabinetId) { this.cabinetId = cabinetId; }
    public void setPatientName(String patientName) { this.patientName = patientName; }
    public void setPatientPhone(String patientPhone) { this.patientPhone = patientPhone; }
    public void setPatientId(int patientId) { this.patientId = patientId; }
    public void setAppointmentDate(String appointmentDate) { this.appointmentDate = appointmentDate; }
    public void setAppointmentTime(String appointmentTime) { this.appointmentTime = appointmentTime; }
    public void setNotes(String notes) { this.notes = notes; }
    public void setStatus(String status) { this.status = status; }
}