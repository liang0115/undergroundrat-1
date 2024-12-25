package com.example.afinal;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Random;


public class MainActivity extends AppCompatActivity {

    // 遊戲相關變數
    private int score = 0; // 紀錄得分
    private boolean isGameRunning = true; // 控制遊戲執行狀態

    // 地鼠相關物件
    private ImageView singleMouse;
    private ImageView tripleMouse;

    // 顯示資訊
    private TextView timerTextView;
    private TextView infoTextView;

    // 地鼠位置
    private final int[][] positions = new int[][]{
            {277, 200}, {535, 200}, {832, 200},
            {1067, 200}, {1328, 200}, {285, 360},
            {645, 360}, {1014, 360}, {1348, 360},
            {319, 600}, {764, 600}, {1229, 600}
    };

    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            if (!isGameRunning) return;

            int index = msg.arg1; // 地鼠位置索引
            if (msg.what == 0x101) {
                showMouse(singleMouse, index);
            } else if (msg.what == 0x102) {
                showMouse(tripleMouse, index);
            }
        }
    };

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 初始化 UI 和遊戲設定
        initGameSettings();
        startCountDownTimer();
        startMouseThreads();
    }

    private void initGameSettings() {
        // 設定全螢幕與橫屏模式
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        // 綁定控件
        timerTextView = findViewById(R.id.timerTextView);
        infoTextView = findViewById(R.id.info);
        singleMouse = findViewById(R.id.imageView1);
        tripleMouse = findViewById(R.id.imageView2);

        // 設定點擊事件
        singleMouse.setOnTouchListener(this::onMouseClicked);
        tripleMouse.setOnTouchListener(this::onTripleMouseClicked);

        infoTextView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                Log.i("Touch Coordinates", "x: " + event.getRawX() + ", y: " + event.getRawY());
            }
            return false;
        });
    }

    private void startCountDownTimer() {
        long GAME_DURATION_MILLIS = 10000; // 30 秒
        new CountDownTimer(GAME_DURATION_MILLIS, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int remainingTime = (int) (millisUntilFinished / 1000);
                timerTextView.setText("剩餘時間：" + remainingTime + "秒");
            }

            @Override
            public void onFinish() {
                endGame();
            }
        }.start();
    }

    private void startMouseThreads() {
        Thread singleMouseThread = new Thread(() -> runMouseLogic(0x101));
        Thread tripleMouseThread = new Thread(() -> runMouseLogic(0x102));

        singleMouseThread.start();
        tripleMouseThread.start();
    }

    private void runMouseLogic(int messageType) {
        Random random = new Random();
        int lastIndex = -1;

        while (isGameRunning) {
            int index;
            do {
                index = random.nextInt(positions.length);
            } while (index == lastIndex);
            lastIndex = index;

            Message message = handler.obtainMessage();
            message.what = messageType;
            message.arg1 = index;
            handler.sendMessage(message);

            try {
                Thread.sleep(random.nextInt(500) + 800);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean onMouseClicked(View view, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            view.setVisibility(View.INVISIBLE);
            score++;
            showScoreToast("打到 [" + score + "] 只地鼠！");
        }
        return false;
    }

    private boolean onTripleMouseClicked(View view, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            view.setVisibility(View.INVISIBLE);
            score -= 2;
            showScoreToast("打到 [" + score + "] 只地鼠！");
        }
        return false;
    }

    private void showMouse(ImageView mouse, int index) {
        mouse.setX(positions[index][0]);
        mouse.setY(positions[index][1]);
        mouse.setVisibility(View.VISIBLE);
    }

    private void showScoreToast(String message) {
        final TextView scoreToastView = new TextView(this);
        scoreToastView.setText(message);
        scoreToastView.setTextColor(0xFFFFFFFF); // 白色文字
        scoreToastView.setTextSize(18);
        scoreToastView.setPadding(20, 10, 20, 10);
        scoreToastView.setX(2450); // 自訂位置
        scoreToastView.setY(1200); // 自訂位置

        addContentView(scoreToastView, new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT));

        // 設定短時間後自動移除
        new Handler().postDelayed(() -> scoreToastView.setVisibility(View.GONE), 500); // 顯示 500 毫秒
    }

    private void endGame() {
        isGameRunning = false;
        singleMouse.setVisibility(View.INVISIBLE);
        tripleMouse.setVisibility(View.INVISIBLE);
        timerTextView.setText("剩餘時間：0秒");

        Toast.makeText(this, "遊戲結束！總得分：" + score, Toast.LENGTH_LONG).show();
    }
}
