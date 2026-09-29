package com.example.myroutine;

import android.content.SharedPreferences;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.myroutine.ui.TrainingHubFragment;
import com.example.myroutine.ui.dashboard.DashboardFragment;
import com.example.myroutine.ui.onboarding.OnboardingActivity;
import com.example.myroutine.ui.profile.ProfileFragment;
import com.example.myroutine.ui.routines.RoutinesFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Main activity serving as the navigation host.
 * <p>
 * Contains a BottomNavigationView with 4 tabs:
 * Dashboard, Calendar, Routines, Profile.
 * <p>
 * On first launch, redirects to {@link OnboardingActivity} for profile setup.
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ── Check onboarding ────────────────────────────────────────────
        SharedPreferences prefs = getSharedPreferences(
                OnboardingActivity.PREFS_NAME, MODE_PRIVATE);
        if (!prefs.getBoolean(OnboardingActivity.KEY_ONBOARDING_COMPLETE, false)) {
            startActivity(new Intent(this, OnboardingActivity.class));
            finish();
            return;
        }

        EdgeToEdge.enable(this);
        com.example.myroutine.ui.ForgeUi.darkBars(this);
        setContentView(R.layout.activity_main);

        // Edge-to-edge inset handling
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        // ── Bottom Navigation ───────────────────────────────────────────
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setItemActiveIndicatorColor(android.content.res.ColorStateList.valueOf(com.example.myroutine.ui.ForgeUi.LINE));

        // Show Dashboard by default
        if (savedInstanceState == null) {
            loadFragment(TrainingHubFragment.create(0));
        }

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment fragment = null;
            int id = item.getItemId();

            if (id == R.id.nav_dashboard) {
                fragment = TrainingHubFragment.create(0);
            } else if (id == R.id.nav_calendar) {
                fragment = TrainingHubFragment.create(2);
            } else if (id == R.id.nav_routines) {
                fragment = TrainingHubFragment.create(1);
            } else if (id == R.id.nav_quests) {
                fragment = TrainingHubFragment.create(3);
            } else if (id == R.id.nav_profile) {
                fragment = TrainingHubFragment.create(4);
            }

            if (fragment != null) {
                loadFragment(fragment);
                return true;
            }
            return false;
        });
    }

    /**
     * Replace the fragment container with the given fragment.
     */
    public void selectTab(int destination) {
        int[] tabs={R.id.nav_dashboard,R.id.nav_routines,R.id.nav_calendar,R.id.nav_quests,R.id.nav_profile};
        ((BottomNavigationView)findViewById(R.id.bottomNav)).setSelectedItemId(tabs[destination]);
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }
}