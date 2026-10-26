package com.example.tp5ex2;

public class User {
    private int id;
    private String fullName;
    private String email;
    private String accountType;
    private String phone;

    public User() {}

    public User(int id, String fullName, String email, String accountType, String phone) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.accountType = accountType;
        this.phone = phone;
    }

    // Getters
    public int getId() { return id; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getAccountType() { return accountType; }
    public String getPhone() { return phone; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setEmail(String email) { this.email = email; }
    public void setAccountType(String accountType) { this.accountType = accountType; }
    public void setPhone(String phone) { this.phone = phone; }

    // Méthodes utilitaires
    public boolean isPatient() { return "patient".equals(accountType); }
    public boolean isDoctor() { return "doctor".equals(accountType); }
    public boolean isAdmin() { return "admin".equals(accountType); }


    public void setId(String userId) {
    }
}