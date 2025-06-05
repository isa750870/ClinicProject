package com.example.privateclinic.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.privateclinic.R;
import com.example.privateclinic.models.TestBooking;

import java.util.List;

public class TestHistoryAdapter extends RecyclerView.Adapter<TestHistoryAdapter.HistoryViewHolder> {
    private List<TestBooking> testBookings;

    public TestHistoryAdapter(List<TestBooking> testBookings) {
        this.testBookings = testBookings;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_test_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        TestBooking booking = testBookings.get(position);
        holder.nameText.setText(booking.getTestName());
        holder.timeText.setText(booking.getFormattedTime());
        holder.statusText.setText(booking.getStatus());
    }

    @Override
    public int getItemCount() {
        return testBookings.size();
    }

    static class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView nameText, timeText, statusText;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.historyTestName);
            timeText = itemView.findViewById(R.id.historyTestTime);
            statusText = itemView.findViewById(R.id.historyTestStatus);
        }
    }
}