package com.example.myroutine.ui.profile;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.myroutine.data.WorkoutRepository;
import com.example.myroutine.data.entity.UserProfile;

/**
 * ViewModel for the Profile screen.
 * Observes and manages the user's profile data.
 */
public class ProfileViewModel extends AndroidViewModel {

    private final WorkoutRepository repository;
    private final LiveData<UserProfile> profile;

    public ProfileViewModel(@NonNull Application application) {
        super(application);
        repository = new WorkoutRepository(application);
        profile = repository.getUserProfile();
    }

    public LiveData<UserProfile> getProfile() {
        return profile;
    }

    /** Save or update the user profile. */
    public void saveProfile(UserProfile profile) {
        profile.setUpdatedAt(System.currentTimeMillis());
        repository.saveUserProfile(profile);
    }
}
