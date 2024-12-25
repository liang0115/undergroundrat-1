package com.example.afinal;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class MyService extends Service {

    public void onCreate(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    Thread.sleep(3000);
                    Intent intent = new Intent(MyService.this,MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    MyService.this.startActivity(intent);
                }
                catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();

        stopSelf();
    }

    @Override
    public int onStartCommand (Intent intent,int flags,int startId){
        super.onStartCommand(intent,flags,startId);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {super.onDestroy();}

    @Override
    public IBinder onBind(Intent intent){
        throw new UnsupportedOperationException("Not yet implement");
    }
}
