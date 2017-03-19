package cn.kongin.sm;


import android.Manifest;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.trace.LBSTraceClient;
import com.baidu.trace.LocationMode;
import com.baidu.trace.OnStartTraceListener;
import com.baidu.trace.OnStopTraceListener;
import com.baidu.trace.Trace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    //鹰眼轨迹
    LBSTraceClient client;
    Trace trace;

    //用于多个页面切换
    private ViewPager mViewPager;
    private FragmentPagerAdapter mAdapter;
    private List<Fragment> mFragments;

    private GridView gridView;
    private SimpleAdapter adapter;
    public static int fragmentPage = 0;

    SharedPreferences mySharedPreferences;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        initView();
        StartTrace();
        initEvent();
        GPSrequest();
        runNotice();
        mySharedPreferences = getSharedPreferences("test",0);
        if(mySharedPreferences.getBoolean("firstUser",true)){
            FirstUserTips();
        }
    }

    private void FirstUserTips() {
        AlertDialog.Builder builder=new AlertDialog.Builder(this);  //先得到构造器
        builder.setTitle("提示"); //设置标题
        builder.setMessage("你尚未设置个人信息，是否现在就去设置"); //设置内容
        builder.setIcon(R.mipmap.ic_launcher);//设置图标，图片id即可
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() { //设置确定按钮
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, MymessageActivity.class);
                startActivity(intent);
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() { //设置取消按钮
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.create().show();
    }

    private void runNotice() {
        mySharedPreferences = getSharedPreferences("test", 0);
        final Handler handler = new Handler();
        Runnable runable = new Runnable() {
            @Override
            public void run() {
                if(Time.getCurrentDate().substring(11).equals(mySharedPreferences.getString("noticeTime","06:00")+":00")){
                    Notice();
                }
                handler.postDelayed(this,1000);
            }
        };
        handler.post(runable);
    }

    private void Notice() {
        NotificationManager nm=(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String title = "Sport Moment" ;
        String content = "是时候运动啦" ;
        Intent intent = new Intent(MainActivity.this, MainActivity.class);

        PendingIntent pi= PendingIntent.getActivity(MainActivity.this, 0, intent, 0);
        //1.实例化一个通知，指定图标、概要、时间
        Notification n = new Notification.Builder(getApplicationContext())
                .setAutoCancel(true)
                .setContentTitle(title)
                .setContentText(content)
                .setContentIntent(pi)
                .setSmallIcon(R.drawable.app_icon)
                .setDefaults(Notification.DEFAULT_SOUND)
                .setWhen(System.currentTimeMillis())
                .build();

        nm.notify(1, n);
    }

    private void initView() {
        BottomMenuSet(0);

        mViewPager = (ViewPager) findViewById(R.id.views);

        mFragments = new ArrayList<Fragment>();
        Fragment tabStep = new stepFragment();
        Fragment tabMap = new mapFragment();
        Fragment tabProject = new projectFragment();
        Fragment tabMessage = new messageFragment();

        mFragments.add(tabStep);
        mFragments.add(tabMessage);
        mFragments.add(tabProject);
        mFragments.add(tabMap);

        mAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return mFragments.get(position);
            }

            @Override
            public int getCount() {
                return mFragments.size();
            }
        };

        mViewPager.setAdapter(mAdapter);
        mViewPager.setOffscreenPageLimit(4);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                BottomMenuSet(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void initEvent() {
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView item = (TextView)view.findViewById(R.id.BottomMenuText);
                String page = item.getText().toString();
                switch (page){
                    case "状态":
                        mViewPager.setCurrentItem(0);
                        break;
                    case "天气":
                        mViewPager.setCurrentItem(1);
                        break;
                    case "计划":
                        mViewPager.setCurrentItem(2);
                        break;
                    case "我的":
                        mViewPager.setCurrentItem(3);
                        break;
                }
            }
        });
    }

    private void BottomMenuSet(int position){
        fragmentPage = position;
        String[] titles=new String[]{"状态","天气","计划","我的"};
        int[] images;
        switch (position){
            case 0:images=new int[]{R.drawable.state2,R.drawable.weather,R.drawable.plan,R.drawable.human};
                break;
            case 1:images=new int[]{R.drawable.state,R.drawable.weather2,R.drawable.plan,R.drawable.human};
                break;
            case 2:images=new int[]{R.drawable.state,R.drawable.weather,R.drawable.plan2,R.drawable.human};
                break;
            case 3:images=new int[]{R.drawable.state,R.drawable.weather,R.drawable.plan,R.drawable.human2};
                break;
            default:images=new int[]{R.drawable.state,R.drawable.weather,R.drawable.plan,R.drawable.human};
                break;
        }
        gridView=(GridView)findViewById(R.id.BottomMenugridView);
        List<Map<String,Object>> list=new ArrayList<Map<String,Object>>();
        for(int i=0;i<images.length;i++){
            Map<String,Object>map=new HashMap<String,Object>();
            map.put("image",images[i]);
            map.put("title",titles[i]);
            list.add(map);
        }
        adapter=new MySimpleAdapter(this,
                list,
                R.layout.bottommenu,
                new String[]{"title","image"},
                new int[]{R.id.BottomMenuText,R.id.BottomMenuImage});
        gridView.setAdapter(adapter);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(false);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void GPSrequest(){
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
    }

    private void StartTrace(){
        //实例化轨迹服务客户端
        client = new LBSTraceClient(getApplicationContext());
        // 采集周期
        int gatherInterval = 10;
        // 打包周期
        int packInterval = 60;
        // http协议类型
        int protocolType = 1;
        // 设置采集和打包周期
        client.setInterval(gatherInterval, packInterval);
        // 设置定位模式
        client.setLocationMode(LocationMode.High_Accuracy);
        // 设置http协议类型
        client.setProtocolType (protocolType);


        //鹰眼服务ID
        long serviceId  = 128352;
        //entity标识
        String entityName = getIMEI();
        //轨迹服务类型（0 : 不上传位置数据，也不接收报警信息； 1 : 不上传位置数据，但接收报警信息；2 : 上传位置数据，且接收报警信息）
        int  traceType = 2;
        //实例化轨迹服务
        trace = new Trace(getApplicationContext(), serviceId, entityName, traceType);
        //实例化开启轨迹服务回调接口
        OnStartTraceListener startTraceListener = new OnStartTraceListener() {
            //开启轨迹服务回调接口（arg0 : 消息编码，arg1 : 消息内容，详情查看类参考）
            @Override
            public void onTraceCallback(int arg0, String arg1) {
                Log.i("trace",arg1);
            }
            //轨迹服务推送接口（用于接收服务端推送消息，arg0 : 消息类型，arg1 : 消息内容，详情查看类参考）
            @Override
            public void onTracePushCallback(byte arg0, String arg1) {
                Log.i("trace",arg1);
            }
        };

        //开启轨迹服务
        client.startTrace(trace, startTraceListener);
    }

    private void StopTrace(){
        OnStopTraceListener stopTraceListener = new OnStopTraceListener(){
            // 轨迹服务停止成功
            @Override
            public void onStopTraceSuccess() {
                Log.i("traces","kill");
                finish();
            }
            // 轨迹服务停止失败（arg0 : 错误编码，arg1 : 消息内容，详情查看类参考）
            @Override
            public void onStopTraceFailed(int arg0, String arg1) {
                Log.i("trace",arg1);
            }
        };
        //停止轨迹服务
        client.stopTrace(trace,stopTraceListener);
    }

    private String getIMEI(){
        TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        String imei = tm.getDeviceId();
        return imei;
    }
}