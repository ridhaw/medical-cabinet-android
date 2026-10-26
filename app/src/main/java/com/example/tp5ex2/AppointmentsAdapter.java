package com.example.tp5ex2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class AppointmentsAdapter extends RecyclerView.Adapter<AppointmentsAdapter.AppointmentViewHolder> {

    private List<Appointment> appointmentsList;
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(Appointment appointment);
    }


    @NonNull
    @Override
    public AppointmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_appointment_card, parent, false);
        return new AppointmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AppointmentViewHolder holder, int position) {
        Appointment appointment = appointmentsList.get(position);
        holder.bind(appointment);
    }

    @Override
    public int getItemCount() {
        return appointmentsList != null ? appointmentsList.size() : 0;
    }

    class AppointmentViewHolder extends RecyclerView.ViewHolder {
        private TextView tvPatientName, tvAppointmentDate, tvAppointmentTime, tvStatus;

        public AppointmentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPatientName = itemView.findViewById(R.id.tvPatientName);
            tvAppointmentDate = itemView.findViewById(R.id.tvAppointmentDate);
            tvAppointmentTime = itemView.findViewById(R.id.tvAppointmentTime);
            tvStatus = itemView.findViewById(R.id.tvStatus);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && onItemClickListener != null) {
                    onItemClickListener.onItemClick(appointmentsList.get(position));
                }
            });
        }

        public void bind(Appointment appointment) {
            tvPatientName.setText(appointment.getPatientName());
            tvAppointmentDate.setText(appointment.getAppointmentDate());
            tvAppointmentTime.setText(appointment.getAppointmentTime());
            tvStatus.setText(appointment.getStatus());
        }
    }
}