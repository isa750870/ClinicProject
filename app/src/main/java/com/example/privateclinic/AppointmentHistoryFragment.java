package com.example.privateclinic;
import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.util.Consumer;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.privateclinic.models.Appointment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AppointmentHistoryFragment extends Fragment {
    private RecyclerView appointmentsRecyclerView;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private List<Appointment> appointments = new ArrayList<>();
    private AppointmentAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_appointment_history, container, false);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        appointmentsRecyclerView = view.findViewById(R.id.appointmentsRecyclerView);
        appointmentsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new AppointmentAdapter(appointments, this::deleteAppointment);
        appointmentsRecyclerView.setAdapter(adapter);

        loadAppointments();

        return view;
    }
    private void deleteAppointment(Appointment appointment) {
        new AlertDialog.Builder(getContext())
                .setTitle("Отмена записи")
                .setMessage("Вы уверены, что хотите отменить запись к " + appointment.getDoctorName() + "?")
                .setPositiveButton("Да", (dialog, which) -> {
                    cancelAppointment(appointment);
                })
                .setNegativeButton("Нет", null)
                .show();
    }

    private void cancelAppointment(Appointment appointment) {
        if (appointment == null || appointment.getId() == null) return;

        db.collection("appointments")
                .document(appointment.getId())
                .update("status", "canceled")
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Запись отменена", Toast.LENGTH_SHORT).show();
                    loadAppointments(); // Обновляем список
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Ошибка отмены записи", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadAppointments() {
        if (auth.getCurrentUser() == null) return;

        db.collection("appointments")
                .whereEqualTo("userId", auth.getCurrentUser().getUid())
                .orderBy("appointmentTime", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    Log.d("AppointmentHistory", "Найдено записей: " + querySnapshot.size());
                    appointments.clear();
                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                        Appointment appointment = document.toObject(Appointment.class);
                        if (appointment != null) {
                            appointment.setId(document.getId());
                            appointments.add(appointment);
                        }
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e("AppointmentHistory", "Ошибка загрузки: ", e);
                    Toast.makeText(getContext(), "Ошибка загрузки данных", Toast.LENGTH_SHORT).show();
                });
    }

    private static class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.ViewHolder> {
        private List<Appointment> appointments;
        private Consumer<Appointment> onDeleteClick;

        public AppointmentAdapter(List<Appointment> appointments, Consumer<Appointment> onDeleteClick) {
            this.appointments = appointments;
            this.onDeleteClick = onDeleteClick;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_appointment, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Appointment appointment = appointments.get(position);

            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
            String dateTime = sdf.format(appointment.getAppointmentTime().toDate());

            holder.doctorText.setText(appointment.getDoctorName());
            holder.specializationText.setText(appointment.getSpecialization());
            holder.dateText.setText(dateTime);
            holder.statusText.setText(appointment.getStatus());
            if ("booked".equals(appointment.getStatus())) {
                holder.deleteButton.setVisibility(View.VISIBLE);
                holder.deleteButton.setOnClickListener(v -> onDeleteClick.accept(appointment));
            } else {
                holder.deleteButton.setVisibility(View.GONE);
            }
            // Установка цвета в зависимости от статуса
            int color;
            switch (appointment.getStatus()) {
                case "completed":
                    color = Color.GREEN;
                    break;
                case "canceled":
                    color = Color.RED;
                    break;
                default:
                    color = Color.BLUE;
            }
            holder.statusText.setTextColor(color);

        }

        @Override
        public int getItemCount() {
            return appointments.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView doctorText, specializationText, dateText, statusText;
            Button deleteButton;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                doctorText = itemView.findViewById(R.id.doctorText);
                specializationText = itemView.findViewById(R.id.specializationText);
                dateText = itemView.findViewById(R.id.dateText);
                statusText = itemView.findViewById(R.id.statusText);
                deleteButton = itemView.findViewById(R.id.deleteButton);
            }
        }
    }
}