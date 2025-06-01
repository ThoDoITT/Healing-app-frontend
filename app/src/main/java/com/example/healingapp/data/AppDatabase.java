package com.example.healingapp.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.healingapp.data.dao.RunningSessionDao;
import com.example.healingapp.data.dao.SleepDao;
import com.example.healingapp.data.models.sleep.SleepSession;
import com.example.healingapp.data.models.workout.RunningSession;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@Database(entities = {RunningSession.class, SleepSession.class}, version = 3, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract RunningSessionDao runDao();

    public abstract SleepDao sleepDao();
    private static volatile AppDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

//     ĐỊNH NGHĨA MIGRATION TỪ VERSION 1 SANG 2
    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Câu lệnh SQL để thêm cột mới 'creationDateMillis' vào bảng 'sleep_sessions'
            // INTEGER dùng để lưu trữ kiểu long (timestamp) trong SQLite
            // NOT NULL DEFAULT 0: đảm bảo cột này không null, và các hàng cũ sẽ có giá trị mặc định là 0
            database.execSQL("ALTER TABLE SleepSessions ADD COLUMN creationDateMillis INTEGER NOT NULL DEFAULT 0");
        }
    };
    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "healing_app_db")
//                            .fallbackToDestructiveMigration()
                            .addMigrations(MIGRATION_2_3)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    public void clearAllTablesData() {
        databaseWriteExecutor.execute(() -> {
            clearAllTables(); // This is a built-in Room method to delete all data

        });
    }

    // Optional: If you want to delete specific tables instead of all
    public void clearRunningSessionsTable() {
        databaseWriteExecutor.execute(() -> {
            runDao().deleteAllRunningSessions();
        });
    }

    public void clearSleepSessionsTable() {
        databaseWriteExecutor.execute(() -> {
            sleepDao().deleteAllSleepSessions();
        });
    }

}
