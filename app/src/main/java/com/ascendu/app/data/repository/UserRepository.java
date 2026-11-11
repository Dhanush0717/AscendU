package com.ascendu.app.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.ascendu.app.data.local.dao.UserProfileDao;
import com.ascendu.app.data.local.database.AppDatabase;
import com.ascendu.app.data.local.entity.UserProfile;
import com.ascendu.app.util.AppExecutors;

public class UserRepository {

    private final UserProfileDao profileDao;
    private final AppExecutors executors;

    public UserRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        this.profileDao = db.userProfileDao();
        this.executors = AppExecutors.getInstance();
    }

    public LiveData<UserProfile> getUserProfile() {
        return profileDao.getUserProfile();
    }

    public void saveProfile(UserProfile profile) {
        executors.diskIO().execute(() -> profileDao.insertOrUpdate(profile));
    }
}
