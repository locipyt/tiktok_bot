package com.example.tiktokbot;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EditText editTextComment = findViewById(R.id.editTextComment);
        Button btnStart = findViewById(R.id.btnStartService);

        btnStart.setOnClickListener(v -> {
            String commentText = editTextComment.getText().toString();
            getSharedPreferences("BotPrefs", MODE_PRIVATE).edit().putString("comment", commentText).apply();

            // Проверка разрешения на плавающее окно
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                startActivity(intent);
                Toast.makeText(this, "Разреши отображение поверх окон", Toast.LENGTH_LONG).show();
            } else {
                // Запуск плавающего окна
                startService(new Intent(this, FloatingService.class));
                
                // Отправляем пользователя в настройки Accessibility для включения бота
                Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                startActivity(intent);
                Toast.makeText(this, "Включи LociBot в Спец. возможностях", Toast.LENGTH_LONG).show();
            }
        });
    }
}
