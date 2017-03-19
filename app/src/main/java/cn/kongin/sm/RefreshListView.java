package cn.kongin.sm;

import android.content.Context;
import android.icu.text.SimpleDateFormat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class RefreshListView extends ListView implements AbsListView.OnScrollListener{

    View header;
    int headerHeight;
    int firstVisibleItem;
    int scrollstate;//listview当前滚动状态
    boolean isRemark;//是否在最顶端
    int startY;//摁下的第一个位置Y

    int state;
    final int NONE = 0;//正常状态
    final int PULL = 1;//下拉状态
    final int RELEASE = 2;//释放状态
    final int REFRESHING = 3;//刷新状态

    IRefreshListener iRefreshListener;//刷新数据借口

    public RefreshListView(Context context) {
        super(context);
        initView(context);
    }

    public RefreshListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public RefreshListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView(context);
    }

    private void initView(Context context){
        LayoutInflater inflater = LayoutInflater.from(context);
        header = inflater.inflate(R.layout.refresh_header,null);
        header.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        headerHeight = header.getMeasuredHeight();
        Log.i("he","height=" + headerHeight);
        //topPadding(-headerHeight);
        this.addHeaderView(header);
        this.setOnScrollListener(this);
    }

    private void topPadding(int topPadding){
        header.setPadding(header.getPaddingLeft(),topPadding,header.getPaddingRight(),header.getPaddingBottom());
        header.invalidate();
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        this.scrollstate = scrollState;
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        this.firstVisibleItem = firstVisibleItem;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()){
            case MotionEvent.ACTION_DOWN:
                if(firstVisibleItem == 0){
                    isRemark = true;
                    startY = (int)ev.getY();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                onMove(ev);
                break;
            case MotionEvent.ACTION_UP:
                if(state == RELEASE){
                    state = REFRESHING;
                    refreshViewByState();
                    iRefreshListener.onRefresh();
                }
                else if(state == PULL){
                    state = NONE;
                    refreshViewByState();
                    isRemark = false;
                }
                break;
        }
        return super.onTouchEvent(ev);
    }

    private void onMove(MotionEvent ev){
        if(!isRemark)return;

        int tempY = (int)ev.getY();
        int space = tempY - startY;
        int topPadding = space - headerHeight;

        switch (state){
            case NONE:
                if(space>0){
                    state = PULL;
                    refreshViewByState();
                }
                break;
            case PULL:
                topPadding(topPadding);
                if(space>headerHeight+30){
                    state = RELEASE;
                    refreshViewByState();
                }
                break;
            case RELEASE:
                topPadding(topPadding);
                if(space<headerHeight+30){
                    state = PULL;
                    refreshViewByState();
                }
                else if(space<=0){
                    state = NONE;
                    refreshViewByState();
                    isRemark = false;
                }
                break;
            case REFRESHING:
                break;
        }
    }

    /**
     * 状态转换时刷新header
     */
    private void refreshViewByState() {
        TextView tip = (TextView) header.findViewById(R.id.tip);
        ImageView arrow = (ImageView)header.findViewById(R.id.pull_to_refresh_arrow);
        ProgressBar progress = (ProgressBar)header.findViewById(R.id.progress);

        RotateAnimation anim = new RotateAnimation(0,180,
                RotateAnimation.RELATIVE_TO_SELF,0.5f,
                RotateAnimation.RELATIVE_TO_SELF,0.5f);
        RotateAnimation anim1 = new RotateAnimation(180,0,
                RotateAnimation.RELATIVE_TO_SELF,0.5f,
                RotateAnimation.RELATIVE_TO_SELF,0.5f);

        anim.setDuration(500);
        anim.setFillAfter(true);
        anim.setDuration(500);
        anim.setFillAfter(true);

        switch (state) {
            case NONE:
                topPadding(-headerHeight);
                arrow.clearAnimation();
                break;
            case PULL:
                arrow.setVisibility(View.VISIBLE);
                progress.setVisibility(View.GONE);
                tip.setText("下拉可以刷新");
                arrow.clearAnimation();
                arrow.setAnimation(anim1);
                break;
            case RELEASE:
                arrow.setVisibility(View.VISIBLE);
                progress.setVisibility(View.GONE);
                tip.setText("松开可以刷新");
                arrow.clearAnimation();
                arrow.setAnimation(anim);
                break;
            case REFRESHING:
                topPadding(50);
                arrow.setVisibility(View.GONE);
                progress.setVisibility(View.VISIBLE);
                tip.setText("正在刷新");
                arrow.clearAnimation();
                break;
        }
    }

    /**
     * 刷新完毕后
     */
    public void refreshComplete(){
        state = NONE;
        isRemark = false;
        refreshViewByState();
        TextView lastupdate_time = (TextView)header.findViewById(R.id.lastupdate_time);
        lastupdate_time.setText("2016/33/33");
    }

    /**
     * 刷新数据借口
     */
    public interface IRefreshListener{
        public void onRefresh();
    }

    public void setInterface(IRefreshListener iRefreshListener){
        this.iRefreshListener = iRefreshListener;
    }
}