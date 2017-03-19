package cn.kongin.sm;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.SlidingDrawer;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.trace.LBSTraceClient;
import com.baidu.trace.LocationMode;
import com.baidu.trace.OnStartTraceListener;
import com.baidu.trace.OnStopTraceListener;
import com.baidu.trace.Trace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class RunActivity extends AppCompatActivity {
    ImageButton sBtn;
    ListView ltn;
    Button control;
    SlidingDrawer slidingDrawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_run);

        setActionBar();

        sBtn = (ImageButton)findViewById(R.id.trigger);
        sBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!Network.GPSisOn(getApplicationContext())){
                    toGPSDialog();
                }
                else if(Network.NetworkIsConnected(getApplicationContext())){
                    Intent intent = new Intent();
                    intent.setClass(RunActivity.this, RunningActivity.class);
                    startActivity(intent);
                }
                else {
                    Toast.makeText(getApplicationContext(),"检测不到网络连接，无法使用跑步功能",Toast.LENGTH_SHORT).show();
                }
            }
        });

        ltn = (ListView)findViewById(R.id.historylist);
        ltn.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView start = (TextView)view.findViewById(R.id.data_start);
                TextView end = (TextView)view.findViewById(R.id.data_end);
                TextView length = (TextView)view.findViewById(R.id.data_length);
                TextView speed = (TextView)view.findViewById(R.id.data_speed);
                Intent intent = new Intent();
                intent.setClass(RunActivity.this, HistoryMapActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("startTime",start.getText().toString());
                bundle.putString("endTime",end.getText().toString());
                bundle.putString("length",length.getText().toString() + "m");
                bundle.putString("speed",speed.getText().toString() + "m/s");
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

        slidingDrawer = (SlidingDrawer)findViewById(R.id.drawer);
        control = (Button) findViewById(R.id.control);
        slidingDrawer.setOnDrawerOpenListener(new SlidingDrawer.OnDrawerOpenListener() {
            @Override
            public void onDrawerOpened() {
                control.setText("点击收起列表");
            }
        });
        slidingDrawer.setOnDrawerCloseListener(new SlidingDrawer.OnDrawerCloseListener() {
            @Override
            public void onDrawerClosed() {
                control.setText("上拉查看历史");
            }
        });
    }

    private void toGPSDialog() {
        AlertDialog.Builder builder=new AlertDialog.Builder(this);  //先得到构造器
        builder.setTitle("提示"); //设置标题
        builder.setMessage("GPS尚未打开，是否去设置?"); //设置内容
        builder.setIcon(R.mipmap.app_icon);//设置图标，图片id即可
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() { //设置确定按钮
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Network.setGPS(getApplicationContext());
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

    @Override
    protected void onResume() {
        super.onResume();
        try{
            Handler handler = new Handler();
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    setlist();
                }
            };
            handler.postDelayed(runnable,500);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    private void setActionBar(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("跑步");
        toolbar.setTitleTextAppearance(this,R.style.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void setlist() {
        LocusOper oper = new LocusOper(getApplicationContext());
        ArrayList<HashMap<String,String>> viewList = oper.getList();
        ArrayList<HashMap<String,Object>> theList = new ArrayList<HashMap<String,Object>>();
        for(int i=0;i<viewList.size();i++){
            HashMap<String,String> tempMap = viewList.get(i);
            HashMap<String,Object> theMap = new HashMap<String,Object>();
            theMap.put("length",tempMap.get("length"));
            theMap.put("speed",tempMap.get("speed"));
            theMap.put("start",tempMap.get("start"));
            theMap.put("end",tempMap.get("end"));
            int tempGrade = Integer.parseInt(tempMap.get("grade"));
            switch (tempGrade){
                case 0:theMap.put("picture",R.drawable.medalblack);
                    break;
                case 1:theMap.put("picture",R.drawable.medalcupro);
                    break;
                case 2:theMap.put("picture",R.drawable.medalsilver);
                    break;
                case 3:theMap.put("picture",R.drawable.medalgold);
                    break;
            }
            theList.add(theMap);
        }
        ListAdapter adapter= new SimpleAdapter(RunActivity.this,
                theList,
                R.layout.historylist_entry,
                new String[]{"length","speed","start","end","picture"},
                new int[]{R.id.data_length,R.id.data_speed,R.id.data_start,R.id.data_end,R.id.pic}
        );
        ltn.setAdapter(adapter);

        double maxspeed = 0;
        double maxlength = 0;
        double temp;
        for(int i=0;i<viewList.size();i++){
            temp = Double.parseDouble(viewList.get(i).get("speed"));
            if(temp>maxspeed)maxspeed = temp;
            temp = Double.parseDouble(viewList.get(i).get("length"));
            if(temp>maxlength)maxlength = temp;
        }
        TextView ms = (TextView)findViewById(R.id.maxspeed);
        ms.setText(MathFunction.cutfloat(maxspeed,1)+"m/s");
        TextView ml = (TextView)findViewById(R.id.maxlength);
        ml.setText(MathFunction.cutfloat(maxlength/1000,2)+"km");
    }
}
