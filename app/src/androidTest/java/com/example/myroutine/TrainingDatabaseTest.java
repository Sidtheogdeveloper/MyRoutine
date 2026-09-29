package com.example.myroutine;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.room.Room;
import com.example.myroutine.data.*;
import com.example.myroutine.data.entity.*;
import org.junit.Test;
import static org.junit.Assert.*;
public class TrainingDatabaseTest {
    @Test public void migrationPreservesWorkoutsAndSets() {
        Context c=InstrumentationRegistry.getInstrumentation().getTargetContext();String name="training_migration_test";
        c.deleteDatabase(name);
        try {
            SQLiteDatabase old=c.openOrCreateDatabase(name,0,null);
            old.execSQL("CREATE TABLE IF NOT EXISTS `exercises` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT, `muscleGroup` TEXT, `secondaryMuscles` TEXT, `movementPattern` TEXT, `equipment` TEXT, `unilateral` INTEGER NOT NULL, `compoundOrIsolation` TEXT, `difficulty` TEXT, `metValue` REAL NOT NULL)");
            old.execSQL("CREATE TABLE IF NOT EXISTS `workouts` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT, `timestamp` INTEGER NOT NULL, `durationSeconds` INTEGER NOT NULL, `caloriesBurnt` REAL NOT NULL)");
            old.execSQL("CREATE TABLE IF NOT EXISTS `workout_sets` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `workoutId` INTEGER NOT NULL, `exerciseId` INTEGER NOT NULL, `reps` INTEGER NOT NULL, `weight` REAL NOT NULL, FOREIGN KEY(`workoutId`) REFERENCES `workouts`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE , FOREIGN KEY(`exerciseId`) REFERENCES `exercises`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
            old.execSQL("CREATE INDEX IF NOT EXISTS `index_workout_sets_workoutId` ON `workout_sets` (`workoutId`)");
            old.execSQL("CREATE INDEX IF NOT EXISTS `index_workout_sets_exerciseId` ON `workout_sets` (`exerciseId`)");
            old.execSQL("CREATE TABLE IF NOT EXISTS `user_profile` (`id` INTEGER NOT NULL, `name` TEXT, `age` INTEGER NOT NULL, `gender` TEXT, `heightCm` REAL NOT NULL, `weightKg` REAL NOT NULL, `bodyFatPercentage` REAL NOT NULL, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, PRIMARY KEY(`id`))");
            old.execSQL("CREATE TABLE IF NOT EXISTS `weekly_routines` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `dayOfWeek` INTEGER NOT NULL, `routineName` TEXT, `isRestDay` INTEGER NOT NULL, `effectiveFrom` INTEGER NOT NULL)");
            old.execSQL("CREATE TABLE IF NOT EXISTS `routine_exercises` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `routineId` INTEGER NOT NULL, `exerciseId` INTEGER NOT NULL, `targetSets` INTEGER NOT NULL, `targetReps` INTEGER NOT NULL, `orderIndex` INTEGER NOT NULL, FOREIGN KEY(`routineId`) REFERENCES `weekly_routines`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE , FOREIGN KEY(`exerciseId`) REFERENCES `exercises`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
            old.execSQL("CREATE INDEX IF NOT EXISTS `index_routine_exercises_routineId` ON `routine_exercises` (`routineId`)");
            old.execSQL("CREATE INDEX IF NOT EXISTS `index_routine_exercises_exerciseId` ON `routine_exercises` (`exerciseId`)");
            old.execSQL("INSERT INTO exercises (id,name,muscleGroup,unilateral,metValue) VALUES(1,'Bench','Chest',0,6)");
            old.execSQL("INSERT INTO workouts (id,name,timestamp,durationSeconds,caloriesBurnt) VALUES(1,'Original workout',1000,1200,100)");
            old.execSQL("INSERT INTO workout_sets (id,workoutId,exerciseId,reps,weight) VALUES(1,1,1,8,60)");
            old.setVersion(3);old.close();
            AppDatabase db=Room.databaseBuilder(c,AppDatabase.class,name).addMigrations(AppDatabase.MIGRATION_3_4).build();
            try {
                Workout w=db.workoutDao().getById(1);WorkoutSet set=db.workoutSetDao().getSetsForWorkoutSync(1).get(0);
                assertEquals("Original workout",w.getName());assertEquals(1200,w.getDurationSeconds());assertEquals("",w.getNotes());
                assertEquals(8,set.getReps());assertEquals(60,set.getWeight(),0);assertEquals("Working",set.getSetType());assertEquals(0,set.getRpe(),0);
            } finally {db.close();}
        } finally {c.deleteDatabase(name);}
    }
    @Test public void failedSetInsertionRollsBackWholeSession() {
        Context c=InstrumentationRegistry.getInstrumentation().getTargetContext();
        AppDatabase db=Room.inMemoryDatabaseBuilder(c,AppDatabase.class).build();
        try {
            try {
                db.runInTransaction(()->{long id=db.workoutDao().insert(new Workout("Should roll back",100));db.workoutSetDao().insert(new WorkoutSet(id,999,8,60));});
                fail("Invalid exercise should violate the foreign key");
            }catch(android.database.sqlite.SQLiteConstraintException expected){}
            assertTrue(db.workoutDao().getWorkoutsBetweenSync(0,1000).isEmpty());
        } finally {db.close();}
    }
}
