package com.example.privateclinic;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ProfileFragment extends Fragment {

    private TextView fullNameTextView, emailTextView, addressTextView, emergencyHistoryTextView;
    private Button editProfileButton, logoutButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        fullNameTextView = view.findViewById(R.id.fullNameTextView);
        emailTextView = view.findViewById(R.id.emailTextView);
        addressTextView = view.findViewById(R.id.addressTextView);
        editProfileButton = view.findViewById(R.id.editProfileButton);
        emergencyHistoryTextView = view.findViewById(R.id.emergencyHistoryTextView);
        logoutButton = view.findViewById(R.id.logoutButton);
        loadEmergencyHistory();
        loadUserData();

        editProfileButton.setOnClickListener(v -> {
            // Переход на фрагмент редактирования
            ((MainActivity) requireActivity()).replaceFragment(new EditProfileFragment());
        });

        logoutButton.setOnClickListener(v -> logoutUser());

        return view;
    }

    private void loadUserData() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            // Загружаем email из Auth
            emailTextView.setText(user.getEmail());

            // Загружаем остальные данные из Firestore
            db.collection("users").document(user.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            fullNameTextView.setText(documentSnapshot.getString("fullName"));
                            addressTextView.setText(documentSnapshot.getString("address"));
                        }
                    });
        }
    }

    private void logoutUser() {
        mAuth.signOut();
        ((MainActivity) requireActivity()).showBottomNavigation(false);
        ((MainActivity) requireActivity()).replaceFragment(new LoginFragment());
        Toast.makeText(getContext(), "Вы вышли из аккаунта", Toast.LENGTH_SHORT).show();
    }

    // В методе loadUserData() добавьте:
    private void loadEmergencyHistory() {
        if (mAuth.getCurrentUser() == null) return;

        db.collection("users")
                .document(mAuth.getCurrentUser().getUid())
                .collection("emergencyCalls")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(5) // Последние 5 вызовов
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    StringBuilder historyBuilder = new StringBuilder();
                    historyBuilder.append("История вызовов:\n\n");

                    SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());

                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                        Timestamp timestamp = document.getTimestamp("timestamp");
                        String status = document.getString("status");

                        if (timestamp != null) {
                            historyBuilder.append(sdf.format(timestamp.toDate()))
                                    .append(" - ")
                                    .append(status != null ? status : "неизвестно")
                                    .append("\n");
                        }
                    }

                    emergencyHistoryTextView.setText(historyBuilder.toString());
                });
    }
}