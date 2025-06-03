package com.example.privateclinic;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppointmentFragment extends Fragment {
    private RecyclerView doctorsRecyclerView;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private List<Doctor> doctors = new ArrayList<>();
    private DoctorsAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_appointment, container, false);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        doctorsRecyclerView = view.findViewById(R.id.doctorsRecyclerView);
        doctorsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new DoctorsAdapter(doctors, doctor -> {
            DoctorDetailsFragment fragment = DoctorDetailsFragment.newInstance(doctor);
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.frame_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        doctorsRecyclerView.setAdapter(adapter);
        loadDoctors();

        return view;
    }

    private void loadDoctors() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        // Загружаем сначала отношения пользователя
        db.collection("user_doctor_relations")
                .whereEqualTo("userId", currentUser.getUid())
                .get()
                .addOnSuccessListener(relationsQuery -> {
                    // Собираем информацию об отношениях
                    Map<String, UserDoctorRelation> relationsMap = new HashMap<>();
                    for (DocumentSnapshot doc : relationsQuery.getDocuments()) {
                        UserDoctorRelation relation = doc.toObject(UserDoctorRelation.class);
                        if (relation != null && relation.getDoctorId() != null) {
                            relationsMap.put(relation.getDoctorId(), relation);
                        }
                    }

                    // Теперь загружаем всех врачей
                    db.collection("doctors")
                            .get()
                            .addOnSuccessListener(doctorsQuery -> {
                                List<Doctor> doctors = new ArrayList<>();
                                for (DocumentSnapshot doc : doctorsQuery.getDocuments()) {
                                    Doctor doctor = doc.toObject(Doctor.class);
                                    if (doctor != null) {
                                        doctor.setId(doc.getId());
                                        // Добавляем информацию об отношениях
                                        UserDoctorRelation relation = relationsMap.get(doctor.getId());
                                        doctor.setFavorite(relation != null && relation.isFavorite());
                                        doctor.setBlocked(relation != null && relation.isBlocked());
                                        doctors.add(doctor);
                                    }
                                }

                                // Сортируем врачей
                                Collections.sort(doctors, (d1, d2) -> {
                                    // 1. Заблокированные врачи в конце
                                    if (d1.isBlocked() && !d2.isBlocked()) return 1;
                                    if (!d1.isBlocked() && d2.isBlocked()) return -1;

                                    // 2. Избранные врачи в начале
                                    if (d1.isFavorite() && !d2.isFavorite()) return -1;
                                    if (!d1.isFavorite() && d2.isFavorite()) return 1;

                                    // 3. Остальные сортируются по рейтингу
                                    return Double.compare(d2.getRating(), d1.getRating());
                                });

                                adapter.updateDoctors(doctors);
                            });
                });
    }
}