package com.example.tiktokbot;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import java.util.List;

public class TikTokBotService extends AccessibilityService {
    private static boolean isRunning = false;
    private SharedPreferences prefs;
    private long lastActionTime = 0;

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
        if (System.currentTimeMillis() - lastActionTime < 2500) return;

        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        if (rootNode == null) return;

        // --- ВКЛЮЧЕНИЕ VPN (если открыто приложение VPN) ---
        List<AccessibilityNodeInfo> connectBtn = rootNode.findAccessibilityNodeInfosByText("Подключить");
        if (connectBtn == null || connectBtn.isEmpty()) {
            connectBtn = rootNode.findAccessibilityNodeInfosByText("Connect");
        }
        if (connectBtn != null && !connectBtn.isEmpty()) {
            connectBtn.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
            lastActionTime = System.currentTimeMillis();
            return;
        }

        // --- TIKTOK (Поиск кнопки комментов) ---
        List<AccessibilityNodeInfo> commentBtns = rootNode.findAccessibilityNodeInfosByText("Комментарии");
        if (commentBtns == null || commentBtns.isEmpty()) {
            commentBtns = rootNode.findAccessibilityNodeInfosByText("Comments");
        }

        if (commentBtns != null && !commentBtns.isEmpty()) {
            commentBtns.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
            lastActionTime = System.currentTimeMillis();
            
            // Ввод текста
            new Thread(() -> {
                SystemClock.sleep(1500);
                AccessibilityNodeInfo root2 = getRootInActiveWindow();
                if (root2 == null) return;
                
                AccessibilityNodeInfo inputField = findEditText(root2);
                if (inputField != null) {
                    android.os.Bundle args = new android.os.Bundle();
                    args.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, prefs.getString("comment", ""));
                    inputField.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args);
                    
                    SystemClock.sleep(500);
                    List<AccessibilityNodeInfo> sendBtn = root2.findAccessibilityNodeInfosByText("Отправить");
                    if (sendBtn == null || sendBtn.isEmpty()) sendBtn = root2.findAccessibilityNodeInfosByText("Send");
                    
                    if (sendBtn != null && !sendBtn.isEmpty()) {
                        sendBtn.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    }
                }
            }).start();
            return; 
        }

        // --- ЛИСТАНИЕ ЛЕНТЫ ---
        AccessibilityNodeInfo scrollableNode = findScrollableNode(rootNode);
        if (scrollableNode != null) {
            scrollableNode.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
            lastActionTime = System.currentTimeMillis();
        }
    }

    private AccessibilityNodeInfo findEditText(AccessibilityNodeInfo root) {
        for (int i = 0; i < root.getChildCount(); i++) {
            AccessibilityNodeInfo child = root.getChild(i);
            if (child != null) {
                if ("android.widget.EditText".equals(child.getClassName().toString())) return child;
                AccessibilityNodeInfo result = findEditText(child);
                if (result != null) return result;
            }
        }
        return null;
    }

    private AccessibilityNodeInfo findScrollableNode(AccessibilityNodeInfo root) {
        for (int i = 0; i < root.getChildCount(); i++) {
            AccessibilityNodeInfo child = root.getChild(i);
            if (child != null) {
                if (child.isScrollable()) return child;
                AccessibilityNodeInfo result = findScrollableNode(child);
                if (result != null) return result;
            }
        }
        return null;
    }

    @Override
    public void onInterrupt() {}
}
