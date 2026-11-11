package com.example.tiktokbot;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // --- АНИМАЦИЯ ФОНА ---
        LinearLayout rootLayout = findViewById(R.id.rootLayout);
        if (rootLayout != null) {
            ObjectAnimator animator = ObjectAnimator.ofFloat(rootLayout, "backgroundColor", 
                    Color.parseColor("#1A0B2E"), Color.parseColor("#0A0A0F"));
            animator.setDuration(5000); // Длительность (5 секунд)
            animator.setRepeatCount(ValueAnimator.INFINITE); // Бесконечно
            animator.setRepeatMode(ValueAnimator.REVERSE); // Плавно туда и обратно
            animator.start();
        }

        EditText editTextComment = findViewById(R.id.editTextComment);
        EditText editTextContact = findViewById(R.id.editTextContact);
        EditText editTextAutoReply = findViewById(R.id.editTextAutoReply);
        Button btnStart = findViewById(R.id.btnStartService);

        btnStart.setOnClickListener(v -> {
            // Сохраняем настройки
            getSharedPreferences("BotPrefs", MODE_PRIVATE).edit()
                .putString("comment", editTextComment.getText().toString())
                .putString("contact", editTextContact.getText().toString())
                .putString("autoreply", editTextAutoReply.getText().toString())
                .apply();

            // Проверка разрешения на плавающее окно
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                startActivity(intent);
                Toast.makeText(this, "Разреши отображение поверх окон", Toast.LENGTH_LONG).show();
            } else {
                // Запуск плавающего окна и настроек доступности
                startService(new Intent(this, FloatingService.class));
                Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                startActivity(intent);
                Toast.makeText(this, "Включи LociBot в Спец. возможностях", Toast.LENGTH_LONG).show();
            }
        });
    }
}
