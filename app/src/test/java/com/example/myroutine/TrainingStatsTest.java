package com.example.myroutine;

import com.example.myroutine.data.entity.*;
import com.example.myroutine.util.TrainingStats;
import org.junit.Test;
import java.util.*;
import static org.junit.Assert.*;

public class TrainingStatsTest {
    private long at(int year,int month,int day,int hour){Calendar c=Calendar.getInstance();c.clear();c.set(year,month-1,day,hour,0);return c.getTimeInMillis();}
    private Workout workout(long id,long t){Workout w=new Workout("Test session",t);w.setId(id);return w;}
    private Exercise exercise(){Exercise e=new Exercise("Bench","Chest",6);e.setId(1);return e;}
    @Test public void emptyHistoryHasNoInventedActivity(){TrainingStats s=TrainingStats.calculate(Collections.emptyList(),Collections.emptyList(),Collections.emptyList(),System.currentTimeMillis());assertEquals(0,s.sessions);assertEquals(0,s.xp);assertEquals(1,s.level());assertEquals(0,s.volume,0);}
    @Test public void volumeAndRecordsUseActualSets(){Workout w=workout(1,at(2026,9,29,10));TrainingStats s=TrainingStats.calculate(Arrays.asList(w),Arrays.asList(new WorkoutSet(1,1,10,60),new WorkoutSet(1,1,8,65)),Arrays.asList(exercise()),at(2026,9,29,12));assertEquals(1120,s.volume,0);assertEquals(2,s.sets);assertEquals(120,s.xp);assertEquals(65,s.bestWeight.get(1),0);assertEquals(82.3333,s.bestE1rm.get(1),.001);assertEquals(2,(int)s.muscles.get("Chest"));}
    @Test public void weekStartsAtLocalMondayMidnight(){long now=at(2026,9,29,12);long monday=at(2026,9,28,0);assertEquals(monday,TrainingStats.weekStart(now));assertEquals(monday,TrainingStats.weekStart(at(2026,10,4,23)));}
    @Test public void priorWeekAndFutureWorkoutsStayOutsideThisWeek(){long now=at(2026,9,29,12);List<Workout> ws=Arrays.asList(workout(1,at(2026,9,27,23)),workout(2,at(2026,9,28,0)),workout(3,at(2026,10,1,12)));List<WorkoutSet> sets=Arrays.asList(new WorkoutSet(1,1,10,10),new WorkoutSet(2,1,10,20),new WorkoutSet(3,1,10,30));TrainingStats s=TrainingStats.calculate(ws,sets,Arrays.asList(exercise()),now);assertEquals(1,s.weekSessions);assertEquals(200,s.weekVolume,0);assertTrue(s.trained[0]);assertFalse(s.trained[3]);}
    @Test public void emptySessionsAndOrphanSetsDoNotEarnXp(){TrainingStats s=TrainingStats.calculate(Arrays.asList(workout(1,System.currentTimeMillis())),Arrays.asList(new WorkoutSet(2,1,10,60)),Arrays.asList(exercise()),System.currentTimeMillis());assertEquals(0,s.xp);assertEquals(0,s.sets);}
    @Test public void weeklyStreakAllowsRecoveryDays(){long now=at(2026,9,29,12);List<Workout> ws=Arrays.asList(workout(1,at(2026,9,29,10)),workout(2,at(2026,9,22,10)),workout(3,at(2026,9,15,10)));List<WorkoutSet> sets=Arrays.asList(new WorkoutSet(1,1,8,0),new WorkoutSet(2,1,8,0),new WorkoutSet(3,1,8,0));TrainingStats s=TrainingStats.calculate(ws,sets,Arrays.asList(exercise()),now);assertEquals(3,s.activeWeeks);assertEquals(0,s.volume,0);assertEquals(330,s.xp);}
    @Test public void completedPreviousWeekKeepsStreakAlive(){TrainingStats s=TrainingStats.calculate(Arrays.asList(workout(1,at(2026,9,22,10))),Arrays.asList(new WorkoutSet(1,1,8,20)),Arrays.asList(exercise()),at(2026,9,28,10));assertEquals(1,s.activeWeeks);}
    @Test public void repMaxIsBoundedToMeaningfulRepRange(){assertEquals(100,TrainingStats.e1rm(100,1),0);assertEquals(0,TrainingStats.e1rm(100,20),0);assertEquals(0,TrainingStats.e1rm(0,10),0);assertEquals(0,TrainingStats.e1rm(100,0),0);assertEquals(500,TrainingStats.sessionXp(100));assertEquals(0,TrainingStats.sessionXp(0));}
}
