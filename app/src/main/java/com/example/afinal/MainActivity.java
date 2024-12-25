package com.example.afinal;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.Random;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private int score = 0;
    private boolean isGameRunning = true;
    private ImageView singleMouse;
    private ImageView tripleMouse;
    private TextView timerTextView;
    private TextView infoTextView;

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

            int index = msg.arg1;
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

        initGameSettings();
        startCountDownTimer();
        startMouseThreads();

        // API 查詢按鈕
        findViewById(R.id.button).setOnClickListener(v -> queryAPI());
    }

    private void initGameSettings() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        timerTextView = findViewById(R.id.timerTextView);
        infoTextView = findViewById(R.id.info);
        singleMouse = findViewById(R.id.imageView1);
        tripleMouse = findViewById(R.id.imageView2);

        singleMouse.setOnTouchListener(this::onMouseClicked);
        tripleMouse.setOnTouchListener(this::onTripleMouseClicked);

        // 載入 GIF 圖片
        ImageView gifImageView = findViewById(R.id.gifImageView);
        RequestOptions options = new RequestOptions()
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.error);

        Glide.with(this)
                .setDefaultRequestOptions(options)
                .load("https://media1.tenor.com/images/a76206ffcbf1b53db2a47c5b186c47f5/tenor.gif")
                .into(gifImageView);
    }

    private void startCountDownTimer() {
        long GAME_DURATION_MILLIS = 10000;
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
            Toast.makeText(this, "打到 [" + score + "] 只地鼠！", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    private boolean onTripleMouseClicked(View view, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            view.setVisibility(View.INVISIBLE);
            score -= 2;
            Toast.makeText(this, "打到 [" + score + "] 只地鼠！", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    private void showMouse(ImageView mouse, int index) {
        mouse.setX(positions[index][0]);
        mouse.setY(positions[index][1]);
        mouse.setVisibility(View.VISIBLE);
    }

    private void endGame() {
        isGameRunning = false;
        singleMouse.setVisibility(View.INVISIBLE);
        tripleMouse.setVisibility(View.INVISIBLE);
        timerTextView.setText("剩餘時間：0秒");

        Toast.makeText(this, "遊戲結束！總得分：" + score, Toast.LENGTH_LONG).show();
    }

    private void queryAPI() {
        String URL = "https://mock-api/json"; // 測試用的 JSON API

        OkHttpClient okHttpClient = UnsafeOkHttpClient.getUnsafeOkhttpClient();
        Request request = new Request.Builder().url(URL).build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                // 處理 API 響應
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    runOnUiThread(() -> {
                        infoTextView.setText("API查詢成功: " + responseData);
                    });
                } else {
                    runOnUiThread(() -> {
                        infoTextView.setText("API查詢失敗: " + response.message());
                    });
                }
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("查詢失敗", e.getMessage());
                runOnUiThread(() -> {
                    infoTextView.setText("API查詢失敗: " + e.getMessage());
                });
            }
        });
    }
}