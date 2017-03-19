package cn.kongin.sm;

import android.support.annotation.NonNull;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Time {
    /**
     * 日期格式字符串转换成时间戳
     * @param dateStr 字符串日期
     * @return
     */
    public static long Date2TimeStamp(String dateStr) {
        try{
            long epoch = new java.text.SimpleDateFormat("yyyy/MM/dd HH:mm:ss").parse(dateStr).getTime()/1000;
            return epoch;
        }
        catch (Exception e){

        }
        return 0;
    }

    /**
     * 将时间戳转换为时间
     * @param s 时间戳
     * @return
     */
    public static String TimeStamp2Date(Long s){
        String res;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date(s);
        res = simpleDateFormat.format(date);
        return res;
    }
    public static String TimeStamp2Date(int s){
        long ss = (long)s * 1000;
        String res;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date(ss);
        res = simpleDateFormat.format(date);
        return res;
    }

    /**
     * 得到当前时间戳
     */
    @NonNull
    public static long getCurrentTime(){
        return Date2TimeStamp(getCurrentDate());
    }

    /**
     * 得到day天前的时间戳
     * @param day
     * @return
     */
    public static long getCurrentTime(int day){
        return Date2TimeStamp(getCurrentDate(day));
    }

    /**
     * 得到当前日期
     * @return
     */
    public static String getCurrentDate(){
        Long l = System.currentTimeMillis();
        String s = TimeStamp2Date(l);
        return s;
    }

    /**
     * 得到day天前的日期
     * @param day
     * @return
     */
    public static String getCurrentDate(int day){
        Long l = System.currentTimeMillis()-day*3600*1000*24;
        String s = TimeStamp2Date(l);
        return s;
    }
}