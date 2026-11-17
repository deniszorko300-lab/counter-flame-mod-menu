package com.counterflame.overlay;
import android.app.*;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
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
    private WindowManager wm; private View bubble,panel; private WindowManager.LayoutParams bubbleParams;
    @Override public void onCreate() {
        super.onCreate(); createChannel();
        Notification.Builder nb=Build.VERSION.SDK_INT>=26?new Notification.Builder(this,CHANNEL):new Notification.Builder(this);
        startForeground(7,nb.setContentTitle("Counter Flame Overlay").setContentText("Плавающая панель работает")
                .setSmallIcon(android.R.drawable.ic_menu_view).setOngoing(true).build());
        wm=(WindowManager)getSystemService(WINDOW_SERVICE); showBubble();
    }
    private int type(){return Build.VERSION.SDK_INT>=26?WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY:WindowManager.LayoutParams.TYPE_PHONE;}
    private void showBubble(){
        if(bubble!=null)return;
        TextView b=new TextView(this); b.setText("CF"); b.setTextColor(Color.WHITE); b.setTextSize(16);
        b.setGravity(Gravity.CENTER); b.setBackgroundColor(Color.rgb(210,45,45)); bubble=b;
        bubbleParams=new WindowManager.LayoutParams(58,58,type(),WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,PixelFormat.TRANSLUCENT);
        bubbleParams.gravity=Gravity.TOP|Gravity.START; bubbleParams.x=24; bubbleParams.y=240;
        b.setOnTouchListener(new View.OnTouchListener(){
            float downX,downY; int sx,sy; boolean moved;
            public boolean onTouch(View v,MotionEvent e){
                if(e.getAction()==MotionEvent.ACTION_DOWN){downX=e.getRawX();downY=e.getRawY();sx=bubbleParams.x;sy=bubbleParams.y;moved=false;return true;}
                if(e.getAction()==MotionEvent.ACTION_MOVE){int dx=(int)(e.getRawX()-downX),dy=(int)(e.getRawY()-downY);
                    if(Math.abs(dx)+Math.abs(dy)>12)moved=true; bubbleParams.x=sx+dx;bubbleParams.y=sy+dy;wm.updateViewLayout(bubble,bubbleParams);return true;}
                if(e.getAction()==MotionEvent.ACTION_UP){if(!moved)togglePanel();return true;} return true;
            }});
        wm.addView(bubble,bubbleParams);
    }
    private void togglePanel(){
        if(panel!=null){wm.removeView(panel);panel=null;return;}
        LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(18,14,18,14);box.setBackgroundColor(Color.rgb(32,32,38));
        TextView h=new TextView(this);h.setText("COUNTER FLAME");h.setTextColor(Color.WHITE);h.setTextSize(17);box.addView(h);
        TextView n=new TextView(this);n.setText("Быстрая панель");n.setTextColor(Color.LTGRAY);box.addView(n);
        addButton(box,"Скрыть меню",this::togglePanel);addButton(box,"Скрыть кнопку",this::removeBubble);addButton(box,"Закрыть оверлей",this::stopSelf);
        panel=box;WindowManager.LayoutParams p=new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,type(),WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,PixelFormat.TRANSLUCENT);
        p.gravity=Gravity.TOP|Gravity.START;p.x=bubbleParams.x;p.y=bubbleParams.y+68;wm.addView(panel,p);
    }
    private void addButton(LinearLayout box,String text,Runnable action){Button b=new Button(this);b.setText(text);b.setAllCaps(false);b.setOnClickListener(v->action.run());box.addView(b);}
    private void removeBubble(){if(panel!=null){wm.removeView(panel);panel=null;}if(bubble!=null){wm.removeView(bubble);bubble=null;}}
    private void createChannel(){if(Build.VERSION.SDK_INT>=26){NotificationChannel c=new NotificationChannel(CHANNEL,"Работа оверлея",NotificationManager.IMPORTANCE_LOW);
        ((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).createNotificationChannel(c);}}
    @Override public void onDestroy(){if(panel!=null)wm.removeView(panel);if(bubble!=null)wm.removeView(bubble);panel=null;bubble=null;super.onDestroy();}
    @Override public IBinder onBind(Intent i){return null;}
}
