package com.example.myroutine.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.myroutine.R;
import com.example.myroutine.data.entity.UserProfile;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

/**
 * Profile screen — view and edit user details.
 * Pre-fills fields from the current profile and saves on button click.
 */
public class ProfileFragment extends Fragment {

    private ProfileViewModel viewModel;
    private TextInputEditText editName, editAge, editHeight, editWeight, editBodyFat;
    private AutoCompleteTextView dropdownGender;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        editName = view.findViewById(R.id.editProfileName);
        editAge = view.findViewById(R.id.editProfileAge);
        dropdownGender = view.findViewById(R.id.dropdownGender);
        editHeight = view.findViewById(R.id.editProfileHeight);
        editWeight = view.findViewById(R.id.editProfileWeight);
        editBodyFat = view.findViewById(R.id.editProfileBodyFat);
        MaterialButton btnSave = view.findViewById(R.id.btnSaveProfile);

        // Gender dropdown
        String[] genders = {getString(R.string.gender_male), getString(R.string.gender_female), getString(R.string.gender_other)};
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, genders);
        dropdownGender.setAdapter(genderAdapter);

        // Pre-fill from existing profile
        viewModel.getProfile().observe(getViewLifecycleOwner(), profile -> {
            if (profile != null) {
                editName.setText(profile.getName());
                editAge.setText(String.valueOf(profile.getAge()));
                dropdownGender.setText(profile.getGender(), false);
                editHeight.setText(String.valueOf(profile.getHeightCm()));
                editWeight.setText(String.valueOf(profile.getWeightKg()));
                if (profile.hasBodyFat()) {
                    editBodyFat.setText(String.valueOf(profile.getBodyFatPercentage()));
                }
            }
        });

        // Save
        btnSave.setOnClickListener(v -> saveProfile());
    }

    private void saveProfile() {
        String name = editName.getText() != null ? editName.getText().toString().trim() : "";
        String ageStr = editAge.getText() != null ? editAge.getText().toString().trim() : "";
        String gender = dropdownGender.getText() != null ? dropdownGender.getText().toString().trim() : "";
        String heightStr = editHeight.getText() != null ? editHeight.getText().toString().trim() : "";
        String weightStr = editWeight.getText() != null ? editWeight.getText().toString().trim() : "";
        String bfStr = editBodyFat.getText() != null ? editBodyFat.getText().toString().trim() : "";

        if (name.isEmpty() || ageStr.isEmpty() || gender.isEmpty() || heightStr.isEmpty() || weightStr.isEmpty()) {
            Toast.makeText(requireContext(), R.string.field_required, Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int age = Integer.parseInt(ageStr);
            double height = Double.parseDouble(heightStr);
            double weight = Double.parseDouble(weightStr);
            double bodyFat = bfStr.isEmpty() ? -1.0 : Double.parseDouble(bfStr);

            if (age < 1 || age > 120 || !Double.isFinite(height) || height < 50 || height > 300 || !Double.isFinite(weight) || weight <= 0 || weight > 500 || !Double.isFinite(bodyFat) || (bodyFat != -1 && (bodyFat < 0 || bodyFat > 100))) {
                Toast.makeText(requireContext(), "Check your age, height, weight and body fat values.", Toast.LENGTH_LONG).show();
                return;
            }

            UserProfile profile = new UserProfile(name, age, gender, height, weight, bodyFat);
            viewModel.saveProfile(profile);
            Toast.makeText(requireContext(), R.string.profile_saved, Toast.LENGTH_SHORT).show();
        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(), R.string.field_required, Toast.LENGTH_SHORT).show();
        }
    }
}
