package com.example.tp5ex2;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;

public class DoctorScheduleActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_schedule);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Mon Emploi du Temps");
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        TextView tvMessage = findViewById(R.id.tvMessage);
        tvMessage.setText("Fonctionnalité à implémenter\n\nGestion des horaires de travail");
    }
}