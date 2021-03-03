package com.example.surfase;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

class TestSurfaceView extends SurfaceView implements SurfaceHolder.Callback{

    public TestSurfaceView(Context context) {
        super(context);
        getHolder().addCallback(this);
    }

    class DrawThread extends Thread{
        int plus=0;
        private SurfaceHolder surfaceHolder;
        Paint p = new Paint();
        Paint b = new Paint();
        int a=2;
        private volatile boolean running = true;

        public DrawThread(Context context, SurfaceHolder surfaceHolder) {
            this.surfaceHolder = surfaceHolder;
            b.setColor(Color.RED);
            p.setColor(Color.BLUE);
        }

        public void requestStop() {
            running = false;
        }

        @Override
        public void run() {
            while (running) {
                Canvas canvas = surfaceHolder.lockCanvas();
                if (canvas != null) {
                    try {
                        canvas.drawRect(0,0,canvas.getWidth(), canvas.getHeight(),b);
                        canvas.drawCircle(x , y , r, p);
                        if (x>canvas.getWidth()-r || x<r) {
                            vx=-vx;
                            x+=vx;
                            vx*=0.5;
                        }
                        if (y>canvas.getHeight()-r || y<r) {
                            vy=-vy;
                            y+=vy;
                            vy*=0.5;
                        }

                    } finally {
                        surfaceHolder.unlockCanvasAndPost(canvas);
                        try {
                            Thread.sleep(30);
                            vy+=a; // ускорение свободного падения
                            y+=vy;
                            x+=vx;
                            vx*=0.96;//сопротивление воздуха отнимает немного скорости
                            vy*=0.96;//cопротивление воздуха отнимает немного скорости
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if (touched) {
                            touched=false;
                           r+=5;
                        }
                    }
                }
            }
        }
    }

    DrawThread dt;
    float x=500,y=500,vx=0,vy=0,s=0;
    float r=50;
    boolean touched =false;

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        dt = new DrawThread(getContext(),getHolder());
        dt.start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        dt.requestStop();
        boolean retry = true;
        while (retry) {
            try {
                dt.join();
                retry = false;
            } catch (InterruptedException e) {
                //

            }
        }
    }

    public boolean onTouchEvent(MotionEvent event){
        this.touched = true;
        this.vx = (event.getX()-this.x)/5; // При нажатии мяч меняет направление в сторону точки касания
        this.vy = (event.getY()-this.y)/5; // При нажатии мяч меняет направление в сторону точки касания
        return false;
    }
}