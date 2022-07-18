package xuanniao.weather.recode;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.Arrays;

import static android.graphics.Typeface.DEFAULT_BOLD;

public class HourlyChart extends View {
    public static String[] time; // 时间
    public static double[] temp; // 温度
    public static String[] skycon; // 天气

    // 重写context
    public HourlyChart(Context context, AttributeSet attrs) {
        super(context, attrs);
//        scroller=new Scroller(context);
//        screenW = this.getWidth();
//        screenH = this.getHeight();
    }

    // 对外方法
    public void SetInfo(String[] HourlyList_time, double[] HourlyList_temp) {
        time = HourlyList_time;
        temp = HourlyList_temp;
    }

    public static int[] Chart() {
        // 导入数组
        time = GetData.HourlyList_time;
        temp = GetData.HourlyList_temp;
        if (time == null) {
            time = new String[]{"0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0"};
        }
        if (temp == null) {
            temp = new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        }
        // 提取最高温
        double max = temp[0];//假设第一个值为最大值
        for (double i : temp) { //和后面的数进行比较
            if (i > max) {
                max = i;
            }
        }
        // 提取最低温
        double min = temp.length;//假设第一个值为最大值
        for (double i : temp) { //和后面的数进行比较
            if (i < min) {
                min = i;
            }
        }
        // 计算最大温差
        double TempVaries = max - min;
        // 计算该组温度的温度总和
        double TempSum = 0;
        for (double SumMix : temp) {
            TempSum += SumMix;
        }
        // 计算该组温度平均值(用整数接收近似值结果，减少浮点运算)
//        int TempAvg = TempSum/temp.length;
        // 指定绘图区域高度
        int ChartHigh = 150;
        // 计算每度需要多少像素表示
        double TempPerPix = ChartHigh / TempVaries;
        // 计算每个温度点在图标中应显示的高度
        int[] HighPix = new int[temp.length];
        for (int c = 0; c < temp.length; c++) {
            // 计算每个温度点相对于最低温的温度差
            double TempD = (temp[c] - min);
            // 计算每个温度点的高度
            double hip = TempD * TempPerPix;
            int hip_int = (int) Math.round(hip);
            HighPix[c] = hip_int;
        }
        return HighPix;
    }

    public static String[] Pic() {
        skycon = GetData.HourlyList_skycon;
        String[] skycon_num;
        if (skycon == null) {
            skycon_num = new String[]{"clear_day", "clear_day", "clear_day", "clear_day", "clear_day","clear_day",
                    "clear_day", "clear_day", "clear_day", "clear_day", "clear_day", "clear_day", "clear_day", "clear_day",
                    "clear_day", "clear_day", "clear_day", "clear_day", "clear_day", "clear_day", "clear_day", "clear_day",
                    "clear_day", "clear_day", "clear_day", "clear_day", "clear_day", "clear_day", "clear_day", "clear_day"};
        }else {
            skycon_num = new String[skycon.length];
            for (int d = 0; d < skycon.length; d++){
                switch(skycon[d]){
                    case"CLEAR_DAY":
                        skycon_num[d]="clear_day";
                        break;
                    case"CLEAR_NIGHT":
                        skycon_num[d]="clear_night";
                        break;
                    case"PARTLY_CLOUDY_DAY":
                        skycon_num[d]="partly_cloudy_day";
                        break;
                    case"PARTLY_CLOUDY_NIGHT":
                        skycon_num[d]="partly_cloudy_night";
                        break;
                    case"CLOUDY":
                        skycon_num[d]="cloudy";
                        break;
                    case"LIGHT_HAZE":
                        skycon_num[d]="light_haze";
                        break;
                    case"MODERATE_HAZE":
                        skycon_num[d]="moderate_haze";
                        break;
                    case"HEAVY_HAZE":
                        skycon_num[d]="smoke";
                        break;
                    case"LIGHT_RAIN":
                        skycon_num[d]="light_rain";
                        break;
                    case"MODERATE_RAIN":
                        skycon_num[d]="moderate_rain";
                        break;
                    case"HEAVY_RAIN":
                        skycon_num[d]="heavy_rain";
                        break;
                    case"STORM_RAIN":
                        skycon_num[d]="storm_rain";
                        break;
                    case"FOG":
                        skycon_num[d]="fog";
                        break;
                    case"LIGHT_SNOW":
                        skycon_num[d]="light_snow";
                        break;
                    case"MODERATE_SNOW":
                        skycon_num[d]="moderate_snow";
                        break;
                    case"HEAVY_SNOW":
                        skycon_num[d]="heavy_snow";
                        break;
                    case"STORM_SNOW":
                        skycon_num[d]="storm_snow";
                        break;
                    case"DUST":
                        skycon_num[d]="dust";
                        break;
                    case"SAND":
                        skycon_num[d]="sand";
                        break;
                    case"WIND":
                        skycon_num[d]="wind";
                        break;
                }
            }
        }
        return skycon_num;
    }


    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);//重写onDraw方法
//        canvas.drawColor(Color.BLUE);//设置背景颜色
        // 创建画笔
        @SuppressLint("DrawAllocation")
        Paint paintH = new Paint();
        paintH.setTextSize(50);//设置字体大小
        paintH.setTypeface(DEFAULT_BOLD);//设置字体类型
        paintH.setAntiAlias(true);// 画笔预设，抗锯齿
        Paint paintB = new Paint();
        paintB.setTextSize(35);//设置字体大小
        paintB.setTypeface(DEFAULT_BOLD);//设置字体类型
        paintB.setAntiAlias(true);// 画笔预设，抗锯齿
        Paint paintW = new Paint();
        paintW.setColor(Color.WHITE);
        paintW.setAntiAlias(true);// 画笔预设，抗锯齿
        // 导入数据
        time = GetData.HourlyList_time;
        int[] HighPix = Chart();
        String[] skycon_num = Pic();
        // 画折线、点、天气、温度
        if (HighPix.length > 1) {
            for (int i = 0; i < HighPix.length; i++) {
                int StartY = HighPix[i];
                // 绘制X轴：时间标注
                canvas.drawText(time[i], i * 200, 375, paintB);
                // 绘制点
                canvas.drawCircle(70 + i * 200, 200 - StartY, 10, paintB);
                // 绘制折线
                if (i + 1 < HighPix.length) {
                    int StopY = HighPix[i + 1];
                    canvas.drawLine(70 + i * 200, 200 - StartY, 70 + (i + 1) * 200, 200 - StopY, paintB);
                }
                canvas.drawCircle(70 + i * 200, 200 - StartY, 7, paintW);
                // 绘制天气图标
                int resID = getResources().getIdentifier(skycon_num[i], "mipmap", getContext().getPackageName());
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), resID);
                canvas.drawBitmap(bitmap, 35 + i * 200, 240 - StartY, paintB);
                // 绘制温度数值
                canvas.drawText(temp[i] + "", 35 + i * 200, 185 - StartY, paintH);
            }
        }
    }
}