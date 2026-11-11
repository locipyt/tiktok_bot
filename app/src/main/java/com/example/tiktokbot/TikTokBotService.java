package com.example.tiktokbot;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import java.util.List;
import java.util.Random;

public class TikTokBotService extends AccessibilityService {
    private static boolean isRunning = false;
    private SharedPreferences prefs;
    private long lastActionTime = 0;
    private int errorCount = 0;
    private Random random = new Random();

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
        // ==========================================
        // 1. АВТООТВЕТЧИК (Ловим уведомления Telegram)
        // ==========================================
        if (event.getEventType() == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
            String contact = prefs.getString("contact", "");
            String replyText = prefs.getString("autoreply", "");
            
            List<CharSequence> textList = event.getText();
            if (!contact.isEmpty() && !replyText.isEmpty() && textList != null && !textList.isEmpty()) {
                // Если в уведомлении есть имя контакта
                if (textList.get(0).toString().contains(contact)) {
                    // Открываем Telegram
                    Intent intent = getPackageManager().getLaunchIntentForPackage("org.telegram.messenger");
                    if (intent != null) {
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        
                        // Ждем загрузки и вводим текст
                        new Thread(() -> {
                            SystemClock.sleep(2000);
                            AccessibilityNodeInfo root = getRootInActiveWindow();
                            if (root != null) {
                                AccessibilityNodeInfo input = findEditText(root);
                                if (input != null) {
                                    // Вставляем текст
                                    android.os.Bundle args = new android.os.Bundle();
                                    args.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, replyText);
                                    input.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args);
                                    
                                    SystemClock.sleep(500);
                                    // Ищем кнопку отправки в Telegram
                                    List<AccessibilityNodeInfo> sendBtn = root.findAccessibilityNodeInfosByViewId("org.telegram.messenger:id/send_button");
                                    if (sendBtn != null && !sendBtn.isEmpty()) {
                                        sendBtn.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                    }
                                }
                            }
                        }).start();
                    }
                }
            }
        }

        // ==========================================
        // 2. ЛОГИКА TIKTOK
        // ==========================================
        if (!isRunning) return;

        // Рандомизация (от 3 до 7 секунд)
        int randomDelay = 3000 + random.nextInt(4000);
        if (System.currentTimeMillis() - lastActionTime < randomDelay) return;

        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        if (rootNode == null) return;

        // Включение VPN (если открыто приложение)
        List<AccessibilityNodeInfo> connectBtn = rootNode.findAccessibilityNodeInfosByText("Подключить");
        if (connectBtn == null || connectBtn.isEmpty()) connectBtn = rootNode.findAccessibilityNodeInfosByText("Connect");
        
        if (connectBtn != null && !connectBtn.isEmpty()) {
            connectBtn.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
            lastActionTime = System.currentTimeMillis();
            return;
        }

        // Поиск кнопки комментов
        List<AccessibilityNodeInfo> commentBtns = rootNode.findAccessibilityNodeInfosByText("Комментарии");
        if (commentBtns == null || commentBtns.isEmpty()) commentBtns = rootNode.findAccessibilityNodeInfosByText("Comments");
        if (commentBtns == null || commentBtns.isEmpty()) commentBtns = rootNode.findAccessibilityNodeInfosByText("Комментарий");

        if (commentBtns != null && !commentBtns.isEmpty()) {
            commentBtns.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
            lastActionTime = System.currentTimeMillis();
            errorCount = 0; 
            
            // Ввод текста
            new Thread(() -> {
                SystemClock.sleep(1500 + random.nextInt(1000));
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
                    } else {
                        // Если текста нет, ищем по ID
                        List<AccessibilityNodeInfo> sendBtnId = root2.findAccessibilityNodeInfosByViewId("com.zhiliaoapp.musically:id/btn_send");
                        if (sendBtnId != null && !sendBtnId.isEmpty()) {
                            sendBtnId.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        }
                    }
                }
            }).start();
            return; 
        }

        // Листание ленты
        AccessibilityNodeInfo scrollableNode = findScrollableNode(rootNode);
        if (scrollableNode != null) {
            scrollableNode.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
            lastActionTime = System.currentTimeMillis();
            errorCount = 0;
        } else {
            errorCount++;
            if (errorCount > 5) stopBot(); // Автопауза
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
