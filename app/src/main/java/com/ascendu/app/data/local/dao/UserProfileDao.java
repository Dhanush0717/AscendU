package com.ascendu.app.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.ascendu.app.data.local.entity.UserProfile;

@Dao
public interface UserProfileDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrUpdate(UserProfile profile);

    @Update
    void update(UserProfile profile);

    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    LiveData<UserProfile> getUserProfile();

    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    UserProfile getUserProfileDirect();
}
