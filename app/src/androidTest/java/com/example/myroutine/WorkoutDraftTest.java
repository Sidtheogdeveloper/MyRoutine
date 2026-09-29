package com.example.myroutine;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import androidx.test.platform.app.InstrumentationRegistry;
import com.example.myroutine.ui.workout.ActiveWorkoutViewModel;
import org.junit.Test;
import static org.junit.Assert.*;

public class WorkoutDraftTest {
    /** Keep fixtures separate from the athlete's own draft and preferences. */
    private static class TestApplication extends Application {
        TestApplication(Context c){attachBaseContext(c);}
        @Override public SharedPreferences getSharedPreferences(String name,int mode){return super.getSharedPreferences("test_"+name,mode);}
    }
    private final String fixture="{\"start\":1000,\"name\":\"Recovered push day\",\"notes\":\"Controlled tempo\",\"rest\":0,\"exercises\":[{\"id\":1,\"name\":\"Bench\",\"muscle\":\"Chest\",\"equipment\":\"Barbell\",\"met\":6,\"sets\":[{\"reps\":8,\"weight\":60,\"complete\":true,\"type\":\"Drop set\",\"rpe\":8.5}]}]}";
    @Test public void completedSetsAndNotesSurviveViewModelRecreation(){
        TestApplication app=new TestApplication(InstrumentationRegistry.getInstrumentation().getTargetContext());
        SharedPreferences p=app.getSharedPreferences("workout_draft",0);p.edit().putString("session",fixture).commit();
        try{InstrumentationRegistry.getInstrumentation().runOnMainSync(()->{
            ActiveWorkoutViewModel vm=new ActiveWorkoutViewModel(app);assertEquals(1,vm.completedCount());assertEquals(480,vm.completedVolume(),0);
            assertEquals("Controlled tempo",vm.getNotes());assertEquals("Drop set",vm.getExerciseList().getValue().get(0).sets.get(0).type);
            vm.setNotes("Restored and updated");ActiveWorkoutViewModel recovered=new ActiveWorkoutViewModel(app);
            assertEquals("Restored and updated",recovered.getNotes());assertEquals(8.5,recovered.getExerciseList().getValue().get(0).sets.get(0).rpe,0);
            recovered.discard();recovered.persist();assertFalse(p.contains("session"));
        });}finally{p.edit().clear().commit();}
    }
    @Test public void pausedSessionKeepsItsElapsedTimeAcrossRecreation(){
        TestApplication app=new TestApplication(InstrumentationRegistry.getInstrumentation().getTargetContext());
        SharedPreferences p=app.getSharedPreferences("workout_draft",0);long now=System.currentTimeMillis();
        p.edit().putString("session",fixture.replace("\"start\":1000","\"start\":"+(now-120000)).replace("\"rest\":0","\"rest\":0,\"pausedAt\":"+(now-60000)+",\"pausedMillis\":0")).commit();
        try{InstrumentationRegistry.getInstrumentation().runOnMainSync(()->{
            ActiveWorkoutViewModel vm=new ActiveWorkoutViewModel(app);assertEquals(60,vm.elapsedSeconds());vm.resumeSession();assertTrue(vm.elapsedSeconds()>=60&&vm.elapsedSeconds()<63);
            ActiveWorkoutViewModel recovered=new ActiveWorkoutViewModel(app);assertTrue(recovered.elapsedSeconds()>=60&&recovered.elapsedSeconds()<63);
        });}finally{p.edit().clear().commit();}
    }
}
