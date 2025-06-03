package com.example.privateclinic;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.util.Consumer;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class BookAppointmentFragment extends Fragment {
    private RecyclerView specializationsRecyclerView;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private List<String> specializations = new ArrayList<>();
    private Map<String, List<Doctor>> doctorsBySpecialization = new HashMap<>();
    private SpecializationAdapter specializationAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_book_appointment, container, false);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        specializationsRecyclerView = view.findViewById(R.id.specializationsRecyclerView);
        specializationsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        specializationAdapter = new SpecializationAdapter(specializations, this::onSpecializationSelected);
        specializationsRecyclerView.setAdapter(specializationAdapter);

        loadSpecializations();

        return view;
    }

    private void loadSpecializations() {
        db.collection("doctors")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    Set<String> uniqueSpecializations = new HashSet<>();
                    doctorsBySpecialization.clear();

                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                        Doctor doctor = document.toObject(Doctor.class);
                        if (doctor != null) {
                            doctor.setId(document.getId());
                            String specialization = doctor.getSpecialization();
                            uniqueSpecializations.add(specialization);

                            if (!doctorsBySpecialization.containsKey(specialization)) {
                                doctorsBySpecialization.put(specialization, new ArrayList<>());
                            }
                            doctorsBySpecialization.get(specialization).add(doctor);
                        }
                    }

                    specializations.clear();
                    specializations.addAll(uniqueSpecializations);
                    specializationAdapter.notifyDataSetChanged();
                });
    }

    private void onSpecializationSelected(String specialization) {
        List<Doctor> doctors = doctorsBySpecialization.get(specialization);
        if (doctors != null) {
            showDoctorsDialog(doctors);
        }
    }

    private void showDoctorsDialog(List<Doctor> doctors) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Выберите врача");

        // Преобразуем список врачей в список строк (например, имена)
        List<String> doctorNames = new ArrayList<>();
        for (Doctor doctor : doctors) {
            doctorNames.add(doctor.getName() + " (" + doctor.getSpecialization() + ")");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_list_item_1, doctorNames);

        builder.setAdapter(adapter, (dialog, which) -> {
            // Обработка выбора врача
            onDoctorSelected(doctors.get(which));
        });

        builder.setNegativeButton("Отмена", null);
        builder.show();
    }

    private void onDoctorSelected(Doctor doctor) {
        showDatePicker(doctor);
    }

    private void showDatePicker(Doctor doctor) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePicker = new DatePickerDialog(getContext(),
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    showTimePicker(doctor, calendar);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));

        // Блокировка дней, когда врач не работает
        datePicker.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePicker.show();
    }

    private void showTimePicker(Doctor doctor, Calendar date) {
        int dayOfWeek = date.get(Calendar.DAY_OF_WEEK);
        if (!doctor.getAvailableDays().contains(String.valueOf(dayOfWeek))) {
            Toast.makeText(getContext(), "Врач не работает в этот день", Toast.LENGTH_SHORT).show();
            return;
        }

        TimePickerDialog timePicker = new TimePickerDialog(getContext(),
                (view, hourOfDay, minute) -> {
                    if (hourOfDay < doctor.getStartHour() || hourOfDay >= doctor.getEndHour()) {
                        Toast.makeText(getContext(),
                                "Врач принимает с " + doctor.getStartHour() + ":00 до " + doctor.getEndHour() + ":00",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Устанавливаем точное время (только полные часы)
                    date.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    date.set(Calendar.MINUTE, 0);
                    date.set(Calendar.SECOND, 0);

                    createAppointment(doctor, date);
                },
                doctor.getStartHour(),
                0,
                true);

        timePicker.setTitle("Выберите время");
        timePicker.show();
    }

    private void createAppointment(Doctor doctor, Calendar date) {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(getContext(), "Необходимо авторизоваться", Toast.LENGTH_SHORT).show();
            return;
        }

        Appointment appointment = new Appointment();
        appointment.setUserId(auth.getCurrentUser().getUid());
        appointment.setDoctorId(doctor.getId());
        appointment.setDoctorName(doctor.getName());
        appointment.setSpecialization(doctor.getSpecialization());
        appointment.setAppointmentTime(new Timestamp(date.getTime()));
        appointment.setCreatedAt(Timestamp.now());
        appointment.setStatus("booked");

        db.collection("appointments")
                .add(appointment)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(getContext(), "Запись успешно создана", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Ошибка создания записи", Toast.LENGTH_SHORT).show();
                });
    }

    private static class SpecializationAdapter extends RecyclerView.Adapter<SpecializationAdapter.ViewHolder> {
        private List<String> specializations;
        private Consumer<String> onItemClick;

        public SpecializationAdapter(List<String> specializations, Consumer<String> onItemClick) {
            this.specializations = specializations;
            this.onItemClick = onItemClick;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_specialization, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            String specialization = specializations.get(position);
            holder.specializationText.setText(specialization);
            holder.itemView.setOnClickListener(v -> onItemClick.accept(specialization));
        }

        @Override
        public int getItemCount() {
            return specializations.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView specializationText;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                specializationText = itemView.findViewById(R.id.specializationText);
            }
        }
    }

    private static class DoctorsAdapter extends RecyclerView.Adapter<DoctorsAdapter.ViewHolder> {
        private List<Doctor> doctors;
        private Consumer<Doctor> onItemClick;

        public DoctorsAdapter(List<Doctor> doctors, Consumer<Doctor> onItemClick) {
            this.doctors = doctors;
            this.onItemClick = onItemClick;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_doctor_simple, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Doctor doctor = doctors.get(position);
            holder.nameText.setText(doctor.getName());
            holder.ratingText.setText(String.format(Locale.getDefault(), "%.1f", doctor.getRating()));
            holder.itemView.setOnClickListener(v -> onItemClick.accept(doctor));
        }

        @Override
        public int getItemCount() {
            return doctors.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView nameText, ratingText;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                nameText = itemView.findViewById(R.id.doctorName);
                ratingText = itemView.findViewById(R.id.doctorRating);
            }
        }
    }
}