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
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

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
        db.collection("doctors")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    doctors = querySnapshot.toObjects(Doctor.class);
                    for (int i = 0; i < querySnapshot.getDocuments().size(); i++) {
                        doctors.get(i).setId(querySnapshot.getDocuments().get(i).getId());
                    }
                    adapter.updateDoctors(doctors);
                });
    }
}