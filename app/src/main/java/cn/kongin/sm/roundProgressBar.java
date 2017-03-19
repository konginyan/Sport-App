package cn.kongin.sm;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;

public class roundProgressBar extends View {

    private float mRadius;//内圆半径
    private float mRingRadius;//进度条半径
    private float mStrokeWidth;//进度条宽度
    private int mCircleColor;//内圆颜色
    private int mBigCircleColor;//大圆颜色
    private int mRingColor;//进度条颜色
    private float mProgress;//当前进度
    private float mTotalProgress;//总进度
    private int mTextColor;//文字颜色
    private float mTextSize;//文字大小
    private float mTxtWidth;//文字宽度
    private float mTxtHeight;//文字高度
    private float mDrawProgress;//加载时画的进度
    private boolean mBarStyle;//画的方法

    private Paint mTextPaint;//文字画笔
    private Paint mRingPaint;//进度条画笔
    private Paint mBigPaint;//大圆画笔
    private Paint mCirclePaint;//内圆画笔

    public static boolean drawing = false;//正在画

    public roundProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAttrs(context, attrs);
        if(mBarStyle){
            initDraw();
        }
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        //基础属性
        TypedArray typeArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.roundProgressBar, 0, 0);
        mRadius = typeArray.getDimension(R.styleable.roundProgressBar_radius, 100);
        mStrokeWidth = typeArray.getDimension(R.styleable.roundProgressBar_strokeWidth, 30);
        mCircleColor = typeArray.getColor(R.styleable.roundProgressBar_circleColor, Color.WHITE);
        mRingColor = typeArray.getColor(R.styleable.roundProgressBar_ringColor, Color.BLACK);
        mProgress = typeArray.getInt(R.styleable.roundProgressBar_Progress, 0);
        mTotalProgress = typeArray.getInt(R.styleable.roundProgressBar_totalProgress, 100);
        mTextColor = typeArray.getColor(R.styleable.roundProgressBar_textColor, Color.BLACK);
        mBigCircleColor = typeArray.getColor(R.styleable.roundProgressBar_bigCircleColor, Color.RED);
        mTextSize = typeArray.getInt(R.styleable.roundProgressBar_textSize, 45);
        mBarStyle = typeArray.getBoolean(R.styleable.roundProgressBar_barStyle, true);
        typeArray.recycle();//注意这里要释放掉
        mRingRadius = mRadius + mStrokeWidth / 2;
        mTxtWidth = mTextSize;
        mTxtHeight = mTextSize;

        //画笔
        mBigPaint = new Paint();
        mRingPaint = new Paint();
        mCirclePaint = new Paint();
        mTextPaint = new Paint();

        mCirclePaint.setColor(mCircleColor);

        mTextPaint.setColor(mTextColor);
        mTextPaint.setTextSize(mTextSize);

        mBigPaint.setColor(mBigCircleColor);
        mBigPaint.setStrokeWidth(mStrokeWidth);
        mBigPaint.setStyle(Paint.Style.STROKE);

        mRingPaint.setColor(mRingColor);
        mRingPaint.setStrokeWidth(mStrokeWidth);
        mRingPaint.setStyle(Paint.Style.STROKE);
    }

    private void initDraw() {
        mDrawProgress = 0;
        drawing = true;
        //绘图线程
        new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000); //通过传递过来的速度参数来决定线程休眠的时间从而达到绘制速度的快慢
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                while (mDrawProgress<mRingRadius+30) {
                    mDrawProgress+=8;
                    postInvalidate();
                    try {
                        Thread.sleep(10); //通过传递过来的速度参数来决定线程休眠的时间从而达到绘制速度的快慢
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                while (mDrawProgress>mRingRadius){
                    mDrawProgress-=2;
                    postInvalidate();
                    try {
                        Thread.sleep(10); //通过传递过来的速度参数来决定线程休眠的时间从而达到绘制速度的快慢
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                drawing = false;
            }
        }.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {

        float mXCenter = getWidth() / 2;
        float mYCenter = getHeight() / 2;
        //canvas.drawCircle(mXCenter, mYCenter, mRadius + mStrokeWidth, mBigPaint);
        //canvas.drawCircle(mXCenter, mYCenter, mRadius, mCirclePaint);

        //圆弧所在区域
        RectF oval = new RectF();
        if(mBarStyle){
            oval.left = (mXCenter - mDrawProgress);
            oval.top = (mYCenter - mDrawProgress);
            oval.right = mDrawProgress * 2 + (mXCenter - mDrawProgress);
            oval.bottom = mDrawProgress * 2 + (mYCenter - mDrawProgress);
        }
        else {
            oval.left = (mXCenter - mRingRadius);
            oval.top = (mYCenter - mRingRadius);
            oval.right = mRingRadius * 2 + (mXCenter - mRingRadius);
            oval.bottom = mRingRadius * 2 + (mYCenter - mRingRadius);
        }

        //绘制圆弧
        canvas.drawArc(oval, -90, 360, false, mBigPaint);
        canvas.drawArc(oval, -90, ((float) mProgress / mTotalProgress) * 360, false, mRingPaint);
        //canvas.drawCircle(mXCenter, mYCenter, mRadius, mCirclePaint);

        //显示当前进度（10%这种形式的）
        String txt = (int)mProgress + "/" + (int)mTotalProgress;
                //(int) (mProgress * 1.0f / mTotalProgress * 100) + "%";
        mTxtWidth = mTextPaint.measureText(txt, 0, txt.length());
        if(mBarStyle){
            if(mDrawProgress < mRingRadius) txt = "";
            canvas.drawText(txt, mXCenter - mTxtWidth / 2, mYCenter + mTxtHeight / 4, mTextPaint);
        }
    }

    public void setProgress(int progress){
        mProgress = progress;
    }

    public int getProgress(){
        return (int)mProgress;
    }

    public void setTotalProgress(int totalProgress){
        mTotalProgress = totalProgress;
    }

    public int getTotalProgress(){
        return (int)mTotalProgress;
    }
}
