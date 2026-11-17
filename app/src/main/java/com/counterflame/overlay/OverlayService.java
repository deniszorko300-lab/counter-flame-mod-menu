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
import android.widget.TextView;

public class OverlayService extends Service {
    private static final String CHANNEL="overlay_service";
    private WindowManager wm;
    private View bubble,panel;
    private WindowManager.LayoutParams bubbleParams;
    private int red=Color.rgb(239,55,67), dark=Color.rgb(18,22,31), card=Color.rgb(29,35,48);

    @Override public void onCreate(){
        super.onCreate();createChannel();
        Notification.Builder nb=Build.VERSION.SDK_INT>=26?new Notification.Builder(this,CHANNEL):new Notification.Builder(this);
        startForeground(7,nb.setContentTitle("Counter Flame • CF").setContentText("Плавающая панель активна").setSmallIcon(android.R.drawable.ic_menu_view).setOngoing(true).build());
        wm=(WindowManager)getSystemService(WINDOW_SERVICE);showBubble();
    }
    private int type(){return Build.VERSION.SDK_INT>=26?WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY:WindowManager.LayoutParams.TYPE_PHONE;}
    private int dp(int v){return (int)(v*getResources().getDisplayMetrics().density+0.5f);}
    private GradientDrawable shape(int color,int radius,int stroke){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));if(stroke!=0)d.setStroke(dp(1),stroke);return d;}
    private TextView label(String s,int size,int color,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);if(bold)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);return t;}
    private void showBubble(){
        if(bubble!=null)return;
        TextView b=label("CF",17,Color.WHITE,true);b.setGravity(Gravity.CENTER);b.setBackground(shape(red,20,Color.rgb(255,130,138)));bubble=b;
        bubbleParams=new WindowManager.LayoutParams(dp(58),dp(58),type(),WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,PixelFormat.TRANSLUCENT);
        bubbleParams.gravity=Gravity.TOP|Gravity.START;bubbleParams.x=dp(20);bubbleParams.y=dp(220);
        b.setElevation(dp(8));
        b.setOnTouchListener(new View.OnTouchListener(){float downX,downY;int sx,sy;boolean moved;
            public boolean onTouch(View v,MotionEvent e){
                if(e.getAction()==MotionEvent.ACTION_DOWN){downX=e.getRawX();downY=e.getRawY();sx=bubbleParams.x;sy=bubbleParams.y;moved=false;return true;}
                if(e.getAction()==MotionEvent.ACTION_MOVE){int dx=(int)(e.getRawX()-downX),dy=(int)(e.getRawY()-downY);if(Math.abs(dx)+Math.abs(dy)>dp(8))moved=true;bubbleParams.x=sx+dx;bubbleParams.y=sy+dy;wm.updateViewLayout(bubble,bubbleParams);return true;}
                if(e.getAction()==MotionEvent.ACTION_UP){if(!moved)togglePanel();return true;}return true;
            }});
        wm.addView(bubble,bubbleParams);
    }
    private void togglePanel(){if(panel!=null){wm.removeView(panel);panel=null;return;}showPanel();}
    private void showPanel(){
        LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(dp(18),dp(17),dp(18),dp(17));box.setBackground(shape(dark,22,Color.rgb(65,74,94)));box.setElevation(dp(12));
        LinearLayout header=new LinearLayout(this);header.setGravity(Gravity.CENTER_VERTICAL);header.setOrientation(LinearLayout.HORIZONTAL);
        TextView logo=label("CF",15,Color.WHITE,true);logo.setGravity(Gravity.CENTER);logo.setBackground(shape(red,12,0));header.addView(logo,new LinearLayout.LayoutParams(dp(42),dp(42)));
        LinearLayout names=new LinearLayout(this);names.setOrientation(LinearLayout.VERTICAL);names.setPadding(dp(11),0,0,0);
        names.addView(label("COUNTER FLAME",15,Color.WHITE,true));names.addView(label("TEST OVERLAY  •  ACTIVE",10,Color.rgb(151,164,185),true));header.addView(names,new LinearLayout.LayoutParams(0,-2,1));
        box.addView(header);
        View line=new View(this);line.setBackgroundColor(Color.rgb(54,63,81));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(1));lp.topMargin=dp(15);lp.bottomMargin=dp(13);box.addView(line,lp);
        box.addView(label("БЫСТРОЕ УПРАВЛЕНИЕ",10,red,true));
        TextView desc=label("Перетащи кнопку CF в удобное место. Нажми на неё, чтобы открыть это меню.",12,Color.rgb(174,184,201),false);
        LinearLayout.LayoutParams dp1=new LinearLayout.LayoutParams(-1,-2);dp1.topMargin=dp(8);dp1.bottomMargin=dp(14);box.addView(desc,dp1);
        addButton(box,"СВЕРНУТЬ МЕНЮ",this::togglePanel,false);
        addButton(box,"СКРЫТЬ КНОПКУ CF",this::removeBubble,true);
        addButton(box,"ЗАКРЫТЬ ОВЕРЛЕЙ",this::stopSelf,false);
        panel=box;
        WindowManager.LayoutParams p=new WindowManager.LayoutParams(dp(290),WindowManager.LayoutParams.WRAP_CONTENT,type(),WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,PixelFormat.TRANSLUCENT);
        p.gravity=Gravity.TOP|Gravity.START;p.x=Math.max(dp(8),bubbleParams.x-dp(110));p.y=bubbleParams.y+dp(68);
        wm.addView(panel,p);
    }
    private void addButton(LinearLayout box,String text,Runnable action,boolean outline){
        Button b=new Button(this);b.setText(text);b.setTextSize(11);b.setTextColor(Color.WHITE);b.setAllCaps(false);b.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        b.setBackground(shape(outline?card:red,13,outline?Color.rgb(75,87,110):0));b.setOnClickListener(v->action.run());
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(48));p.topMargin=dp(7);box.addView(b,p);
    }
    private void removeBubble(){if(panel!=null){wm.removeView(panel);panel=null;}if(bubble!=null){wm.removeView(bubble);bubble=null;}}
    private void createChannel(){if(Build.VERSION.SDK_INT>=26){NotificationChannel c=new NotificationChannel(CHANNEL,"Работа оверлея",NotificationManager.IMPORTANCE_LOW);((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).createNotificationChannel(c);}}
    @Override public void onDestroy(){if(panel!=null)wm.removeView(panel);if(bubble!=null)wm.removeView(bubble);panel=null;bubble=null;super.onDestroy();}
    @Override public IBinder onBind(Intent i){return null;}
}
