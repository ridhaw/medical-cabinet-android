package com.example.tp5ex2;

public class MedicalCabinet {
    private int id;
    private String name;
    private int imageResource;
    private String phone;
    private String email;
    private String website;
    private String address;
    private String speciality;
    private String services;
    private double latitude;
    private double longitude;
    private float rating;
    private String availability;
    private String doctorName;
    private boolean isAvailable;
    private int doctorId;
    public MedicalCabinet() {}
    // Constructeur complet
    public MedicalCabinet(int id, String name, int imageResource, String phone,
                          String email, String website, String address, String speciality,
                          double latitude, double longitude, float rating,
                          String availability, String doctorName, boolean isAvailable, int doctorId) {
        this.id = id;
        this.name = name;
        this.imageResource = imageResource;
        this.phone = phone;
        this.email = email;
        this.website = website;
        this.address = address;
        this.speciality = speciality;
        this.latitude = latitude;
        this.longitude = longitude;
        this.rating = rating;
        this.availability = availability;
        this.doctorName = doctorName;
        this.isAvailable = isAvailable;
        this.doctorId = doctorId;
    }


    // Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public int getImageResource() { return imageResource; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public String getWebsite() { return website; }
    public String getAddress() { return address; }
    public String getSpeciality() { return speciality; }
    public String getServices() { return services; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public float getRating() { return rating; }
    public String getAvailability() { return availability; }
    public String getDoctorName() { return doctorName; }
    public boolean isAvailable() { return isAvailable; }
    public int getDoctorId() { return doctorId; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setImageResource(int imageResource) { this.imageResource = imageResource; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setEmail(String email) { this.email = email; }
    public void setWebsite(String website) { this.website = website; }
    public void setAddress(String address) { this.address = address; }
    public void setSpeciality(String speciality) { this.speciality = speciality; }
    public void setServices(String services) { this.services = services; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public void setRating(float rating) { this.rating = rating; }
    public void setAvailability(String availability) { this.availability = availability; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }
    public void setAvailable(boolean available) { isAvailable = available; }
    public void setDoctorId(int doctorId) { this.doctorId = doctorId; }
}