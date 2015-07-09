package com.itxiaowu.surfaceview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by thinkpad on 2015/7/8.
 */
public class SurfaceTemple extends SurfaceView implements SurfaceHolder.Callback ,Runnable{
    private SurfaceHolder holder;
    private Canvas canvas;
    private Thread thread;
    private boolean isRunning;

    private int mCount=6;
    private String[] prizeStrs=new String[]{"单反相机","IPAD","恭喜发财","IPHONE","衣服一套","恭喜发财"};

    private int[] colors=new int[]{0xFFFFC300,0xFFF17E01,0xFFFFC300,0xFFF17E01,0xFFFFC300,0xFFF17E01,0xFFFFC300,0xFFF17E01};
    /**
     * 与文字对应的图片
     */
    private int[] mImgs = new int[]{R.drawable.danfan, R.drawable.ipad,
            R.drawable.f040, R.drawable.iphone, R.drawable.meizi,
            R.drawable.f040};

    private Bitmap bgBitmap;
    private Bitmap[] imagesBitmap;

    private float startAngle;
    private float speed=0;
    private boolean isShouldEnd=false;

    private int mRadius;
    private int mCenter;
    private int padding=80;
    private RectF mRange;

    private Paint mTextPaint;
    private Paint mArcPaint;
    private float mTextSize= TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,15,getResources().getDisplayMetrics());

    public SurfaceTemple(Context context) {
        super(context);
    }

    public SurfaceTemple(Context context, AttributeSet attrs) {

        super(context,attrs);
        holder=getHolder();
        holder.addCallback(this);
        bgBitmap= BitmapFactory.decodeResource(getResources(),R.drawable.bg2);
        Log.d("zxw","SurfaceTemple(Context context, AttributeSet attrs)");
    }

    public void luckStart(int index){
        int angle=360/mCount;
        /**
         * 2 旋转90-150
         * 1 旋转150-210
         * 0 旋转210-270
         */
        float start=210-index*angle;
        float end=210-index*angle+angle;

        float targetStart=4*360+start;
        float targetEnd=4*360+end;
        /**
         * <pre>
         *     V1 -->  0
         *     且每次-1
         *     (v1+0)*(v1+1)/2=targetStart
         *     v1*v1+v1-2*targetStart=0;
         *     求解得v1=(-1+Math.sqrt(1+8targetStart))/2
         * </pre>
         */

        float v1= (float) ((-1+Math.sqrt((1+8*targetStart)))/2);
        float v2= (float) ((-1+Math.sqrt((1+8*targetEnd)))/2);

        speed= (float) (v1+ Math.random()*(v2-v1));
//        speed=  v1;
        isShouldEnd=false;
//        speed=50;
    }

    public void luckEnd(){
        isShouldEnd=true;
        startAngle=0;
    }

    public boolean isLuckRunning(){
        return speed>0;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width=Math.min(getMeasuredWidth(),getMeasuredHeight());
        mCenter=width/2;
        mRadius=width-2*padding;
        setMeasuredDimension(width,width);
        Log.d("zxw","onMeasure()");
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d("zxw","surfaceCreated()");
        mArcPaint=new Paint();
        mArcPaint.setAntiAlias(true);
        mArcPaint.setDither(true);

        mTextPaint=new Paint();
        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setColor(Color.WHITE);

        mRange=new RectF(padding,padding, mRadius +padding, mRadius +padding);

        imagesBitmap=new Bitmap[mCount];

        for (int i=0;i<mCount;i++){
            imagesBitmap[i]=BitmapFactory.decodeResource(getResources(),mImgs[i]);
        }

        isRunning=true;
        thread=new Thread(this);
        thread.start();

    }

    @Override
    public void run() {
        Log.d("zxw","run()");
        while (isRunning){
            long start=System.currentTimeMillis();
            draw();
            long end=System.currentTimeMillis();
            if (end-start<50){
                try {
                    Thread.sleep(50-(end-start));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

    }

//be careful
    private void draw() {
        try {
            canvas=holder.lockCanvas();
            if (canvas!=null){
                // draw background
                drawBg();
                // draw table
                float tmpAngle=startAngle;
                float sweepAngle=360/mCount;
                for (int i=0;i<mCount;i++){
                    mArcPaint.setColor(colors[i]);
                    canvas.drawArc(mRange,tmpAngle,sweepAngle,true,mArcPaint);
                    //draw text
                    drawText( tmpAngle,sweepAngle,prizeStrs[i]);
                    drawImage(tmpAngle,imagesBitmap[i]);
                    tmpAngle=tmpAngle+sweepAngle;
                }
                startAngle+=speed;
                if (isShouldEnd){
                    speed-=1;
                }
                if (speed<=0){
                    isShouldEnd=false;
                    speed=0;
                }
                //doing something
                Log.d("zxw","draw()");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (canvas!=null){
                holder.unlockCanvasAndPost(canvas);
            }

        }

    }

    private void drawImage(float tmpAngle, Bitmap bitmap) {
        int imageWidth=mRadius/8;
        float angle= (float) ((tmpAngle+360/mCount/2)*Math.PI/180);
        int centerH= (int) (mCenter+mRadius/4*Math.cos(angle));
        int centerV=(int) (mCenter+mRadius/4*Math.sin(angle));
        Rect rect=new Rect(centerH-imageWidth/2,centerV-imageWidth/2,
                centerH+imageWidth/2,centerV+imageWidth/2);
        canvas.drawBitmap(bitmap,null,rect,null);
    }

    /**
     * draw text for every part
     * @param tmpAngle
     * @param sweepAngle
     * @param prizeStr
     */
    private void drawText(float tmpAngle, float sweepAngle, String prizeStr) {
        Log.d("zxw","drawText");
        Path path=new Path();
        path.addArc(mRange,tmpAngle,sweepAngle);
        float textWidth=mTextPaint.measureText(prizeStr);
        float hOffset= (float) (mRadius*Math.PI/mCount/2-textWidth/2);
        float vOffset=mRadius/2/6;
        canvas.drawTextOnPath(prizeStr,path,hOffset,vOffset,mTextPaint);
    }

    private void drawBg() {
        Log.d("zxw","drawBg");
        canvas.drawColor(0xffff00ff);
        canvas.drawBitmap(bgBitmap,null,
                new Rect(padding/2,padding/2,getMeasuredWidth()-padding/2,getMeasuredWidth()-padding/2),null);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

        isRunning=false;
//        if (canvas!=null){
//            holder.unlockCanvasAndPost(canvas);
//            Log.d("zxw","surfaceDestroyed");
//        }
    }


    public boolean isShouldEnd() {
        return isShouldEnd;
    }
}
