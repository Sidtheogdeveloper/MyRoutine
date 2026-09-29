package com.example.myroutine.ui.workout;

import android.os.*;
import android.view.*;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.core.view.*;
import androidx.core.graphics.Insets;
import com.example.myroutine.ui.ForgeUi;
import com.example.myroutine.ui.TrainingHubFragment;
import com.example.myroutine.data.entity.Exercise;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.util.*;
import static com.example.myroutine.ui.ForgeUi.*;

/** Focused logger: previous performance, set types, RPE, rest and recoverable drafts. */
public class ActiveWorkoutActivity extends AppCompatActivity {
    private ActiveWorkoutViewModel model;
    private LinearLayout cards;
    private TextView elapsed, summary, rest, title;
    private android.widget.Button finishButton;
    private final Handler handler=new Handler(Looper.getMainLooper());
    private int restSeconds=90;
    private List<Exercise> library=new ArrayList<>();
    private final Runnable tick=new Runnable(){public void run(){
        long seconds=model.elapsedSeconds();
        elapsed.setText(String.format(Locale.getDefault(),"%02d:%02d",seconds/60,seconds%60));
        long remaining=Math.max(0,(model.getRestEnd()-System.currentTimeMillis()+999)/1000);
        if(model.getRestEnd()>0&&remaining==0){model.setRestEnd(0);rest.setText("Rest complete · Ready for your next set");rest.performHapticFeedback(HapticFeedbackConstants.CONFIRM);}
        else if(remaining>0)rest.setText(String.format(Locale.getDefault(),"REST  %02d:%02d   ·   Tap to skip",remaining/60,remaining%60));
        else rest.setText("Rest timer · "+restSeconds+"s · Tap to change");
        handler.postDelayed(this,1000);
    }};
    @Override public void onCreate(Bundle state){
        super.onCreate(state);EdgeToEdge.enable(this);getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        model=new ViewModelProvider(this).get(ActiveWorkoutViewModel.class);
        restSeconds=getSharedPreferences("training",0).getInt("restSeconds",90);
        LinearLayout root=column(this);root.setBackgroundColor(BG);setContentView(root);ForgeUi.darkBars(this);
        ViewCompat.setOnApplyWindowInsetsListener(root,(v,insets)->{Insets b=insets.getInsets(WindowInsetsCompat.Type.systemBars()|WindowInsetsCompat.Type.ime());v.setPadding(b.left,b.top,b.right,b.bottom);return insets;});
        LinearLayout header=column(this);header.setPadding(dp(this,22),dp(this,16),dp(this,22),dp(this,14));
        LinearLayout top=row(this);TextView back=text(this,"‹",30,MUTED,true);back.setContentDescription("Save draft and leave workout");back.setPadding(0,0,dp(this,20),0);back.setMinHeight(dp(this,48));back.setOnClickListener(v->leave());top.addView(back);
        top.addView(tag(this,"SESSION IN PROGRESS",LIME),new LinearLayout.LayoutParams(0,-2,1));elapsed=text(this,"00:00",22,LIME,true);top.addView(elapsed);add(header,top,0);
        title=text(this,model.getWorkoutName()+"  ✎",24,TEXT,true);title.setOnClickListener(v->rename());add(header,title,14);
        summary=text(this,"0 sets  ·  0 kg volume",13,MUTED,false);add(header,summary,8);
        rest=text(this,"Rest timer · "+restSeconds+"s",13,PURPLE,true);rest.setPadding(dp(this,14),dp(this,14),dp(this,14),dp(this,14));rest.setBackground(shape(this,CARD,12));rest.setOnClickListener(v->{if(model.getRestEnd()>System.currentTimeMillis())model.setRestEnd(0);else timerOptions();});add(header,rest,16);root.addView(header);
        cards=column(this);cards.setPadding(dp(this,22),0,dp(this,22),dp(this,16));ScrollView scroll=ForgeUi.scroll(this,cards);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        LinearLayout footer=column(this);footer.setPadding(dp(this,22),dp(this,8),dp(this,22),dp(this,12));
        add(footer,button(this,"+  Add exercise",false,()->picker()),0);
        LinearLayout buttons=row(this);buttons.addView(button(this,"Notes",false,()->notes()),new LinearLayout.LayoutParams(0,-2,1));finishButton=button(this,"Finish workout  ✓",true,()->finishSession());LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,-2,2);p.leftMargin=dp(this,10);buttons.addView(finishButton,p);add(footer,buttons,8);root.addView(footer);
        model.getExerciseList().observe(this,list->{render();title.setText(model.getWorkoutName()+"  ✎");});
        model.getAllExercises().observe(this,list->{library=list==null?new ArrayList<>():list;if(!library.isEmpty())model.configure(getIntent().getStringExtra("template"),getIntent().getLongExtra("repeatWorkout",0));});
        model.getError().observe(this,error->{if(error!=null)Toast.makeText(this,error,Toast.LENGTH_LONG).show();});
        model.getSaving().observe(this,saving->{finishButton.setEnabled(!Boolean.TRUE.equals(saving));finishButton.setText(Boolean.TRUE.equals(saving)?"Saving…":"Finish workout  ✓");});
        model.getWorkoutSaved().observe(this,saved->{if(Boolean.TRUE.equals(saved)){handler.removeCallbacks(tick);celebrate();}});
        getOnBackPressedDispatcher().addCallback(this,new OnBackPressedCallback(true){public void handleOnBackPressed(){leave();}});
    }
    @Override protected void onResume(){super.onResume();model.resumeSession();handler.post(tick);}
    @Override protected void onPause(){super.onPause();handler.removeCallbacks(tick);model.persist();}
    private void refresh(){summary.setText(String.format(Locale.getDefault(),"%d completed sets  ·  %,.0f kg volume",model.completedCount(),model.completedVolume()));}
    private void render(){
        cards.removeAllViews();List<ActiveWorkoutViewModel.ExerciseWithSets> entries=model.getExerciseList().getValue();
        if(entries.isEmpty()){LinearLayout empty=card(this);add(empty,text(this,"Every session starts\nwith one movement.",24,TEXT,true),0);add(empty,text(this,"Add an exercise, log weight and reps, then tap the check to complete a set. Use 0 kg for bodyweight movements.",14,MUTED,false),16);add(cards,empty,10);}
        for(int i=0;i<entries.size();i++){
            final int position=i;ActiveWorkoutViewModel.ExerciseWithSets entry=entries.get(i);LinearLayout card=card(this);
            LinearLayout head=row(this);head.addView(text(this,entry.exercise.getName(),19,TEXT,true),new LinearLayout.LayoutParams(0,-2,1));TextView remove=text(this,"×",24,MUTED,false);remove.setMinWidth(dp(this,40));remove.setMinHeight(dp(this,48));remove.setGravity(Gravity.CENTER);remove.setContentDescription("Remove "+entry.exercise.getName());remove.setOnClickListener(v->new MaterialAlertDialogBuilder(this).setTitle("Remove exercise?").setMessage("This removes its sets from this session.").setPositiveButton("Remove",(d,k)->model.removeExercise(position)).setNegativeButton("Keep",null).show());head.addView(remove);add(card,head,0);
            add(card,text(this,entry.exercise.getMuscleGroup()+" · "+entry.previous,12,MUTED,false),6);
            LinearLayout labels=row(this);labels.addView(text(this,"SET",10,MUTED,true),new LinearLayout.LayoutParams(dp(this,38),-2));labels.addView(text(this,"KG",10,MUTED,true),new LinearLayout.LayoutParams(0,-2,1));labels.addView(text(this,"REPS",10,MUTED,true),new LinearLayout.LayoutParams(0,-2,1));labels.addView(text(this,"DONE",10,MUTED,true),new LinearLayout.LayoutParams(dp(this,48),-2));add(card,labels,20);
            for(int j=0;j<entry.sets.size();j++){
                final int index=j;ActiveWorkoutViewModel.SetData sd=entry.sets.get(j);LinearLayout set=row(this);TextView no=text(this,String.valueOf(j+1),15,MUTED,true);set.addView(no,new LinearLayout.LayoutParams(dp(this,34),-2));
                EditText kg=input(this,"0",sd.weight>0?format(sd.weight):"",true),reps=input(this,"0",sd.reps>0?String.valueOf(sd.reps):"",true);
                LinearLayout.LayoutParams kp=new LinearLayout.LayoutParams(0,dp(this,48),1);kp.rightMargin=dp(this,6);set.addView(kg,kp);LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(0,dp(this,48),1);rp.rightMargin=dp(this,6);set.addView(reps,rp);
                TextView check=text(this,sd.complete?"✓":"○",22,sd.complete?BG:LIME,true);check.setGravity(Gravity.CENTER);check.setBackground(shape(this,sd.complete?LIME:LINE,10));check.setContentDescription("Complete set "+(j+1));set.addView(check,new LinearLayout.LayoutParams(dp(this,48),dp(this,48)));
                Runnable update=()->{sd.complete=false;check.setText("○");check.setTextColor(LIME);check.setBackground(shape(this,LINE,10));try{sd.weight=kg.getText().length()==0?0:Double.parseDouble(kg.getText().toString());}catch(NumberFormatException e){sd.weight=-1;}try{sd.reps=reps.getText().length()==0?0:Integer.parseInt(reps.getText().toString());}catch(NumberFormatException e){sd.reps=-1;}model.changed();refresh();};
                TrainingHubFragment.watch(kg,update);TrainingHubFragment.watch(reps,update);
                check.setOnClickListener(v->{if(!sd.complete&&!model.valid(sd)){Toast.makeText(this,"Enter whole reps (1–1000), weight (0–10000 kg), and RPE (0–10).",Toast.LENGTH_LONG).show();return;}sd.complete=!sd.complete;check.setText(sd.complete?"✓":"○");check.setTextColor(sd.complete?BG:LIME);check.setBackground(shape(this,sd.complete?LIME:LINE,10));
                    if(sd.complete){check.performHapticFeedback(HapticFeedbackConstants.CONFIRM);model.setRestEnd(System.currentTimeMillis()+restSeconds*1000L);if(entry.previousBest>0&&sd.weight>entry.previousBest)Toast.makeText(this,"New best load! Keep the form strong.",Toast.LENGTH_SHORT).show();}model.changed();refresh();});
                add(card,set,10);LinearLayout detail=row(this);TextView type=text(this,sd.type+"  ·  "+(sd.rpe>0?"RPE "+sd.rpe:"Add RPE")+"  ▾",11,MUTED,false);type.setMinHeight(dp(this,44));type.setGravity(Gravity.CENTER_VERTICAL);type.setOnClickListener(v->setDetails(sd,type));detail.addView(type,new LinearLayout.LayoutParams(0,-2,1));TextView delete=text(this,"Remove",11,MUTED,false);delete.setMinHeight(dp(this,44));delete.setGravity(Gravity.CENTER);delete.setPadding(dp(this,8),0,0,0);delete.setOnClickListener(v->model.removeSet(position,index));detail.addView(delete);add(card,detail,0);
            }
            add(card,button(this,"+  Add set",false,()->model.addSet(position)),8);add(cards,card,12);
        }refresh();
    }
    private String format(double d){return d==Math.floor(d)?String.valueOf((int)d):String.valueOf(d);}
    private void timerOptions(){String[] labels={"30 seconds","60 seconds","90 seconds","2 minutes","3 minutes","5 minutes"};int[] values={30,60,90,120,180,300};new MaterialAlertDialogBuilder(this).setTitle("Rest between sets").setItems(labels,(d,k)->{restSeconds=values[k];getSharedPreferences("training",0).edit().putInt("restSeconds",restSeconds).apply();model.setRestEnd(System.currentTimeMillis()+restSeconds*1000L);}).setNegativeButton("Cancel",null).show();}
    private void setDetails(ActiveWorkoutViewModel.SetData sd,TextView label){
        LinearLayout content=column(this);content.setPadding(dp(this,24),dp(this,8),dp(this,24),0);Spinner types=new Spinner(this);String[] choices={"Working","Warm-up","Drop set","Failure"};types.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,choices));for(int i=0;i<choices.length;i++)if(choices[i].equals(sd.type))types.setSelection(i);add(content,types,0);
        add(content,text(this,"RPE · effort from 1–10 (optional)",13,MUTED,false),12);EditText rpe=input(this,"Leave blank if unused",sd.rpe>0?String.valueOf(sd.rpe):"",true);add(content,rpe,8);
        androidx.appcompat.app.AlertDialog dialog=new MaterialAlertDialogBuilder(this).setTitle("Set details").setView(content).setPositiveButton("Save",null).setNegativeButton("Cancel",null).create();dialog.show();dialog.getButton(-1).setOnClickListener(v->{try{double effort=rpe.getText().length()==0?0:Double.parseDouble(rpe.getText().toString());if(!Double.isFinite(effort)||effort<0||effort>10)throw new NumberFormatException();sd.type=choices[types.getSelectedItemPosition()];sd.rpe=effort;model.changed();label.setText(sd.type+"  ·  "+(sd.rpe>0?"RPE "+sd.rpe:"Add RPE")+"  ▾");dialog.dismiss();}catch(NumberFormatException ex){rpe.setError("Use a value from 1 to 10, or leave blank");}});
    }
    private void rename(){EditText input=input(this,"Session name",model.getWorkoutName(),false);new MaterialAlertDialogBuilder(this).setTitle("Name your session").setView(input).setPositiveButton("Save",(d,k)->{model.setWorkoutName(input.getText().toString());title.setText(model.getWorkoutName()+"  ✎");}).setNegativeButton("Cancel",null).show();}
    private void notes(){EditText input=input(this,"How did the session feel?",model.getNotes(),false);input.setSingleLine(false);input.setMinLines(3);new MaterialAlertDialogBuilder(this).setTitle("Session notes").setView(input).setPositiveButton("Save",(d,k)->model.setNotes(input.getText().toString())).setNegativeButton("Cancel",null).show();}
    private void picker(){
        LinearLayout content=column(this);content.setPadding(dp(this,16),0,dp(this,16),0);EditText search=input(this,"Search exercise, muscle, equipment","",false);add(content,search,0);add(content,text(this,"Hold a movement to add or remove a favorite ?",11,MUTED,false),8);
        LinearLayout result=column(this);ScrollView scroll=ForgeUi.scroll(this,result);content.addView(scroll,new LinearLayout.LayoutParams(-1,dp(this,350)));
        androidx.appcompat.app.AlertDialog dialog=new MaterialAlertDialogBuilder(this).setTitle("Add a movement").setView(content).setNegativeButton("Done",null).create();
        Runnable filter=()->{result.removeAllViews();String q=search.getText().toString().toLowerCase(Locale.ROOT);int count=0;Set<String> favorites=getSharedPreferences("training",0).getStringSet("favorites",new HashSet<>());
            List<Exercise> sorted=new ArrayList<>(library);sorted.sort(Comparator.comparing((Exercise e)->!favorites.contains(String.valueOf(e.getId()))));
            for(Exercise e:sorted)if((e.getName()+" "+e.getMuscleGroup()+" "+e.getEquipment()).toLowerCase(Locale.ROOT).contains(q)){if(count++>=80)break;LinearLayout item=card(this);add(item,text(this,(favorites.contains(String.valueOf(e.getId()))?"★  ":"")+e.getName(),15,TEXT,true),0);add(item,text(this,e.getMuscleGroup()+" · "+e.getEquipment(),11,MUTED,false),5);item.setOnClickListener(v->{model.addExercise(e);dialog.dismiss();});item.setOnLongClickListener(v->{Set<String> next=new HashSet<>(getSharedPreferences("training",0).getStringSet("favorites",new HashSet<>()));String id=String.valueOf(e.getId());if(!next.add(id))next.remove(id);getSharedPreferences("training",0).edit().putStringSet("favorites",next).apply();Toast.makeText(this,next.contains(id)?"Added to favorites":"Removed from favorites",Toast.LENGTH_SHORT).show();return true;});add(result,item,8);}
            if(count==0)add(result,text(this,library.isEmpty()?"Loading exercise library. Close and reopen in a moment.":"No matching movements",13,MUTED,false),20);};
        TrainingHubFragment.watch(search,filter);filter.run();dialog.show();
    }
    private void finishSession(){if(model.completedCount()==0){Toast.makeText(this,"Complete a set first using the check button.",Toast.LENGTH_LONG).show();return;}
        new MaterialAlertDialogBuilder(this).setTitle("Finish strong.").setMessage(model.completedCount()+" completed sets will be saved. Unchecked sets stay out of your history.").setPositiveButton("Save session",(d,k)->model.finishWorkout(model.elapsedSeconds())).setNegativeButton("Keep training",null).show();}
    private void celebrate(){LinearLayout content=card(this);add(content,tag(this,"SESSION COMPLETE",LIME),0);add(content,text(this,"Work put in.\nProgress earned.",30,TEXT,true),16);add(content,text(this,"+"+model.savedXp+" XP",34,LIME,true),20);add(content,text(this,String.format(Locale.getDefault(),"%d sets · %,.0f kg volume",model.savedSets,model.savedVolume),15,MUTED,false),10);if(model.newRecords>0)add(content,text(this,model.newRecords+" new load records!",16,ORANGE,true),12);
        new MaterialAlertDialogBuilder(this).setView(content).setCancelable(false).setPositiveButton("Back to my journey",(d,k)->finish()).show();}
    private void leave(){if(Boolean.TRUE.equals(model.getSaving().getValue()))return;new MaterialAlertDialogBuilder(this).setTitle("Pause your session?").setMessage("Your workout is saved as a draft. Resume whenever you're ready.").setPositiveButton("Save & leave",(d,k)->{model.pauseSession();finish();}).setNegativeButton("Keep training",null).setNeutralButton("Discard",(d,k)->new MaterialAlertDialogBuilder(this).setTitle("Discard this workout?").setMessage("Its unsaved sets will be removed.").setPositiveButton("Discard",(a,b)->{model.discard();finish();}).setNegativeButton("Keep draft",null).show()).show();}
}
