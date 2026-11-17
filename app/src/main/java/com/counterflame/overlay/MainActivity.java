package com.counterflame.overlay;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
    private TextView status;
    private int bg = Color.rgb(13, 16, 23), card = Color.rgb(24, 29, 40), red = Color.rgb(239, 55, 67);

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(22), dp(28), dp(22), dp(24));
        root.setBackgroundColor(bg);

        TextView brand = text("CF  /  COUNTER FLAME", 13, red, true);
        root.addView(brand);
        TextView title = text("MOD MENU", 30, Color.WHITE, true);
        LinearLayout.LayoutParams tp = params(-1, -2); tp.topMargin = dp(8); root.addView(title, tp);
        TextView subtitle = text("Панель тестирования и быстрых инструментов", 14, Color.rgb(160,169,185), false);
        LinearLayout.LayoutParams sp = params(-1,-2); sp.topMargin=dp(5); root.addView(subtitle,sp);

        LinearLayout statusCard = card();
        TextView st = text("СОСТОЯНИЕ ОВЕРЛЕЯ", 11, red, true);
        statusCard.addView(st);
        status = text("", 15, Color.WHITE, false);
        LinearLayout.LayoutParams stp=params(-1,-2);stp.topMargin=dp(8);statusCard.addView(status,stp);
        LinearLayout.LayoutParams cp=params(-1,-2);cp.topMargin=dp(24);root.addView(statusCard,cp);

        TextView tools = text("УПРАВЛЕНИЕ", 12, Color.rgb(160,169,185), true);
        LinearLayout.LayoutParams tlp=params(-1,-2);tlp.topMargin=dp(25);root.addView(tools,tlp);
        Button permission = button("РАЗРЕШЕНИЕ ПОВЕРХ ОКОН");
        root.addView(permission, buttonParams());
        permission.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= 23 && !Settings.canDrawOverlays(this)) {
                startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:"+getPackageName())));
            } else startOverlay();
        });
        Button start = button("ЗАПУСТИТЬ ПЛАВАЮЩУЮ ПАНЕЛЬ");
        LinearLayout.LayoutParams bp=buttonParams();bp.topMargin=dp(10);root.addView(start,bp);
        start.setOnClickListener(v -> startOverlay());
        Button stop = button("ОСТАНОВИТЬ ПАНЕЛЬ");
        LinearLayout.LayoutParams xp=buttonParams();xp.topMargin=dp(10);root.addView(stop,xp);
        stop.setOnClickListener(v -> { stopService(new Intent(this,OverlayService.class)); updateStatus(); });

        TextView note = text("ТЕСТОВЫЙ ИНТЕРФЕЙС\nОверлей управляет только собственной панелью и не изменяет игровой клиент.", 12, Color.rgb(135,146,164), false);
        LinearLayout.LayoutParams np=params(-1,-2);np.topMargin=dp(25);root.addView(note,np);
        setContentView(root);
        updateStatus();
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},10);
    }

    private LinearLayout card() {
        LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);l.setPadding(dp(18),dp(17),dp(18),dp(17));
        GradientDrawable d=new GradientDrawable();d.setColor(card);d.setCornerRadius(dp(18));d.setStroke(dp(1),Color.rgb(42,49,64));l.setBackground(d);return l;
    }
    private TextView text(String s,int size,int color,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);if(bold)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);return t;}
    private Button button(String s){Button b=new Button(this);b.setText(s);b.setTextSize(12);b.setTextColor(Color.WHITE);b.setAllCaps(false);b.setTypeface(Typeface.DEFAULT,Typeface.BOLD);GradientDrawable d=new GradientDrawable();d.setColor(red);d.setCornerRadius(dp(13));b.setBackground(d);return b;}
    private LinearLayout.LayoutParams params(int w,int h){return new LinearLayout.LayoutParams(w,h);}
    private LinearLayout.LayoutParams buttonParams(){LinearLayout.LayoutParams p=params(-1,dp(54));return p;}
    private int dp(int v){return (int)(v*getResources().getDisplayMetrics().density+0.5f);}
    private void startOverlay(){
        if(Build.VERSION.SDK_INT>=23&&!Settings.canDrawOverlays(this)){status.setText("Сначала разреши отображение поверх окон.");return;}
        Intent i=new Intent(this,OverlayService.class);if(Build.VERSION.SDK_INT>=26)startForegroundService(i);else startService(i);
        status.setText("Панель запущена — нажми CF поверх экрана.");
    }
    private void updateStatus(){boolean ok=Build.VERSION.SDK_INT<23||Settings.canDrawOverlays(this);if(status!=null)status.setText(ok?"Разрешение оверлея: выдано":"Разрешение оверлея: не выдано");}
    @Override protected void onResume(){super.onResume();updateStatus();}
}
