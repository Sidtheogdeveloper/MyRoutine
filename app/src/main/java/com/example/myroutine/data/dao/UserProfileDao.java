package com.example.myroutine.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.myroutine.data.entity.UserProfile;

/**
 * Data Access Object for the {@link UserProfile} entity.
 * Since the profile is a singleton (always id=1), most queries
 * are parameterless.
 */
@Dao
public interface UserProfileDao {

    /** Insert or replace the user profile (upsert behavior). */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrUpdate(UserProfile profile);

    /** Update an existing profile. */
    @Update
    void update(UserProfile profile);

    /** Observe the user's profile (LiveData for reactive UI updates). */
    @Query("SELECT * FROM user_profile WHERE id = 1")
    LiveData<UserProfile> getProfile();

    /** Get the profile synchronously (for background calorie calculations). */
    @Query("SELECT * FROM user_profile WHERE id = 1")
    UserProfile getProfileSync();

    /** Check if a profile exists (used to determine if onboarding is needed). */
    @Query("SELECT COUNT(*) FROM user_profile")
    int getProfileCount();
}
