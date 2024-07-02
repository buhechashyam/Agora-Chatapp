package com.example.agorachatapp.room;

import android.app.Application;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {User.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract UserDao userDao();
    public static AppDatabase appDatabase = null;

    public static AppDatabase getAppDatabase(Application application) {

        if (appDatabase == null){
            appDatabase = Room.databaseBuilder(application, AppDatabase.class,"users-db").allowMainThreadQueries().build();
            return appDatabase;
        }
        return appDatabase;
    }
}
