package xuanniao.weather.recode;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class SettingControl extends AppCompatActivity {
    private String Token;
    private EditText token;
    private EditText city_input;
    private String CityNameZh;
    private String laglat;
    private SharedPreferences preferences;

    // 获取token、city和laglat并写入SetInfo
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting);
        // 获取Shared Preferences对象
        SharedPreferences setInfo = getSharedPreferences("SetInfo", MODE_PRIVATE);
        // 获取Editor
        SharedPreferences.Editor editor = setInfo.edit();

        setEditText();

        // 监听令牌输入框
        token = findViewById(R.id.token);
        token.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_DONE) {
                    //隐藏软键盘
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
                    Token = textView.getText().toString();
                    editor.putString("user_token", Token);
                    editor.commit();
                    // Log.i("Token", Token);
                }
                return false;
            }
        });


        // 监听城市输入框
        city_input = findViewById(R.id.city_input);
        city_input.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_DONE) {
                    //隐藏软键盘
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
                    CityNameZh = textView.getText().toString();
                    editor.putString("CityNameZh", CityNameZh);
                    editor.commit();
                    // Log.i("city", CityNameZh);
                    try {
                        laglat = cityJson(CityNameZh);
                        editor.putString("LagLat", laglat);
                        editor.commit();
                        // Log.i("laglat", laglat);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                return false;
            }
        });


        // 天气记录的开关
        Switch mSwitch = findViewById(R.id.switch_recode);
        Intent i = new Intent(this, AlarmManager.class);
        PendingIntent pi = PendingIntent.getService(SettingControl.this, 0, i, 0);
        mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    // 开启时对应的程序
                    startService(i);

                }else {
                    // 关闭时对应的程序
//                    mWorkManager.cancelUniqueWork("Recode");
                    stopService(i);
                }
            }
        });
    }

    private void setEditText() {
        preferences = getSharedPreferences("SetInfo", MODE_PRIVATE);
        // Log.i("存在性", String.valueOf(preferences));
        // 如果SetInfo文件已经被创建 则读取其中内容
        if (preferences != null) {
            city_input = findViewById(R.id.city_input);
            CityNameZh = preferences.getString("CityNameZh", null);
            if (CityNameZh == null) {
                city_input.setText("不需要带市，如：北京");
            } else {
                city_input.setText("");
                city_input.append(CityNameZh);
            }
            token = findViewById(R.id.token);
            Token = preferences.getString("user_token", null);
            if (Token == null) {
                token.setText("注意区分大小写");
            } else {
                token.setText("");
                token.append(Token);
            }
        }
    }

    // 通过文件名读取到CityLagLat 并解析出城市经纬度
    public String cityJson(String city) throws IOException {
        String laglat = "请输入正确城市名";
        try {
            InputStreamReader inputStreamReader = new InputStreamReader(getAssets().open("CityLagLat.json"), "UTF-8");
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line;
            StringBuilder stringBuilder = new StringBuilder();
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
            bufferedReader.close();
            inputStreamReader.close();
            JSONObject jsonObject = new JSONObject(stringBuilder.toString());
            laglat = jsonObject.getString(city);
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
        return laglat;
    }
}