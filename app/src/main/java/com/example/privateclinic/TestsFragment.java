package com.example.privateclinic;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class TestsFragment extends Fragment {
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private RecyclerView testsRecyclerView, historyRecyclerView;
    private TabLayout testsTabLayout;
    private ViewPager2 testsViewPager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tests, container, false);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        testsTabLayout = view.findViewById(R.id.testsTabLayout);
        testsViewPager = view.findViewById(R.id.testsViewPager);

        setupViewPager();

        return view;
    }

    private void setupViewPager() {
        TestsPagerAdapter adapter = new TestsPagerAdapter(this);
        testsViewPager.setAdapter(adapter);

        new TabLayoutMediator(testsTabLayout, testsViewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Записаться");
                    break;
                case 1:
                    tab.setText("История");
                    break;
            }
        }).attach();
    }

    private static class TestsPagerAdapter extends FragmentStateAdapter {
        public TestsPagerAdapter(@NonNull Fragment fragment) {
            super(fragment);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0: return new BookTestsFragment();
                case 1: return new TestsHistoryFragment();
                default: return new BookTestsFragment();
            }
        }

        @Override
        public int getItemCount() {
            return 2;
        }
    }
}