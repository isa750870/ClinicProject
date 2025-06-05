package com.example.privateclinic.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.privateclinic.R;
import com.example.privateclinic.models.Doctor;

import java.util.List;
import java.util.Locale;

public class DoctorsAdapter extends RecyclerView.Adapter<DoctorsAdapter.ViewHolder> {
    private List<Doctor> doctors;
    private OnDoctorClickListener listener;

    public DoctorsAdapter(List<Doctor> doctors, OnDoctorClickListener listener) {
        this.doctors = doctors;
        this.listener = listener;
    }

    public void updateDoctors(List<Doctor> newDoctors) {
        doctors = newDoctors;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_doctor, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Doctor doctor = doctors.get(position);
        holder.nameText.setText(doctor.getName());
        holder.specializationText.setText(doctor.getSpecialization());
        holder.ratingText.setText(String.format(Locale.getDefault(), "%.1f (%d)",
                doctor.getRating(), doctor.getReviewCount()));

        holder.itemView.setOnClickListener(v -> listener.onDoctorClick(doctor));
    }

    @Override
    public int getItemCount() {
        return doctors.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameText, specializationText, ratingText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.doctorName);
            specializationText = itemView.findViewById(R.id.doctorSpecialization);
            ratingText = itemView.findViewById(R.id.doctorRating);
        }
    }

    public interface OnDoctorClickListener {
        void onDoctorClick(Doctor doctor);
    }
}