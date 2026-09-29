package com.example.myroutine.ui.onboarding;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myroutine.MainActivity;
import com.example.myroutine.R;
import com.example.myroutine.data.AppDatabase;
import com.example.myroutine.data.WorkoutRepository;
import com.example.myroutine.data.entity.UserProfile;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

/**
 * First-launch onboarding screen that collects the user's physical
 * profile (name, age, gender, height, weight, optional body fat).
 * <p>
 * Once completed, saves the profile to Room and sets a SharedPreferences
 * flag so the user is never asked again.
 */
public class OnboardingActivity extends AppCompatActivity {

    public static final String PREFS_NAME = "myroutine_prefs";
    public static final String KEY_ONBOARDING_COMPLETE = "onboarding_complete";

    private TextInputEditText editName, editAge, editHeight, editWeight, editBodyFat;
    private AutoCompleteTextView dropdownGender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if onboarding was already completed
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        if (prefs.getBoolean(KEY_ONBOARDING_COMPLETE, false)) {
            goToMain();
            return;
        }

        setContentView(R.layout.activity_onboarding);
        com.example.myroutine.ui.ForgeUi.insets(this, ((android.view.ViewGroup)findViewById(android.R.id.content)).getChildAt(0));

        // ── Find views ──────────────────────────────────────────────────
        editName = findViewById(R.id.editOnboardName);
        editAge = findViewById(R.id.editOnboardAge);
        dropdownGender = findViewById(R.id.dropdownOnboardGender);
        editHeight = findViewById(R.id.editOnboardHeight);
        editWeight = findViewById(R.id.editOnboardWeight);
        editBodyFat = findViewById(R.id.editOnboardBodyFat);
        MaterialButton btnGetStarted = findViewById(R.id.btnGetStarted);

        // ── Gender dropdown ─────────────────────────────────────────────
        String[] genders = {
            getString(R.string.gender_male),
            getString(R.string.gender_female),
            getString(R.string.gender_other)
        };
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, genders);
        dropdownGender.setAdapter(genderAdapter);

        // ── Get Started button ──────────────────────────────────────────
        btnGetStarted.setOnClickListener(v -> attemptSave());
    }

    private void attemptSave() {
        String name = getText(editName);
        String ageStr = getText(editAge);
        String gender = dropdownGender.getText() != null ? dropdownGender.getText().toString().trim() : "";
        String heightStr = getText(editHeight);
        String weightStr = getText(editWeight);
        String bfStr = getText(editBodyFat);

        // Validate required fields
        if (name.isEmpty()) { editName.setError(getString(R.string.field_required)); return; }
        if (ageStr.isEmpty()) { editAge.setError(getString(R.string.field_required)); return; }
        if (gender.isEmpty()) { Toast.makeText(this, R.string.field_required, Toast.LENGTH_SHORT).show(); return; }
        if (heightStr.isEmpty()) { editHeight.setError(getString(R.string.field_required)); return; }
        if (weightStr.isEmpty()) { editWeight.setError(getString(R.string.field_required)); return; }

        try {
            int age = Integer.parseInt(ageStr);
            double height = Double.parseDouble(heightStr);
            double weight = Double.parseDouble(weightStr);
            double bodyFat = bfStr.isEmpty() ? -1.0 : Double.parseDouble(bfStr);

            if (age < 1 || age > 120 || !Double.isFinite(height) || height < 50 || height > 300 || !Double.isFinite(weight) || weight <= 0 || weight > 500 || !Double.isFinite(bodyFat) || (bodyFat != -1 && (bodyFat < 0 || bodyFat > 100))) {
                Toast.makeText(this, "Check your age, height, weight and body fat values.", Toast.LENGTH_LONG).show();
                return;
            }

            // Save profile
            UserProfile profile = new UserProfile(name, age, gender, height, weight, bodyFat);
            WorkoutRepository repo = new WorkoutRepository(getApplication());
            repo.saveUserProfile(profile);

            // Mark onboarding complete
            getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                    .edit()
                    .putBoolean(KEY_ONBOARDING_COMPLETE, true)
                    .apply();

            goToMain();

        } catch (NumberFormatException e) {
            Toast.makeText(this, R.string.field_required, Toast.LENGTH_SHORT).show();
        }
    }

    private String getText(TextInputEditText edit) {
        return edit.getText() != null ? edit.getText().toString().trim() : "";
    }

    private void goToMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
