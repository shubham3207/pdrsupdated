package org.pitambar.sakshipdr;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

public class MySurfaceView extends SurfaceView implements SurfaceHolder.Callback, View.OnTouchListener {

    private Bitmap mImage;
    private float mX;
    private float mY;
    private float mVelocityX;
    private Paint mPaint;

    private UpdateThread mUpdateThread;
    private float mWidth;

    public MySurfaceView(Context context, int width, int height) {
        super(context);
        // Initialize the image and paint objects

        DisplayMetrics displayMetrics = new DisplayMetrics();

        mImage = BitmapFactory.decodeResource(getResources(), R.drawable.indoor_map);
        mPaint = new Paint();
        mPaint.setColor(Color.RED);

        // Set the x-coordinate of the image's position to the center of the screen
        mX = width/2 - mImage.getWidth() / 2;

        // Set the y-coordinate of the image's position to the center of the screen
        mY = height/2 - mImage.getHeight() / 2;

        // Set the velocity of the image's movement to 0
        mVelocityX = 0;

        mWidth = width;

        // Set this view as the touch listener
        setOnTouchListener(this);

        // Get the SurfaceHolder object for this view and add this view as a callback
        SurfaceHolder holder = getHolder();
        holder.addCallback(this);
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
// When the surface is created, start the update thread
        mUpdateThread = new UpdateThread(holder, this);
        mUpdateThread.setRunning(true);
        mUpdateThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
//do nothing
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // When the surface is destroyed, stop the update thread
        mUpdateThread.setRunning(false);
        try {
            mUpdateThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        // Update the velocity of the image's movement based on the touch input
        float oldX = mX;
        float newX = event.getX();
        mVelocityX = newX - oldX;
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
//        super.onDraw(canvas);
        canvas.drawBitmap(mImage, mX, -10, mPaint);
    }

    public void update() {
//         Update the x-coordinate of the image's position based on the velocity
        mX += mVelocityX;

        // If the image has moved off the screen, wrap it around to the other side
        if (mX > mWidth) {
            mX = -mImage.getWidth();
        } else if (mX + mImage.getWidth() < 0) {
            mX = mWidth;
        }

    }

    private class UpdateThread extends Thread{
        private SurfaceHolder mHolder;
        private MySurfaceView mView;
        private boolean mIsRunning;

        public UpdateThread(SurfaceHolder holder, MySurfaceView view){
            mHolder = holder;
            mView = view;
            mIsRunning = false;
        }

        public void setRunning(boolean isRunning) {
            mIsRunning = isRunning;
        }

        @Override
        public void run() {
            while (mIsRunning){
                mView.update();
//                if(mHolder.getSurface().isValid())
//                    continue;

                Canvas canvas = mHolder.lockCanvas();
                mView.draw(canvas);
                mView.setBackgroundColor(Color.WHITE);
                mHolder.unlockCanvasAndPost(canvas);

                try{
                    Thread.sleep(10);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
        }
    }
}
