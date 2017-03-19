package cn.kongin.sm;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.trace.LBSTraceClient;
import com.baidu.trace.OnTrackListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HistoryMapActivity extends AppCompatActivity {
    ListView data;

    private MapView mapView = null;
    private BaiduMap baiduMap;

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

    //显示数据
    String stText;
    String etText;
    String lenText;
    String spText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("map","oncre");
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_history_map);
        initView();

        //在OnTrackListener的onQueryHistoryTrackCallback()回调接口中，判断是否已查询完毕。
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
                    else drawMap(dataJson);
                } catch (JSONException e) {
                }
            }
        };
        initSearch();
        if(!Network.NetworkIsConnected(getApplicationContext())){
            Toast.makeText(getApplicationContext(),"检测不到网络连接，无法获取地图",Toast.LENGTH_SHORT).show();
        }
    }

    private void initView() {
        mapView = (MapView) findViewById(R.id.bmap);
        baiduMap = mapView.getMap();
        baiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        setlist();
    }

    /**
     * 轨迹信息列表
     */
    private void setlist() {
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        List<Map<String,Object>> viewList =new ArrayList<Map<String,Object>>();
        String[] listTitle = new String[]{"开始时间：", "结束时间：","里程：","平均速度："};
        String[] listContent = new String[]{bundle.getString("startTime"),
                bundle.getString("endTime"),
                bundle.getString("length"),
                bundle.getString("speed")
        };

        for(int i=0;i<listTitle.length;i++){
            Map<String,Object>map=new HashMap<String,Object>();
            map.put("title",listTitle[i]);
            map.put("content",listContent[i]);
            viewList.add(map);
        }

        data = (ListView)findViewById(R.id.locus_data);
        ListAdapter adapter= new SimpleAdapter(HistoryMapActivity.this,
                viewList,
                R.layout.data_line,
                new String[]{"title","content"},
                new int[]{R.id.title,R.id.content}
        );
        data.setAdapter(adapter);
    }

    /**
     * 页面刚打开通过历史列表点进来的查询轨迹
     */
    private void initSearch() {
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        int st = (int)Time.Date2TimeStamp(bundle.getString("startTime"));
        int et = (int)Time.Date2TimeStamp(bundle.getString("endTime"));

        queryHistoryMap(st,et);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        client.onDestroy();
        mapView.onDestroy();
    }

    /**
     * 查询轨迹
     * @param st 开始时间
     * @param et 结束时间
     */
    public void queryHistoryMap(int st,int et) {
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

    private void drawMap(JSONObject js){
        try{
            List<LatLng> points = new ArrayList<LatLng>();
            JSONArray pointArray = js.getJSONArray("points");
            for(int i=0;i<js.getInt("total");i++){
                JSONObject pointObject = pointArray.getJSONObject(i);
                JSONArray lat = pointObject.getJSONArray("location");
                points.add(new LatLng(lat.getDouble(1),lat.getDouble(0)));
            }
            if(pointArray.length()>2) {
                OverlayOptions ooPolyline = new PolylineOptions().width(10).color(Color.GREEN).points(points);
                //画轨迹
                baiduMap.clear();
                baiduMap.addOverlay(ooPolyline);
            }

            //获得起点和终点
            JSONObject sPl = js.getJSONObject("start_point");
            JSONObject ePl = js.getJSONObject("end_point");
            LatLng startPoint = new LatLng(sPl.getDouble("latitude"),sPl.getDouble("longitude"));
            LatLng endPoint = new LatLng(ePl.getDouble("latitude"),ePl.getDouble("longitude"));

            //构建Marker图标
            BitmapDescriptor startIcon = BitmapDescriptorFactory.fromResource(R.drawable.start_point);
            BitmapDescriptor endIcon = BitmapDescriptorFactory.fromResource(R.drawable.end_point);

            //在地图上添加Marker，并显示
            OverlayOptions startOption = new MarkerOptions().position(startPoint).icon(startIcon);
            baiduMap.addOverlay(startOption);
            OverlayOptions endOption = new MarkerOptions().position(endPoint).icon(endIcon);
            baiduMap.addOverlay(endOption);

            //视角移动到起点位置
            baiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(startPoint));

            //放大地图
            baiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(new MapStatus.Builder().zoom(17).build()));
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    private String getIMEI(){
        TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        String imei = tm.getDeviceId();
        return imei;
    }
}
