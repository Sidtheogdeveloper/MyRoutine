package com.example.myroutine.ui;

import android.content.*;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import com.example.myroutine.R;
import com.example.myroutine.MainActivity;
import com.example.myroutine.data.*;
import com.example.myroutine.data.entity.*;
import com.example.myroutine.ui.calendar.CalendarFragment;
import com.example.myroutine.ui.profile.ProfileFragment;
import com.example.myroutine.ui.routines.RoutineDetailActivity;
import com.example.myroutine.ui.workout.ActiveWorkoutActivity;
import com.example.myroutine.util.TrainingStats;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.text.SimpleDateFormat;
import java.util.*;
import java.io.*;
import org.json.*;
import static com.example.myroutine.ui.ForgeUi.*;

/** Five connected training destinations, populated exclusively from local user data. */
public class TrainingHubFragment extends Fragment {
    private LinearLayout body; private ScrollView scroll; private AppDatabase db;
    private List<Workout> workouts=new ArrayList<>(); private List<WorkoutSet> sets=new ArrayList<>();
    private List<Exercise> exercises=new ArrayList<>(); private List<WeeklyRoutine> routines=new ArrayList<>();
    private UserProfile profile; private TrainingStats stats=new TrainingStats();
    private int mode; private boolean ready; private String exportData="";
    private final ActivityResultLauncher<String> export=registerForActivityResult(new ActivityResultContracts.CreateDocument("text/csv"),uri->{
        if(uri==null)return;Context c=requireContext().getApplicationContext();String data=exportData;
        AppDatabase.databaseWriteExecutor.execute(()->{
            try(OutputStream out=c.getContentResolver().openOutputStream(uri)) {
                if(out==null)throw new IOException("No output stream");out.write(data.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                new android.os.Handler(android.os.Looper.getMainLooper()).post(()->Toast.makeText(c,"Workout history exported",Toast.LENGTH_SHORT).show());
            }catch(IOException e){new android.os.Handler(android.os.Looper.getMainLooper()).post(()->Toast.makeText(c,"Export failed. Try another location.",Toast.LENGTH_LONG).show());}
        });
    });
    public static TrainingHubFragment create(int mode){TrainingHubFragment f=new TrainingHubFragment();Bundle b=new Bundle();b.putInt("mode",mode);f.setArguments(b);return f;}
    private SharedPreferences prefs(){return requireContext().getSharedPreferences("training",Context.MODE_PRIVATE);}
    @Override public View onCreateView(@NonNull LayoutInflater i,ViewGroup parent,Bundle state){
        mode=getArguments()==null?0:getArguments().getInt("mode");body=column(requireContext());
        body.setPadding(dp(requireContext(),22),dp(requireContext(),20),dp(requireContext(),22),dp(requireContext(),28));
        scroll=ForgeUi.scroll(requireContext(),body);return scroll;
    }
    @Override public void onViewCreated(@NonNull View v,Bundle state){
        db=AppDatabase.getInstance(requireContext());
        db.workoutDao().getAll().observe(getViewLifecycleOwner(),w->{workouts=w;render();});
        db.workoutSetDao().getAll().observe(getViewLifecycleOwner(),s->{sets=s;render();});
        db.exerciseDao().getAll().observe(getViewLifecycleOwner(),e->{exercises=e;render();});
        db.userProfileDao().getProfile().observe(getViewLifecycleOwner(),p->{profile=p;render();});
        db.weeklyRoutineDao().getAllRoutinesLive().observe(getViewLifecycleOwner(),r->{
            routines=new ArrayList<>();Set<Integer> seen=new HashSet<>();
            for(WeeklyRoutine item:r)if(item.getEffectiveFrom()<=System.currentTimeMillis()&&seen.add(item.getDayOfWeek()))routines.add(item);
            render();
        });
        ready=true;render();
    }
    @Override public void onResume(){super.onResume();if(ready)render();}
    @Override public void onDestroyView(){super.onDestroyView();ready=false;body=null;scroll=null;}
    private void render(){
        if(body==null||!isAdded())return;int y=scroll.getScrollY();body.removeAllViews();
        stats=TrainingStats.calculate(workouts,sets,exercises,System.currentTimeMillis());stats.xp+=prefs().getInt("questXp",0);
        if(mode==0)home();else if(mode==1)train();else if(mode==2)progressPage();else if(mode==3)quests();else athlete();
        scroll.post(()->{if(scroll!=null)scroll.scrollTo(0,y);});
    }
    private Context c(){return requireContext();}
    private void label(String s){TextView t=text(c(),s,11,MUTED,true);t.setLetterSpacing(.16f);add(body,t,0);}
    private void title(String s,String sub){add(body,text(c(),s,32,TEXT,true),8);add(body,text(c(),sub,14,MUTED,false),6);space(body,24);}
    private void section(String s,String link,Runnable action){
        LinearLayout r=row(c());TextView t=text(c(),s,19,TEXT,true);r.addView(t,new LinearLayout.LayoutParams(0,-2,1));
        if(link!=null){TextView a=text(c(),link+"  ›",12,LIME,true);a.setPadding(dp(c(),8),dp(c(),12),0,dp(c(),12));a.setOnClickListener(v->action.run());r.addView(a);}
        add(body,r,24);
    }
    private void nav(int destination){((MainActivity)requireActivity()).selectTab(destination);}
    private void start(String template){
        if(template!=null&&c().getSharedPreferences("workout_draft",0).contains("session")){
            new MaterialAlertDialogBuilder(c()).setTitle("You have a workout in progress").setMessage("Resume your saved session or discard it to begin a new one.")
                .setPositiveButton("Resume",(d,k)->start(null)).setNegativeButton("Cancel",null)
                .setNeutralButton("Start new",(d,k)->new MaterialAlertDialogBuilder(c()).setTitle("Replace your draft?").setMessage("The unsaved sets in your previous session will be removed.").setPositiveButton("Start new",(a,b)->{c().getSharedPreferences("workout_draft",0).edit().remove("session").apply();start(template);}).setNegativeButton("Keep draft",null).show()).show();return;
        }
        Intent i=new Intent(c(),ActiveWorkoutActivity.class);if(template!=null)i.putExtra("template",template);startActivity(i);
    }
    private String number(double n){return String.format(Locale.getDefault(),"%,.0f",n);}
    private void metrics(String a,String av,String b,String bv){
        LinearLayout r=row(c());LinearLayout left=card(c()),right=card(c());
        add(left,text(c(),av,27,LIME,true),0);add(left,text(c(),a,12,MUTED,false),6);
        add(right,text(c(),bv,27,TEXT,true),0);add(right,text(c(),b,12,MUTED,false),6);
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,-1,1);p.rightMargin=dp(c(),10);r.addView(left,p);r.addView(right,new LinearLayout.LayoutParams(0,-1,1));add(body,r,12);
    }
    private void home(){
        LinearLayout brand=row(c());TextView logo=text(c(),"MYROUTINE",12,LIME,true);logo.setLetterSpacing(.2f);brand.addView(logo,new LinearLayout.LayoutParams(0,-2,1));
        brand.addView(tag(c(),"LVL "+stats.level()+" · "+stats.rank().toUpperCase(Locale.ROOT),LIME));add(body,brand,0);
        String name=profile==null?"athlete":profile.getName().split(" ")[0];
        title("Let's get stronger,\n"+name+".",new SimpleDateFormat("EEEE, dd MMMM",Locale.getDefault()).format(new Date())+"  ·  Your next rep counts.");
        LinearLayout week=card(c());LinearLayout heading=row(c());heading.addView(text(c(),"Your training week",14,TEXT,true),new LinearLayout.LayoutParams(0,-2,1));heading.addView(text(c(),stats.weekSessions+" / "+goal()+" sessions",12,LIME,true));add(week,heading,0);
        LinearLayout days=row(c());String[] names={"M","T","W","T","F","S","S"};int today=(Calendar.getInstance().get(Calendar.DAY_OF_WEEK)+5)%7;
        Calendar weekDate=Calendar.getInstance();weekDate.setTimeInMillis(TrainingStats.weekStart(System.currentTimeMillis()));
        for(int d=0;d<7;d++){LinearLayout day=column(c());day.setGravity(Gravity.CENTER);add(day,text(c(),names[d],11,MUTED,false),0);
            TextView dot=text(c(),stats.trained[d]?"✓":String.valueOf(weekDate.get(Calendar.DAY_OF_MONTH)),15,stats.trained[d]?BG:d==today?LIME:MUTED,true);dot.setGravity(Gravity.CENTER);weekDate.add(Calendar.DATE,1);
            dot.setBackground(shape(c(),stats.trained[d]?LIME:d==today?LINE:BG,14));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(dp(c(),34),dp(c(),38));p.topMargin=dp(c(),10);day.addView(dot,p);
            days.addView(day,new LinearLayout.LayoutParams(0,-2,1));}add(week,days,14);add(body,week,0);
        section("Today's mission",null,null);
        WeeklyRoutine routine=todayRoutine();LinearLayout hero=card(c());
        android.graphics.drawable.GradientDrawable gradient=new android.graphics.drawable.GradientDrawable(android.graphics.drawable.GradientDrawable.Orientation.TL_BR,new int[]{0xff344228,0xff1c2419});gradient.setCornerRadius(dp(c(),22));hero.setBackground(gradient);
        add(hero,tag(c(),routine!=null&&routine.isRestDay()?"RECOVER & RECHARGE":"BUILD YOUR NEXT BEST",LIME),0);
        add(hero,text(c(),routine==null?"Your session.\nYour rules.":routine.getRoutineName(),29,TEXT,true),18);
        add(hero,text(c(),routine!=null&&routine.isRestDay()?"Recovery belongs in your training plan. Your weekly streak welcomes rest days.":"Log every set. Chase your personal best.\nEarn 100 XP + 10 XP per completed set.",13,MUTED,false),10);
        boolean draft=c().getSharedPreferences("workout_draft",0).contains("session");
        add(hero,button(c(),draft?"Resume workout  ↗":routine!=null&&routine.isRestDay()?"Start a freestyle workout  ↗":"Start workout  ↗",true,()->start(null)),22);add(body,hero,12);
        metrics("Sets this week",String.valueOf(stats.weekSets),"Volume this week · kg",number(stats.weekVolume));
        section("Level up your consistency","Quests",()->nav(3));
        LinearLayout xp=card(c());add(xp,text(c(),stats.rank()+"  ·  Level "+stats.level(),17,TEXT,true),0);progress(xp,stats.xp%500,500,LIME);
        add(xp,text(c(),(stats.xp%500)+" / 500 XP to next level     •     "+stats.activeWeeks+" active weeks",12,MUTED,false),8);add(body,xp,12);
        section("Recent sessions","See progress",()->nav(2));history(3);
        section("Small habits. Strong foundation.",null,null);hydration();
    }
    private int goal(){return prefs().getInt("weeklyGoal",4);}
    private WeeklyRoutine todayRoutine(){int d=(Calendar.getInstance().get(Calendar.DAY_OF_WEEK)+5)%7+1;for(WeeklyRoutine r:routines)if(r.getDayOfWeek()==d)return r;return null;}
    private void train(){
        label("THE TRAINING FLOOR");title("Make it count.","A plan for every day. A log for every rep.");
        add(body,button(c(),"+  Start freestyle workout",true,()->start("Freestyle")),0);
        section("Quick-start sessions",null,null);
        preset("Push","Chest · shoulders · triceps","Push",LIME);
        preset("Pull","Back · biceps · rear delts","Pull",PURPLE);
        preset("Legs","Quads · hamstrings · calves","Legs",ORANGE);
        preset("Full body","A balanced session from head to toe","Full body",LIME);
        section("Your weekly split",null,null);add(body,text(c(),"Tap a day to choose exercises, sets and reps.",13,MUTED,false),4);
        for(int d=1;d<=7;d++){final int day=d;WeeklyRoutine found=null;for(WeeklyRoutine r:routines)if(r.getDayOfWeek()==d)found=r;final WeeklyRoutine routine=found;
            LinearLayout card=card(c());LinearLayout line=row(c());TextView dayText=text(c(),WeeklyRoutine.dayName(d).substring(0,3).toUpperCase(Locale.ROOT),12,LIME,true);line.addView(dayText,new LinearLayout.LayoutParams(dp(c(),50),-2));
            line.addView(text(c(),found==null?"Build your routine":found.getRoutineName(),16,TEXT,true),new LinearLayout.LayoutParams(0,-2,1));line.addView(text(c(),"›",24,MUTED,false));add(card,line,0);
            card.setOnClickListener(v->{Intent i=new Intent(c(),RoutineDetailActivity.class);i.putExtra(RoutineDetailActivity.EXTRA_DAY_OF_WEEK,day);if(routine!=null)i.putExtra(RoutineDetailActivity.EXTRA_ROUTINE_ID,routine.getId());startActivity(i);});add(body,card,10);
        }
        section("Exercise library",null,null);add(body,text(c(),exercises.size()+" movements · Search by muscle or equipment in your workout.",14,MUTED,false),8);
        add(body,button(c(),"Explore exercises",false,()->library()),12);
        section("Tools of the trade",null,null);
        add(body,button(c(),"Barbell plate calculator",false,()->plateCalculator()),12);
    }
    private void preset(String name,String desc,String key,int color){LinearLayout card=card(c());LinearLayout r=row(c());LinearLayout copy=column(c());add(copy,text(c(),name,22,color,true),0);add(copy,text(c(),desc,12,MUTED,false),6);r.addView(copy,new LinearLayout.LayoutParams(0,-2,1));r.addView(text(c(),"↗",26,color,true));add(card,r,0);card.setOnClickListener(v->start(key));add(body,card,12);}
    private void library(){
        LinearLayout content=column(c());content.setPadding(dp(c(),16),0,dp(c(),16),0);EditText search=input(c(),"Search name, muscle, equipment","",false);add(content,search,0);
        LinearLayout results=column(c());ScrollView sc=ForgeUi.scroll(c(),results);content.addView(sc,new LinearLayout.LayoutParams(-1,dp(c(),360)));
        Runnable filter=()->{results.removeAllViews();String q=search.getText().toString().toLowerCase(Locale.ROOT);int count=0;
            for(Exercise e:exercises)if((e.getName()+" "+e.getMuscleGroup()+" "+e.getEquipment()).toLowerCase(Locale.ROOT).contains(q)){
                if(count++>=80)break;LinearLayout item=card(c());add(item,text(c(),e.getName(),15,TEXT,true),0);add(item,text(c(),e.getMuscleGroup()+" · "+e.getEquipment()+" · "+e.getDifficulty(),11,MUTED,false),6);
                item.setOnClickListener(v->new MaterialAlertDialogBuilder(c()).setTitle(e.getName()).setMessage("Primary: "+e.getMuscleGroup()+"\nSecondary: "+e.getSecondaryMuscles()+"\nMovement: "+e.getMovementPattern()+"\nEquipment: "+e.getEquipment()+"\nType: "+e.getCompoundOrIsolation()+"\nDifficulty: "+e.getDifficulty()).setPositiveButton("Got it",null).show());add(results,item,8);}
            if(count==0)add(results,text(c(),"No exercises match your search.",14,MUTED,false),20);};
        watch(search,filter);filter.run();new MaterialAlertDialogBuilder(c()).setTitle("Movement library").setView(content).setPositiveButton("Done",null).show();
    }
    private void progressPage(){
        label("PROGRESS, EARNED");title("Stronger over time.","Your training story, measured in real work.");metrics("Total sessions",String.valueOf(stats.sessions),"Lifetime volume · kg",number(stats.volume));
        section("This week's volume",null,null);LinearLayout graph=card(c());add(graph,text(c(),number(stats.weekVolume)+" kg",27,LIME,true),0);add(graph,text(c(),"Completed sets · Monday to Sunday",12,MUTED,false),6);
        double max=1;for(double n:stats.dailyVolume)max=Math.max(max,n);LinearLayout bars=row(c());bars.setGravity(Gravity.BOTTOM);String[] days={"M","T","W","T","F","S","S"};
        for(int i=0;i<7;i++){LinearLayout col=column(c());col.setGravity(Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL);TextView value=text(c(),number(stats.dailyVolume[i]),9,MUTED,false);col.addView(value);
            View bar=new View(c());bar.setBackground(shape(c(),stats.dailyVolume[i]>0?LIME:LINE,5));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(dp(c(),22),dp(c(),(float)(8+92*stats.dailyVolume[i]/max)));p.topMargin=dp(c(),8);p.bottomMargin=dp(c(),8);col.addView(bar,p);col.addView(text(c(),days[i],11,MUTED,false));bars.addView(col,new LinearLayout.LayoutParams(0,dp(c(),145),1));}add(graph,bars,18);add(body,graph,12);
        section("Personal records",null,null);add(body,text(c(),"Best load and estimated 1RM from sets of 1–12 reps. All weights in kg.",12,MUTED,false),6);
        int count=0;for(Exercise e:exercises)if(stats.bestWeight.getOrDefault(e.getId(),0.0)>0){if(count++>=12)break;LinearLayout record=card(c());add(record,text(c(),e.getName(),16,TEXT,true),0);
            add(record,text(c(),number(stats.bestWeight.get(e.getId()))+" kg best load  ·  "+number(stats.bestE1rm.getOrDefault(e.getId(),0.0))+" kg est. 1RM",13,LIME,true),8);record.setOnClickListener(v->strengthHistory(e));add(body,record,10);}
        if(count==0)empty("Your first PR is waiting.","Complete a weighted set to start your record book.");
        section("Muscle balance",null,null);if(stats.muscles.isEmpty())empty("A clean slate.","Your weekly sets by primary muscle appear here.");
        else {LinearLayout muscles=card(c());int top=Collections.max(stats.muscles.values());for(Map.Entry<String,Integer> e:stats.muscles.entrySet()){add(muscles,text(c(),e.getKey()+"  ·  "+e.getValue()+" sets",13,TEXT,false),12);progress(muscles,e.getValue(),top,PURPLE);}add(body,muscles,12);}
        section("Workout journal","Calendar",()->openFragment(new CalendarFragment()));add(body,text(c(),"Latest 50 sessions · Tap for details · Hold to delete",12,MUTED,false),4);history(50);
    }
    private void empty(String headline,String detail){LinearLayout card=card(c());add(card,text(c(),headline,17,TEXT,true),0);add(card,text(c(),detail,13,MUTED,false),8);add(body,card,12);}
    private Exercise exercise(int id){for(Exercise e:exercises)if(e.getId()==id)return e;return null;}
    private void history(int limit){
        if(workouts.isEmpty()){empty("Day one starts here.","Your completed workouts will appear here. Let's build a history worth looking back on.");return;}
        int n=0;for(Workout w:workouts){if(n++>=limit)break;List<WorkoutSet> ws=stats.byWorkout.getOrDefault(w.getId(),Collections.emptyList());double v=0;for(WorkoutSet s:ws)v+=s.getWeight()*s.getReps();
            LinearLayout card=card(c());add(card,text(c(),new SimpleDateFormat("EEE, dd MMM · HH:mm",Locale.getDefault()).format(new Date(w.getTimestamp())).toUpperCase(Locale.getDefault()),10,MUTED,true),0);
            add(card,text(c(),w.getName(),17,TEXT,true),8);add(card,text(c(),ws.size()+" sets  ·  "+(w.getDurationSeconds()/60)+" min  ·  "+number(v)+" kg volume",12,LIME,false),8);card.setOnClickListener(view->details(w));card.setOnLongClickListener(view->{new MaterialAlertDialogBuilder(c()).setTitle("Delete session?").setMessage("This removes the workout and its sets from your history and statistics.").setPositiveButton("Delete",(d,k)->AppDatabase.databaseWriteExecutor.execute(()->db.workoutDao().delete(w))).setNegativeButton("Keep",null).show();return true;});add(body,card,10);
        }
    }
    private void details(Workout w){
        StringBuilder s=new StringBuilder((w.getDurationSeconds()/60)+" min · "+number(w.getCaloriesBurnt())+" estimated kcal\n");String previous="";for(WorkoutSet set:stats.byWorkout.getOrDefault(w.getId(),Collections.emptyList())){Exercise e=exercise(set.getExerciseId());String name=e==null?"Exercise":e.getName();if(!name.equals(previous)){s.append("\n").append(name).append("\n");previous=name;}
            s.append(set.getSetType()).append(" · ").append(number(set.getWeight())).append(" kg × ").append(set.getReps());if(set.getRpe()>0)s.append(" · RPE ").append(set.getRpe());s.append("\n");}
        if(w.getNotes()!=null&&!w.getNotes().isEmpty())s.append("\nNotes: ").append(w.getNotes());
        new MaterialAlertDialogBuilder(c()).setTitle(w.getName()).setMessage(s.length()==0?"No sets recorded":s.toString())
            .setPositiveButton("Repeat",(d,k)->{Intent i=new Intent(c(),ActiveWorkoutActivity.class);i.putExtra("repeatWorkout",w.getId());startActivity(i);})
            .setNeutralButton("Share",(d,k)->{Intent i=new Intent(Intent.ACTION_SEND);i.setType("text/plain");i.putExtra(Intent.EXTRA_TEXT,"MyRoutine · "+w.getName()+"\n"+s);startActivity(Intent.createChooser(i,"Share your session"));})
            .setNegativeButton("Close",null).show();
    }
    private void quests(){
        label("THE LONG GAME");title("Show up. Level up.","Collect milestones. Build a habit that lasts.");
        LinearLayout level=card(c());add(level,tag(c(),"YOUR ATHLETE RANK",LIME),0);add(level,text(c(),stats.rank(),34,TEXT,true),16);add(level,text(c(),"Level "+stats.level()+"  ·  "+number(stats.xp)+" lifetime XP",14,LIME,true),8);progress(level,stats.xp%500,500,LIME);add(level,text(c(),(500-stats.xp%500)+" XP until your next level",12,MUTED,false),8);add(body,level,0);
        section("Weekly quests",null,null);add(body,text(c(),"Reset each Monday · Claim rewards when you finish",12,MUTED,false),6);
        quest("showup","Show up for yourself","Complete "+goal()+" sessions",stats.weekSessions,goal(),150,LIME);
        quest("sets","Rep by rep","Complete 40 sets",stats.weekSets,40,100,PURPLE);
        quest("volume","Move a mountain","Lift 10,000 kg of total volume",(int)stats.weekVolume,10000,100,ORANGE);
        section("Trophy cabinet",null,null);
        badge("01","First rep","Save your first workout",stats.sessions>=1,LIME);
        badge("10","Finding your rhythm","Complete 10 workouts",stats.sessions>=10,PURPLE);
        badge("50","Iron regular","Complete 50 workouts",stats.sessions>=50,ORANGE);
        badge("100","Century club","Log 100 completed sets",stats.sets>=100,LIME);
        badge("10T","Heavy hitter","Lift 10,000 kg across all sessions",stats.volume>=10000,PURPLE);
        badge("4W","Built on consistency","Train in 4 consecutive weeks",stats.activeWeeks>=4,ORANGE);
        add(body,text(c(),"Rest days are part of the game. Weekly consistency celebrates sustainable training.",12,MUTED,false),22);
    }
    private void quest(String key,String title,String description,int value,int target,int reward,int color){
        String id="claim_"+TrainingStats.weekStart(System.currentTimeMillis())+"_"+key;boolean claimed=prefs().getBoolean(id,false);boolean done=value>=target;
        LinearLayout card=card(c());add(card,tag(c(),claimed?"REWARD COLLECTED":"+"+reward+" XP",color),0);add(card,text(c(),title,20,TEXT,true),12);add(card,text(c(),description,12,MUTED,false),6);progress(card,Math.min(value,target),target,color);
        add(card,text(c(),number(Math.min(value,target))+" / "+number(target),12,MUTED,false),8);
        if(done&&!claimed)add(card,button(c(),"Claim "+reward+" XP",true,()->{prefs().edit().putBoolean(id,true).putInt("questXp",prefs().getInt("questXp",0)+reward).apply();render();Toast.makeText(c(),"Quest complete! +"+reward+" XP",Toast.LENGTH_SHORT).show();}),12);add(body,card,12);
    }
    private void badge(String icon,String title,String detail,boolean unlocked,int color){LinearLayout card=card(c());LinearLayout r=row(c());TextView emblem=tag(c(),icon,unlocked?color:MUTED);r.addView(emblem);LinearLayout copy=column(c());add(copy,text(c(),title,16,unlocked?TEXT:MUTED,true),0);add(copy,text(c(),detail,11,MUTED,false),6);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,-2,1);p.leftMargin=dp(c(),14);r.addView(copy,p);r.addView(text(c(),unlocked?"✓":"○",20,unlocked?color:MUTED,true));add(card,r,0);add(body,card,10);}
    private void hydration(){
        String key="water_"+new SimpleDateFormat("yyyy-MM-dd",Locale.ROOT).format(new Date());int glasses=prefs().getInt(key,0);
        LinearLayout card=card(c());add(card,text(c(),"Hydration check-in",17,TEXT,true),0);add(card,text(c(),glasses+" glasses logged today · 250 ml per glass",12,MUTED,false),8);progress(card,Math.min(glasses,8),8,PURPLE);
        LinearLayout r=row(c());r.addView(button(c(),"−",false,()->{prefs().edit().putInt(key,Math.max(0,glasses-1)).apply();render();}),new LinearLayout.LayoutParams(0,-2,1));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,-2,3);p.leftMargin=dp(c(),8);r.addView(button(c(),"+  Log a glass",false,()->{prefs().edit().putInt(key,glasses+1).apply();render();}),p);add(card,r,12);add(body,card,12);
    }
    private void athlete(){
        label("YOUR ATHLETE SPACE");title(profile==null?"Your journey.":profile.getName(),"Train with intent. Make this space yours.");
        metrics("Athlete level",String.valueOf(stats.level()),"Active week streak",String.valueOf(stats.activeWeeks));
        section("Training preferences",null,null);LinearLayout settings=card(c());
        add(settings,text(c(),"Weekly target · "+goal()+" sessions",17,TEXT,true),0);add(settings,text(c(),"Your goal powers the consistency quest.",12,MUTED,false),8);
        add(settings,button(c(),"Change weekly goal",false,()->new MaterialAlertDialogBuilder(c()).setTitle("Sessions per week").setSingleChoiceItems(new String[]{"2 sessions","3 sessions","4 sessions","5 sessions","6 sessions"},goal()-2,(d,k)->{prefs().edit().putInt("weeklyGoal",k+2).apply();d.dismiss();render();}).setNegativeButton("Cancel",null).show()),14);
        add(settings,button(c(),"Edit physical profile",false,()->openFragment(new ProfileFragment())),10);add(body,settings,12);
        section("Bodyweight journal",null,null);LinearLayout weight=card(c());String latest="No check-ins yet";try{JSONArray entries=new JSONArray(prefs().getString("bodyweights","[]"));if(entries.length()>0){JSONObject e=entries.getJSONObject(entries.length()-1);latest=e.getString("kg")+" kg · "+e.getString("date");}}catch(JSONException ignored){}
        add(weight,text(c(),latest,20,TEXT,true),0);add(weight,text(c(),"Track change over time, at your own pace.",12,MUTED,false),8);add(weight,button(c(),"+  Log bodyweight",false,()->logWeight()),14);add(weight,button(c(),"View check-ins",false,()->weightHistory()),8);add(body,weight,12);
        section("Your data, your control",null,null);LinearLayout data=card(c());add(data,text(c(),"Offline by design",18,TEXT,true),0);add(data,text(c(),"Your workouts stay on this device. Export every set with notes, set type and RPE as a CSV.",13,MUTED,false),8);
        add(data,button(c(),"Export workout history  ↗",false,()->{exportData=csv();export.launch("MyRoutine-workouts.csv");}),16);add(body,data,12);
        add(body,text(c(),"MYROUTINE  /  BUILT FOR THE WORK",11,MUTED,true),32);
    }
    private void logWeight(){EditText input=input(c(),"Bodyweight in kg","",true);
        androidx.appcompat.app.AlertDialog dialog=new MaterialAlertDialogBuilder(c()).setTitle("Bodyweight check-in").setView(input).setNegativeButton("Cancel",null).setPositiveButton("Save",null).create();dialog.show();
        dialog.getButton(-1).setOnClickListener(v->{try{double kg=Double.parseDouble(input.getText().toString());if(!Double.isFinite(kg)||kg<=0||kg>500)throw new NumberFormatException();
            JSONArray a=new JSONArray(prefs().getString("bodyweights","[]"));JSONObject e=new JSONObject();e.put("kg",kg);e.put("date",new SimpleDateFormat("dd MMM yyyy",Locale.getDefault()).format(new Date()));a.put(e);prefs().edit().putString("bodyweights",a.toString()).apply();dialog.dismiss();render();
        }catch(Exception ex){input.setError("Enter a weight between 0 and 500 kg");}});
    }
    private void weightHistory(){StringBuilder text=new StringBuilder();try{JSONArray a=new JSONArray(prefs().getString("bodyweights","[]"));for(int i=a.length()-1;i>=0;i--){JSONObject e=a.getJSONObject(i);text.append(e.getString("date")).append("   ·   ").append(e.getString("kg")).append(" kg\n");}}catch(JSONException ignored){}new MaterialAlertDialogBuilder(c()).setTitle("Bodyweight journal").setMessage(text.length()==0?"No check-ins yet. Log your first one to begin.":text.toString()).setPositiveButton("Done",null).show();}
    private void strengthHistory(Exercise exercise){StringBuilder s=new StringBuilder();for(Workout w:workouts){double best=0,estimate=0;int reps=0;for(WorkoutSet row:stats.byWorkout.getOrDefault(w.getId(),Collections.emptyList()))if(row.getExerciseId()==exercise.getId()){if(row.getWeight()>=best){best=row.getWeight();reps=row.getReps();}estimate=Math.max(estimate,TrainingStats.e1rm(row.getWeight(),row.getReps()));}if(reps>0)s.append(new SimpleDateFormat("dd MMM yyyy",Locale.getDefault()).format(new Date(w.getTimestamp()))).append("\n").append(number(best)).append(" kg × ").append(reps).append(" · ").append(number(estimate)).append(" kg est. 1RM\n\n");}new MaterialAlertDialogBuilder(c()).setTitle(exercise.getName()).setMessage(s.toString()).setPositiveButton("Done",null).show();}
    private void plateCalculator(){
        LinearLayout content=card(c());add(content,text(c(),"Weight on each side · standard kg plates",13,MUTED,false),0);EditText target=input(c(),"Target total kg","60",true),bar=input(c(),"Bar weight kg","20",true);add(content,target,12);add(content,bar,12);TextView result=text(c(),"",16,LIME,true);add(content,result,18);
        Runnable calculate=()->{try{double total=Double.parseDouble(target.getText().toString()),barWeight=Double.parseDouble(bar.getText().toString());if(!Double.isFinite(total)||!Double.isFinite(barWeight)||total<barWeight||barWeight<0||total>1000)throw new NumberFormatException();double remaining=(total-barWeight)/2;StringBuilder s=new StringBuilder();for(double p:new double[]{25,20,15,10,5,2.5,1.25}){int count=(int)Math.floor((remaining+.00001)/p);if(count>0){s.append(count).append(" × ").append(p).append(" kg\n");remaining-=count*p;}}if(s.length()==0)s.append("Empty bar\n");if(remaining>.001)s.append("Unallocated: ").append(String.format(Locale.getDefault(),"%.2f kg per side",remaining));else s.append("Load this on EACH side.");result.setText(s);}catch(NumberFormatException e){result.setText("Enter a total at least as heavy as the bar (up to 1000 kg).");}};
        watch(target,calculate);watch(bar,calculate);calculate.run();new MaterialAlertDialogBuilder(c()).setTitle("Load your bar").setView(content).setPositiveButton("Done",null).show();
    }
    private String csv(){StringBuilder b=new StringBuilder("workout_id,date,workout,exercise,weight_kg,reps,set_type,rpe,notes\n");for(Workout w:workouts)for(WorkoutSet s:stats.byWorkout.getOrDefault(w.getId(),Collections.emptyList())){Exercise e=exercise(s.getExerciseId());b.append(w.getId()).append(',').append(quote(new SimpleDateFormat("yyyy-MM-dd HH:mm",Locale.ROOT).format(new Date(w.getTimestamp())))).append(',').append(quote(w.getName())).append(',').append(quote(e==null?"Exercise":e.getName())).append(',').append(s.getWeight()).append(',').append(s.getReps()).append(',').append(quote(s.getSetType())).append(',').append(s.getRpe()).append(',').append(quote(w.getNotes())).append('\n');}return b.toString();}
    private String quote(String s){String value=s==null?"":s;if(value.matches("^[=+@-].*"))value="'"+value;return "\""+value.replace("\"","\"\"")+"\"";}
    private void openFragment(Fragment f){getParentFragmentManager().beginTransaction().replace(R.id.fragmentContainer,f).addToBackStack(null).commit();}
    public static void watch(EditText e,Runnable action){e.addTextChangedListener(new android.text.TextWatcher(){public void beforeTextChanged(CharSequence s,int a,int n,int after){}public void onTextChanged(CharSequence s,int a,int before,int count){}public void afterTextChanged(android.text.Editable s){action.run();}});}
}
