package xuanniao.weather.recode;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import androidx.annotation.RequiresApi;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.ss.usermodel.DataFormatter;

import java.io.*;
import java.util.Date;
import java.util.Map;

import java.text.SimpleDateFormat;

import static android.content.Context.MODE_PRIVATE;


public class SaveAsExcel {
    private static final SaveAsExcel instance = new SaveAsExcel();
    public static SaveAsExcel getInstance(){
        return instance;
    }
    static HSSFWorkbook work_book;
    static HSSFWorkbook workbook;
    static HSSFSheet sheet;
    static FileInputStream fis;
    static FileOutputStream fos;
    // 定义日期格式
    static Date date = new Date();
    static SimpleDateFormat dateFormat = new SimpleDateFormat("dd");


    @RequiresApi(api = Build.VERSION_CODES.N)
    public static String createExcel(Context context, Map<String, String> RealtimeMap, Integer RecodeTime) throws IOException {
        // 使用项目根目录, 文件名加上月份时间戳
        SimpleDateFormat monthFormat = new SimpleDateFormat("yyyy-MM");
        String filename = monthFormat.format(date) + ".xls";
        boolean flag = fileIsExists(context, filename);
        if (flag == false) {
            try {
                // Log.i("文件不存在","新建了今天的记录文件");
                // 如果指定路径不存在文件 则创建新Excel 工作簿
                HSSFWorkbook workbook = new HSSFWorkbook();
                fos = context.openFileOutput(filename, MODE_PRIVATE);
                workbook.write(fos);
                workbook.close();
                fos.flush();
                fos.close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        try {
            fis = context.openFileInput(filename);
//            POIFSFileSystem fs = new POIFSFileSystem(fis);
            work_book = new HSSFWorkbook(fis);
            workbook = write_row(work_book, RealtimeMap, RecodeTime);
            fos = context.openFileOutput(filename, MODE_PRIVATE);
            workbook.write(fos);
            workbook.close();
            fos.flush();
            fos.close();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        return filename;
    }

    public static HSSFWorkbook write_row(HSSFWorkbook workbook, Map<String,String> RealtimeMap, Integer RecodeTime) {
        // 获取当前日期的Sheet（工作表）Id
        int sheetIndex = workbook.getSheetIndex(dateFormat.format(date));
        // 如果没有就创建一个工作表 没有就创建 有就读取
        if (sheetIndex == -1) {
            sheet = workbook.createSheet(dateFormat.format(date));
        } else {
            sheet = workbook.getSheetAt(sheetIndex);
        }
        // 判断工作表是否是空的
        HSSFRow firstRow = sheet.getRow(0);
        if (String.valueOf(firstRow).equals("null")) {
            sheet = sheetfirstrow_write();
        }
        // 确定该小时数据应写入第t+1行 并开始写入
        int t;
        if (RecodeTime == null) {
            SimpleDateFormat hourFormat = new SimpleDateFormat("HH");
            t = Integer.parseInt(hourFormat.format(date))+1;
        }else {
            t = RecodeTime;
        }
        // Log.i("行数", String.valueOf(t+1));
        HSSFRow work_row = sheet.createRow((short) t);
        for (int a = 0; a < RealtimeMap.size() - 1; a++) {
            // 读取首行
            HSSFRow key_row = sheet.getRow(0);
            // 读取首行第a列的单元格
            HSSFCell keycell = key_row.getCell(a);
            // 读取key值
            DataFormatter formatter = new DataFormatter();
            String key = formatter.formatCellValue(keycell);
            // 按key值取出weather_data中的对应值
            String cell_content = RealtimeMap.get(key);
            if (key == "更新时间") {
                cell_content = cell_content.replaceAll("\\:\\d{2}\\:\\d{2}\\:", "");
            }
            // 把对应值写入cell中
            HSSFCell work_cell = work_row.createCell((short) a);
            work_cell.setCellValue(cell_content);
        }
        return  workbook;
    }


    // 写入表头
    public static HSSFSheet sheetfirstrow_write() {
        String[] itemlist = {"更新时间", "天气", "气温（℃）", "湿度", "地面气压（Pa）", "降水量（mm/h）",
                "云量", "风向", "风速（m/s）", "体感温度", "舒适度指数", "国标aqi", "美标aqi", "pm2.5", "pm10",
                "O3","NO2", "SO2", "CO", "国标空气质量描述", "美标空气质量描述", "预警标题", "预警内容"};
        // 在索引0的位置创建行（首行）
        HSSFRow row = sheet.createRow(0);
        for (int i = 0; i < itemlist.length; i++) {
            // 在索引i的位置创建单元格（左端第i+1列）
            HSSFCell itemcell = row.createCell((short) i);
            // 读取项目列表中的第i项
            String item = itemlist[i];
            // 向目标单元格中写入第i项的值
            itemcell.setCellValue(item);
        }
        return sheet;
    }


    //判断文件是否存在
    public static boolean fileIsExists(Context context, String filename) {
        try {
            String AbsolutePath = context.getFilesDir().getAbsolutePath();
            File f=new File(AbsolutePath+"/"+filename);
            if(!f.exists()) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }
}