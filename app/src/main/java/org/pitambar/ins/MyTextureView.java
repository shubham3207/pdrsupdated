package org.pitambar.ins;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;

import java.util.ArrayList;

public class MyTextureView extends TextureView implements TextureView.SurfaceTextureListener {

    private Bitmap mImage;
    private Bitmap mMarker;
    private float mX;
    private float mY;
    private float mVelocityX;
    private float mVelocityY;
    private Paint mPaint;
    private Surface mSurface;
    private int mWidth, mHeight;
    private ArrayList<float[]> mPositionArray = new ArrayList<>();
    Canvas canvas;

    public MyTextureView(Context context, int width, int height, Bitmap image) {
        super(context);

        mWidth = width;
        mHeight = height;

        mImage = image;
        mPaint = new Paint();
        mPaint.setColor(Color.RED);

        mMarker = BitmapFactory.decodeResource(getResources(), R.drawable.compass_icon);

        // Set the x-coordinate of the image's position to the center of the screen
        mX = width / 2 - mImage.getWidth() / 2;

        // Set the y-coordinate of the image's position to the center of the screen
        mY = height / 2 - mImage.getHeight() / 2;

        // Set the velocity of the image's movement to 0
        mVelocityX = 0;
        mVelocityY = 0;
        setSurfaceTextureListener(this);

    }

    public MyTextureView(Context context, int width, int height) {
        super(context);
        // Initialize the image and paint objects

        DisplayMetrics displayMetrics = new DisplayMetrics();

        mWidth = width;
        mHeight = height;

        mImage = BitmapFactory.decodeResource(getResources(), R.drawable.floorplan_hero);
        mPaint = new Paint();
        mPaint.setColor(Color.RED);

        mMarker = BitmapFactory.decodeResource(getResources(), R.drawable.compass_icon);

        float imageWidth = mImage.getWidth();
        float imageHeight = mImage.getHeight();


        // Set the x-coordinate of the image's position to the center of the screen
        mX = width / 2 - mImage.getWidth() / 2;

        // Set the y-coordinate of the image's position to the center of the screen
        mY = height / 2 - mImage.getHeight() / 2;

        // Set the velocity of the image's movement to 0
        mVelocityX = 0;
        mVelocityY = 0;
        // Set this view as the touch listener
//        setOnTouchListener(this);
        setSurfaceTextureListener(this);

    }

    public void setmVelocityX(float mVelocityX) {
        this.mVelocityX = mVelocityX;
    }

    public void setmVelocityY(float mVelocityY) {
        this.mVelocityY = mVelocityY;
    }

    public void setmPositionArray(ArrayList<float[]> mPositionArray) {
        this.mPositionArray = mPositionArray;
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        mSurface = new Surface(surface);
        drawImage();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        mSurface.release();
        mSurface = null;
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    public void drawImage() {
        update();
        canvas = mSurface.lockCanvas(null);
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        Bitmap bit = drawCircle(mImage);
        canvas.drawBitmap(bit, mX, mY, mPaint);
        canvas.drawCircle(mWidth / 2, mHeight / 2, 10f, mPaint);
        mSurface.unlockCanvasAndPost(canvas);
    }

    private Bitmap drawCircle(Bitmap bm) {

        Bitmap bmOverlay = Bitmap.createBitmap(bm.getWidth(), bm.getHeight(), bm.getConfig());
        Canvas canvas = new Canvas(bmOverlay);
        Paint paint = new Paint();
        paint.setColor(Color.BLUE);
        canvas.drawBitmap(bm, new Matrix(), null);
        // Draw the circle at the current position
        if (mPositionArray.size() > 0) {
            for (int i = 1; i < mPositionArray.size(); i++) {
                float tempX = mPositionArray.get(i)[0];
                float tempY = mPositionArray.get(i)[1];
                canvas.drawCircle(tempX,tempY, 5f, paint);
                if (mPositionArray.size() > 1 && i <= mPositionArray.size() - 2) {
                    canvas.drawLine(mPositionArray.get(i)[0],
                            mPositionArray.get(i)[1]
                            , mPositionArray.get(i + 1)[0],
                            mPositionArray.get(i + 1)[1], paint);
                }
            }
            postInvalidate();
        }


        return bmOverlay;
    }

    public void update() {
        // Update the x-coordinate of the image's position based on the velocity
        mX = mX - mVelocityX;
        //update the y-coordinate of the image's position based on the velocity
        mY = mY - mVelocityY;

//        if(mX<(mImage.getWidth()/2-mWidth/2)){
//            mX = -(mImage.getWidth()/2-mWidth/2);
//        }else if(mY<(mImage.getHeight()/2 -mHeight/2)){
//            mY = -(mImage.getHeight()/2 -mHeight/2);
//        }else if(mX > 0){
//            mX = 0;
//        }else if(mY > 0){
//            mY = 0;
//        }
    }
}
