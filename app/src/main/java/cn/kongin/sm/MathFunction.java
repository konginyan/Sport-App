package cn.kongin.sm;

import android.app.Activity;
import android.content.SharedPreferences;
import android.telephony.TelephonyManager;
import android.util.Log;

public class MathFunction {
    /**
     * 身高测步距
     * @return m/步
     */
    public static double height2step(){
        return 0.65;
    }

    /**
     * 步数算距离
     * @param step 步数
     * @param speed height2step()得到
     * @return m
     */
    public static double step2length(int step, double speed){
        return step*speed;
    }

    /**
     * 计步算卡路里
     * @param length km
     * @return 千卡
     */
    public static double step2calorie(double length){
        return 32*length;
    }

    /**
     * 跑步算卡路里
     * @param weight kg
     * @param length km
     * @return 千卡
     */
    public static double run2calorie(double weight, double length){
        return 1.036*length*weight;
    }

    public static double getStepLength(Activity activity){
        SharedPreferences mySharedPreferences;
        mySharedPreferences = activity.getSharedPreferences("test", 0);
        double speed = height2step();
        int step = mySharedPreferences.getInt("RealStep",0);
        return step2length(step,speed);
    }

    public static double getStepCalorie(Activity activity){
        double length = getStepLength(activity);
        return step2calorie(length/1000);
    }

    public static double getRunCalorie(Activity activity, double length){
        SharedPreferences mySharedPreferences;
        mySharedPreferences = activity.getSharedPreferences("test", 0);
        String temp = mySharedPreferences.getString("pWeight","未设置");
        if(temp.equals("未设置")){
            return -1;
        }
        double weight = Double.parseDouble(temp.substring(0,temp.length()-2));
        return run2calorie(weight, length);
    }

    public static String cutfloat(double fnum, int side){
        java.text.DecimalFormat df;
        switch (side){
            case 1:df = new java.text.DecimalFormat("#0.0");
                break;
            case 2:df = new java.text.DecimalFormat("#0.00");
                break;
            default:df = new java.text.DecimalFormat("#0.0");
                break;
        }
        return df.format(fnum);
    }
}
