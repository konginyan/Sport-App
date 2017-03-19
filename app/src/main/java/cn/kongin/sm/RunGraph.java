package cn.kongin.sm;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

public class RunGraph extends Fragment {
    View view;
    LineGraphicView graph;
    ArrayList<Double> yList;
    ArrayList<String> xRawDatas;
    boolean firstload = true;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.run_graph, container, false);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        initGraph();
        if(!firstload)graph.direDraw();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(isVisibleToUser){
            if(!LineGraphicView.runDrawing) loadGraph();
        }
        else {
            if(!LineGraphicView.runDrawing) clearGraph();
        }
    }

    private void initGraph() {
        graph = (LineGraphicView) view.findViewById(R.id.line_graphic2);

        LocusOper locusOper = new LocusOper(getContext());

        yList = new ArrayList<Double>();
        xRawDatas = new ArrayList<String>();

        int max = 600;
        int gra = 0;
        int clearDay = 0;
        double totallength = 0;
        double totalspeed = 0;

        for(int day=6;day>=0;day--){
            Double ysp = 0.0;
            Double ydata = 0.0;
            try{
                xRawDatas.add(Time.getCurrentDate(day).substring(5,10));
                ArrayList<HashMap<String, String>> selectlist;
                selectlist = locusOper.getList(Time.getCurrentDate(day).substring(0,10));
                if(selectlist.size() == 0){
                    yList.add(0.0);
                }
                else {
                    gra = 0;
                    for(int j=0;j<selectlist.size();j++){
                        ysp += Double.parseDouble(selectlist.get(j).get("speed"));
                        ydata += Double.parseDouble(selectlist.get(j).get("length"));
                        int grade = Integer.parseInt(selectlist.get(j).get("grade"));
                        if (grade>=1)gra = 1;
                    }
                    yList.add(ydata);
                }
                if(ydata>max){
                    max = (int)ydata.doubleValue();
                }
                totallength += ydata.doubleValue();
                if(selectlist.size()>0)totalspeed += ysp.doubleValue()/selectlist.size();
                if(gra == 1)clearDay++;
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
        graph.setData(yList, xRawDatas, max, max/6);
        TextView t1 = (TextView)view.findViewById(R.id.textView2);
        TextView t2 = (TextView)view.findViewById(R.id.textView7);
        TextView t3 = (TextView)view.findViewById(R.id.textView4);
        TextView t4 = (TextView)view.findViewById(R.id.textView8);
        TextView t5 = (TextView)view.findViewById(R.id.textView10);
        t1.setText(String.valueOf(clearDay)+"天");
        t2.setText(MathFunction.cutfloat(totallength/7,1)+"米");
        if(MathFunction.getRunCalorie(getActivity(),totallength/7)<0){
            t3.setText("需要设置体重");
        }
        else t3.setText(MathFunction.cutfloat(MathFunction.getRunCalorie(getActivity(),totallength/7),1)+"千卡");
        t4.setText(MathFunction.cutfloat(totalspeed/7,1)+"米/秒");
        if(totalspeed==0)t5.setText("0分钟");
        else t5.setText(MathFunction.cutfloat(totallength/totalspeed/60,1)+"分钟");

    }

    public void loadGraph(){
        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(200);
                    graph.slowDraw("run");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        t.start();
    }

    public void clearGraph(){
        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(200);
                    graph.clearLine();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        t.start();
    }
}
