package com.example.tiktokbot;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

public class FloatingService extends Service {
    private WindowManager windowManager;
    private View floatingView;
    private WindowManager.LayoutParams params;
    private boolean isMenuOpen = false;
    private boolean isSpyRunning = false;

    @Override
    public IBinder onBind(Intent intent) { return null; }

    @Override
    public void onCreate() {
        super.onCreate();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        floatingView = LayoutInflater.from(this).inflate(R.layout.floating_layout, null);
        
        int layoutFlag = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O 
                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY 
                : WindowManager.LayoutParams.TYPE_PHONE;
        
        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT,
                layoutFlag, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 50; params.y = 150;

        windowManager.addView(floatingView, params);
        
        Button btnMenu = floatingView.findViewById(R.id.btnMenu);
        Button btnModeAgent = floatingView.findViewById(R.id.btnModeAgent);
        Button btnModeTikTok = floatingView.findViewById(R.id.btnModeTikTok);
        Button btnSpy = floatingView.findViewById(R.id.btnSpy);
        Button btnClose = floatingView.findViewById(R.id.btnClose);
        LinearLayout menuLayout = floatingView.findViewById(R.id.menuLayout);

        btnMenu.setOnClickListener(v -> {
            if (isMenuOpen) {
                menuLayout.setVisibility(View.GONE);
                isMenuOpen = false;
            } else {
                menuLayout.setVisibility(View.VISIBLE);
                isMenuOpen = true;
            }
        });

        btnModeAgent.setOnClickListener(v -> {
            Toast.makeText(this, "Режим: Агент", Toast.LENGTH_SHORT).show();
            menuLayout.setVisibility(View.GONE);
            isMenuOpen = false;
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });

        btnModeTikTok.setOnClickListener(v -> {
            Toast.makeText(this, "Тикток Бот ВКЛ", Toast.LENGTH_SHORT).show();
            menuLayout.setVisibility(View.GONE);
            isMenuOpen = false;
            TikTokBotService.startBot();
        });

        btnSpy.setOnClickListener(v -> {
            if (!isSpyRunning) {
                startService(new Intent(this, SpyService.class));
                isSpyRunning = true;
                Toast.makeText(this, "Шпион ВКЛ", Toast.LENGTH_SHORT).show();
            } else {
                stopService(new Intent(this, SpyService.class));
                isSpyRunning = false;
                Toast.makeText(this, "Шпион ВЫКЛ", Toast.LENGTH_SHORT).show();
            }
        });

        btnClose.setOnClickListener(v -> stopSelf());

        btnMenu.setOnTouchListener(new View.OnTouchListener() {
            private int initialX, initialY;
            private float initialTouchX, initialTouchY;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x; initialY = params.y;
                        initialTouchX = event.getRawX(); initialTouchY = event.getRawY();
                        return false;
                    case MotionEvent.ACTION_MOVE:
                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);
                        windowManager.updateViewLayout(floatingView, params);
                        return true;
                }
                return false;
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (floatingView != null) windowManager.removeView(floatingView);
    }
}
