package cn.kongin.sm;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.opengl.Visibility;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.http.HttpClient;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class messageFragment extends Fragment{
    View view;
    TextView today1, today2, towea, totem;
    ImageView weaAnim;
    ListView lv;
    String out,yubao1,yubao2,zhishu,zhishu1;
    LinearLayout reload;
    RelativeLayout wealay;
    Button reloadBtn;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.tab_message,container,false);
        weaAnim = (ImageView)view.findViewById(R.id.weatherAnim);
        today1 = (TextView) view.findViewById(R.id.today1);
        today2 = (TextView) view.findViewById(R.id.today2);
        towea = (TextView) view.findViewById(R.id.towea);
        totem = (TextView) view.findViewById(R.id.totem);
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        reload = (LinearLayout) view.findViewById(R.id.reloadLayout);
        wealay = (RelativeLayout) view.findViewById(R.id.weatherLayout);
        reloadBtn = (Button) view.findViewById(R.id.reload);
        reloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                load();
            }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        load();
    }

    private void load() {
        TextView errmsg = (TextView) view.findViewById(R.id.errmsg);
        if(!Network.NetworkIsConnected(getContext())){
            reload.setVisibility(View.VISIBLE);
            wealay.setVisibility(View.GONE);
            errmsg.setText("检测不到网络，请检查网络状况");
        }
//        else if(!Network.GPSisOn(getContext())){
//            reload.setVisibility(View.VISIBLE);
//            wealay.setVisibility(View.GONE);
//            errmsg.setText("检测到GPS没有打开，请打开后重试");
//        }
        else {
            getCity();
            reload.setVisibility(View.GONE);
            wealay.setVisibility(View.VISIBLE);
        }
    }

    private void setBG(String weather) {
        if(weather.contains("晴"))weaAnim.setBackgroundResource(R.drawable.sunnybg);
        else if(weather.contains("多云")||weather.contains("阴"))weaAnim.setBackgroundResource(R.drawable.cloudbg);
        else if(weather.contains("雷"))weaAnim.setBackgroundResource(R.drawable.thunderbg);
        else if(weather.contains("雨"))weaAnim.setBackgroundResource(R.drawable.rainybg);
    }

    private void animWalk(String weather){
        if(weather.contains("晴"))walkInSun();
        else if(weather.contains("多云")||weather.contains("阴"))walkInCloud();
        else if(weather.contains("雷"))walkInThunder();
        else if(weather.contains("雨"))walkInRain();
    }

    private void walkInThunder() {
        drawableAnim("walkinthunder",14);
    }

    private void walkInRain() {
        drawableAnim("walkinrain",21);
    }

    private void walkInCloud() {
        drawableAnim("walkincloud",25);
    }

    private void walkInSun() {
        drawableAnim("walkinsun",15);
    }

    public void th(String citycode) {
        lv=(ListView)view.findViewById(R.id.listView);
        URL url= null;
        String str=null;
        try {
            //网络链接
            HttpGet httpRequest = new HttpGet("http://op.juhe.cn/onebox/weather/query?cityname=" + citycode + "&key=ebdb38be1d2833e3bb6facc7838c3a7a");
            DefaultHttpClient httpclient = new DefaultHttpClient();
            HttpResponse httpResponse = httpclient.execute(httpRequest);

            str = EntityUtils.toString(httpResponse.getEntity());
            JSONObject jsonObject = new JSONObject(str);
            JSONObject jsonObject1=jsonObject.getJSONObject("result");
            JSONObject jsonObject2=jsonObject1.getJSONObject("data");
            JSONObject jsonObject3=jsonObject2.getJSONObject("realtime");
            String city=jsonObject3.optString("city_name");
            JSONObject jsonObject4=jsonObject3.getJSONObject("weather");
            String temp=jsonObject4.optString("temperature");
            String tq=jsonObject4.optString("info");
            out="城市："+city+"\n今天："+"\n"+"温度："+temp+"\n"+"天气："+tq;
            towea.setText(tq);
            totem.setText(temp + "℃");
            setBG(out);

            JSONArray jsonArray=jsonObject2.getJSONArray("weather");
            JSONObject jsonObject5=jsonArray.getJSONObject(1);
            JSONObject jsonObject6=jsonArray.getJSONObject(1);
            JSONObject jsonObject7=jsonObject6.getJSONObject("info");
            JSONArray jsonArray1=jsonObject7.getJSONArray("day");
            JSONArray jsonArrayDawn=jsonObject7.getJSONArray("dawn");
            yubao1="明天：\n"+"天气："+jsonArray1.optString(1)+"\n"+"温度："+jsonArrayDawn.optString(2)+"~"+jsonArray1.optString(2);

            JSONObject jsonObject10=jsonArray.getJSONObject(2);
            JSONObject jsonObject11=jsonArray.getJSONObject(2);
            JSONObject jsonObject12=jsonObject11.getJSONObject("info");
            JSONArray jsonArray4=jsonObject12.getJSONArray("day");
            JSONArray jsonArrayDawn2=jsonObject12.getJSONArray("dawn");
            yubao2="后天：\n天气："+jsonArray4.optString(1)+"\n温度："+jsonArrayDawn2.optString(2)+"~"+jsonArray4.optString(2);


            JSONObject jsonObject8=jsonObject2.getJSONObject("life");
            JSONObject jsonObject9=jsonObject8.getJSONObject("info");
            JSONArray jsonArray2=jsonObject9.getJSONArray("yundong");
            JSONArray jsonArray3=jsonObject9.getJSONArray("chuanyi");
            zhishu="运动建议："+"\n"+jsonArray2.optString(1);
            zhishu1="穿衣建议："+"\n"+jsonArray3.optString(1);


            ListAdapter ld;
            String a[]={yubao1,yubao2,zhishu,zhishu1};
            int b[]={selectWeaPic(yubao1),selectWeaPic(yubao2),R.drawable.sport,R.drawable.cloth};
            List<Map<String,Object>> viewList =new ArrayList<Map<String,Object>>();
            for(int i=0;i<4;i++)
            {
                Map<String,Object> map=new HashMap<String,Object>();
                map.put("title",a[i]);
                map.put("weather",b[i]);
                viewList.add(map);
            }
            ld = new SimpleAdapter(getActivity(),
                    viewList,
                    R.layout.weather_data,
                    new String[]{"title","weather"},
                    new int[]{R.id.textView,R.id.weapic});
            lv.setAdapter(ld);
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
    }

    private int selectWeaPic(String weather){
        if(weather.contains("晴"))return R.drawable.sun;
        else if(weather.contains("多云"))return R.drawable.cloud;
        else if(weather.contains("阴"))return R.drawable.cloud_sun;
        else if(weather.contains("雷"))return R.drawable.thunder;
        else if(weather.contains("雨"))return R.drawable.rain;
        return R.drawable.sun;
    }

    private void texton(final AnimationDrawable frameAnimation){
        today1.setTextSize(0);
        today2.setTextSize(0);
        towea.setTextSize(0);
        totem.setTextSize(0);
        int duration = 0;
        for(int i=0;i<frameAnimation.getNumberOfFrames();i++){
            duration += frameAnimation.getDuration(i);
        }
        final Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                frameAnimation.stop();
                if(today1.getTextSize()<=30){
                    today1.setTextSize(today1.getTextSize()+3);
                }
                if(today1.getTextSize()>=30&&towea.getTextSize()<=30){
                    towea.setTextSize(towea.getTextSize()+3);
                }
                if(towea.getTextSize()>=30&&today2.getTextSize()<=30){
                    today2.setTextSize(today2.getTextSize()+3);
                }
                if(today2.getTextSize()>=30&&totem.getTextSize()<=30){
                    totem.setTextSize(totem.getTextSize()+3);
                }
                if(!(totem.getTextSize()>=30)) handler.postDelayed(this,100);
            }
        };
        handler.postDelayed(runnable,duration);
    }

    private void drawableAnim(String weather, int length){
        Drawable mDrawable;
        AnimationDrawable frameAnimation = new AnimationDrawable();
        int d = getResources().getIdentifier(weather + length, "drawable", getContext().getPackageName());
        mDrawable = getResources().getDrawable(d);
        frameAnimation.addFrame(mDrawable, 0);
        for (int i = 1; i <= length; i++) {//循环装载所有名字类似的资源如“a1、a2……a15”的图片
            int id = getResources().getIdentifier(weather + i, "drawable", getContext().getPackageName());
            mDrawable = getResources().getDrawable(id);
            frameAnimation.addFrame(mDrawable, 100);
        }
        //是否循环播放  
        frameAnimation.setOneShot(true);
        weaAnim.setBackgroundDrawable(frameAnimation);
        frameAnimation.start();
        texton(frameAnimation);
    }

    private void getCity(){
        LocationClient locationClient= new LocationClient(getContext());

        LocationClientOption locOption = new LocationClientOption();
        locOption.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);// 设置定位模式
        locOption.setCoorType("bd09ll");// 设置定位结果类型
        locOption.setScanSpan(0);// 设置发起定位请求的间隔时间,ms,0即只请求一次
        locOption.setIsNeedAddress(true);// 返回的定位结果包含地址信息
        locOption.setNeedDeviceDirect(true);// 设置返回结果包含手机的方向
        locationClient.setLocOption(locOption);
        locationClient.registerLocationListener(new MyLocationListener());
        locationClient.start();
    }

    class MyLocationListener implements BDLocationListener {
        // 异步返回的定位结果
        @Override
        public void onReceiveLocation(BDLocation location) {
            //Receive Location
            //经纬度
            double lati = location.getLatitude();
            double longa = location.getLongitude();
            //打印出当前位置
            Log.i("TAG", "location.getAddrStr()=" + location.getAddrStr());
            //打印出当前城市
            Log.i("TAG", "location.getCity()=" + location.getCity());
            try{
                th(URLEncoder.encode(location.getCity(),"utf-8"));
            }catch (Exception e){
                e.printStackTrace();
            }
            //返回码
            int i = location.getLocType();
        }
    }
}
