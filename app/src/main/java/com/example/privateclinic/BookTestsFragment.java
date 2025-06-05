package com.example.privateclinic;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.privateclinic.adapters.TestsAdapter;
import com.example.privateclinic.models.TestType;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BookTestsFragment extends Fragment {
    private RecyclerView testsRecyclerView;
    private FirebaseFirestore db;
    private List<TestType> testTypes = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_book_tests, container, false);

        db = FirebaseFirestore.getInstance();
        testsRecyclerView = view.findViewById(R.id.testsRecyclerView);
        testsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        loadTestTypes();

        return view;
    }

    private void loadTestTypes() {
        db.collection("testTypes")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    testTypes = querySnapshot.toObjects(TestType.class);
                    TestsAdapter adapter = new TestsAdapter(testTypes, this::showTimePickerDialog);
                    testsRecyclerView.setAdapter(adapter);
                });
    }

    private void showTimePickerDialog(TestType testType) {
        Calendar calendar = Calendar.getInstance();

        // Проверяем доступные дни для анализа
        if (!testType.getAvailableDays().contains(calendar.get(Calendar.DAY_OF_WEEK))) {
            Toast.makeText(getContext(), "Этот анализ доступен только в " +
                    getDaysString(testType.getAvailableDays()), Toast.LENGTH_LONG).show();
            return;
        }

        TimePickerDialog timePicker = new TimePickerDialog(getContext(), (view, hourOfDay, minute) -> {
            // Проверяем доступное время
            if (hourOfDay < testType.getStartHour() || hourOfDay >= testType.getEndHour()) {
                Toast.makeText(getContext(),
                        "Этот анализ доступен с " + testType.getStartHour() + ":00 до " +
                                testType.getEndHour() + ":00", Toast.LENGTH_LONG).show();
                return;
            }

            bookTest(testType, hourOfDay, minute);
        },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true);

        timePicker.setTitle("Выберите время для " + testType.getName());
        timePicker.show();
    }

    private String getDaysString(List<Integer> days) {
        String[] dayNames = {"", "пн", "вт", "ср", "чт", "пт", "сб", "вс"};
        StringBuilder sb = new StringBuilder();
        for (Integer day : days) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(dayNames[day]);
        }
        return sb.toString();
    }

    private void bookTest(TestType testType, int hour, int minute) {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        Map<String, Object> testBooking = new HashMap<>();
        testBooking.put("testId", testType.getId());
        testBooking.put("testName", testType.getName());
        testBooking.put("userId", FirebaseAuth.getInstance().getCurrentUser().getUid());
        testBooking.put("bookingTime", new Timestamp(calendar.getTime()));
        testBooking.put("status", "booked");
        testBooking.put("createdAt", FieldValue.serverTimestamp());

        db.collection("testBookings")
                .add(testBooking)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(getContext(),
                            "Вы записаны на " + testType.getName() + " в " +
                                    String.format("%02d:%02d", hour, minute),
                            Toast.LENGTH_SHORT).show();
                });
    }
}


