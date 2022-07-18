package xuanniao.weather.recode;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.session.MediaSession;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.RequiresApi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class Recode extends Service {
    private SharedPreferences preferences;

    @Override
    public void onCreate() {
        super.onCreate();
//        // Log.i("Recode","onCreate");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                SimpleDateFormat hourFormat = new SimpleDateFormat("mm");
                Date date = new Date();
                int RecodeTime = Integer.parseInt(hourFormat.format(date));
                // Log.i("Recode_minute", hourFormat.format(date));
                // 获取Shared Preferences对象
                preferences = getSharedPreferences("SetInfo", MODE_PRIVATE);
                if (preferences != null) {
                    String Token = preferences.getString("user_token", null);
                    String LagLat = preferences.getString("LagLat", null);
                    if (Token == null||LagLat == null) {
                        Toast.makeText(getApplicationContext(), "请在设置页面输入令牌", Toast.LENGTH_SHORT).show();
                    } else if(Token.equals("")||LagLat.equals("")){
                        Toast.makeText(getApplicationContext(), "请在设置页面输入令牌", Toast.LENGTH_SHORT).show();
                    } else {
                        GetDataFromURL(RecodeTime, Token, LagLat);
                    }
                }else{
                    Toast.makeText(getApplicationContext(), "请在设置页面输入令牌", Toast.LENGTH_SHORT).show();
                }
                stopSelf();//结束服务
            }
        }).start();
        return super.onStartCommand(intent, flags, startId);
    }

    //服务销毁的时候调用
    @Override
    public void onDestroy(){
        super.onDestroy();
        // Log.i("Recode","onDestroy方法被调用");
    }

    // 解析数据
    private HashMap<String, String> RealtimeMap;
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void GetDataFromURL(int RecodeTime, String Token, String LagLat) {
        try {
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            URL url = new URL("https://api.caiyunapp.com/v2.6/" + Token + "/" + LagLat + "/weather?alert=true&unit=metric:v2&dailysteps=7&hourlysteps=24");
            // Log.i("url", String.valueOf(url));
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(6000);
            connection.setReadTimeout(6000);
            String weather_data;
            StringBuilder response = null;
            if (connection.getResponseCode() == 200) {
                InputStream in = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(in));
                response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
            }
            weather_data = response.toString();
            // Log.i("解析", "开始");
            // 开始解析数据
            RealtimeMap = GetData.Realtime(weather_data);
            SaveAsExcel.createExcel(getApplicationContext(),RealtimeMap,RecodeTime);
            // Log.i("Recode解析","完成");
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
        // 本地数据模拟彩云api数据
//        try {
//            InputStreamReader inputStreamReader = new InputStreamReader(getAssets().open("CaiYunAPIJson.json"), "UTF-8");
//            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
//            String line;
//            StringBuilder stringBuilder_data = new StringBuilder();
//            while ((line = bufferedReader.readLine()) != null) {
//                stringBuilder_data.append(line);
//            }
//            bufferedReader.close();
//            inputStreamReader.close();
//            String weather_data = stringBuilder_data.toString();
//            // 开始解析数据
//            RealtimeMap = GetData.Realtime(weather_data);
//            SaveAsExcel.createExcel(getApplicationContext(),RealtimeMap,RecodeTime);
//            // Log.i("Recode解析","完成");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

}