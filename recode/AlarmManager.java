package xuanniao.weather.recode;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

public class AlarmManager extends Service {
    String Tag = "AlarmManager";
    android.app.AlarmManager manager;
    PendingIntent pi;

    @SuppressLint({"ServiceCast", "WrongConstant", "UnspecifiedImmutableFlag"})
    @Override
    public void onCreate() {
        // Log.i(Tag,"创建");
    }

    @Override
    public IBinder onBind(Intent intent) {
        // Log.i(Tag,"绑定");
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // “闹钟”功能的设置
        manager = (android.app.AlarmManager) getSystemService(ALARM_SERVICE);
        int anHour = 2 * 1000;
        long triggerAtTime = System.currentTimeMillis() + anHour;
        Intent i = new Intent(this, Recode.class);
        pi = PendingIntent.getService(this, 0, i, 0);
        manager.setRepeating(android.app.AlarmManager.RTC_WAKEUP,
                triggerAtTime, 60 * 1000, pi);
        return super.onStartCommand(intent, flags, startId);
//        return START_STICKY;
    }

    // 服务销毁时调用
    @Override
    public void onDestroy(){
        manager.cancel(pi);
        super.onDestroy();
        // Log.i("AlarmManager","onDestroy方法被调用");
    }
}