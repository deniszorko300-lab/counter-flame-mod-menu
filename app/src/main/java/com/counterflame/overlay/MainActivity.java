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
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
    private TextView status;
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL); root.setPadding(28,36,28,28);
        root.setBackgroundColor(Color.rgb(20,20,24));
        TextView title = new TextView(this); title.setText("COUNTER FLAME • OVERLAY");
        title.setTextColor(Color.WHITE); title.setTextSize(22); title.setGravity(Gravity.CENTER);
        root.addView(title,new LinearLayout.LayoutParams(-1,72));
        TextView info = new TextView(this);
        info.setText("Плавающая панель быстрых инструментов. Она не меняет игру и не вмешивается в её работу.");
        info.setTextColor(Color.LTGRAY); info.setTextSize(16); info.setPadding(0,20,0,24); root.addView(info);
        status = new TextView(this); status.setTextColor(Color.WHITE); root.addView(status);
        Button permission = button("Разрешить отображение поверх окон");
        permission.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= 23 && !Settings.canDrawOverlays(this)) {
                startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:"+getPackageName())));
            } else startOverlay();
        }); root.addView(permission);
        Button start = button("Запустить плавающую кнопку"); start.setOnClickListener(v -> startOverlay()); root.addView(start);
        Button stop = button("Остановить панель"); stop.setOnClickListener(v -> stopService(new Intent(this,OverlayService.class))); root.addView(stop);
        setContentView(root); updateStatus();
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},10);
    }
    private Button button(String s) { Button b=new Button(this); b.setText(s); b.setAllCaps(false); return b; }
    private void startOverlay() {
        if (Build.VERSION.SDK_INT >= 23 && !Settings.canDrawOverlays(this)) { status.setText("Сначала разреши отображение поверх других окон."); return; }
        Intent i=new Intent(this,OverlayService.class);
        if (Build.VERSION.SDK_INT >= 26) startForegroundService(i); else startService(i);
        status.setText("Панель запущена. Можно открыть Counter Flame.");
    }
    private void updateStatus() {
        boolean ok=Build.VERSION.SDK_INT < 23 || Settings.canDrawOverlays(this);
        status.setText(ok ? "Разрешение оверлея: есть" : "Разрешение оверлея: не выдано");
    }
    @Override protected void onResume() { super.onResume(); if(status!=null) updateStatus(); }
}
