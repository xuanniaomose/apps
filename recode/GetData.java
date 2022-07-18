package xuanniao.weather.recode;

import android.annotation.SuppressLint;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

// 需求的数据：
// 当前模块：温度、天气、日出日落时间、风力风向、湿度、体感温度、气压、舒适度、空气质量、预警、当前降水量
// 24小时预报：未来24小时逐小时温度和天气
// 未来一周预报：未来一周逐日的：最高最低温、天气状况、空气质量、风向风力
public class GetData {

    /** 实时信息*/
    public static HashMap<String, String> RealtimeMap;
    public static HashMap<String, String> Realtime(String weather_data) {
        try {
            RealtimeMap = new HashMap<String, String>();
            JSONObject WeatherData = new JSONObject(weather_data);
            /**预警信息-当前*/
            Long server_time = WeatherData.getLong("server_time");
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat format =  new SimpleDateFormat("HH:mm");
            String Server_time = format.format(server_time);
            RealtimeMap.put("更新时间", Server_time);
            JSONObject result = WeatherData.getJSONObject("result");
            JSONObject alert = result.getJSONObject("alert");
            JSONArray content_a = alert.getJSONArray("content");
            for (int i = 0; i < content_a.length(); i++) {
                JSONObject content = content_a.getJSONObject(i);
                String warn_code = content.getString("code");
                RealtimeMap.put("预警代码", warn_code);
                String warn_title = content.getString("title");
                RealtimeMap.put("预警标题", warn_title);
                String warn_description = content.getString("description");
                RealtimeMap.put("预警内容", warn_description);
            }
            /**实时预报-当前天气*/
            JSONObject realtime = result.getJSONObject("realtime");
            String realtime_temperature = realtime.getString("temperature");
            String realtime_skycon = realtime.getString("skycon");
            String realtime_humidity = realtime.getString("humidity");
            String realtime_cloudrate = realtime.getString("cloudrate");
            JSONObject realtime_wind = realtime.getJSONObject("wind");
            String realtime_wind_speed = realtime_wind.getString("speed");
            String realtime_wind_direction = realtime_wind.getString("direction");
            String realtime_pressure = realtime.getString("pressure");
            String realtime_apparent_temperature = realtime.getString("apparent_temperature");
            JSONObject realtime_precipitation = realtime.getJSONObject("precipitation");
            JSONObject local = realtime_precipitation.getJSONObject("local");
            String realtime_local_intensity = local.getString("intensity");
            JSONObject realtime_air_quality = realtime.getJSONObject("air_quality");
            String realtime_air_quality_pm25 = realtime_air_quality.getString("pm25");
            String realtime_air_quality_pm10 = realtime_air_quality.getString("pm10");
            String realtime_air_quality_o3 = realtime_air_quality.getString("o3");
            String realtime_air_quality_so2 = realtime_air_quality.getString("so2");
            String realtime_air_quality_no2 = realtime_air_quality.getString("no2");
            String realtime_air_quality_co = realtime_air_quality.getString("co");
            JSONObject realtime_air_quality_aqi = realtime_air_quality.getJSONObject("aqi");
            String realtime_air_quality_aqi_chn = realtime_air_quality_aqi.getString("chn");
            String realtime_air_quality_aqi_usa = realtime_air_quality_aqi.getString("usa");
            JSONObject realtime_air_quality_description = realtime_air_quality.getJSONObject("description");
            String realtime_air_quality_description_chn = realtime_air_quality_description.getString("chn");
            String realtime_air_quality_description_usa = realtime_air_quality_description.getString("usa");
            JSONObject realtime_life_index = realtime.getJSONObject("life_index");
            JSONObject realtime_life_index_comfort = realtime_life_index.getJSONObject("comfort");
            String realtime_life_index_comfort_desc = realtime_life_index_comfort.getString("desc");

            RealtimeMap.put("气温（℃）", realtime_temperature);
            RealtimeMap.put("天气", realtime_skycon);
            RealtimeMap.put("湿度", realtime_humidity);
            RealtimeMap.put("云量", realtime_cloudrate);
            RealtimeMap.put("风速（m/s）", realtime_wind_speed);
            RealtimeMap.put("风向", realtime_wind_direction);
            RealtimeMap.put("地面气压（Pa）", realtime_pressure);
            RealtimeMap.put("体感温度", realtime_apparent_temperature);
            RealtimeMap.put("降水量（mm/h）", realtime_local_intensity);
            RealtimeMap.put("舒适度指数", realtime_life_index_comfort_desc);
            RealtimeMap.put("pm2.5", realtime_air_quality_pm25);
            RealtimeMap.put("pm10", realtime_air_quality_pm10);
            RealtimeMap.put("O3", realtime_air_quality_o3);
            RealtimeMap.put("NO2", realtime_air_quality_no2);
            RealtimeMap.put("SO2", realtime_air_quality_so2);
            RealtimeMap.put("CO", realtime_air_quality_co);
            RealtimeMap.put("国标aqi", realtime_air_quality_aqi_chn);
            RealtimeMap.put("美标aqi", realtime_air_quality_aqi_usa);
            RealtimeMap.put("国标空气质量描述", realtime_air_quality_description_chn);
            RealtimeMap.put("美标空气质量描述", realtime_air_quality_description_usa);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return RealtimeMap;
    }

    /**小时级预报-未来24小时*/
    public static String[] HourlyList_time;
    public static double[] HourlyList_temp;
    public static String[] HourlyList_skycon;
    public static String[] Hourly_time(String weather_data) {
        HourlyList_time = new String[24];
        try {
            JSONObject WeatherData = new JSONObject(weather_data);
            JSONObject result = WeatherData.getJSONObject("result");
            JSONObject hourly = result.getJSONObject("hourly");
            JSONArray hourly_temperature_a = hourly.getJSONArray("temperature");
            // Log.i("HourlyList_temp", String.valueOf(hourly_temperature_a));
            for (int i = 0; i < hourly_temperature_a.length(); i++) {
                JSONObject hourly_temperature = hourly_temperature_a.getJSONObject(i);
                String hourly_temperature_datetime_complete = hourly_temperature.getString("datetime");
                String hourly_temperature_datetime_a = hourly_temperature_datetime_complete.replaceAll("\\d{4}\\-\\d{2}\\-", "");
                String hourly_temperature_datetime_b = hourly_temperature_datetime_a.replaceAll("\\:\\d{2}\\+\\d{2}\\:\\d{2}", "时");
                String hourly_temperature_datetime = hourly_temperature_datetime_b.replaceAll("T", "日");
                HourlyList_time[i] = hourly_temperature_datetime;
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return HourlyList_time;
    }

    public static double[] Hourly_temp(String weather_data) {
        HourlyList_temp = new double[24];
        try {
            JSONObject WeatherData = new JSONObject(weather_data);
            JSONObject result = WeatherData.getJSONObject("result");
            JSONObject hourly = result.getJSONObject("hourly");
            JSONArray hourly_temperature_a = hourly.getJSONArray("temperature");
            for (int i = 0; i < hourly_temperature_a.length(); i++) {
                JSONObject hourly_temperature = hourly_temperature_a.getJSONObject(i);
                double hourly_temperature_value = hourly_temperature.getDouble("value");
                HourlyList_temp[i] = hourly_temperature_value;
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        // Log.i("HourlyList_temp", Arrays.toString(HourlyList_temp));
        return HourlyList_temp;
    }

    public static String[] Hourly_skycon(String weather_data) {
        HourlyList_skycon = new String[24];
        try {
            JSONObject WeatherData = new JSONObject(weather_data);
            JSONObject result = WeatherData.getJSONObject("result");
            JSONObject hourly = result.getJSONObject("hourly");
            JSONArray hourly_skycon_a = hourly.getJSONArray("skycon");
            // Log.i("HourlyList_skycon", String.valueOf(hourly_skycon_a));
            for (int i = 0; i < hourly_skycon_a.length(); i++) {
                JSONObject hourly_skycon = hourly_skycon_a.getJSONObject(i);
                String hourly_skycon_value = hourly_skycon.getString("value");
                HourlyList_skycon[i] = hourly_skycon_value;
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        // Log.i("HourlyList_skycon", Arrays.toString(HourlyList_skycon));
        return HourlyList_skycon;
    }



    /**天级别预报*/
    public static String[] Daily_date;
    public static String[] Daily_sunrise;
    public static String[] Daily_sunset;
    public static String[] Daily_temperature_d;
    public static String[] Daily_temperature_n;
    public static String[] Daily_skycon_d;
    public static String[] Daily_skycon_n;

    public static String[] Daily_date(String weather_data) {
        Daily_date = new String[7];
        try {
            JSONObject WeatherData = new JSONObject(weather_data);
            JSONObject result = WeatherData.getJSONObject("result");
            JSONObject daily = result.getJSONObject("daily");
            JSONArray daily_astro_complete = daily.getJSONArray("astro");
            // Log.i("Daily_astro", String.valueOf(daily_astro_complete));
            for (int i = 0; i < daily_astro_complete.length(); i++) {
                JSONObject daily_astro_date_a = daily_astro_complete.getJSONObject(i);
                String daily_astro_date_b = daily_astro_date_a.getString("date");
                String daily_astro_date_c = daily_astro_date_b.replaceAll("\\d{4}\\-\\d{2}\\-", "");
                String daily_astro_date_d = daily_astro_date_c.replaceAll("\\d{2}\\:\\d{2}\\+\\d{2}\\:\\d{2}", "");
                String daily_astro_date = daily_astro_date_d.replaceAll("T", "日");
                Daily_date[i] = daily_astro_date;
            }
            // Log.i("Daily_date", Arrays.toString(Daily_date));
            // 昼间温度
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Daily_date;
    }

    // 日出时间
    public static String[] Daily_sunrise(String weather_data) {
        Daily_sunrise = new String[7];
        try {
            JSONObject WeatherData = new JSONObject(weather_data);
            JSONObject result = WeatherData.getJSONObject("result");
            JSONObject daily = result.getJSONObject("daily");
            JSONArray daily_astro_complete = daily.getJSONArray("astro");
            for (int i = 0; i < daily_astro_complete.length(); i++) {
                JSONObject daily_astro_date_a = daily_astro_complete.getJSONObject(i);
                JSONObject daily_astro_sunrise = daily_astro_date_a.getJSONObject("sunrise");
                String daily_astro_sunrise_time = daily_astro_sunrise.getString("time");
                Daily_sunrise[i] = daily_astro_sunrise_time;
            }
            // Log.i("Daily_sunrise", Arrays.toString(Daily_sunrise));
            // 昼间温度
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Daily_sunrise;
    }

    // 日落时间
    public static String[] Daily_sunset(String weather_data) {
        Daily_sunset = new String[7];
        try {
            JSONObject WeatherData = new JSONObject(weather_data);
            JSONObject result = WeatherData.getJSONObject("result");
            JSONObject daily = result.getJSONObject("daily");
            JSONArray daily_astro_complete = daily.getJSONArray("astro");
            for (int i = 0; i < daily_astro_complete.length(); i++) {
                JSONObject daily_astro_date_a = daily_astro_complete.getJSONObject(i);
                String daily_astro_date_b = daily_astro_date_a.getString("date");
                String daily_astro_date_c = daily_astro_date_b.replaceAll("\\d{4}\\-\\d{2}\\-", "");
                String daily_astro_date_d = daily_astro_date_c.replaceAll("\\d{2}\\:\\d{2}\\+\\d{2}\\:\\d{2}", "");
                String daily_astro_date = daily_astro_date_d.replaceAll("T", "日");
                JSONObject daily_astro_sunset = daily_astro_date_a.getJSONObject("sunset");
                String daily_astro_sunset_time = daily_astro_sunset.getString("time");
                Daily_sunset[i] = daily_astro_sunset_time;
            }
            // Log.i("Daily_sunset", Arrays.toString(Daily_sunset));
            // 昼间温度
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Daily_sunset;
    }

    // 昼间温度
    public static String[] Daily_tempD(String weather_data) {
        Daily_temperature_d = new String[7];
        try {
            JSONObject WeatherData = new JSONObject(weather_data);
            JSONObject result = WeatherData.getJSONObject("result");
            JSONObject daily = result.getJSONObject("daily");
            JSONArray daily_temperature_08h_20h_complete = daily.getJSONArray("temperature_08h_20h");
            // Log.i("Daily_temperature_08h_20h_complete", String.valueOf(daily_temperature_08h_20h_complete));
            for (int i = 0; i < daily_temperature_08h_20h_complete.length(); i++) {
                JSONObject daily_temperature_08h_20h = daily_temperature_08h_20h_complete.getJSONObject(i);
                String daily_temperature_08h_20h_max = daily_temperature_08h_20h.getString("max");
                Daily_temperature_d[i] = daily_temperature_08h_20h_max;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Daily_temperature_d;
    }

    // 昼间天气
    public static String[] Daily_skyconD(String weather_data) {
        Daily_skycon_d = new String[7];
        try {
            JSONObject WeatherData = new JSONObject(weather_data);
            JSONObject result = WeatherData.getJSONObject("result");
            JSONObject daily = result.getJSONObject("daily");
            JSONArray skycon = daily.getJSONArray("skycon_08h_20h");
            // Log.i("Daily_skycon_08h_20h", String.valueOf(skycon));
            for (int i = 0; i < skycon.length(); i++) {
                JSONObject skyconD = skycon.getJSONObject(i);
                String Daily_skyconD = skyconD.getString("value");
                Daily_skycon_d[i] = Daily_skyconD;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Daily_skycon_d;
    }

    // 夜间温度
    public static String[] Daily_tempN(String weather_data) {
        Daily_temperature_n = new String[7];
        try {
            JSONObject WeatherData = new JSONObject(weather_data);
            JSONObject result = WeatherData.getJSONObject("result");
            JSONObject daily = result.getJSONObject("daily");
            JSONArray daily_temperature_20h_32h_complete = daily.getJSONArray("temperature_20h_32h");
            // Log.i("Daily_temperature_20h_32h_complete", String.valueOf(daily_temperature_20h_32h_complete));
            for (int i = 0; i < daily_temperature_20h_32h_complete.length(); i++) {
                JSONObject daily_temperature_20h_32h = daily_temperature_20h_32h_complete.getJSONObject(i);
                String daily_temperature_20h_32h_min = daily_temperature_20h_32h.getString("min");
                Daily_temperature_n[i] = daily_temperature_20h_32h_min;
            }
            // Log.i("Daily_temperature_n", Arrays.toString(Daily_temperature_n));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Daily_temperature_n;
    }

    // 夜间天气
    public static String[] Daily_skyconN(String weather_data) {
        Daily_skycon_n = new String[7];
        try {
            JSONObject WeatherData = new JSONObject(weather_data);
            JSONObject result = WeatherData.getJSONObject("result");
            JSONObject daily = result.getJSONObject("daily");
            JSONArray skycon = daily.getJSONArray("skycon_20h_32h");
            // Log.i("Daily_skycon_20h_32h", String.valueOf(skycon));
            for (int i = 0; i < skycon.length(); i++) {
                JSONObject skyconN = skycon.getJSONObject(i);
                String Daily_skyconN = skyconN.getString("value");
                Daily_skycon_n[i] = Daily_skyconN;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Daily_skycon_n;
    }
}