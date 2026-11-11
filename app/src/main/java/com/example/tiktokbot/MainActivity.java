package com.example.tiktokbot;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
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

        LinearLayout layoutAgent = findViewById(R.id.layoutAgent);
        LinearLayout layoutTikTok = findViewById(R.id.layoutTikTok);
        Button tabAgentBtn = findViewById(R.id.tabAgentBtn);
        Button tabTikTokBtn = findViewById(R.id.tabTikTokBtn);

        tabAgentBtn.setOnClickListener(v -> {
            layoutAgent.setVisibility(View.VISIBLE);
            layoutTikTok.setVisibility(View.GONE);
        });
        tabTikTokBtn.setOnClickListener(v -> {
            layoutAgent.setVisibility(View.GONE);
            layoutTikTok.setVisibility(View.VISIBLE);
        });

        EditText editTextCommand = findViewById(R.id.editTextCommand);
        Button btnExecute = findViewById(R.id.btnExecute);
        btnExecute.setOnClickListener(v -> {
            String command = editTextCommand.getText().toString().trim().toLowerCase();
            if (command.startsWith("открой ")) {
                openApp(command.replace("открой ", "").trim());
            } else {
                Toast.makeText(this, "Команда не распознана", Toast.LENGTH_SHORT).show();
            }
        });

        EditText editTextComment = findViewById(R.id.editTextComment);
        Button btnStartTikTok = findViewById(R.id.btnStartTikTok);
        btnStartTikTok.setOnClickListener(v -> {
            getSharedPreferences("BotPrefs", MODE_PRIVATE).edit()
                .putString("comment", editTextComment.getText().toString()).apply();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            } else {
                startService(new Intent(this, FloatingService.class));
                startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
            }
        });
    }

    private void openApp(String appName) {
        PackageManager pm = getPackageManager();
        for (ApplicationInfo appInfo : pm.getInstalledApplications(0)) {
            String label = pm.getApplicationLabel(appInfo).toString().toLowerCase();
            if (label.contains(appName)) {
                Intent launchIntent = pm.getLaunchIntentForPackage(appInfo.packageName);
                if (launchIntent != null) {
                    startActivity(launchIntent);
                    return;
                }
            }
        }
        Toast.makeText(this, "Приложение не найдено", Toast.LENGTH_SHORT).show();
    }
}
