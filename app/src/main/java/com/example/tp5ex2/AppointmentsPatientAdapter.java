package com.example.tp5ex2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AppointmentsPatientAdapter extends RecyclerView.Adapter<AppointmentsPatientAdapter.ViewHolder> {

    private List<Appointment> appointments;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Appointment appointment);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public AppointmentsPatientAdapter(List<Appointment> appointments) {
        this.appointments = appointments;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_appointment_patient, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Appointment appointment = appointments.get(position);

        // Nom du patient (déjà connu)
        holder.tvPatientName.setText(appointment.getPatientName());

        // Date et heure
        holder.tvDate.setText(appointment.getAppointmentDate());
        holder.tvTime.setText(appointment.getAppointmentTime());

        // Statut avec couleur
        String status = appointment.getStatus();
        if (status != null) {
            status = status.toLowerCase();

            if (status.contains("pending") || status.contains("en attente")) {
                holder.tvStatus.setText("⏳ En attente");
                holder.tvStatus.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.status_pending));
            } else if (status.contains("confirmé") || status.contains("confirmed")) {
                holder.tvStatus.setText("✅ Confirmé");
                holder.tvStatus.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.status_confirmed));
            } else if (status.contains("annulé") || status.contains("cancelled")) {
                holder.tvStatus.setText("❌ Annulé");
                holder.tvStatus.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.status_cancelled));
            } else {
                holder.tvStatus.setText(status);
                holder.tvStatus.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.status_completed));
            }
        }

        // Cabinet (chargé dynamiquement dans le clic)
        holder.tvCabinet.setText("Voir les détails...");

        // Gestion du clic
        holder.cardView.setOnClickListener(v -> {
            if (listener != null) {
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
        TextView tvStatus;
        TextView tvCabinet;

        ViewHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardAppointment);
            tvPatientName = itemView.findViewById(R.id.tvPatientName);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvCabinet = itemView.findViewById(R.id.tvCabinet);
        }
    }
}