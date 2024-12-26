package com.example.afinal;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private int score = 0;
    private boolean GameRunning = true;
    private ImageView Usagi;
    private ImageView Tao;
    private TextView timerTextView;

    private final int[][] spot = new int[][]{
            {277, 200}, {535, 200}, {832, 200}, {1067, 200}, {1328, 200},
            {190, 425}, {445, 425}, {1014, 425}, {1348, 425},{1552, 425},
            {319, 650}, {764, 650}, {1229, 650}, {120, 650}, {1493, 650}
    };

    private final Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            if (!GameRunning) return;

            int index = msg.arg1;
            if (msg.what == 0x101) {
                showChiikawa(Usagi, index);
            } else if (msg.what == 0x102) {
                showChiikawa(Tao, index);
            }
        }
    };

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        GameSettings();
        startCountDownTimer();
        startGameThreads();
    }

    private void GameSettings() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        timerTextView = findViewById(R.id.timerTextView);
        Usagi = findViewById(R.id.gifImageView);
        Tao = findViewById(R.id.gifImageView2);

        Usagi.setOnTouchListener(this::onUsagiClicked);
        Tao.setOnTouchListener(this::onTaoClicked);

        // 載入 GIF 圖片，設定大小一致
        ImageView gifImageView = findViewById(R.id.gifImageView);
        Glide.with(this)
                .asGif()
                .load("https://memeprod.ap-south-1.linodeobjects.com/user-maker/af8c1267dedda2a613286a3a8ccb6a1c.gif")
                .apply(new RequestOptions().override(240, 270)) // 設定大小
                .into(gifImageView);

        gifImageView = findViewById(R.id.gifImageView2);
        Glide.with(this)
                .asGif()
                .load("https://i.giphy.com/media/v1.Y2lkPTc5MGI3NjExMnJ0MWF4NGttaHE0d293cDV2M2I3bDl0ampqOHNsbWxrMmNnaW13YiZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/IeFB04H51Ntwtam8Y8/giphy.gif")
                .apply(new RequestOptions().override(270, 300)) // 設定大小
                .into(gifImageView);
    }

    private void startCountDownTimer() {
        long GAME_DURATION_MILLIS =15000;
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

    private void startGameThreads() {
        Thread UsagiThread = new Thread(() -> runGameLogic(0x101));
        Thread TaoThread = new Thread(() -> runGameLogic(0x102));

        UsagiThread.start();
        TaoThread.start();
    }

    private void runGameLogic(int messageType) {
        Random random = new Random();
        int lastUsagiIndex = -1;
        int lastTaoIndex = -1;
        int nextUsagiIndex = -1;
        int nextTaoIndex = -1;

        while (GameRunning) {
            int index;

            do {
                index = random.nextInt(spot.length);
            } while ((messageType == 0x101 && (index == lastUsagiIndex || index == nextTaoIndex)) ||
                    (messageType == 0x102 && (index == lastTaoIndex || index == nextUsagiIndex)) ||
                    (lastUsagiIndex != -1 && lastTaoIndex != -1 && nextUsagiIndex == lastTaoIndex));

            if (messageType == 0x101) {
                lastUsagiIndex = index;
                nextUsagiIndex = index;
            } else {
                lastTaoIndex = index;
                nextTaoIndex = index;
            }

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

    private boolean onUsagiClicked(View view, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            view.setVisibility(View.INVISIBLE);
            score++;
            showScoreToast("+1 分！目前得分：" + score);
        }
        return false;
    }

    private boolean onTaoClicked(View view, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            view.setVisibility(View.INVISIBLE);
            score -= 2;
            showScoreToast("-2 分！目前得分：" + score);
        }
        return false;
    }

    private void showChiikawa(ImageView chii, int index) {
        chii.setX(spot[index][0]);
        chii.setY(spot[index][1]);
        chii.setVisibility(View.VISIBLE);
    }

    private void showScoreToast(String message) {
        final TextView scoreToastView = new TextView(this);
        scoreToastView.setText(message);
        scoreToastView.setBackgroundColor(0xAA000000); // 半透明背景
        scoreToastView.setTextColor(0xFFFFFFFF); // 白色文字
        scoreToastView.setTextSize(32);
        scoreToastView.setPadding(20, 10, 20, 10);

        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT
        );
        scoreToastView.setX(1750); // 自訂位置
        scoreToastView.setY(1150); // 自訂位置

        addContentView(scoreToastView, layoutParams);

        // 設定短時間後自動移除
        new Handler().postDelayed(() -> scoreToastView.setVisibility(View.GONE), 500); // 顯示 500 毫秒
    }

    private void endGame() {
        GameRunning = false;
        Usagi.setVisibility(View.INVISIBLE);
        Tao.setVisibility(View.INVISIBLE);
        timerTextView.setText("剩餘時間：0秒");

        Toast.makeText(this, "遊戲結束！總得分：" + score, Toast.LENGTH_LONG).show();
    }
}
