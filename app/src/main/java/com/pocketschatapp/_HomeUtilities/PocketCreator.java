package com.pocketschatapp._HomeUtilities;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

/**
 * Created by Chris on 2/23/2015.
 */
public class PocketCreator extends View implements View.OnTouchListener, View.OnLongClickListener {

    private static PocketCreatorListener mPocketCreatorListener;
    public interface PocketCreatorListener {
        public void onLongTouch(float longTouchedCenterX, float longTouchedCenterY);
    }

    private float prevX = 100;
    private float prevY = 100;

    private boolean clicked;
    private boolean longTouched;
    private boolean moving;
    private boolean scaling;

    private float baseWidth;
    private float baseHeight;
    private float mScaleFactor = 1.f;


    private float xAdjustor = 0;
    private float yAdjustor = 0;

    private float longTouchedCenterX;
    private float longTouchedCenterY;



    private ScaleGestureDetector mScaleDetector;

    public PocketCreator(Context context, RelativeLayout parentView, float width, float height, float xCoord, float yCoord, boolean manualDraw, PocketCreatorListener listener) {
        super(context);

        setDrawingCacheEnabled(true);
        setOnTouchListener(this);
        setOnLongClickListener(this);

        mPocketCreatorListener = listener;

        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());

        clicked = false;
        longTouched = false;
        moving = false;
        scaling = false;

        baseWidth = width;
        baseHeight = height;

        if(manualDraw)
        {
            setX(xCoord - 200);
            setY(yCoord - 200);

            Log.d("testing", "x: " + String.valueOf(xCoord) + " y: " + String.valueOf(yCoord));
        }
        else
        {
            xAdjustor = 0;
            yAdjustor = 0;
        }

        hide();

        setLayoutParams(new ViewGroup.LayoutParams(200, 200));
        setMinimumWidth(200);
        setMinimumHeight(200);

        parentView.addView(this);
        //this.setAlpha(20);

        show();

        invalidate();
        forceLayout();
    }

    //do i need invalidate()?
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.BLUE);
        paint.setAlpha(50);
        canvas.drawCircle(((baseWidth * mScaleFactor) / 2), ((baseHeight * mScaleFactor) / 2), 100 * mScaleFactor, paint);

        Paint paint1 = new Paint();
        paint1.setStyle(Paint.Style.STROKE);
        paint1.setColor(Color.WHITE);
        paint.setAlpha(80);
        paint1.setStrokeWidth(10);
        canvas.drawCircle(((baseWidth * mScaleFactor) / 2), ((baseHeight * mScaleFactor) / 2), 90 * mScaleFactor, paint);
    }

    @Override
    public boolean onTouch(final View v, final MotionEvent event)
    {
        mScaleDetector.onTouchEvent(event);

        final RelativeLayout.LayoutParams par = (RelativeLayout.LayoutParams) v.getLayoutParams();
        switch(event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
            {
                Log.d("testing", "clicked!");
                if(scaling)
                    return false;

                clicked = true;
                longTouched = false;
                moving = false;

                prevX=(int)event.getRawX();
                prevY=(int)event.getRawY();
                par.bottomMargin=-2*v.getHeight();
                par.rightMargin=-2*v.getWidth();
                v.setLayoutParams(par);
                return false;
            }
            case MotionEvent.ACTION_MOVE:
            {
                if(scaling)
                    return false;

                clicked = false;
                longTouched = false;

                par.topMargin+=(int)event.getRawY()-prevY;
                prevY=(int)event.getRawY();
                par.leftMargin+=(int)event.getRawX()-prevX;
                prevX=(int)event.getRawX();
                v.setLayoutParams(par);


                if((Math.abs(prevX - event.getX()) > 15.0) || (Math.abs(prevY - event.getY()) > 15.0))
                {
                    moving = true;
                }


                return false;
            }
            case MotionEvent.ACTION_UP:
            {
                Log.d("testing", "released");

                if(scaling) {
                    scaling = false;
                    return false;
                }

                moving  = false;
                clicked = false;
                longTouched = false;

                //if clicked
                //if long touched
                //is moving

                par.topMargin+=(int)event.getRawY()-prevY;
                par.leftMargin+=(int)event.getRawX()-prevX;
                v.setLayoutParams(par);

                return false;
            }
        }
        return false;
    }

    //add long click delay
    //on release, check if long clicked, then do long click action
    //if moved after registering long click, invalidate long click
    @Override
    public boolean onLongClick(View v) {
        Log.d("testing", "long touched");

        if(moving)
        {
            return false;
        }

        //Home.addRoomToMap(v.getX() + getRadius(), v.getY() + getRadius());

        longTouchedCenterX = v.getX() + getRadius();
        longTouchedCenterY = v.getY() + getRadius();



        //zoom level
        //lat1 lon1 lat2 lon2




        Log.d("testing", String.valueOf(v.getX()));
        Log.d("testing", String.valueOf(v.getY()));
        //popup to create room and confirm
        //require GCM verification


        //adjust to radius marker colors


        longTouched = true;

        mPocketCreatorListener.onLongTouch(longTouchedCenterX, longTouchedCenterY);

        return true;
    }

    public float getLongTouchedCenterX() {
        return longTouchedCenterX;
    }

    public float getLongTouchedCenterY() {
        return longTouchedCenterY;
    }

    public Bitmap getAsBitmap()
    {
        Bitmap.Config conf = Bitmap.Config.ARGB_8888; // see other conf types
        Bitmap bmp = Bitmap.createBitmap(getWidth(), getHeight(), conf); // this creates a MUTABLE bitmap
        Canvas tempCanvas = new Canvas(bmp);

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(100, 102, 255, 255));
        paint.setAlpha(50);
        tempCanvas.drawCircle(((baseWidth * mScaleFactor) / 2), ((baseHeight * mScaleFactor) / 2), 100 * mScaleFactor, paint);

        Paint paint1 = new Paint();
        paint1.setStyle(Paint.Style.STROKE);
        paint1.setColor(Color.BLUE);
        paint1.setAlpha(100);
        paint1.setStrokeWidth(2);
        paint1.setAntiAlias(true);
        tempCanvas.drawCircle(((baseWidth * mScaleFactor) / 2), ((baseHeight * mScaleFactor) / 2), 99 * mScaleFactor, paint1);

        return bmp;
    }

    public float getRoomWidth()
    {
        return (100 * mScaleFactor) * 2;
    }

    public void resetRoomSize()
    {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) getLayoutParams();

        baseWidth = 200;
        baseHeight = 200;
        mScaleFactor = 1.f;

        params.width = 200;
        params.height = 200;

        setLayoutParams(params);
    }

    public float getRoomHeight()
    {
        return (100 * mScaleFactor) * 2;
    }

    public float getRadius()
    {
        return 100 * mScaleFactor;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {

            scaling = true;

            mScaleFactor *= detector.getScaleFactor();
            mScaleFactor = Math.max(1f, Math.min(mScaleFactor, 2f));

            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) getLayoutParams();

            int oldWidth = params.width;
            int oldHeight = params.height;
            params.width = (int) (baseWidth * mScaleFactor);
            params.height = (int) (baseHeight * mScaleFactor);
            setLayoutParams(params);

            if(oldWidth <= params.width) {
                setX(getX() - (Math.abs(oldWidth - params.width) / 2));
                setY(getY() - (Math.abs(oldHeight - params.height) / 2));
            }
            else if(oldWidth >= params.width) {
                setX(getX() + (Math.abs(oldWidth - params.width) / 2));
                setY(getY() + (Math.abs(oldHeight - params.height) / 2));
            }

            invalidate();
            return true;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            scaling = true;
            // Return true here to tell the ScaleGestureDetector we
            // are in a scale and want to continue tracking.
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {

            //scaling = false;
            // We don't care about end events, but you could handle this if
            // you wanted to write finished values or interact with the user
            // when they are finished.
        }
    }

    public boolean isVisible()
    {
        return getVisibility() == View.VISIBLE;
    }

    public void hide()
    {
        setVisibility(View.GONE);
    }

    public void show()
    {
        setVisibility(View.VISIBLE);
    }
}
