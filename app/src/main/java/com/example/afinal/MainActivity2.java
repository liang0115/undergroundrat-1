package com.example.afinal;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity2 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        Button button = findViewById(R.id.button); // 綁定按鈕
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 切換到 MainActivity
                Intent intent = new Intent(MainActivity2.this, MainActivity.class);
                startActivity(intent);
                Toast.makeText(MainActivity2.this, "切換到主遊戲畫面", Toast.LENGTH_SHORT).show();
                finish(); // 關閉 MainActivity2
            }
        });
    }
}
