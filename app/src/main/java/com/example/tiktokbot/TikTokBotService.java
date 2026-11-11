package com.example.tiktokbot;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.SharedPreferences;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import java.util.List;

public class TikTokBotService extends AccessibilityService {
    private static boolean isRunning = false;
    private SharedPreferences prefs;

    public static void startBot() { isRunning = true; }
    public static void stopBot() { isRunning = false; }

    @Override
    public void onServiceConnected() {
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        setServiceInfo(info);
        prefs = getSharedPreferences("BotPrefs", MODE_PRIVATE);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (!isRunning) return;
        
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        if (rootNode == null) return;

        List<AccessibilityNodeInfo> commentBtns = rootNode.findAccessibilityNodeInfosByViewId("com.zhiliaoapp.musically:id/comment_btn");
        if (commentBtns != null && !commentBtns.isEmpty()) {
            AccessibilityNodeInfo commentBtn = commentBtns.get(0);
            commentBtn.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        }
    }

    @Override
    public void onInterrupt() {}
}
