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
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterFragment extends Fragment {

    private EditText emailEditText, passwordEditText, confirmPasswordEditText,
            fullNameEditText, addressEditText;
    private Button registerButton;
    private TextView loginTextView;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        emailEditText = view.findViewById(R.id.emailEditText);
        passwordEditText = view.findViewById(R.id.passwordEditText);
        confirmPasswordEditText = view.findViewById(R.id.confirmPasswordEditText);
        fullNameEditText = view.findViewById(R.id.fullNameEditText);
        addressEditText = view.findViewById(R.id.addressEditText);
        registerButton = view.findViewById(R.id.registerButton);
        loginTextView = view.findViewById(R.id.loginTextView);

        registerButton.setOnClickListener(v -> registerUser());
        loginTextView.setOnClickListener(v ->
                ((MainActivity) requireActivity()).replaceFragment(new LoginFragment()));

        return view;
    }

    private void registerUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();
        String fullName = fullNameEditText.getText().toString().trim();
        String address = addressEditText.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()
                || fullName.isEmpty() || address.isEmpty()) {
            Toast.makeText(getContext(), "Заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(getContext(), "Пароли не совпадают", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(requireActivity(), task -> {
                    if (task.isSuccessful()) {
                        // Регистрация успешна, сохраняем дополнительные данные
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // 1. Обновляем displayName в Firebase Auth
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(fullName)
                                    .build();

                            user.updateProfile(profileUpdates);

                            // 2. Сохраняем все данные в Firestore
                            Map<String, Object> userData = new HashMap<>();
                            userData.put("email", email);
                            userData.put("fullName", fullName);
                            userData.put("address", address);

                            db.collection("users").document(user.getUid())
                                    .set(userData)
                                    .addOnSuccessListener(aVoid -> {
                                        // Переходим в основное приложение
                                        ((MainActivity) requireActivity()).showBottomNavigation(true);
                                        ((MainActivity) requireActivity()).replaceFragment(new ProfileFragment());
                                        Toast.makeText(getContext(), "Регистрация успешна", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(getContext(), "Ошибка сохранения данных", Toast.LENGTH_SHORT).show();
                                    });
                        }
                    } else {
                        Toast.makeText(getContext(), "Ошибка регистрации: " +
                                task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}