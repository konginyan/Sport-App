package cn.kongin.sm;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class mapFragment extends Fragment{
    View view;
    Spinner stepTarget;
    Spinner runTarget;
    Spinner weightTarget;
    Spinner timeTarget1;
    Spinner timeTarget2;
    CircleImageView personIcon;

    SharedPreferences mySharedPreferences;
    SharedPreferences.Editor editor;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.tab_map,container,false);
        stepTarget = (Spinner)view.findViewById(R.id.StepTargetNum);
        runTarget = (Spinner)view.findViewById(R.id.RunTagetNum);
        weightTarget = (Spinner)view.findViewById(R.id.WeightTargetNum);
        timeTarget1 = (Spinner)view.findViewById(R.id.TimeSetStart);
        timeTarget2 = (Spinner)view.findViewById(R.id.TimeSetEnd);
        inithead();
        getSelection();
        setTarget();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        mySharedPreferences = getActivity().getSharedPreferences("test", 0);
        boolean headerExist = mySharedPreferences.getBoolean("HeaderExist",false);
        if(headerExist) initHeadPic();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(isVisibleToUser){
            try{
                updateData();
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private void updateData() {
        int totalstep = 0;
        int passdays = 0;
        RecordOper recordOper = new RecordOper(getContext());
        ArrayList<HashMap<String, String>> selectlist = recordOper.getList();
        for(int i=0;i<selectlist.size();i++){
            totalstep += Integer.parseInt(selectlist.get(i).get("step"));
            if(Integer.parseInt(selectlist.get(i).get("grade"))==1){
                passdays++;
            }
        }
        TextView avestep = (TextView)view.findViewById(R.id.AverageStepNum);
        if(selectlist.size()!=0) avestep.setText(totalstep/selectlist.size() + "步");
        else avestep.setText("0步");
        TextView disnum = (TextView)view.findViewById(R.id.DistanceNum);
        disnum.setText(MathFunction.cutfloat(totalstep*6.5/1000,1) + "千米");
        TextView numday = (TextView)view.findViewById(R.id.NumDayNum);
        numday.setText(passdays + "天");
    }

    private void inithead() {
        personIcon = (CircleImageView) view.findViewById(R.id.Person);
        personIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(getActivity(), MymessageActivity.class);
                startActivity(intent);
            }
        });
    }

    private void initHeadPic(){
        String fileName = "head.jpg";
        FileInputStream fin = null;
        try {
            fin = getActivity().openFileInput(fileName);
            Bitmap bt = BitmapFactory.decodeStream(fin);// 从SD卡中找头像，转换成Bitmap
            if (bt != null) {
                @SuppressWarnings("deprecation")
                Drawable drawable = new BitmapDrawable(bt);// 转换成drawable
                personIcon.setImageDrawable(drawable);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                // 关闭流
                fin.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void getSelection() {
        mySharedPreferences = getActivity().getSharedPreferences("test", 0);
        stepTarget.setSelection(mySharedPreferences.getInt("SpinnerStep",0));
        runTarget.setSelection(mySharedPreferences.getInt("SpinnerRun",0));
        weightTarget.setSelection(mySharedPreferences.getInt("SpinnerWeight",0));
        timeTarget1.setSelection(mySharedPreferences.getInt("SpinnerTime1",0));
        timeTarget2.setSelection(mySharedPreferences.getInt("SpinnerTime2",0));
    }

    private void setTarget() {
        stepTarget.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mySharedPreferences = getActivity().getSharedPreferences("test", 0);
                editor = mySharedPreferences.edit();
                Resources res =getResources();
                String[] stepArray = res.getStringArray(R.array.intype);
                editor.putInt("stepTarget",Integer.parseInt(stepArray[position]));
                editor.putInt("SpinnerStep",position);
                editor.commit();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        runTarget.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mySharedPreferences = getActivity().getSharedPreferences("test", 0);
                editor = mySharedPreferences.edit();
                Resources res =getResources();
                String[] runArray = res.getStringArray(R.array.intype3);
                Float f = Float.parseFloat(runArray[position]);
                int runT = (int)(f.floatValue()*1000);
                editor.putInt("runTarget",runT);
                editor.putInt("SpinnerRun",position);
                editor.commit();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        weightTarget.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mySharedPreferences = getActivity().getSharedPreferences("test", 0);
                editor = mySharedPreferences.edit();
                editor.putInt("SpinnerWeight",position);
                editor.commit();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        timeTarget1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mySharedPreferences = getActivity().getSharedPreferences("test", 0);
                editor = mySharedPreferences.edit();
                Resources res =getResources();
                String[] timeArray = res.getStringArray(R.array.intype4);
                editor.putInt("SpinnerTime1",position);
                editor.putString("noticeTime",timeArray[position]);
                editor.commit();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        timeTarget2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mySharedPreferences = getActivity().getSharedPreferences("test", 0);
                editor = mySharedPreferences.edit();
                editor.putInt("SpinnerTime2",position);
                editor.commit();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
}
