package com.counterflame.overlay;

import android.app.*;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.IBinder;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;

public class OverlayService extends Service {
    private static final String CHANNEL="overlay_service";
    private WindowManager wm;
    private View bubble,panel;
    private WindowManager.LayoutParams bubbleParams;
    private final int red=Color.rgb(255,55,70), dark=Color.rgb(12,15,23), card=Color.rgb(24,30,42);
    private final int muted=Color.rgb(148,160,180), white=Color.WHITE;

    @Override public void onCreate(){
        super.onCreate();createChannel();
        Notification.Builder nb=Build.VERSION.SDK_INT>=26?new Notification.Builder(this,CHANNEL):new Notification.Builder(this);
        startForeground(7,nb.setContentTitle("Counter Flame • CF").setContentText("Тестовая оболочка активна").setSmallIcon(android.R.drawable.ic_menu_view).setOngoing(true).build());
        wm=(WindowManager)getSystemService(WINDOW_SERVICE);showBubble();
    }
    private int type(){return Build.VERSION.SDK_INT>=26?WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY:WindowManager.LayoutParams.TYPE_PHONE;}
    private int dp(int v){return (int)(v*getResources().getDisplayMetrics().density+0.5f);}
    private GradientDrawable shape(int color,int radius,int stroke){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));if(stroke!=0)d.setStroke(dp(1),stroke);return d;}
    private TextView label(String s,int size,int color,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);if(bold)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);return t;}
    private void showBubble(){
        if(bubble!=null)return;
        TextView b=label("CF",17,white,true);b.setGravity(Gravity.CENTER);b.setBackground(shape(red,19,Color.rgb(255,145,153)));bubble=b;
        bubbleParams=new WindowManager.LayoutParams(dp(58),dp(58),type(),WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,PixelFormat.TRANSLUCENT);
        bubbleParams.gravity=Gravity.TOP|Gravity.START;bubbleParams.x=dp(20);bubbleParams.y=dp(220);b.setElevation(dp(9));
        b.setOnTouchListener(new View.OnTouchListener(){float downX,downY;int sx,sy;boolean moved;
            public boolean onTouch(View v,MotionEvent e){
                if(e.getAction()==MotionEvent.ACTION_DOWN){downX=e.getRawX();downY=e.getRawY();sx=bubbleParams.x;sy=bubbleParams.y;moved=false;return true;}
                if(e.getAction()==MotionEvent.ACTION_MOVE){int dx=(int)(e.getRawX()-downX),dy=(int)(e.getRawY()-downY);if(Math.abs(dx)+Math.abs(dy)>dp(8))moved=true;bubbleParams.x=sx+dx;bubbleParams.y=sy+dy;wm.updateViewLayout(bubble,bubbleParams);return true;}
                if(e.getAction()==MotionEvent.ACTION_UP){if(!moved)togglePanel();return true;}return true;
            }});
        wm.addView(bubble,bubbleParams);
    }
    private void togglePanel(){if(panel!=null){wm.removeView(panel);panel=null;}else showPanel();}
    private void showPanel(){
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(dp(17),dp(16),dp(17),dp(15));root.setBackground(shape(dark,23,Color.rgb(57,67,86)));root.setElevation(dp(16));
        LinearLayout header=new LinearLayout(this);header.setGravity(Gravity.CENTER_VERTICAL);
        TextView logo=label("CF",16,white,true);logo.setGravity(Gravity.CENTER);logo.setBackground(shape(red,13,0));header.addView(logo,new LinearLayout.LayoutParams(dp(44),dp(44)));
        LinearLayout brand=new LinearLayout(this);brand.setOrientation(LinearLayout.VERTICAL);brand.setPadding(dp(11),0,0,0);
        brand.addView(label("COUNTER FLAME",14,white,true));brand.addView(label("CONTROL CENTER  •  DEMO",9,muted,true));header.addView(brand,new LinearLayout.LayoutParams(0,-2,1));
        TextView close=label("×",27,muted,false);close.setGravity(Gravity.CENTER);header.addView(close,new LinearLayout.LayoutParams(dp(34),dp(40)));close.setOnClickListener(v->togglePanel());root.addView(header);
        View divider=new View(this);divider.setBackgroundColor(Color.rgb(48,57,74));LinearLayout.LayoutParams dl=new LinearLayout.LayoutParams(-1,dp(1));dl.topMargin=dp(14);dl.bottomMargin=dp(12);root.addView(divider,dl);
        ScrollView scroll=new ScrollView(this);LinearLayout items=new LinearLayout(this);items.setOrientation(LinearLayout.VERTICAL);
        section(items,"МОНИТОРИНГ");
        addToggle(items,"FPS-индикатор","Демонстрационный переключатель");
        addToggle(items,"Сетевой статус","Демонстрационный переключатель");
        addToggle(items,"Журнал тестирования","Демонстрационный переключатель");
        section(items,"ИНСТРУМЕНТЫ");
        addToggle(items,"Отображение касаний","Демонстрационный переключатель");
        addToggle(items,"Режим тестовой разметки","Демонстрационный переключатель");
        TextView foot=label("Это визуальная оболочка. Переключатели пока не подключены к игре.",10,muted,false);
        foot.setPadding(dp(2),dp(13),dp(2),dp(5));items.addView(foot);
        scroll.addView(items);root.addView(scroll,new LinearLayout.LayoutParams(-1,dp(310)));
        Button hide=button("СВЕРНУТЬ ПАНЕЛЬ",card);LinearLayout.LayoutParams hp=new LinearLayout.LayoutParams(-1,dp(45));hp.topMargin=dp(10);root.addView(hide,hp);hide.setOnClickListener(v->togglePanel());
        panel=root;
        WindowManager.LayoutParams p=new WindowManager.LayoutParams(dp(310),WindowManager.LayoutParams.WRAP_CONTENT,type(),WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,PixelFormat.TRANSLUCENT);
        p.gravity=Gravity.TOP|Gravity.START;p.x=Math.max(dp(6),bubbleParams.x-dp(125));p.y=bubbleParams.y+dp(66);
        wm.addView(panel,p);
    }
    private void section(LinearLayout parent,String title){TextView t=label(title,10,red,true);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.topMargin=dp(5);p.bottomMargin=dp(8);parent.addView(t,p);}
    private void addToggle(LinearLayout parent,String title,String hint){
        LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(dp(12),dp(9),dp(9),dp(9));row.setBackground(shape(card,14,Color.rgb(42,51,68)));
        LinearLayout words=new LinearLayout(this);words.setOrientation(LinearLayout.VERTICAL);words.addView(label(title,13,white,true));TextView sub=label(hint,9,muted,false);LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(-1,-2);sp.topMargin=dp(3);words.addView(sub,sp);
        row.addView(words,new LinearLayout.LayoutParams(0,-2,1));
        Switch sw=new Switch(this);sw.setChecked(false);row.addView(sw);
        LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(-1,-2);rp.bottomMargin=dp(7);parent.addView(row,rp);
    }
    private Button button(String s,int color){Button b=new Button(this);b.setText(s);b.setTextSize(10);b.setTextColor(white);b.setAllCaps(false);b.setTypeface(Typeface.DEFAULT,Typeface.BOLD);b.setBackground(shape(color,13,Color.rgb(62,73,94)));return b;}
    private void removeBubble(){if(panel!=null){wm.removeView(panel);panel=null;}if(bubble!=null){wm.removeView(bubble);bubble=null;}}
    private void createChannel(){if(Build.VERSION.SDK_INT>=26){NotificationChannel c=new NotificationChannel(CHANNEL,"Работа оверлея",NotificationManager.IMPORTANCE_LOW);((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).createNotificationChannel(c);}}
    @Override public void onDestroy(){if(panel!=null)wm.removeView(panel);if(bubble!=null)wm.removeView(bubble);panel=null;bubble=null;super.onDestroy();}
    @Override public IBinder onBind(Intent i){return null;}
}