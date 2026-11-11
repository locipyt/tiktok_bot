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
        EditText editTextContact = findViewById(R.id.editTextContact);
        EditText editTextAutoReply = findViewById(R.id.editTextAutoReply);
        Button btnStart = findViewById(R.id.btnStartService);

        btnStart.setOnClickListener(v -> {
            // Сохраняем все три поля в память
            getSharedPreferences("BotPrefs", MODE_PRIVATE).edit()
                .putString("comment", editTextComment.getText().toString())
                .putString("contact", editTextContact.getText().toString())
                .putString("autoreply", editTextAutoReply.getText().toString())
                .apply();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                startActivity(intent);
                Toast.makeText(this, "Разреши отображение поверх окон", Toast.LENGTH_LONG).show();
            } else {
                startService(new Intent(this, FloatingService.class));
                Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                startActivity(intent);
                Toast.makeText(this, "Включи LocoBot в Спец. возможностях", Toast.LENGTH_LONG).show();
            }
        });
    }
}
