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
public class EditProfileFragment extends Fragment {

    private EditText fullNameEditText, emailEditText, addressEditText;
    private Button saveButton, cancelButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_profile, container, false);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();

        fullNameEditText = view.findViewById(R.id.fullNameEditText);
        emailEditText = view.findViewById(R.id.emailEditText);
        addressEditText = view.findViewById(R.id.addressEditText);
        saveButton = view.findViewById(R.id.saveButton);
        cancelButton = view.findViewById(R.id.cancelButton);

        loadUserData();

        saveButton.setOnClickListener(v -> saveUserData());
        cancelButton.setOnClickListener(v -> {
            // Возврат к просмотру профиля
            ((MainActivity) requireActivity()).replaceFragment(new ProfileFragment());
        });

        return view;
    }

    private void loadUserData() {
        if (currentUser != null) {
            emailEditText.setText(currentUser.getEmail());

            db.collection("users").document(currentUser.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            fullNameEditText.setText(documentSnapshot.getString("fullName"));
                            addressEditText.setText(documentSnapshot.getString("address"));
                        }
                    });
        }
    }

    private void saveUserData() {
        String fullName = fullNameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String address = addressEditText.getText().toString().trim();

        if (fullName.isEmpty() || email.isEmpty() || address.isEmpty()) {
            Toast.makeText(getContext(), "Заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }

        // Обновляем данные в Firestore
        Map<String, Object> updates = new HashMap<>();
        updates.put("fullName", fullName);
        updates.put("address", address);

        db.collection("users").document(currentUser.getUid())
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    // Обновляем displayName в Auth
                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setDisplayName(fullName)
                            .build();
                    currentUser.updateProfile(profileUpdates);

                    // Обновляем email (если изменился)
                    if (!email.equals(currentUser.getEmail())) {
                        currentUser.updateEmail(email)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(getContext(), "Данные сохранены", Toast.LENGTH_SHORT).show();
                                        ((MainActivity) requireActivity()).replaceFragment(new ProfileFragment());
                                    } else {
                                        Toast.makeText(getContext(), "Ошибка обновления email", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Toast.makeText(getContext(), "Данные сохранены", Toast.LENGTH_SHORT).show();
                        ((MainActivity) requireActivity()).replaceFragment(new ProfileFragment());
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Ошибка сохранения данных", Toast.LENGTH_SHORT).show();
                });
    }
}