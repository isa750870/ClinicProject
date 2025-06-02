package com.example.privateclinic;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TestsAdapter extends RecyclerView.Adapter<TestsAdapter.TestViewHolder> {
    private List<TestType> testTypes;
    private OnTestClickListener listener;

    public TestsAdapter(List<TestType> testTypes, OnTestClickListener listener) {
        this.testTypes = testTypes;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_test_type, parent, false);
        return new TestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TestViewHolder holder, int position) {
        TestType testType = testTypes.get(position);
        holder.nameText.setText(testType.getName());
        holder.descriptionText.setText(testType.getDescription());

        String availability = "Доступно: " +
                holder.getDaysString(testType.getAvailableDays()) + ", " +
                testType.getStartHour() + ":00-" + testType.getEndHour() + ":00";

        holder.availabilityText.setText(availability);
        holder.bookButton.setOnClickListener(v -> listener.onTestClick(testType));
    }

    @Override
    public int getItemCount() {
        return testTypes.size();
    }

    static class TestViewHolder extends RecyclerView.ViewHolder {
        TextView nameText, descriptionText, availabilityText;
        Button bookButton;

        public TestViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.testNameText);
            descriptionText = itemView.findViewById(R.id.testDescriptionText);
            availabilityText = itemView.findViewById(R.id.testAvailabilityText);
            bookButton = itemView.findViewById(R.id.bookTestButton);
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
    }

    public interface OnTestClickListener {
        void onTestClick(TestType testType);
    }
}