package xuanniao.weather.recode;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.*;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.os.*;
import android.text.Layout;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Xml;
import android.view.*;
import android.widget.*;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Bundle;
import org.xmlpull.v1.XmlPullParser;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.*;

public class MainActivity extends AppCompatActivity {
    private TextView city;
    private String CityNameZh;
    private String LagLat;
    private TextView laglatnum;
    private SwipeRefreshLayout swipeRefreshLayout;
    private HashMap<String, String> RealtimeMap;
    private SharedPreferences preferences;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 获取网络权限
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        setHead();

        // 获取下拉刷新布局 和其中包含的次级布局
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        // 设置下拉刷新监听器
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh(){//刷新的处理事件
                // Log.i("调用了","下拉刷新");
                //开启子线程
                new Thread(new Runnable() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void run() {
                        Looper.prepare();
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
                                GetDataFromURL(Token);
                            }
                        }else{
                            Toast.makeText(getApplicationContext(), "请在设置页面输入令牌", Toast.LENGTH_SHORT).show();
                        }
                        Looper.loop();
                    }
                }).start();
                // 使用Handler(Looper.myLooper())替代旧版的Handler来明确消息循环
                new Handler(Looper.myLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.setRefreshing(false);
                        // Log.i("下拉刷新","结束");
                    }
                    // 转圈圈设置1秒后停止
                }, 1000);
            }
        });
        // 设置刷新头样式
        swipeRefreshLayout.setDistanceToTriggerSync(70);//设置触发下拉刷新的滑动距离
        swipeRefreshLayout.setColorSchemeColors(Color.RED, Color.BLUE);
    }

    private void setHead() {
        preferences = getSharedPreferences("SetInfo", MODE_PRIVATE);
        // 如果SetInfo文件已经被创建 则读取其中内容
        if (preferences != null) {
            city = findViewById(R.id.city);
            laglatnum = findViewById(R.id.laglatnum);

            CityNameZh = preferences.getString("CityNameZh", null);
            if (CityNameZh == null) {
                city.setText("设置页面输入");
            } else {
                city.setText("");
                city.append("当前城市：" + CityNameZh);
            }

            LagLat = preferences.getString("LagLat", null);
            if (LagLat == null) {
                laglatnum.setText("设置页面输入");
            } else {
                laglatnum.setText("");
                laglatnum.append(LagLat);
            }
        }
    }


    // 链接menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        //设置menu界面为res/menu/main_menu.xml
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    // 处理菜单事件
    public boolean onOptionsItemSelected(MenuItem item){
        //得到当前选中的MenuItem的ID,
        int item_id = item.getItemId();
        switch (item_id) {
            case R.id.setting:
                // 新建一个Intent对象
                Intent intent = new Intent();
                // 指定intent要启动的类
                intent.setClass(MainActivity.this, SettingControl.class);
                // 启动一个新的Activity
                startActivity(intent);
                break;
        }
        return true;
    }



    @RequiresApi(api = Build.VERSION_CODES.N)
    public void GetDataFromURL(String Token) {
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

            String[] HourlyList_time = GetData.Hourly_time(weather_data);
            double[] HourlyList_temp = GetData.Hourly_temp(weather_data);
            String[] HourlyList_skycon = GetData.Hourly_skycon(weather_data);

            String[] Daily_date = GetData.Daily_date(weather_data);
            String[] Daily_sunrise = GetData.Daily_sunrise(weather_data);
            String[] Daily_sunset = GetData.Daily_sunset(weather_data);
            String[] Daily_temperature_d = GetData.Daily_tempD(weather_data);
            String[] Daily_temperature_n = GetData.Daily_tempN(weather_data);
            String[] Daily_skycon_d = GetData.Daily_skyconD(weather_data);
            String[] Daily_skycon_n = GetData.Daily_skyconN(weather_data);

            setContext();
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
//            // Log.i("解析","开始");
//            // 开始解析数据
//            RealtimeMap = GetData.Realtime(weather_data);
//
//            String[] HourlyList_time = GetData.Hourly_time(weather_data);
//            double[] HourlyList_temp = GetData.Hourly_temp(weather_data);
//            String[] HourlyList_skycon = GetData.Hourly_skycon(weather_data);
//
//            String[] Daily_date = GetData.Daily_date(weather_data);
//            String[] Daily_sunrise = GetData.Daily_sunrise(weather_data);
//            String[] Daily_sunset = GetData.Daily_sunset(weather_data);
//            String[] Daily_temperature_d = GetData.Daily_tempD(weather_data);
//            String[] Daily_temperature_n = GetData.Daily_tempN(weather_data);
//            String[] Daily_skycon_d = GetData.Daily_skyconD(weather_data);
//            String[] Daily_skycon_n = GetData.Daily_skyconN(weather_data);
//
//            setContext();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }


    // 内容注入
    public void setContext() {
        try {
            runOnUiThread(new Runnable() {
                @RequiresApi(api = Build.VERSION_CODES.N)
                public void run() {
                    RefreshMainActivity();
                }
            });
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 刷新数据显示页面（主页面）
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void RefreshMainActivity() {
        setHead();
        setNow();
        setHourly();
        setDaily();
    }



    // 显示实时数据
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void setNow() {
        // 从RealtimeMap列表中逐项提取数据
        HashMap<String, String> RealtimeMap = GetData.RealtimeMap;
        // Log.i("RealtimeMap", String.valueOf(RealtimeMap));
        // 设置更新时间
        String Server_time = RealtimeMap.get("更新时间");
        String warn_code = RealtimeMap.get("warn_code");
        String warn_icon_num = "";
        if(Objects.equals(warn_code, null)) {
            warn_icon_num = "p9999999";
        }else {
            warn_code = RealtimeMap.get("预警代码");
            String warn_title = RealtimeMap.get("预警标题");
            String warn_description = RealtimeMap.get("预警内容");
            assert warn_code != null;
            char[] warn_code_Array = warn_code.toCharArray();
            // Log.i("warn_code_Array", String.valueOf(warn_code_Array));
            String warn_type_caiyun = new String(warn_code_Array,0,2);
            String warn_type = "";
            switch(warn_type_caiyun){
                case "01"://台风
                    warn_type = "p0001";
                    break;
                case "02"://暴雨
                    warn_type = "p0002";
                    break;
                case "03"://暴雪
                    warn_type = "p0006";
                    break;
                case "04"://寒潮
                    warn_type = "p0004";
                    break;
                case "05"://大风
                    warn_type = "p0007";
                    break;
                case "06"://沙尘暴
                    warn_type = "p0008";
                    break;
                case "07"://高温
                    warn_type = "p0003";
                    break;
                case "08"://干旱
                    warn_type = "p0010";
                    break;
                case "09"://雷电
                    warn_type = "p0012";
                    break;
                case "10"://冰雹
                    warn_type = "p0009";
                    break;
                case "11"://霜冻
                    warn_type = "p0013";
                    break;
                case "12"://大雾
                    warn_type = "p0005";
                    break;
                case "13"://霾
                    warn_type = "p0014";
                    break;
                case "14"://道路结冰
                    warn_type = "p0011";
                    break;
                case "15"://森林火灾
                    warn_type = "p0030";
                    break;
                case "16"://雷雨大风
                    warn_type = "p0015";
                    break;
            }
            String warn_rank_caiyun = new String(warn_code_Array,2,2);
            // Log.i("warn_rank_caiyun", warn_rank_caiyun);
            String warn_rank = "";
            switch(warn_rank_caiyun) {
                case "01"://蓝色
                    warn_rank = "001";
                    break;
                case "02"://黄色
                    warn_rank = "002";
                    break;
                case "03"://橙色
                    warn_rank = "003";
                    break;
                case "04"://红色
                    warn_rank = "004";
                    break;
            }
            warn_icon_num = warn_type + warn_rank;
        }
        String Wi = warn_icon_num;
        // Log.i("Wi", Wi);
        String Wc = warn_code;
        int realtime_temperature = (int) Math.ceil(Double.parseDouble(RealtimeMap.get("气温（℃）")));
        String realtime_skycon = RealtimeMap.get("天气");

        // 获取系统时间
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat localmonth = new SimpleDateFormat("MM");
        SimpleDateFormat localdate = new SimpleDateFormat("dd");
        SimpleDateFormat localhour = new SimpleDateFormat("HH");
        SimpleDateFormat localminute = new SimpleDateFormat("mm");
        int LocalMonth = Integer.parseInt(localmonth.format(calendar.getTime()));
        int LocalDate = Integer.parseInt(localdate.format(calendar.getTime()));
        int LocalHour = Integer.parseInt(localhour.format(calendar.getTime()));
        int LocalMinute = Integer.parseInt(localminute.format(calendar.getTime()));
        // Log.i("timenow", String.valueOf(LocalHour)+String.valueOf(LocalMinute));
        // 获取日出日落时、分数据
        String sun_rise = GetData.Daily_sunrise[0];
        String sun_set = GetData.Daily_sunset[0];
        int sun_rise_h = Integer.parseInt(sun_rise.replaceAll("\\:\\d{2}",""));
        int sun_rise_m = Integer.parseInt(sun_rise.replaceAll("\\d{2}\\:",""));
        int sun_set_h = Integer.parseInt(sun_set.replaceAll("\\:\\d{2}",""));
        int sun_set_m = Integer.parseInt(sun_set.replaceAll("\\d{2}\\:",""));
        // 计算白昼已经过了几分钟/总共有几分钟
        int partofday = (LocalHour - sun_rise_h) * 60 + (LocalMinute - sun_set_m);
        int wholeday = (sun_set_h - sun_rise_h) * 60 + (sun_rise_m - sun_set_m);
        int sun_angle = ((partofday * 180) / wholeday) - 90;
        // Log.i("sun_angle", String.valueOf(sun_angle));

        float realtime_humidity_1 = Float.parseFloat(Objects.requireNonNull(RealtimeMap.get("湿度")))*100;
        String realtime_humidity = (int) realtime_humidity_1+"";
        String realtime_cloudrate = RealtimeMap.get("云量");
        String realtime_wind_speed = RealtimeMap.get("风速（m/s）");
        String realtime_wind_d = RealtimeMap.get("风向");
        assert realtime_wind_d != null;
        float realtime_wind_360 = Float.parseFloat(realtime_wind_d);
        int realtime_wind_direction_judge = (int) Math.ceil((realtime_wind_360) / 90);
        String realtime_wind_direction_8 = "null";
        switch (realtime_wind_direction_judge){
            case 1:
                realtime_wind_direction_8 = "东北";
                break;
            case 2:
                realtime_wind_direction_8 = "东南";
                break;
            case 3:
                realtime_wind_direction_8 = "西南";
                break;
            case 4:
                realtime_wind_direction_8 = "西北";
                break;
        }
        String realtime_wind_direction = realtime_wind_direction_8;
        float realtime_pressure_pa = Float.parseFloat(Objects.requireNonNull(RealtimeMap.get("地面气压（Pa）")));
        String realtime_pressure_hpa = ((int)realtime_pressure_pa/100)+"";
        String realtime_apparent_temperature = RealtimeMap.get("体感温度");
        String realtime_local_intensity = RealtimeMap.get("降水量（mm/h）");
        String realtime_air_quality_pm25 = RealtimeMap.get("pm2.5");
        String realtime_air_quality_pm10 = RealtimeMap.get("pm10");
        String realtime_air_quality_o3 = RealtimeMap.get("O3");
        String realtime_air_quality_no2 = RealtimeMap.get("NO2");
        String realtime_air_quality_co = RealtimeMap.get("CO");
        String realtime_air_quality_aqi_chn = RealtimeMap.get("国标aqi");
        String realtime_air_quality_description_usa = RealtimeMap.get("美标空气质量描述");

        runOnUiThread(new Runnable() {
            TextView server_time = findViewById(R.id.server_time);
            TextView intensity_num = findViewById(R.id.intensity_num);
            ImageView warn_icon = findViewById(R.id.warn_icon);
            ImageView sun_position = findViewById(R.id.sun_position);
            TextView sunrise = findViewById(R.id.sunrise);
            TextView sunset = findViewById(R.id.sunset);
            TextView temp_now = findViewById(R.id.temp_now);
            TextView fell_temp = findViewById(R.id.fell_temp);
            TextView wind_direction = findViewById(R.id.wind_direction);
            TextView wind_power = findViewById(R.id.wind_power);
            TextView humidity_num = findViewById(R.id.humidity_num);
            TextView pressure_num = findViewById(R.id.pressure_num);
            TextView api_num = findViewById(R.id.api_num);

            @SuppressLint("ResourceType")
            @Override
            public void run() {
                server_time.setText("");
                server_time.append(Server_time+"更新");

                ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) sun_position.getLayoutParams();
                layoutParams.circleAngle = sun_angle;
                int realtime_skyconID = getResources().getIdentifier(realtime_skycon.toLowerCase(), "mipmap", getPackageName());
                sun_position.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(),realtime_skyconID));

                sunrise.setText("");
                sunrise.append(sun_rise);

                sunset.setText("");
                sunset.append(sun_set);

                intensity_num.setText("");
                intensity_num.append(realtime_local_intensity+"mm");

                int WiID = getResources().getIdentifier(Wi, "mipmap", getPackageName());
                warn_icon.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(),WiID));
                temp_now.setText("");
                temp_now.append(realtime_temperature+"℃");
                fell_temp.setText("");
                fell_temp.append(realtime_apparent_temperature+"℃");
                wind_direction.setText("");
                wind_direction.append(realtime_wind_direction+"风");
                wind_power.setText("");
                wind_power.append(realtime_wind_speed+"m/s");
                humidity_num.setText("");
                humidity_num.append(realtime_humidity+"%");
                pressure_num.setText("");
                pressure_num.append(realtime_pressure_hpa+"hPa");
                api_num.setText("");
                api_num.append(realtime_air_quality_aqi_chn+"/"+realtime_air_quality_description_usa);
            }
        });
    }

    void setHourly() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                XmlPullParser parser = getResources().getXml(R.xml.chart_view);
//                AttributeSet attributes = Xml.asAttributeSet(parser);
//                HourlyChart hourlychart = new HourlyChart(getApplicationContext(),attributes);
//                hourlychart.invalidate();
                // Log.i("temp", "刷新图表");
//                //先获取当前布局的填充器
//                LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
//                //通过填充器获取另外一个布局的对象
//                View hourly_chartView = inflater.inflate(R.layout.hourly_chart, null);
//                //通过另外一个布局对象的findViewById获取其中的控件
//                HourlyChart hourly_chart = hourly_chartView.findViewById(R.id.ChartView1);
//                hourly_chart.invalidate();
                HourlyChart hourly_chart = findViewById(R.id.ChartView1);
                hourly_chart.invalidate();
            }
        });
    }


    // 显示白天温度数据
    void setDaily() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // 日期
                String[] Daily_date = GetData.Daily_date;
                // Log.i("星期", String.valueOf(Daily_date));
                TextView dd3 = findViewById(R.id.dd3);
                dd3.setText("");
                dd3.append(Daily_date[2]);
                TextView dd4 = findViewById(R.id.dd4);
                dd4.setText("");
                dd4.append(Daily_date[3]);
                TextView dd5 = findViewById(R.id.dd5);
                dd5.setText("");
                dd5.append(Daily_date[4]);
                TextView dd6 = findViewById(R.id.dd6);
                dd6.setText("");
                dd6.append(Daily_date[5]);
                TextView dd7 = findViewById(R.id.dd7);
                dd7.setText("");
                dd7.append(Daily_date[6]);


                // 昼间天气图标
                String[] skyconD = GetData.Daily_skycon_d;
                // Log.i("skyconD", Arrays.toString(skyconD));
                ImageView dDp1 = findViewById(R.id.dDp1);
                int skyconD1 = getResources().getIdentifier(skyconD[0].toLowerCase(), "mipmap", getPackageName());
                dDp1.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(),skyconD1));
                ImageView dDp2 = findViewById(R.id.dDp2);
                int skyconD2 = getResources().getIdentifier(skyconD[1].toLowerCase(), "mipmap", getPackageName());
                dDp2.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(),skyconD2));
                ImageView dDp3 = findViewById(R.id.dDp3);
                int skyconD3 = getResources().getIdentifier(skyconD[2].toLowerCase(), "mipmap", getPackageName());
                dDp3.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(),skyconD3));
                ImageView dDp4 = findViewById(R.id.dDp4);
                int skyconD4 = getResources().getIdentifier(skyconD[3].toLowerCase(), "mipmap", getPackageName());
                dDp4.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(),skyconD4));
                ImageView dDp5 = findViewById(R.id.dDp5);
                int skyconD5 = getResources().getIdentifier(skyconD[4].toLowerCase(), "mipmap", getPackageName());
                dDp5.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(),skyconD5));
                ImageView dDp6 = findViewById(R.id.dDp6);
                int skyconD6 = getResources().getIdentifier(skyconD[5].toLowerCase(), "mipmap", getPackageName());
                dDp6.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(),skyconD6));
                ImageView dDp7 = findViewById(R.id.dDp7);
                int skyconD7 = getResources().getIdentifier(skyconD[6].toLowerCase(), "mipmap", getPackageName());
                dDp7.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(),skyconD7));


                // 昼间温度
                String[] tempD = GetData.Daily_temperature_d;
                // Log.i("tempD", Arrays.toString(tempD));

                TextView dDt1 = findViewById(R.id.dDt1);
                dDt1.setText("");
                dDt1.append(tempD[0]);
                TextView dDt2 = findViewById(R.id.dDt2);
                dDt2.setText("");
                dDt2.append(tempD[1]);
                TextView dDt3 = findViewById(R.id.dDt3);
                dDt3.setText("");
                dDt3.append(tempD[2]);
                TextView dDt4 = findViewById(R.id.dDt4);
                dDt4.setText("");
                dDt4.append(tempD[3]);
                TextView dDt5 = findViewById(R.id.dDt5);
                dDt5.setText("");
                dDt5.append(tempD[4]);
                TextView dDt6 = findViewById(R.id.dDt6);
                dDt6.setText("");
                dDt6.append(tempD[5]);
                TextView dDt7 = findViewById(R.id.dDt7);
                dDt7.setText("");
                dDt7.append(tempD[6]);


                // 夜间温度
                String[] tempN = GetData.Daily_temperature_n;

                TextView dNt1 = findViewById(R.id.dNt1);
                dNt1.setText("");
                dNt1.append(tempN[0]);
                TextView dNt2 = findViewById(R.id.dNt2);
                dNt2.setText("");
                dNt2.append(tempN[1]);
                TextView dNt3 = findViewById(R.id.dNt3);
                dNt3.setText("");
                dNt3.append(tempN[2]);
                TextView dNt4 = findViewById(R.id.dNt4);
                dNt4.setText("");
                dNt4.append(tempN[3]);
                TextView dNt5 = findViewById(R.id.dNt5);
                dNt5.setText("");
                dNt5.append(tempN[4]);
                TextView dNt6 = findViewById(R.id.dNt6);
                dNt6.setText("");
                dNt6.append(tempN[5]);
                TextView dNt7 = findViewById(R.id.dNt7);
                dNt7.setText("");
                dNt7.append(tempN[6]);

                // 夜间天气图标
                String[] skyconN = GetData.Daily_skycon_n;

                ImageView dNp1 = findViewById(R.id.dNp1);
                int skyconN1 = getResources().getIdentifier(skyconN[0].toLowerCase(), "mipmap", getPackageName());
                dNp1.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(),skyconN1));
                ImageView dNp2 = findViewById(R.id.dNp2);
                int skyconN2 = getResources().getIdentifier(skyconN[1].toLowerCase(), "mipmap", getPackageName());
                dNp2.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(),skyconN2));
                ImageView dNp3 = findViewById(R.id.dNp3);
                int skyconN3 = getResources().getIdentifier(skyconN[2].toLowerCase(), "mipmap", getPackageName());
                dNp3.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(),skyconN3));
                ImageView dNp4 = findViewById(R.id.dNp4);
                int skyconN4 = getResources().getIdentifier(skyconN[3].toLowerCase(), "mipmap", getPackageName());
                dNp4.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(),skyconN4));
                ImageView dNp5 = findViewById(R.id.dNp5);
                int skyconN5 = getResources().getIdentifier(skyconN[4].toLowerCase(), "mipmap", getPackageName());
                dNp5.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(),skyconN5));
                ImageView dNp6 = findViewById(R.id.dNp6);
                int skyconN6 = getResources().getIdentifier(skyconN[5].toLowerCase(), "mipmap", getPackageName());
                dNp6.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(),skyconN6));
                ImageView dNp7 = findViewById(R.id.dNp7);
                int skyconN7 = getResources().getIdentifier(skyconN[6].toLowerCase(), "mipmap", getPackageName());
                dNp7.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(),skyconN7));
            }
        });
    }
}