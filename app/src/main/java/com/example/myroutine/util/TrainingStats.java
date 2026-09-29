package com.example.myroutine.util;

import com.example.myroutine.data.entity.*;
import java.util.*;

/** Deterministic statistics from saved sessions; no seeded or invented activity. */
public final class TrainingStats {
    public int sessions, sets, weekSessions, weekSets, xp, activeWeeks;
    public double volume, weekVolume;
    public final double[] dailyVolume=new double[7];
    public final boolean[] trained=new boolean[7];
    public final Map<Integer,Double> bestWeight=new HashMap<>(), bestE1rm=new HashMap<>();
    public final Map<String,Integer> muscles=new TreeMap<>();
    public final Map<Long,List<WorkoutSet>> byWorkout=new HashMap<>();
    public static long weekStart(long now) {
        Calendar c=Calendar.getInstance();c.setTimeInMillis(now);
        c.set(Calendar.HOUR_OF_DAY,0);c.set(Calendar.MINUTE,0);c.set(Calendar.SECOND,0);c.set(Calendar.MILLISECOND,0);
        c.add(Calendar.DATE,-((c.get(Calendar.DAY_OF_WEEK)+5)%7));return c.getTimeInMillis();
    }
    public static double e1rm(double weight,int reps) { return reps>0&&reps<=12&&weight>0 ? (reps==1?weight:weight*(1+reps/30.0)):0; }
    public static int sessionXp(int completedSets) { return completedSets<=0?0:100+Math.min(completedSets,40)*10; }
    public int level() { return 1+xp/500; }
    public String rank() { int l=level();return l<5?"Rookie":l<10?"Contender":l<20?"Challenger":l<35?"Elite":"Legend"; }
    public static TrainingStats calculate(List<Workout> workouts,List<WorkoutSet> allSets,List<Exercise> exercises,long now) {
        TrainingStats s=new TrainingStats();Map<Integer,Exercise> library=new HashMap<>();
        for(Exercise e:exercises)library.put(e.getId(),e);
        for(WorkoutSet set:allSets)s.byWorkout.computeIfAbsent(set.getWorkoutId(),k->new ArrayList<>()).add(set);
        long start=weekStart(now);Set<Long> weeks=new HashSet<>();
        for(Workout w:workouts) {
            List<WorkoutSet> rows=s.byWorkout.getOrDefault(w.getId(),Collections.emptyList());if(rows.isEmpty())continue;
            s.sessions++;s.xp+=sessionXp(rows.size());weeks.add(weekStart(w.getTimestamp()));
            boolean inWeek=w.getTimestamp()>=start&&w.getTimestamp()<=now;
            Calendar c=Calendar.getInstance();c.setTimeInMillis(w.getTimestamp());int day=(c.get(Calendar.DAY_OF_WEEK)+5)%7;
            if(inWeek){s.weekSessions++;s.trained[day]=true;}
            for(WorkoutSet set:rows) {
                s.sets++;double v=set.getReps()*set.getWeight();s.volume+=v;
                if(inWeek){s.weekSets++;s.weekVolume+=v;s.dailyVolume[day]+=v;
                    Exercise e=library.get(set.getExerciseId());if(e!=null)s.muscles.merge(e.getMuscleGroup(),1,Integer::sum);}
                s.bestWeight.merge(set.getExerciseId(),set.getWeight(),Math::max);
                s.bestE1rm.merge(set.getExerciseId(),e1rm(set.getWeight(),set.getReps()),Math::max);
            }
        }
        long current=start;if(!weeks.contains(current))current=previousWeek(current);
        while(weeks.contains(current)){s.activeWeeks++;current=previousWeek(current);}
        return s;
    }
    private static long previousWeek(long t){Calendar c=Calendar.getInstance();c.setTimeInMillis(t);c.add(Calendar.DATE,-7);return c.getTimeInMillis();}
}
