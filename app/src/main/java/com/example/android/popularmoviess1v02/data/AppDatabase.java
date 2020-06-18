package com.example.android.popularmoviess1v02.data;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import android.util.Log;

@Database(entities = {MovieEntry.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private final static String LOG_TAG = AppDatabase.class.getSimpleName();
    private final static Object LOCK = new Object();
    private final static String DATABASE_NAME = "movieslist";
    private static AppDatabase sInstance;

    public static AppDatabase getInstance(Context context){
        if(sInstance == null){
            synchronized (LOCK){
                Log.d(LOG_TAG, "Creating new database instance.");
                sInstance = Room.databaseBuilder(context.getApplicationContext(),
                        AppDatabase.class, AppDatabase.DATABASE_NAME)
                        .build();

            }
        }

        Log.d(LOG_TAG, "Getting the database instance.");
        return sInstance;
    }

    public abstract MovieDao movieDao();
}
