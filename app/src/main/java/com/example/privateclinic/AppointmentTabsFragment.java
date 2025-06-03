package com.example.privateclinic;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class AppointmentTabsFragment extends Fragment {
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private AppointmentPagerAdapter pagerAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_appointment_tabs, container, false);

        tabLayout = view.findViewById(R.id.tabLayout);
        viewPager = view.findViewById(R.id.viewPager);

        pagerAdapter = new AppointmentPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Запись");
                    break;
                case 1:
                    tab.setText("История");
                    break;
            }
        }).attach();

        return view;
    }

    private static class AppointmentPagerAdapter extends FragmentStateAdapter {
        public AppointmentPagerAdapter(@NonNull Fragment fragment) {
            super(fragment);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0: return new BookAppointmentFragment();
                case 1: return new AppointmentHistoryFragment();
                default: return new BookAppointmentFragment();
            }
        }

        @Override
        public int getItemCount() {
            return 2;
        }
    }
}