package com.example.afinal;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class GoodGameActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);

        // 取得傳遞的數據
        int finalScore = getIntent().getIntExtra("FINAL_SCORE", 0);
        int usagiCount = getIntent().getIntExtra("USAGI_COUNT", 0);
        int taoCount = getIntent().getIntExtra("TAO_COUNT", 0);

        // 設定數據到 TextView
        TextView scoreTextView = findViewById(R.id.scoreTextView);
        TextView usagiTextView = findViewById(R.id.usagiTextView);
        TextView taoTextView = findViewById(R.id.taoTextView);

        scoreTextView.setText("總分：" + finalScore);
        usagiTextView.setText("打到的 Usagi 數量：" + usagiCount);
        taoTextView.setText("打到的 Tao 數量：" + taoCount);
    }
}
