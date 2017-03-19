package cn.kongin.sm;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.annotation.StringDef;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import de.hdodenhof.circleimageview.CircleImageView;

public class stepFragment extends Fragment{

    private View view;
    SharedPreferences mySharedPreferences;
    int stepTarget;
    private roundProgressBar stepview;
    boolean firstdraw = true;//第一次画进度条

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view =  inflater.inflate(R.layout.tab_step,container,false);

        stepview = (roundProgressBar) view.findViewById(R.id.bar);
        mySharedPreferences = getActivity().getSharedPreferences("test", 0);

        CircleImageView runBtn = (CircleImageView) view.findViewById(R.id.run);
        runBtn.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(getActivity(), RunActivity.class);
                startActivity(intent);
            }

        });

        init();

        final Handler handler = new Handler();
        Runnable runnable = new Runnable(){
            @Override
            public void run() {
                if((!roundProgressBar.drawing)&&(stepview.getProgress()>StepService.realstep)){
                    stepview.setProgress(StepService.realstep);
                }
                if((!roundProgressBar.drawing)&&(stepview.getProgress()<StepService.realstep)){
                    if(firstdraw&&StepService.realstep>100){
                        stepview.setProgress(stepview.getProgress()+StepService.realstep/100);
                        if (stepview.getProgress()>=StepService.realstep){
                            stepview.setProgress(StepService.realstep);
                            firstdraw = false;
                        }
                    }
                    else stepview.setProgress(stepview.getProgress()+1);
                    stepview.postInvalidate();
                }
                stepTarget = mySharedPreferences.getInt("stepTarget",1000);
                if(stepview.getTotalProgress() != stepTarget){
                    stepview.setTotalProgress(stepTarget);
                    stepview.postInvalidate();
                }
                TextView len = (TextView)view.findViewById(R.id.steplength);
                TextView cal = (TextView)view.findViewById(R.id.stepcalorie);
                len.setText("今日里程："+MathFunction.cutfloat(MathFunction.getStepLength(getActivity()),1)+"米");
                cal.setText("消耗卡路里："+MathFunction.cutfloat(MathFunction.getStepCalorie(getActivity()),1)+"千卡");
                handler.postDelayed(this, 1);
            }
        };
        handler.postDelayed(runnable, 1000);// 打开定时器，执行操作

        return view;
    }

    private void init() {
        Intent intent = new Intent(getActivity(), StepService.class);
        getActivity().startService(intent);
    }
}
