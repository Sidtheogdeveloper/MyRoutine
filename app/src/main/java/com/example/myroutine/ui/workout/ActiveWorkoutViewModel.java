package com.example.myroutine.ui.workout;

import android.app.Application;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.lifecycle.*;
import com.example.myroutine.data.*;
import com.example.myroutine.data.entity.*;
import com.example.myroutine.util.CalorieCalculator;
import com.example.myroutine.util.TrainingStats;
import java.util.*;
import org.json.*;

/** Recoverable training session, with validated completed sets and atomic persistence. */
public class ActiveWorkoutViewModel extends AndroidViewModel {
    private final AppDatabase db;
    private final SharedPreferences draft;
    private final MutableLiveData<List<ExerciseWithSets>> exerciseList=new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> workoutSaved=new MutableLiveData<>(false);
    private final MutableLiveData<String> error=new MutableLiveData<>();
    private final MutableLiveData<Boolean> saving=new MutableLiveData<>(false);
    private long startTimestamp=System.currentTimeMillis(), restEnd=0, pausedAt=0, pausedMillis=0;
    private String workoutName="Freestyle session", notes="";
    private boolean configured=false, discarded=false;
    public int savedSets, savedXp, newRecords;
    public double savedVolume;
    public ActiveWorkoutViewModel(@NonNull Application app){super(app);db=AppDatabase.getInstance(app);draft=app.getSharedPreferences("workout_draft",0);restore();}
    public LiveData<List<ExerciseWithSets>> getExerciseList(){return exerciseList;}
    public LiveData<Boolean> getWorkoutSaved(){return workoutSaved;}
    public LiveData<Boolean> getSaving(){return saving;}
    public LiveData<String> getError(){return error;}
    public LiveData<List<Exercise>> getAllExercises(){return db.exerciseDao().getAll();}
    public String getWorkoutName(){return workoutName;}
    public void setWorkoutName(String name){workoutName=name;persist();}
    public String getNotes(){return notes;}
    public void setNotes(String value){notes=value;persist();}
    public long getStartTimestamp(){return startTimestamp;}
    public long elapsedSeconds(){return Math.max(0,((pausedAt>0?pausedAt:System.currentTimeMillis())-startTimestamp-pausedMillis)/1000);}
    public void pauseSession(){pausedAt=System.currentTimeMillis();restEnd=0;persist();}
    public void resumeSession(){if(pausedAt>0){pausedMillis+=Math.max(0,System.currentTimeMillis()-pausedAt);pausedAt=0;persist();}}
    public long getRestEnd(){return restEnd;}
    public void setRestEnd(long value){restEnd=value;persist();}
    public void configure(String template,long repeat){
        if(configured)return;configured=true;
        AppDatabase.databaseWriteExecutor.execute(()->{
            List<ExerciseWithSets> planned=new ArrayList<>();String name="Freestyle session";
            if(repeat>0){Workout w=db.workoutDao().getById(repeat);if(w!=null)name=w.getName();
                Map<Integer,ExerciseWithSets> group=new LinkedHashMap<>();
                for(WorkoutSet set:db.workoutSetDao().getSetsForWorkoutSync(repeat)){
                    Exercise e=db.exerciseDao().getById(set.getExerciseId());if(e==null)continue;
                    ExerciseWithSets entry=group.computeIfAbsent(e.getId(),k->new ExerciseWithSets(e));
                    SetData sd=new SetData();sd.reps=set.getReps();sd.weight=set.getWeight();sd.type=set.getSetType();entry.sets.add(sd);
                }planned.addAll(group.values());
            }else if(template==null){int day=(Calendar.getInstance().get(Calendar.DAY_OF_WEEK)+5)%7+1;
                WeeklyRoutine r=db.weeklyRoutineDao().getActiveRoutineForDay(day,System.currentTimeMillis());
                if(r!=null&&!r.isRestDay()){name=r.getRoutineName();for(RoutineExercise re:db.routineExerciseDao().getExercisesForRoutineSync(r.getId())){
                    Exercise e=db.exerciseDao().getById(re.getExerciseId());if(e==null)continue;ExerciseWithSets entry=new ExerciseWithSets(e);
                    for(int n=0;n<re.getTargetSets();n++){SetData sd=new SetData();sd.reps=re.getTargetReps();entry.sets.add(sd);}planned.add(entry);
                }}
            }else if(!template.equals("Freestyle")){
                name=template+" session";String[] names;
                switch(template){case "Push":names=new String[]{"Barbell Bench Press","Incline Dumbbell Bench Press","Dumbbell Lateral Raise","Triceps Pushdown"};break;
                case "Pull":names=new String[]{"Lat Pulldown","Barbell Row","Face Pull","Dumbbell Curl"};break;
                case "Legs":names=new String[]{"Barbell Back Squat","Romanian Deadlift","Leg Extension","Standing Calf Raise"};break;
                default:names=new String[]{"Barbell Back Squat","Barbell Bench Press","Lat Pulldown","Romanian Deadlift"};}
                List<Exercise> library=db.exerciseDao().getAllSync();for(String desired:names){Exercise selected=null;
                    for(Exercise e:library)if(e.getName().equalsIgnoreCase(desired)){selected=e;break;}
                    if(selected==null)for(Exercise e:library)if(e.getName().toLowerCase(Locale.ROOT).contains(desired.toLowerCase(Locale.ROOT))){selected=e;break;}
                    if(selected!=null){ExerciseWithSets entry=new ExerciseWithSets(selected);for(int n=0;n<3;n++){SetData sd=new SetData();sd.reps=8;entry.sets.add(sd);}planned.add(entry);}
                }
            }
            for(ExerciseWithSets entry:planned)fillPrevious(entry);
            final String finalName=name;new Handler(Looper.getMainLooper()).post(()->{if(exerciseList.getValue().isEmpty()){workoutName=finalName;exerciseList.setValue(planned);persist();}});
        });
    }
    private void fillPrevious(ExerciseWithSets entry){List<WorkoutSet> history=db.workoutSetDao().getExerciseHistory(entry.exercise.getId());if(history.isEmpty())return;
        WorkoutSet recent=history.get(0);entry.previous=String.format(Locale.getDefault(),"Last: %.1f kg × %d",recent.getWeight(),recent.getReps());
        for(SetData sd:entry.sets)if(sd.weight==0)sd.weight=recent.getWeight();
        for(WorkoutSet s:history)entry.previousBest=Math.max(entry.previousBest,s.getWeight());
    }
    public void addExercise(Exercise e){
        AppDatabase.databaseWriteExecutor.execute(()->{
            ExerciseWithSets entry=new ExerciseWithSets(e);entry.sets.add(new SetData());fillPrevious(entry);
            new Handler(Looper.getMainLooper()).post(()->{
                if(discarded||Boolean.TRUE.equals(workoutSaved.getValue()))return;
                List<ExerciseWithSets> list=exerciseList.getValue();for(ExerciseWithSets existing:list)if(existing.exercise.getId()==e.getId())return;
                list.add(entry);exerciseList.setValue(list);persist();
            });
        });
    }
    public void removeExercise(int p){List<ExerciseWithSets> l=exerciseList.getValue();if(p>=0&&p<l.size()){l.remove(p);exerciseList.setValue(l);persist();}}
    public void addSet(int p){List<ExerciseWithSets> l=exerciseList.getValue();if(p<0||p>=l.size())return;List<SetData> rows=l.get(p).sets;SetData sd=new SetData();if(!rows.isEmpty()){SetData last=rows.get(rows.size()-1);sd.reps=last.reps;sd.weight=last.weight;}rows.add(sd);exerciseList.setValue(l);persist();}
    public void removeSet(int p,int n){List<ExerciseWithSets> l=exerciseList.getValue();if(p>=0&&p<l.size()&&n>=0&&n<l.get(p).sets.size()){l.get(p).sets.remove(n);exerciseList.setValue(l);persist();}}
    private SetData at(int p,int n){List<ExerciseWithSets> l=exerciseList.getValue();return p>=0&&p<l.size()&&n>=0&&n<l.get(p).sets.size()?l.get(p).sets.get(n):null;}
    public void updateReps(int p,int n,int reps){SetData s=at(p,n);if(s!=null){s.reps=reps;s.complete=false;persist();}}
    public void updateWeight(int p,int n,double weight){SetData s=at(p,n);if(s!=null){s.weight=weight;s.complete=false;persist();}}
    public void changed(){persist();}
    public int completedCount(){int n=0;for(ExerciseWithSets e:exerciseList.getValue())for(SetData s:e.sets)if(s.complete)n++;return n;}
    public double completedVolume(){double v=0;for(ExerciseWithSets e:exerciseList.getValue())for(SetData s:e.sets)if(s.complete)v+=s.weight*s.reps;return v;}
    public boolean valid(SetData s){return s.reps>0&&s.reps<=1000&&Double.isFinite(s.weight)&&s.weight>=0&&s.weight<=10000&&Double.isFinite(s.rpe)&&s.rpe>=0&&s.rpe<=10;}
    public void finishWorkout(long seconds){
        if(Boolean.TRUE.equals(saving.getValue()))return;
        List<WorkoutSet> rows=new ArrayList<>();List<ExerciseWithSets> current=exerciseList.getValue();
        for(ExerciseWithSets e:current)for(SetData s:e.sets)if(s.complete){if(!valid(s)){error.setValue("Check reps, weight and RPE on completed sets.");return;}
            WorkoutSet row=new WorkoutSet(0,e.exercise.getId(),s.reps,s.weight);row.setSetType(s.type);row.setRpe(s.rpe);rows.add(row);}
        if(rows.isEmpty()){error.setValue("Complete at least one set using the check button.");return;}
        if(workoutName.trim().isEmpty())workoutName="Freestyle session";
        List<ExerciseWithSets> calorieSnapshot=new ArrayList<>();
        for(ExerciseWithSets entry:current){ExerciseWithSets copy=new ExerciseWithSets(entry.exercise);for(SetData sd:entry.sets)if(sd.complete){SetData item=new SetData();item.complete=true;item.reps=sd.reps;item.weight=sd.weight;copy.sets.add(item);}calorieSnapshot.add(copy);}
        saving.setValue(true);Workout w=new Workout(workoutName.trim(),startTimestamp);w.setDurationSeconds(Math.max(0,seconds));w.setNotes(notes);
        AppDatabase.databaseWriteExecutor.execute(()->{try{
            UserProfile p=db.userProfileDao().getProfileSync();w.setCaloriesBurnt(CalorieCalculator.calculateCalories(calorieSnapshot,p==null?70:p.getWeightKg(),seconds));
            Set<Integer> prs=new HashSet<>();for(WorkoutSet row:rows){double previous=0;for(WorkoutSet h:db.workoutSetDao().getExerciseHistory(row.getExerciseId()))previous=Math.max(previous,h.getWeight());if(previous>0&&row.getWeight()>previous)prs.add(row.getExerciseId());}
            db.runInTransaction(()->{long id=db.workoutDao().insert(w);for(WorkoutSet row:rows)row.setWorkoutId(id);db.workoutSetDao().insertAll(rows);});
            savedSets=rows.size();savedXp=TrainingStats.sessionXp(savedSets);savedVolume=0;for(WorkoutSet row:rows)savedVolume+=row.getWeight()*row.getReps();newRecords=prs.size();
            draft.edit().remove("session").commit();workoutSaved.postValue(true);
        }catch(Exception ex){saving.postValue(false);error.postValue("Couldn't save your session. Your draft is safe. Please try again.");}});
    }
    public void discard(){discarded=true;draft.edit().remove("session").apply();}
    public void persist(){if(discarded||Boolean.TRUE.equals(workoutSaved.getValue())||Boolean.TRUE.equals(saving.getValue()))return;try{
        JSONObject root=new JSONObject();root.put("start",startTimestamp);root.put("pausedAt",pausedAt);root.put("pausedMillis",pausedMillis);root.put("name",workoutName);root.put("notes",notes);root.put("rest",restEnd);JSONArray entries=new JSONArray();
        for(ExerciseWithSets e:exerciseList.getValue()){JSONObject item=new JSONObject();item.put("id",e.exercise.getId());item.put("name",e.exercise.getName());item.put("muscle",e.exercise.getMuscleGroup());item.put("equipment",e.exercise.getEquipment());item.put("met",e.exercise.getMetValue());item.put("previous",e.previous);item.put("best",Double.isFinite(e.previousBest)?e.previousBest:0);JSONArray a=new JSONArray();
            for(SetData sd:e.sets){JSONObject row=new JSONObject();row.put("reps",sd.reps);row.put("weight",Double.isFinite(sd.weight)?sd.weight:0);row.put("complete",sd.complete);row.put("type",sd.type);row.put("rpe",Double.isFinite(sd.rpe)?sd.rpe:0);a.put(row);}item.put("sets",a);entries.put(item);}
        root.put("exercises",entries);draft.edit().putString("session",root.toString()).apply();
    }catch(JSONException ignored){}}
    private void restore(){String json=draft.getString("session",null);if(json==null)return;try{JSONObject root=new JSONObject(json);startTimestamp=root.getLong("start");pausedAt=root.optLong("pausedAt");pausedMillis=root.optLong("pausedMillis");workoutName=root.getString("name");notes=root.optString("notes");restEnd=root.optLong("rest");List<ExerciseWithSets> entries=new ArrayList<>();JSONArray a=root.getJSONArray("exercises");
        for(int i=0;i<a.length();i++){JSONObject item=a.getJSONObject(i);Exercise e=new Exercise(item.getString("name"),item.getString("muscle"),item.optDouble("met",6));e.setId(item.getInt("id"));e.setEquipment(item.optString("equipment"));ExerciseWithSets entry=new ExerciseWithSets(e);entry.previous=item.optString("previous");entry.previousBest=item.optDouble("best",0);JSONArray rows=item.getJSONArray("sets");
            for(int j=0;j<rows.length();j++){JSONObject row=rows.getJSONObject(j);SetData sd=new SetData();sd.reps=row.getInt("reps");sd.weight=row.getDouble("weight");sd.complete=row.optBoolean("complete");sd.type=row.optString("type","Working");sd.rpe=row.optDouble("rpe",0);entry.sets.add(sd);}entries.add(entry);}
        exerciseList.setValue(entries);configured=true;
    }catch(JSONException ignored){draft.edit().remove("session").apply();}}
    public static class ExerciseWithSets {
        public final Exercise exercise;public final List<SetData> sets=new ArrayList<>();public String previous="No previous session";public double previousBest;
        public ExerciseWithSets(Exercise e){exercise=e;}
    }
    public static class SetData {public int reps;public double weight,rpe;public String type="Working";public boolean complete;}
}
