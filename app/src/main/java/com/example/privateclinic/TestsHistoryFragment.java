package com.example.privateclinic;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.privateclinic.adapters.TestHistoryAdapter;
import com.example.privateclinic.models.TestBooking;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class TestsHistoryFragment extends Fragment {
    private RecyclerView historyRecyclerView;
    private FirebaseFirestore db;
    private List<TestBooking> testBookings = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tests_history, container, false);

        db = FirebaseFirestore.getInstance();
        historyRecyclerView = view.findViewById(R.id.historyRecyclerView);
        historyRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        loadTestHistory();

        return view;
    }

    private void loadTestHistory() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;

        db.collection("testBookings")
                .whereEqualTo("userId", FirebaseAuth.getInstance().getCurrentUser().getUid())
                .orderBy("bookingTime", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    testBookings = querySnapshot.toObjects(TestBooking.class);
                    TestHistoryAdapter adapter = new TestHistoryAdapter(testBookings);
                    historyRecyclerView.setAdapter(adapter);
                });
    }
}