package com.example.myroutine.data;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.myroutine.data.dao.ExerciseDao;
import com.example.myroutine.data.dao.RoutineExerciseDao;
import com.example.myroutine.data.dao.UserProfileDao;
import com.example.myroutine.data.dao.WeeklyRoutineDao;
import com.example.myroutine.data.dao.WorkoutDao;
import com.example.myroutine.data.dao.WorkoutSetDao;
import com.example.myroutine.data.entity.Exercise;
import com.example.myroutine.data.entity.RoutineExercise;
import com.example.myroutine.data.entity.UserProfile;
import com.example.myroutine.data.entity.WeeklyRoutine;
import com.example.myroutine.data.entity.Workout;
import com.example.myroutine.data.entity.WorkoutSet;
import com.example.myroutine.util.ExerciseCsvParser;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The Room database for the MyRoutine application (V3).
 * <p>
 * Includes entities for exercises, workouts, sets, user profile,
 * weekly routines, and routine exercises.
 * <p>
 * Pre-populates the exercise library with the full workout database CSV
 * from app assets on initial launch or when empty.
 */
@Database(
    entities = {
        Exercise.class,
        Workout.class,
        WorkoutSet.class,
        UserProfile.class,
        WeeklyRoutine.class,
        RoutineExercise.class
    },
    version = 4,
    exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {
    public static final androidx.room.migration.Migration MIGRATION_3_4 = new androidx.room.migration.Migration(3,4) {
        @Override public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("ALTER TABLE workout_sets ADD COLUMN setType TEXT DEFAULT 'Working'");
            db.execSQL("ALTER TABLE workout_sets ADD COLUMN rpe REAL NOT NULL DEFAULT 0");
            db.execSQL("ALTER TABLE workouts ADD COLUMN notes TEXT DEFAULT ''");
        }
    };


    // ── Abstract DAO accessors ──────────────────────────────────────────

    public abstract ExerciseDao exerciseDao();
    public abstract WorkoutDao workoutDao();
    public abstract WorkoutSetDao workoutSetDao();
    public abstract UserProfileDao userProfileDao();
    public abstract WeeklyRoutineDao weeklyRoutineDao();
    public abstract RoutineExerciseDao routineExerciseDao();

    // ── Singleton ───────────────────────────────────────────────────────

    private static volatile AppDatabase INSTANCE;

    /** Fixed-size thread pool for all database write operations. */
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(4);

    /**
     * Returns the singleton database instance, creating it if necessary.
     *
     * @param context Application context (avoids Activity leaks).
     */
    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    Context appContext = context.getApplicationContext();
                    INSTANCE = Room.databaseBuilder(
                            appContext,
                            AppDatabase.class,
                            "myroutine_database"
                    )
                    .addMigrations(MIGRATION_3_4)
                    .addCallback(new RoomDatabase.Callback() {
                        @Override
                        public void onCreate(@NonNull SupportSQLiteDatabase db) {
                            super.onCreate(db);
                            seedDatabase(appContext);
                        }

                        @Override
                        public void onOpen(@NonNull SupportSQLiteDatabase db) {
                            super.onOpen(db);
                            // Safety self-heal: if exercises table is ever empty, seed from CSV
                            databaseWriteExecutor.execute(() -> {
                                if (INSTANCE != null && INSTANCE.exerciseDao().getCount() == 0) {
                                    seedDatabase(appContext);
                                }
                            });
                        }
                    })
                    .build();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Seeds the exercise library from assets/exercises.csv.
     * Runs on the write executor so it doesn't block the main thread.
     */
    private static void seedDatabase(Context context) {
        databaseWriteExecutor.execute(() -> {
            if (INSTANCE != null) {
                ExerciseDao dao = INSTANCE.exerciseDao();
                List<Exercise> exercises = ExerciseCsvParser.parseFromAssets(context);
                if (exercises != null && !exercises.isEmpty()) {
                    INSTANCE.runInTransaction(() -> {
                        if (dao.getCount() == 0) dao.insertAll(exercises);
                    });
                }
            }
        });
    }
}
