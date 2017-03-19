package cn.kongin.sm;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

public class StepService extends Service{

    public static int realstep;
    public int forestep;
    public boolean flag;
    private SensorManager sManager;
    private SensorEventListener sel;
    private Thread thread;

    SharedPreferences mySharedPreferences;
    SharedPreferences.Editor editor;

    String startDate;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.i("mess","onbin");
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        thread = new Thread(new Runnable() {
            public void run() {
                initStep();
            }
        });
        thread.start();
        recordData();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sManager.unregisterListener(sel);
        flag = false;
    }

    private void initStep() {
        flag = true;

        sel = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {

                loadstepdata();
                float steps = event.values[0];
                Log.i("data",String.valueOf(steps));
                if(forestep == -1)forestep = (int)steps;
                if(forestep>steps)forestep = 0;
                realstep += (int)steps - forestep;
                savestepdata(steps);
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };

        sManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sManager.registerListener(sel, sManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER),
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void loadstepdata(){
        mySharedPreferences= getSharedPreferences("test", 0);
        forestep = mySharedPreferences.getInt("ForeStep",-1);
        realstep = mySharedPreferences.getInt("RealStep",0);
        Log.i("tag","load");
    }

    private void savestepdata(float steps){
        mySharedPreferences = getSharedPreferences("test", 0);
        editor = mySharedPreferences.edit();
        editor.putInt("ForeStep",(int)steps);
        editor.putInt("RealStep",realstep);
        editor.commit();
        Log.i("tag","save");
    }

    private void recordData(){
        mySharedPreferences= getSharedPreferences("test", 0);
        editor = mySharedPreferences.edit();
        startDate = mySharedPreferences.getString("startDate","");
        if(startDate.equals("")){
            editor.putString("startDate", Time.getCurrentDate());
            editor.commit();
        }
        final Handler handler = new Handler();
        Runnable runnable = new Runnable(){
            @Override
            public void run() {
                String thisDate = Time.getCurrentDate();
                startDate = mySharedPreferences.getString("startDate","");
                int grade;
                int stepTarget = mySharedPreferences.getInt("stepTarget",1000);

//                Log.i("record",startDate+" "+thisDate);
//                Log.i("record",String.valueOf(!((thisDate.substring(0,10)).equals(startDate.substring(0,10)))));

                if(!((thisDate.substring(0,10)).equals(startDate.substring(0,10)))){
                    if(realstep>=stepTarget){
                        grade = 1;
                    }
                    else grade = 0;
                    RecordOper recordOper = new RecordOper(getApplicationContext());
                    Record record = new Record(realstep,grade,startDate.substring(0,10));
                    recordOper.insert(record);

                    editor.putInt("RealStep",0);
                    editor.putString("startDate", Time.getCurrentDate());
                    realstep = 0;
                    editor.commit();
                }
                handler.postDelayed(this, 10000);
            }
        };
        handler.post(runnable);// 打开定时器，执行操作
    }
}
