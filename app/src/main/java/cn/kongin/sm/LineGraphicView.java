package cn.kongin.sm;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import java.util.ArrayList;

public class LineGraphicView extends View{
    /**
     * 公共部分
     */
    private static final int CIRCLE_SIZE = 10;

    private static enum Linestyle
    {
        SlowLine, Line, Curve, none//缓慢直线，直线，曲线，不画
    }

    private Context mContext;
    private Paint mPaint;
    private Resources res;
    private DisplayMetrics dm;

    /**
     * data
     */
    private Linestyle mStyle = Linestyle.none;

    private int canvasHeight;
    private int canvasWidth;
    private int bheight = 0;
    private int blwidh;
    private boolean isMeasure = true;
    /**
     * Y轴最大值
     */
    private int maxValue;
    /**
     * Y轴间距值
     */
    private int averageValue;
    private int marginTop = 20;
    private int marginBottom = 40;

    /**
     * 曲线上总点数
     */
    private Point[] mPoints;
    /**
     * 纵坐标值
     */
    private ArrayList<Double> yRawData;
    /**
     * 横坐标值
     */
    private ArrayList<String> xRawDatas;
    private ArrayList<Integer> xList;// 记录每个x的值
    private int spacingHeight;

    /**
     * 缓慢画线计数
     */
    private int pCount;
    private int part;
    private int partCount;

    public LineGraphicView(Context context)
    {
        this(context, null);
    }

    public LineGraphicView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        this.mContext = context;
        initView();
    }

    private void initView()
    {
        xList = new ArrayList<Integer>();
        xRawDatas = new ArrayList<String>();
        yRawData = new ArrayList<Double>();

        this.res = mContext.getResources();
        this.mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dm = new DisplayMetrics();
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(dm);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh)
    {
        if (isMeasure)
        {
            this.canvasHeight = getHeight();
            this.canvasWidth = getWidth();
            if (bheight == 0)
                bheight = (int) (canvasHeight - marginBottom)-dip2px(15);
            blwidh = dip2px(40);
            isMeasure = false;
        }
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        mPaint.setColor(res.getColor(R.color.color_f2f2f2));

        drawAllXLine(canvas);
        // 画直线（纵向）
        drawAllYLine(canvas);
        // 点的操作设置
        mPoints = getPoints();

        mPaint.setColor(res.getColor(R.color.color_ff4631));
        mPaint.setStrokeWidth(dip2px(1.0f));
        mPaint.setStyle(Paint.Style.STROKE);
        if (mStyle == Linestyle.Curve) {
            drawScrollLine(canvas);
        }
        else if(mStyle == Linestyle.Line) {
            drawLine(canvas);
        }
        else if(mStyle == Linestyle.SlowLine){
            drawLine(canvas, true);
        }
        else {}
    }

    /**
     * 画点
     * @param canvas
     * @param count 要画的点个数
     */
    private  void drawPoints(Canvas canvas, int count){
        mPaint.setStyle(Paint.Style.FILL);
        try{
            if(count > mPoints.length)count = mPoints.length;
            for (int i = 0; i < count; i++)
            {
                canvas.drawCircle(mPoints[i].x, mPoints[i].y, CIRCLE_SIZE / 2, mPaint);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     *  画所有横向表格，包括X轴
     */
    private void drawAllXLine(Canvas canvas) {
        if(spacingHeight == 0){
           return;
        }
        for (int i = 0; i < spacingHeight + 1; i++)
        {
            canvas.drawLine(blwidh, bheight - (bheight / spacingHeight) * i + marginTop, (canvasWidth - blwidh),
                    bheight - (bheight / spacingHeight) * i + marginTop, mPaint);// Y坐标
            drawText(String.valueOf(averageValue * i), blwidh / 2, bheight - (bheight / spacingHeight) * i + marginTop+dip2px(3),
                    canvas);
        }
    }

    /**
     * 画所有纵向表格，包括Y轴
     */
    private void drawAllYLine(Canvas canvas) {
        for (int i = 0; i < yRawData.size(); i++)
        {
            xList.add(blwidh + (canvasWidth - blwidh) / yRawData.size() * i);
            canvas.drawLine(blwidh + (canvasWidth - blwidh) / yRawData.size() * i, marginTop, blwidh
                    + (canvasWidth - blwidh) / yRawData.size() * i, bheight + marginTop, mPaint);
            drawText(xRawDatas.get(i), blwidh-dip2px(12)+ (canvasWidth - blwidh) / yRawData.size() * i, bheight + dip2px(18),
                    canvas);// X坐标
        }
    }

    /**
     *画曲线
     * @param canvas
     */
    private void drawScrollLine(Canvas canvas) {
        Point startp = new Point();
        Point endp = new Point();
        for (int i = 0; i < mPoints.length - 1; i++)
        {
            startp = mPoints[i];
            endp = mPoints[i + 1];
            int wt = (startp.x + endp.x) / 2;
            Point p3 = new Point();
            Point p4 = new Point();
            p3.y = startp.y;
            p3.x = wt;
            p4.y = endp.y;
            p4.x = wt;

            Path path = new Path();
            path.moveTo(startp.x, startp.y);
            path.cubicTo(p3.x, p3.y, p4.x, p4.y, endp.x, endp.y);
            canvas.drawPath(path, mPaint);
            drawPoints(canvas, mPoints.length);
        }
    }

    /**
     * 画直线
     * @param canvas
     */
    private void drawLine(Canvas canvas){
        for (int i = 0; i < mPoints.length - 1; i++) {
            Point startp = mPoints[i];
            Point endp = mPoints[i+1];
            canvas.drawLine(startp.x, startp.y, endp.x, endp.y, mPaint);
        }
        drawPoints(canvas, mPoints.length);
    }

    private void drawLine(Canvas canvas, boolean slow) {
        for (int i = 0; i < pCount; i++) {
            Point startp = mPoints[i];
            Point endp = mPoints[i+1];
            canvas.drawLine(startp.x, startp.y, endp.x, endp.y, mPaint);
        }
        Point lastp = mPoints[pCount];
        Point nextp;
        try{
            if(pCount>=mPoints.length - 1)nextp = mPoints[mPoints.length - 1];
            else nextp = mPoints[pCount+1];
            Point thisp = new Point();
            thisp.x = lastp.x + (part + 1) * (nextp.x - lastp.x) / partCount;
            thisp.y = lastp.y + (part + 1) * (nextp.y - lastp.y) / partCount;
            canvas.drawLine(lastp.x, lastp.y, thisp.x, thisp.y, mPaint);
            drawPoints(canvas, pCount + (part + 1) / partCount + 1);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static boolean stepDrawing = false;
    public static boolean runDrawing = false;
    /**
     * 缓慢画出直线
     */
    public void slowDraw(final String page){
        if(page.equals("step"))stepDrawing = true;
        else runDrawing = true;
        mStyle = Linestyle.SlowLine;
        partCount = 10;
        new Thread() {
            @Override
            public void run() {
                for (pCount = 0; pCount < mPoints.length - 1; pCount++) {
                    for (part = 0; part < partCount; part++){
                        try {
                            postInvalidate();
                            Thread.sleep(30); //通过传递过来的速度参数来决定线程休眠的时间从而达到绘制速度的快慢
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }
                if(page.equals("step"))stepDrawing = false;
                else runDrawing = false;
            }
        }.start();
    }

    /**
     * 直接画出直线
     */
    public void direDraw(){
        mStyle = Linestyle.Line;
        invalidate();
    }

    public void clearLine(){
        mStyle = Linestyle.none;
        postInvalidate();
    }

    private void drawText(String text, int x, int y, Canvas canvas)
    {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setTextSize(dip2px(12));
        p.setColor(res.getColor(R.color.color_999999));
        p.setTextAlign(Paint.Align.LEFT);
        canvas.drawText(text, x, y, p);
    }

    private Point[] getPoints()
    {
        Point[] points = new Point[yRawData.size()];
        for (int i = 0; i < yRawData.size(); i++)
        {
            int ph = bheight - (int) (bheight * (yRawData.get(i) / maxValue));

            points[i] = new Point(xList.get(i), ph + marginTop);
        }
        return points;
    }

    public void setData(ArrayList<Double> yRawData, ArrayList<String> xRawData, int maxValue, int averageValue)
    {
        this.maxValue = maxValue;
        this.averageValue = averageValue;
        this.mPoints = new Point[yRawData.size()];
        this.xRawDatas = xRawData;
        this.yRawData = yRawData;
        this.spacingHeight = maxValue / averageValue;
    }

    public void setTotalvalue(int maxValue)
    {
        this.maxValue = maxValue;
    }

    public void setPjvalue(int averageValue)
    {
        this.averageValue = averageValue;
    }

    public void setMargint(int marginTop)
    {
        this.marginTop = marginTop;
    }

    public void setMarginb(int marginBottom)
    {
        this.marginBottom = marginBottom;
    }

    /**
     * 选择画直线，曲线
     * @param mStyle
     */
    public void setMstyle(Linestyle mStyle)
    {
        this.mStyle = mStyle;
    }

    public void setBheight(int bheight)
    {
        this.bheight = bheight;
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    private int dip2px(float dpValue)
    {
        return (int) (dpValue * dm.density + 0.5f);
    }
}
