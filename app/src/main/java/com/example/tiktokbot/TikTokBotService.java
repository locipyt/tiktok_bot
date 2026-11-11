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
        info.flags = AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS;
        setServiceInfo(info);
        prefs = getSharedPreferences("BotPrefs", MODE_PRIVATE);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (!isRunning) return;
        
        // Ограничение скорости: не чаще 1 раза в 2 секунды (чтобы не забанили)
        if (System.currentTimeMillis() - lastActionTime < 2000) return;

        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        if (rootNode == null) return;

        // 1. Ищем кнопку комментариев (по ID или тексту)
        List<AccessibilityNodeInfo> commentBtns = rootNode.findAccessibilityNodeInfosByViewId("com.zhiliaoapp.musically:id/comment_btn");
        if (commentBtns == null || commentBtns.isEmpty()) {
            commentBtns = rootNode.findAccessibilityNodeInfosByText("Комментарии"); // Если ID нет, ищем по тексту
        }

        if (commentBtns != null && !commentBtns.isEmpty()) {
            commentBtns.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
            lastActionTime = System.currentTimeMillis();
            
            // 2. Ждем открытия и ищем поле ввода
            new Thread(() -> {
                SystemClock.sleep(1500); // Ждем загрузки
                AccessibilityNodeInfo root2 = getRootInActiveWindow();
                if (root2 == null) return;
                
                List<AccessibilityNodeInfo> inputField = root2.findAccessibilityNodeInfosByViewId("com.zhiliaoapp.musically:id/comment_input");
                if (inputField != null && !inputField.isEmpty()) {
                    AccessibilityNodeInfo field = inputField.get(0);
                    // Вставляем текст
                    android.os.Bundle args = new android.os.Bundle();
                    args.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, prefs.getString("comment", ""));
                    field.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args);
                    
                    // 3. Ищем кнопку отправки
                    SystemClock.sleep(500);
                    List<AccessibilityNodeInfo> sendBtn = root2.findAccessibilityNodeInfosByViewId("com.zhiliaoapp.musically:id/comment_send_btn");
                    if (sendBtn != null && !sendBtn.isEmpty()) {
                        sendBtn.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    }
                }
            }).start();
            
            return; // Выходим, чтобы не листать сразу
        }

        // 4. Если комментарии не найдены, листаем ленту
        // Имитация свайпа вверх
        AccessibilityNodeInfo scrollableNode = findScrollableNode(rootNode);
        if (scrollableNode != null) {
            scrollableNode.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
            lastActionTime = System.currentTimeMillis();
        }
    }
    
    // Вспомогательный метод для поиска прокручиваемого контейнера
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
