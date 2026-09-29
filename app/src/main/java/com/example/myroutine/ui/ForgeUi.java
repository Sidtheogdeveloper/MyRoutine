package com.example.myroutine.ui;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.content.res.ColorStateList;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import com.google.android.material.button.MaterialButton;

/** Shared visual language for the training experience. */
public final class ForgeUi {
    public static final int BG=0xff101210, CARD=0xff1b1e1a, LINE=0xff30362d,
            TEXT=0xfff2f4eb, MUTED=0xff9ca595, LIME=0xffd4fa74, ORANGE=0xffffb074, PURPLE=0xffb9a2f6;
    private ForgeUi() {}
    public static void darkBars(androidx.appcompat.app.AppCompatActivity activity) {
        androidx.core.view.WindowInsetsControllerCompat controller=new androidx.core.view.WindowInsetsControllerCompat(activity.getWindow(),activity.getWindow().getDecorView());
        controller.setAppearanceLightStatusBars(false);controller.setAppearanceLightNavigationBars(false);
    }
    public static void insets(androidx.appcompat.app.AppCompatActivity activity,View view) {
        androidx.activity.EdgeToEdge.enable(activity);darkBars(activity);
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(view,(v,insets)->{
            androidx.core.graphics.Insets b=insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars()|androidx.core.view.WindowInsetsCompat.Type.ime());
            v.setPadding(b.left,b.top,b.right,b.bottom);return insets;
        });
    }
    public static int dp(Context c, float n) { return (int)(n*c.getResources().getDisplayMetrics().density+.5f); }
    public static GradientDrawable shape(Context c,int color,int radius) {
        GradientDrawable d=new GradientDrawable(); d.setColor(color); d.setCornerRadius(dp(c,radius)); return d;
    }
    public static LinearLayout column(Context c) { LinearLayout l=new LinearLayout(c); l.setOrientation(1); return l; }
    public static LinearLayout row(Context c) { LinearLayout l=new LinearLayout(c); l.setGravity(Gravity.CENTER_VERTICAL); return l; }
    public static TextView text(Context c,String s,int size,int color,boolean bold) {
        TextView t=new TextView(c); t.setText(s); t.setTextSize(size); t.setTextColor(color);
        t.setFontFeatureSettings("tnum"); if(bold)t.setTypeface(Typeface.create("sans-serif-medium",Typeface.NORMAL));
        return t;
    }
    public static void add(LinearLayout l,View v,int top) {
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2); p.topMargin=dp(l.getContext(),top); l.addView(v,p);
    }
    public static void space(LinearLayout l,int h) { View v=new View(l.getContext()); l.addView(v,new LinearLayout.LayoutParams(1,dp(l.getContext(),h))); }
    public static LinearLayout card(Context c) {
        LinearLayout l=column(c); l.setBackground(shape(c,CARD,22)); int p=dp(c,20); l.setPadding(p,p,p,p); return l;
    }
    public static TextView tag(Context c,String s,int color) {
        TextView t=text(c,s,11,color,true); t.setLetterSpacing(.1f); t.setPadding(dp(c,10),dp(c,6),dp(c,10),dp(c,6));
        t.setBackground(shape(c,(color&0x00ffffff)|0x18000000,8)); return t;
    }
    public static MaterialButton button(Context c,String s,boolean primary,Runnable action) {
        MaterialButton b=new MaterialButton(c); b.setText(s); b.setAllCaps(false); b.setTextSize(15);
        b.setCornerRadius(dp(c,14)); b.setMinHeight(dp(c,54)); b.setInsetTop(0); b.setInsetBottom(0);
        b.setBackgroundTintList(ColorStateList.valueOf(primary?LIME:LINE)); b.setTextColor(primary?BG:TEXT);
        b.setOnClickListener(v->action.run()); return b;
    }
    public static void progress(LinearLayout l,int value,int max,int color) {
        ProgressBar p=new ProgressBar(l.getContext(),null,android.R.attr.progressBarStyleHorizontal);
        p.setMax(Math.max(1,max)); p.setProgress(value); p.setProgressTintList(ColorStateList.valueOf(color));
        p.setProgressBackgroundTintList(ColorStateList.valueOf(LINE));
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(l.getContext(),8));lp.topMargin=dp(l.getContext(),12); l.addView(p,lp);
    }
    public static EditText input(Context c,String hint,String value,boolean numeric) {
        EditText e=new EditText(c); e.setTextColor(TEXT);e.setHintTextColor(MUTED); e.setTextSize(15);
        e.setSingleLine(true);e.setHint(hint);e.setText(value);e.setPadding(dp(c,12),dp(c,8),dp(c,12),dp(c,8));
        e.setBackground(shape(c,BG,10));e.setMinHeight(dp(c,48));
        if(numeric)e.setInputType(android.text.InputType.TYPE_CLASS_NUMBER|android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        return e;
    }
    public static ScrollView scroll(Context c,LinearLayout content) {
        ScrollView s=new ScrollView(c);s.setFillViewport(true);s.setClipToPadding(false);s.setBackgroundColor(BG);
        s.addView(content);s.setVerticalScrollBarEnabled(false);return s;
    }
}
