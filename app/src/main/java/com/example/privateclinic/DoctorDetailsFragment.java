package com.example.privateclinic;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class DoctorDetailsFragment extends Fragment {
    private static final String ARG_DOCTOR = "doctor";

    private Doctor doctor;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private UserDoctorRelation userRelation;
    private EditText reviewEditText, noteEditText;
    private RatingBar reviewRatingBar;
    private TextView ratingTextView, reviewCountTextView;
    private Button favoriteButton, blockButton;
    private TextView existingReviewText;

    public static DoctorDetailsFragment newInstance(Doctor doctor) {
        DoctorDetailsFragment fragment = new DoctorDetailsFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_DOCTOR, doctor);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            doctor = getArguments().getParcelable(ARG_DOCTOR);
        }
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_doctor_details, container, false);

        // Инициализация UI элементов

        TextView nameText = view.findViewById(R.id.doctorDetailName);
        TextView specializationText = view.findViewById(R.id.doctorDetailSpecialization);
        TextView descriptionText = view.findViewById(R.id.doctorDetailDescription);
        ratingTextView = view.findViewById(R.id.doctorDetailRating);
        reviewCountTextView = view.findViewById(R.id.doctorDetailReviewCount);
        reviewEditText = view.findViewById(R.id.reviewEditText);
        reviewRatingBar = view.findViewById(R.id.reviewRatingBar);
        noteEditText = view.findViewById(R.id.noteEditText);
        favoriteButton = view.findViewById(R.id.favoriteButton);
        blockButton = view.findViewById(R.id.blockButton);
        Button submitReviewButton = view.findViewById(R.id.submitReviewButton);
        Button saveNoteButton = view.findViewById(R.id.saveNoteButton);


        // Заполнение данных врача
        nameText.setText(doctor.getName());
        specializationText.setText(doctor.getSpecialization());
        descriptionText.setText(doctor.getDescription());
        updateRatingViews();

        // Загрузка отношений пользователя с врачом
        loadUserRelation();

        favoriteButton.setOnClickListener(v -> {
            if (checkAuthAndDoctor()) {
                toggleFavorite();
            }
        });

        blockButton.setOnClickListener(v -> {
            if (checkAuthAndDoctor()) {
                toggleBlock();
            }
        });

        // Обработчики кнопок
        favoriteButton.setOnClickListener(v -> toggleFavorite());
        blockButton.setOnClickListener(v -> toggleBlock());
        submitReviewButton.setOnClickListener(v -> submitReview());
        saveNoteButton.setOnClickListener(v -> saveNote());
        existingReviewText = view.findViewById(R.id.existingReviewText);
        checkExistingReview();
        return view;
    }

    private boolean checkAuthAndDoctor() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(getContext(), "Войдите в систему", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (doctor == null || doctor.getId() == null) {
            Toast.makeText(getContext(), "Данные врача не загружены", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
    private void checkExistingReview() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null || doctor == null) return;

        db.collection("doctor_reviews")
                .whereEqualTo("doctorId", doctor.getId())
                .whereEqualTo("userId", currentUser.getUid())
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        DoctorReview existingReview = querySnapshot.getDocuments().get(0).toObject(DoctorReview.class);
                        reviewEditText.setText(existingReview.getReviewText());
                        reviewRatingBar.setRating(existingReview.getRating());
                        existingReviewText.setVisibility(View.VISIBLE);
                    } else {
                        existingReviewText.setVisibility(View.GONE);
                    }
                });
    }
    private void loadUserRelation() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null || doctor == null || doctor.getId() == null) {
            initializeDefaultRelation();
            return;
        }

        String relationId = currentUser.getUid() + "_" + doctor.getId();

        db.collection("user_doctor_relations").document(relationId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().exists()) {
                            userRelation = task.getResult().toObject(UserDoctorRelation.class);
                            if (userRelation == null) {
                                initializeDefaultRelation();
                            } else {
                                // Убедимся, что ID установлены
                                userRelation.setUserId(currentUser.getUid());
                                userRelation.setDoctorId(doctor.getId());
                            }
                        } else {
                            initializeDefaultRelation();
                        }
                        updateRelationViews();
                    } else {
                        Log.e("Firestore", "Ошибка загрузки", task.getException());
                        initializeDefaultRelation();
                    }
                });
    }

    private void initializeDefaultRelation() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null || doctor == null) return;

        userRelation = new UserDoctorRelation();
        userRelation.setUserId(currentUser.getUid());
        userRelation.setDoctorId(doctor.getId());
        userRelation.setFavorite(false);
        userRelation.setBlocked(false);
        userRelation.setUserNote("");
    }

    private void toggleFavorite() {
        if (userRelation == null) {
            userRelation = new UserDoctorRelation();
            userRelation.setUserId(FirebaseAuth.getInstance().getCurrentUser().getUid());
            userRelation.setDoctorId(doctor.getId());
        }

        // Взаимоисключающая логика
        boolean newFavoriteState = !userRelation.isFavorite();
        userRelation.setFavorite(newFavoriteState);

        if (newFavoriteState) {
            userRelation.setBlocked(false); // Снимаем из ЧС если добавляем в избранное
        }

        saveUserRelation();
    }

    private void toggleBlock() {
        if (userRelation == null) {
            userRelation = new UserDoctorRelation();
            userRelation.setUserId(FirebaseAuth.getInstance().getCurrentUser().getUid());
            userRelation.setDoctorId(doctor.getId());
        }

        // Взаимоисключающая логика
        boolean newBlockedState = !userRelation.isBlocked();
        userRelation.setBlocked(newBlockedState);

        if (newBlockedState) {
            userRelation.setFavorite(false); // Убираем из избранного если добавляем в ЧС
        }

        saveUserRelation();
    }

    private void saveUserRelation() {
        if (userRelation == null ||
                userRelation.getUserId() == null ||
                userRelation.getDoctorId() == null) {
            Toast.makeText(getContext(), "Ошибка: данные не загружены", Toast.LENGTH_SHORT).show();
            return;
        }

        // Проверка взаимоисключения
        if (userRelation.isFavorite() && userRelation.isBlocked()) {
            userRelation.setBlocked(false); // Автоматически снимаем блокировку
        }

        Map<String, Object> data = new HashMap<>();
        data.put("userId", userRelation.getUserId());
        data.put("doctorId", userRelation.getDoctorId());
        data.put("isFavorite", userRelation.isFavorite());
        data.put("isBlocked", userRelation.isBlocked());
        data.put("userNote", userRelation.getUserNote());

        db.collection("user_doctor_relations")
                .document(userRelation.getUserId() + "_" + userRelation.getDoctorId())
                .set(data, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Сохранено", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(),
                            "Ошибка сохранения: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }


    private void updateRelationViews() {
        if (userRelation == null) return;

        // Обновляем кнопки с учетом взаимоисключения
        favoriteButton.setText(userRelation.isFavorite() ?
                "★ В избранном" : "☆ В избранное");
        favoriteButton.setBackgroundColor(userRelation.isFavorite() ?
                Color.parseColor("#FFD700") : Color.TRANSPARENT);

        blockButton.setText(userRelation.isBlocked() ?
                "☒ В ЧС" : "☐ В ЧС");
        blockButton.setBackgroundColor(userRelation.isBlocked() ?
                Color.parseColor("#FF6347") : Color.TRANSPARENT);

        noteEditText.setText(userRelation.getUserNote());
    }


    private void saveNote() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "Войдите, чтобы сохранить заметку", Toast.LENGTH_SHORT).show();
            return;
        }

        if (doctor == null || doctor.getId() == null) {
            Toast.makeText(getContext(), "Ошибка: данные врача не загружены", Toast.LENGTH_SHORT).show();
            return;
        }

        String noteText = noteEditText.getText().toString().trim();
        userRelation.setUserNote(noteText);

        // Создаем или обновляем документ
        Map<String, Object> relationData = new HashMap<>();
        relationData.put("userId", currentUser.getUid());
        relationData.put("doctorId", doctor.getId());
        relationData.put("isFavorite", userRelation.isFavorite());
        relationData.put("isBlocked", userRelation.isBlocked());
        relationData.put("userNote", noteText);

        // Используем merge для обновления или создания документа
        db.collection("user_doctor_relations")
                .document(currentUser.getUid() + "_" + doctor.getId()) // Уникальный ID
                .set(relationData, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Заметка сохранена", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Ошибка сохранения: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void submitReview() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "Войдите, чтобы оставить отзыв", Toast.LENGTH_SHORT).show();
            return;
        }

        String reviewText = reviewEditText.getText().toString().trim();
        int rating = (int) reviewRatingBar.getRating();

        if (rating == 0) {
            Toast.makeText(getContext(), "Поставьте оценку", Toast.LENGTH_SHORT).show();
            return;
        }

        // Проверяем, есть ли уже отзыв от этого пользователя
        db.collection("doctor_reviews")
                .whereEqualTo("doctorId", doctor.getId())
                .whereEqualTo("userId", currentUser.getUid())
                .limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {
                            // Отзыв уже существует - обновляем его
                            DocumentSnapshot existingReview = task.getResult().getDocuments().get(0);
                            updateExistingReview(existingReview.getId(), reviewText, rating);
                        } else {
                            // Создаем новый отзыв
                            createNewReview(reviewText, rating);
                        }
                    } else {
                        Toast.makeText(getContext(), "Ошибка проверки отзывов", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void createNewReview(String reviewText, int rating) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        DoctorReview review = new DoctorReview();
        review.setDoctorId(doctor.getId());
        review.setUserId(currentUser.getUid());
        review.setUserName(currentUser.getEmail());
        review.setReviewText(reviewText);
        review.setRating(rating);
        review.setCreatedAt(Timestamp.now());

        db.collection("doctor_reviews")
                .add(review)
                .addOnSuccessListener(documentReference -> {
                    updateDoctorRating(rating, true);
                    reviewEditText.setText("");
                    reviewRatingBar.setRating(0);
                    Toast.makeText(getContext(), "Отзыв добавлен", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Ошибка добавления отзыва", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateExistingReview(String reviewId, String reviewText, int newRating) {
        // Сначала получаем старый рейтинг
        db.collection("doctor_reviews").document(reviewId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Integer oldRating = documentSnapshot.getLong("rating").intValue();

                        // Обновляем отзыв
                        Map<String, Object> updates = new HashMap<>();
                        updates.put("reviewText", reviewText);
                        updates.put("rating", newRating);
                        updates.put("createdAt", FieldValue.serverTimestamp());

                        db.collection("doctor_reviews").document(reviewId)
                                .update(updates)
                                .addOnSuccessListener(aVoid -> {
                                    // Обновляем рейтинг врача с учетом изменения
                                    updateDoctorRating(newRating - oldRating, false);
                                    reviewEditText.setText("");
                                    reviewRatingBar.setRating(0);
                                    Toast.makeText(getContext(), "Отзыв обновлен", Toast.LENGTH_SHORT).show();
                                });
                    }
                });
    }
    private void updateDoctorRating(int ratingChange, boolean isNewReview) {
        db.collection("doctors").document(doctor.getId())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        double currentRating = documentSnapshot.getDouble("rating") != null ?
                                documentSnapshot.getDouble("rating") : 0.0;
                        int currentReviewCount = documentSnapshot.getLong("reviewCount") != null ?
                                documentSnapshot.getLong("reviewCount").intValue() : 0;

                        double newAverageRating;
                        int newReviewCount;

                        if (isNewReview) {
                            newAverageRating = (currentRating * currentReviewCount + ratingChange) /
                                    (currentReviewCount + 1);
                            newReviewCount = currentReviewCount + 1;
                        } else {
                            newAverageRating = (currentRating * currentReviewCount + ratingChange) /
                                    currentReviewCount;
                            newReviewCount = currentReviewCount;
                        }

                        Map<String, Object> updates = new HashMap<>();
                        updates.put("rating", newAverageRating);
                        updates.put("reviewCount", newReviewCount);

                        db.collection("doctors").document(doctor.getId())
                                .update(updates)
                                .addOnSuccessListener(aVoid -> {
                                    doctor.setRating(newAverageRating);
                                    doctor.setReviewCount(newReviewCount);
                                    updateRatingViews();
                                });
                    }
                });
    }


    private void updateRatingViews() {
        ratingTextView.setText(String.format(Locale.getDefault(), "%.1f", doctor.getRating()));
        reviewCountTextView.setText(String.format(Locale.getDefault(), "(%d отзывов)", doctor.getReviewCount()));
    }


}