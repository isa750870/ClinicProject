package com.example.privateclinic;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.privateclinic.databinding.ActivityMainBinding;
import com.example.privateclinic.MenuIDs;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();

        // Проверка авторизации
        if (mAuth.getCurrentUser() == null) {
            showLoginFragment();
        } else {
            showMainApp();
        }
    }

    private void showLoginFragment() {
        replaceFragment(new LoginFragment());
        binding.bottomNavigationView.setVisibility(View.GONE);
    }

    private void showMainApp() {
        replaceFragment(new HomeFragment());
        binding.bottomNavigationView.setVisibility(View.VISIBLE);
        setupBottomNavigation();
        binding.bottomNavigationView.setSelectedItemId(R.id.nav_home);
    }

    private void setupBottomNavigation() {
        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                replaceFragment(new HomeFragment());
                return true;
            } else if (itemId == R.id.nav_tabappointment) {
                replaceFragment(new AppointmentTabsFragment());
                return true;
            } else if (itemId == R.id.nav_appointment) {
                replaceFragment(new AppointmentFragment());
                return true;
            } else if (itemId == R.id.nav_tests) {
                replaceFragment(new TestsFragment());
                return true;
            } else if (itemId == R.id.nav_profile) {
                replaceFragment(new ProfileFragment());
                return true;
            }
            return false;
        });
    }

    public void replaceFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame_container, fragment)
                .commit();
    }

    public void showBottomNavigation(boolean show) {
        binding.bottomNavigationView.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onBackPressed() {
        // Если открыт фрагмент редактирования, возвращаемся к просмотру профиля
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.frame_container);
        if (currentFragment instanceof EditProfileFragment) {
            replaceFragment(new ProfileFragment());
        } else {
            super.onBackPressed();
        }
    }
    public void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}