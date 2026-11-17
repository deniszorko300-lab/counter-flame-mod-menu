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
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;

public class OverlayService extends Service {
    private static final String CHANNEL="overlay_service";
    private WindowManager wm;
    private View bubble,panel;
    private WindowManager.LayoutParams bubbleParams;
    private LinearLayout pageContent, tabStrip;
    private String selectedTab="Главное";
    private final String[] tabs={"Главное","ESP","Игроки","Оружие","Прочее","Настройки","О нас"};
    private final int red=Color.rgb(255,55,70), dark=Color.rgb(10,12,18),
        card=Color.rgb(23,27,37), card2=Color.rgb(30,35,47),
        muted=Color.rgb(137,147,165), white=Color.WHITE, line=Color.rgb(48,54,69);

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
        bubbleParams=new WindowManager.LayoutParams(dp(54),dp(54),type(),WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,PixelFormat.TRANSLUCENT);
        bubbleParams.gravity=Gravity.TOP|Gravity.START;bubbleParams.x=dp(18);bubbleParams.y=dp(210);b.setElevation(dp(9));
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
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(13),dp(13),dp(13),dp(12));root.setBackground(shape(dark,21,Color.rgb(65,48,56)));root.setElevation(dp(18));
        LinearLayout header=new LinearLayout(this);header.setGravity(Gravity.CENTER_VERTICAL);
        TextView logo=label("CF",16,white,true);logo.setGravity(Gravity.CENTER);logo.setBackground(shape(red,12,0));header.addView(logo,new LinearLayout.LayoutParams(dp(39),dp(39)));
        LinearLayout brand=new LinearLayout(this);brand.setOrientation(LinearLayout.VERTICAL);brand.setPadding(dp(9),0,0,0);
        brand.addView(label("COUNTER FLAME",13,white,true));brand.addView(label("MOD MENU  •  v1.0",9,muted,true));header.addView(brand,new LinearLayout.LayoutParams(0,-2,1));
        TextView status=label("DEMO",9,red,true);status.setPadding(dp(8),dp(5),dp(8),dp(5));status.setBackground(shape(Color.rgb(54,24,31),8,Color.rgb(111,43,54)));header.addView(status);
        TextView close=label("×",25,muted,false);close.setGravity(Gravity.CENTER);header.addView(close,new LinearLayout.LayoutParams(dp(31),dp(38)));close.setOnClickListener(v->togglePanel());root.addView(header);
        View divider=new View(this);divider.setBackgroundColor(line);LinearLayout.LayoutParams dl=new LinearLayout.LayoutParams(-1,dp(1));dl.topMargin=dp(11);dl.bottomMargin=dp(9);root.addView(divider,dl);

        HorizontalScrollView tabsScroll=new HorizontalScrollView(this);tabsScroll.setHorizontalScrollBarEnabled(false);
        tabStrip=new LinearLayout(this);tabStrip.setOrientation(LinearLayout.HORIZONTAL);tabsScroll.addView(tabStrip);root.addView(tabsScroll,new LinearLayout.LayoutParams(-1,dp(39)));
        pageContent=new LinearLayout(this);pageContent.setOrientation(LinearLayout.VERTICAL);
        ScrollView scroll=new ScrollView(this);scroll.setFillViewport(false);scroll.addView(pageContent);
        LinearLayout.LayoutParams contentParams=new LinearLayout.LayoutParams(-1,dp(340));contentParams.topMargin=dp(9);root.addView(scroll,contentParams);
        TextView foot=label("ВИЗУАЛЬНЫЙ МАКЕТ • НЕ ВЛИЯЕТ НА ИГРУ",8,muted,true);foot.setGravity(Gravity.CENTER);LinearLayout.LayoutParams fp=new LinearLayout.LayoutParams(-1,-2);fp.topMargin=dp(8);root.addView(foot,fp);
        Button hide=button("СВЕРНУТЬ",card2);LinearLayout.LayoutParams hp=new LinearLayout.LayoutParams(-1,dp(40));hp.topMargin=dp(9);root.addView(hide,hp);hide.setOnClickListener(v->togglePanel());
        panel=root;
        WindowManager.LayoutParams p=new WindowManager.LayoutParams(dp(316),WindowManager.LayoutParams.WRAP_CONTENT,type(),WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,PixelFormat.TRANSLUCENT);
        p.gravity=Gravity.TOP|Gravity.START;p.x=Math.max(dp(5),bubbleParams.x-dp(130));p.y=bubbleParams.y+dp(61);
        wm.addView(panel,p);
        renderTabs();renderPage();
    }
    private void renderTabs(){
        tabStrip.removeAllViews();
        for(String tab:tabs){
            TextView t=label(tab,10,selectedTab.equals(tab)?white:muted,true);t.setGravity(Gravity.CENTER);
            t.setPadding(dp(11),0,dp(11),0);t.setBackground(shape(selectedTab.equals(tab)?red:card,10,selectedTab.equals(tab)?red:line));
            LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-2,dp(34));p.rightMargin=dp(6);tabStrip.addView(t,p);
            t.setOnClickListener(v->{selectedTab=tab;renderTabs();renderPage();});
        }
    }
    private void renderPage(){
        pageContent.removeAllViews();
        switch(selectedTab){
            case "Главное":
                section("БЫСТРЫЙ ДОСТУП");
                addToggle("FPS-индикатор","Демонстрационный элемент интерфейса");
                addToggle("Сетевой статус","Только макет отображения");
                addToggle("Журнал тестирования","Не подключён к игровому клиенту");
                info("Панель CF готова к визуальному тестированию. Игровые данные не считываются.");
                break;
            case "ESP":
                section("ESP • МАКЕТ НАСТРОЕК");
                addToggle("ESP игроков","Переключатель интерфейса, не работает в игре");
                addToggle("ESP оружия","Переключатель интерфейса, не работает в игре");
                addToggle("ESP гранат","Переключатель интерфейса, не работает в игре");
                addToggle("Показывать здоровье","Демонстрационный переключатель");
                addToggle("Показывать дистанцию","Демонстрационный переключатель");
                break;
            case "Игроки":
                section("ОТОБРАЖЕНИЕ ИГРОКОВ");
                addToggle("Список игроков","Демонстрационный пункт");
                addToggle("Отображать имя","Демонстрационный пункт");
                addToggle("Отображать команду","Демонстрационный пункт");
                info("Здесь можно разместить безопасные инструменты проверки интерфейса и отчёты тестирования.");
                break;
            case "Оружие":
                section("ПАРАМЕТРЫ ОРУЖИЯ • МАКЕТ");
                addToggle("Информация об оружии","Демонстрационный пункт");
                addToggle("Счётчик боеприпасов","Демонстрационный пункт");
                addToggle("Статус перезарядки","Демонстрационный пункт");
                break;
            case "Прочее":
                section("ДОПОЛНИТЕЛЬНО");
                addToggle("Отображение касаний","Локальный элемент макета");
                addToggle("Тестовая разметка","Не взаимодействует с игрой");
                addToggle("Журнал событий","Демонстрационный переключатель");
                break;
            case "Настройки":
                section("НАСТРОЙКИ ПАНЕЛИ");
                addToggle("Компактный режим","Макет настройки");
                addToggle("Закрепить панель","Макет настройки");
                addToggle("Звуки интерфейса","Макет настройки");
                info("Настройки внешнего вида будут подключены отдельно.");
                break;
            default:
                section("О ПРОЕКТЕ");
                info("COUNTER FLAME MOD MENU\nВерсия интерфейса 1.0\nAndroid overlay • Java\n\nЭто тестовая визуальная оболочка. Она не внедряется в игру, не читает память и не обходит защиту.");
                break;
        }
    }
    private void section(String title){TextView t=label(title,10,red,true);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.topMargin=dp(5);p.bottomMargin=dp(9);pageContent.addView(t,p);}
    private void addToggle(String title,String hint){
        LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(dp(11),dp(9),dp(8),dp(9));row.setBackground(shape(card,12,line));
        LinearLayout words=new LinearLayout(this);words.setOrientation(LinearLayout.VERTICAL);words.addView(label(title,12,white,true));
        TextView sub=label(hint,8,muted,false);LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(-1,-2);sp.topMargin=dp(3);words.addView(sub,sp);
        row.addView(words,new LinearLayout.LayoutParams(0,-2,1));
        Switch sw=new Switch(this);sw.setChecked(false);sw.setScaleX(.82f);sw.setScaleY(.82f);row.addView(sw);
        LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(-1,-2);rp.bottomMargin=dp(6);pageContent.addView(row,rp);
    }
    private void info(String text){TextView t=label(text,10,muted,false);t.setPadding(dp(11),dp(12),dp(11),dp(12));t.setBackground(shape(card2,12,line));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.topMargin=dp(5);pageContent.addView(t,p);}
    private Button button(String s,int color){Button b=new Button(this);b.setText(s);b.setTextSize(10);b.setTextColor(white);b.setAllCaps(false);b.setTypeface(Typeface.DEFAULT,Typeface.BOLD);b.setBackground(shape(color,12,line));return b;}
    private void createChannel(){if(Build.VERSION.SDK_INT>=26){NotificationChannel c=new NotificationChannel(CHANNEL,"Работа оверлея",NotificationManager.IMPORTANCE_LOW);((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).createNotificationChannel(c);}}
    @Override public void onDestroy(){if(panel!=null)wm.removeView(panel);if(bubble!=null)wm.removeView(bubble);panel=null;bubble=null;super.onDestroy();}
    @Override public IBinder onBind(Intent i){return null;}
}