package cn.kongin.sm;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.trace.LBSTraceClient;
import com.baidu.trace.LocationMode;
import com.baidu.trace.OnStartTraceListener;
import com.baidu.trace.OnStopTraceListener;
import com.baidu.trace.OnTrackListener;
import com.baidu.trace.Trace;

import org.json.JSONException;
import org.json.JSONObject;

public class RunningActivity extends AppCompatActivity {

    int count;
    private ImageButton running;
    private roundProgressBar bar;
    boolean isTouching;

    int st;
    int et;
    Handler handler;
    Handler thandler;
    Runnable runnable;
    Runnable trun;

    TextView length;
    TextView speed;
    TextView time;
    String lengthStr;
    String speedStr;
    String timeStr;

    //鹰眼轨迹
    LBSTraceClient client;
    // 轨迹服务ID
    long serviceId;
    // entity标识
    String entityName;
    // 是否返回精简结果
    int simpleReturn;
    // 是否纠偏
    int isProcessed;
    // 纠偏选项
    String processOption;
    // 分页大小
    int pageSize;
    // 分页索引
    int pageIndex;
    //开始时间
    int startTime;
    //结束时间
    int endTime;

    OnTrackListener trackListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_running);
        length = (TextView) findViewById(R.id.length);
        speed = (TextView) findViewById(R.id.speed);
        time = (TextView) findViewById(R.id.time);
        lengthStr = "";
        speedStr = "";
        timeStr = "";
        setTrackListener();
        firstQuery();
        refreshText();
        setRunningButton();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try{
            lastQuery();
        }catch (Exception e){
            e.printStackTrace();
        }
        if(!lengthStr.equals("")){
            if(Double.parseDouble(lengthStr)>0){
                saveLocus();
            }
            else {
                Toast.makeText(getApplicationContext(),"未检测到跑步，记录失败",Toast.LENGTH_SHORT).show();
            }
        }
        else {
            Toast.makeText(getApplicationContext(),"未检测到跑步，记录失败",Toast.LENGTH_SHORT).show();
        }
        client.onDestroy();
    }

    /**
     * 第一次请求
     */
    private void firstQuery() {
        st = (int)Time.getCurrentTime();
        handler = new Handler();
        runnable = new Runnable(){
            @Override
            public void run() {
                et = (int)Time.getCurrentTime();
                queryHistoryMap(st,et);
                handler.postDelayed(this, 500);
            }
        };
        handler.post(runnable);// 打开定时器，执行操作
    }

    private void refreshText(){
        thandler = new Handler();
        trun = new Runnable() {
            @Override
            public void run() {
                length.setText(lengthStr + "m");
                speed.setText(speedStr + "m/s");
                time.setText(timeStr + "s");
                thandler.postDelayed(this,500);
            }
        };
        thandler.post(trun);
    }

    /**
     * 最后一次请求
     */
    private void lastQuery() {
        handler.removeCallbacks(runnable);
        thandler.removeCallbacks(trun);
    }

    private void saveLocus(){
        LocusOper oper = new LocusOper(getApplicationContext());
        int grade;
        SharedPreferences mySharedPreferences= getSharedPreferences("test", 0);
        int runTarget = mySharedPreferences.getInt("runTarget",1000);
        if(Double.parseDouble(lengthStr.substring(0,lengthStr.length()-1))>=runTarget*1.5){
            grade = 3;
        }
        else if(Double.parseDouble(lengthStr.substring(0,lengthStr.length()-1))>=runTarget*1.2){
            grade = 2;
        }
        else if(Double.parseDouble(lengthStr.substring(0,lengthStr.length()-1))>=runTarget){
            grade = 1;
        }
        else grade = 0;
//        Log.i("run",Time.TimeStamp2Date(st).substring(0,10));
        Locus locus = new Locus(Time.TimeStamp2Date(st),
                Time.TimeStamp2Date(et),
                lengthStr,
                speedStr,
                Time.TimeStamp2Date(st).substring(0,10),
                grade);
        oper.insert(locus);
    }

    private void setTrackListener() {
        trackListener = new OnTrackListener() {
            @Override
            public void onRequestFailedCallback(String s) {
                Log.i("mess","request fail");
            }

            @Override
            public void onQueryHistoryTrackCallback(String s) {
                super.onQueryHistoryTrackCallback(s);
                try {
                    JSONObject dataJson = new JSONObject(s);
                    int total = dataJson.getInt("total");
                    Log.i("result",String.valueOf(total));
                    if (total > (pageSize * pageIndex)) {
                        queryHistoryTrack(++pageIndex);
                    }
                    else getLocusData(dataJson);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
    }

    private void getLocusData(JSONObject dataJson) {
        try {
            Double distance = dataJson.getDouble("distance");
            lengthStr = MathFunction.cutfloat(distance,2);
            int t = et - st;
            timeStr = String.valueOf(t);
            speedStr = MathFunction.cutfloat(distance/t,2);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    private void setRunningButton() {
        isTouching = true;
        count = 1;
        running = (ImageButton) findViewById(R.id.running);
        bar = (roundProgressBar) findViewById(R.id.stopbar);

        running.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(bar.getProgress()>=bar.getTotalProgress()){
                    finish();
                }
                else {
                    isTouching = true;
                    bar.setProgress(bar.getProgress()+3);
                }
                return false;
            }
        });

        final Handler handler = new Handler();
        Runnable runnable = new Runnable(){
            @Override
            public void run() {
                switch (count){
                    case 1:running.setBackgroundResource(R.drawable.run2);
                        count++;
                        break;
                    case 2:running.setBackgroundResource(R.drawable.run3);
                        count++;
                        break;
                    case 3:running.setBackgroundResource(R.drawable.run4);
                        count++;
                        break;
                    case 4:running.setBackgroundResource(R.drawable.run5);
                        count++;
                        break;
                    case 5:running.setBackgroundResource(R.drawable.run6);
                        count++;
                        break;
                    case 6:running.setBackgroundResource(R.drawable.run7);
                        count++;
                        break;
                    case 7:running.setBackgroundResource(R.drawable.run8);
                        count++;
                        break;
                    case 8:running.setBackgroundResource(R.drawable.run1);
                        count=1;
                        break;
                }
                if(bar.getProgress()>0&&!isTouching&& bar.getProgress()<bar.getTotalProgress()){
                    bar.setProgress(bar.getProgress()-1);
                }
                bar.postInvalidate();
                handler.postDelayed(this, 70);
            }
        };
        handler.postDelayed(runnable, 0);// 打开定时器，执行操作

        final Handler handler2 = new Handler();
        Runnable runnable2 = new Runnable(){
            @Override
            public void run() {
                isTouching = false;
                handler.postDelayed(this, 1000);
            }
        };
        handler2.postDelayed(runnable2, 0);// 打开定时器，执行操作
    }

    /**
     * 查询轨迹
     * @param st 开始时间
     * @param et 结束时间
     */
    public void queryHistoryMap(int st,int et) {
        //实例化轨迹服务客户端
        client = new LBSTraceClient(getApplicationContext());
        // 轨迹服务ID
        serviceId = 128352;
        // entity标识
        entityName = getIMEI();
        // 是否返回精简结果
        simpleReturn = 0;
        // 是否纠偏
        isProcessed = 1;
        // 纠偏选项
        processOption = "";
        // 分页大小
        pageSize = 5000;
        // 分页索引
        pageIndex = 1;
        //开始时间
        startTime = st;
        //结束时间
        endTime = et;

        queryHistoryTrack(pageIndex);
    }

    //调用queryHistoryTrack()查询历史轨迹
    private void queryHistoryTrack(int pageIndex) {
        client.queryHistoryTrack(serviceId, entityName, simpleReturn, isProcessed,
                processOption, startTime, endTime, pageSize, pageIndex, trackListener);
    }

    private String getIMEI(){
        TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        String imei = tm.getDeviceId();
        return imei;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(false);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
