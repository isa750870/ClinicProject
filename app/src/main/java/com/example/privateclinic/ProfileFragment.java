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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ProfileFragment extends Fragment {

    private TextView fullNameTextView, emailTextView, addressTextView;
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
        logoutButton = view.findViewById(R.id.logoutButton);

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
}