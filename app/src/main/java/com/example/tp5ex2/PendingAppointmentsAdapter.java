package com.example.tp5ex2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PendingAppointmentsAdapter extends RecyclerView.Adapter<PendingAppointmentsAdapter.ViewHolder> {

    private List<Appointment> appointments;
    private OnItemClickListener listener;
    private SimpleDateFormat displayDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private SimpleDateFormat displayTimeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    public interface OnItemClickListener {
        void onItemClick(Appointment appointment);
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pending_appointment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (appointments == null || appointments.isEmpty()) {
            return;
        }

        Appointment appointment = appointments.get(position);

        // Afficher le nom du patient
        if (appointment.getPatientName() != null && !appointment.getPatientName().isEmpty()) {
            holder.tvPatientName.setText(appointment.getPatientName());
        } else {
            holder.tvPatientName.setText("Patient #" + appointment.getPatientId());
        }

        // Afficher la date et l'heure
        try {
            String dateStr = appointment.getAppointmentDate();
            String timeStr = appointment.getAppointmentTime();

            if (dateStr != null && !dateStr.isEmpty() && timeStr != null && !timeStr.isEmpty()) {
                // Formater la date
                SimpleDateFormat inputDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Date date = inputDateFormat.parse(dateStr);
                holder.tvDate.setText(displayDateFormat.format(date));

                // Formater l'heure
                holder.tvTime.setText(timeStr.substring(0, Math.min(timeStr.length(), 5))); // Garder seulement HH:mm
            } else {
                holder.tvDate.setText("--/--/----");
                holder.tvTime.setText("--:--");
            }
        } catch (ParseException e) {
            holder.tvDate.setText("--/--/----");
            holder.tvTime.setText("--:--");
        }

        // Afficher le motif
        if (appointment.getNotes() != null && !appointment.getNotes().isEmpty()) {
            holder.tvReason.setText(appointment.getNotes());
        } else if (appointment.getNotes() != null && !appointment.getNotes().isEmpty()) {
            holder.tvReason.setText(appointment.getNotes());
        } else {
            holder.tvReason.setText("Consultation");
        }

        // Gérer le clic sur l'élément
        holder.cardView.setOnClickListener(v -> {
            if (listener != null && appointment != null) {
                listener.onItemClick(appointment);
            }
        });
    }

    @Override
    public int getItemCount() {
        return appointments != null ? appointments.size() : 0;
    }

    public void updateData(List<Appointment> newAppointments) {
        this.appointments = newAppointments;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvPatientName;
        TextView tvDate;
        TextView tvTime;
        TextView tvReason;

        ViewHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardAppointment);
            tvPatientName = itemView.findViewById(R.id.tvPatientName);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvReason = itemView.findViewById(R.id.tvReason);
        }
    }
}