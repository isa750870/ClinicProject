package com.example.privateclinic;

import android.animation.ObjectAnimator;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class HomeFragment extends Fragment {
    private Button emergencyButton;
    private ProgressBar progressBar;
    private TextView timerText;
    private CountDownTimer emergencyTimer;
    private boolean isTimerRunning = false;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        emergencyButton = view.findViewById(R.id.emergencyButton);
        progressBar = view.findViewById(R.id.progressBar);
        timerText = view.findViewById(R.id.timerText);

        setupEmergencyButton();
        checkActiveEmergency();

        return view;
    }

    private void setupEmergencyButton() {
        emergencyButton.setOnTouchListener(new View.OnTouchListener() {
            private Handler handler = new Handler();
            private Runnable longPressRunnable;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        longPressRunnable = new Runnable() {
                            @Override
                            public void run() {
                                startEmergencyCountdown();
                            }
                        };
                        handler.postDelayed(longPressRunnable, 3000);
                        startPressAnimation();
                        return true;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        handler.removeCallbacks(longPressRunnable);
                        resetButtonState();
                        return true;
                }
                return false;
            }
        });
    }

    private void startPressAnimation() {
        emergencyButton.setScaleX(0.9f);
        emergencyButton.setScaleY(0.9f);
        emergencyButton.setAlpha(0.8f);
        progressBar.setVisibility(View.VISIBLE);
        progressBar.setProgress(0);

        ObjectAnimator progressAnimator = ObjectAnimator.ofInt(progressBar, "progress", 0, 100);
        progressAnimator.setDuration(3000);
        progressAnimator.start();
    }

    private void resetButtonState() {
        emergencyButton.setScaleX(1f);
        emergencyButton.setScaleY(1f);
        emergencyButton.setAlpha(1f);
        progressBar.setVisibility(View.INVISIBLE);
    }

    private void startEmergencyCountdown() {
        if (isTimerRunning || mAuth.getCurrentUser() == null) return;

        isTimerRunning = true;
        emergencyButton.setEnabled(false);
        timerText.setVisibility(View.VISIBLE);

        // Создаем данные о вызове
        Map<String, Object> emergencyCall = new HashMap<>();
        emergencyCall.put("timestamp", FieldValue.serverTimestamp());
        emergencyCall.put("status", "active");

        // Добавляем в подколлекцию emergencyCalls внутри документа пользователя
        db.collection("users")
                .document(mAuth.getCurrentUser().getUid())
                .collection("emergencyCalls")
                .add(emergencyCall)
                .addOnSuccessListener(documentReference -> {
                    startEmergencyTimer(documentReference.getId());
                });
    }

    private void startEmergencyTimer(String callId) {
        emergencyTimer = new CountDownTimer(20 * 60 * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long minutes = millisUntilFinished / 60000;
                long seconds = (millisUntilFinished % 60000) / 1000;
                timerText.setText(String.format("%02d:%02d", minutes, seconds));
            }

            @Override
            public void onFinish() {
                resetEmergencyState();
                // Обновляем статус вызова
                db.collection("users")
                        .document(mAuth.getCurrentUser().getUid())
                        .collection("emergencyCalls")
                        .document(callId)
                        .update("status", "completed",
                                "completedAt", FieldValue.serverTimestamp());
            }
        }.start();
    }

    private void resetEmergencyState() {
        if (emergencyTimer != null) {
            emergencyTimer.cancel();
        }
        isTimerRunning = false;
        emergencyButton.setEnabled(true);
        timerText.setVisibility(View.INVISIBLE);
        resetButtonState();
    }

    private void checkActiveEmergency() {
        if (mAuth.getCurrentUser() == null) return;

        // Проверяем наличие активного вызова
        db.collection("users")
                .document(mAuth.getCurrentUser().getUid())
                .collection("emergencyCalls")
                .whereEqualTo("status", "active")
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        DocumentSnapshot document = querySnapshot.getDocuments().get(0);
                        Timestamp timestamp = document.getTimestamp("timestamp");
                        String callId = document.getId();

                        if (timestamp != null) {
                            long elapsed = System.currentTimeMillis() - timestamp.toDate().getTime();
                            long remaining = 20 * 60 * 1000 - elapsed;

                            if (remaining > 0) {
                                isTimerRunning = true;
                                emergencyButton.setEnabled(false);
                                timerText.setVisibility(View.VISIBLE);
                                startEmergencyTimerWithRemaining(callId, remaining);
                            } else {
                                // Автоматически завершаем просроченный вызов
                                document.getReference().update(
                                        "status", "completed",
                                        "completedAt", FieldValue.serverTimestamp()
                                );
                            }
                        }
                    }
                });
    }

    private void startEmergencyTimerWithRemaining(String callId, long remainingMillis) {
        emergencyTimer = new CountDownTimer(remainingMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long minutes = millisUntilFinished / 60000;
                long seconds = (millisUntilFinished % 60000) / 1000;
                timerText.setText(String.format("%02d:%02d", minutes, seconds));
            }

            @Override
            public void onFinish() {
                resetEmergencyState();
                Map<String, Object> update = new HashMap<>();
                update.put("status", "completed");
                update.put("completedAt", FieldValue.serverTimestamp());

                db.collection("emergency_calls")
                        .document(mAuth.getCurrentUser().getUid())
                        .update(update);
            }
        }.start();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (emergencyTimer != null) {
            emergencyTimer.cancel();
        }
    }
}