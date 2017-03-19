package cn.kongin.sm;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class StepGraph extends Fragment {
    View view;
    LineGraphicView graph;
    ArrayList<Double> yList;
    ArrayList<String> xRawDatas;
    boolean firstload = true;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.step_graph, container, false);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        initGraph();
        if(!firstload)graph.direDraw();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(isVisibleToUser){
            if(!LineGraphicView.stepDrawing) loadGraph();
        }
        else {
            if(!LineGraphicView.stepDrawing) clearGraph();
        }
    }

    private void initGraph() {
        graph = (LineGraphicView) view.findViewById(R.id.line_graphic);

        RecordOper recordOper = new RecordOper(getContext());

        yList = new ArrayList<Double>();
        xRawDatas = new ArrayList<String>();

        int max = 600;
        int gra = 0;
        int clearDay = 0;
        double calorie = 0;
        double totalstep = 0;

        for(int day=7;day>=1;day--){
            Double ydata = 0.0;
            try{
                xRawDatas.add(Time.getCurrentDate(day).substring(5,10));
                ArrayList<HashMap<String, String>> selectlist;
                selectlist = recordOper.getList(Time.getCurrentDate(day).substring(0,10));
                if(selectlist.size() == 0){
                    yList.add(0.0);
                }
                else {
                    gra = 0;
                    for(int j=0;j<selectlist.size();j++){
                        ydata += Double.parseDouble(selectlist.get(j).get("step"));
                        int grade = Integer.parseInt(selectlist.get(j).get("grade"));
                        if (grade==1)gra = 1;
                    }
                    yList.add(ydata);
                }
                totalstep += (int)ydata.doubleValue();
                if(gra == 1)clearDay++;
                if(ydata>max){
                    max = (int)ydata.doubleValue();
                }
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
        graph.setData(yList, xRawDatas, max, max/6);
        TextView t1 = (TextView)view.findViewById(R.id.textView2);
        TextView t2 = (TextView)view.findViewById(R.id.textView7);
        TextView t3 = (TextView)view.findViewById(R.id.textView4);
        t1.setText(String.valueOf(clearDay)+"天");
        t2.setText(MathFunction.cutfloat(totalstep/7,1)+"步");
        t3.setText(MathFunction.cutfloat(calorie,1)+"千卡");
    }

    public void loadGraph(){
        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(500);
                    graph.slowDraw("step");
                } catch (Exception e) {
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
