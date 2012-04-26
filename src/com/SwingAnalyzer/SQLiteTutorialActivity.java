package com.SwingAnalyzer;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

public class SQLiteTutorialActivity extends Activity {
    /** Called when the activity is first created. */
	final static int ARRAY_SIZE = 5;
	SwingStatistics [] swingStats = new SwingStatistics[ARRAY_SIZE];
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    
        initSwingStatsData();
        DatabaseHandler db = new DatabaseHandler(this);
        
        Log.d("sqlite", "Inserting");
        
        for(int i=0 ; i<ARRAY_SIZE; i++)
        {
        	db.addSwingStats(swingStats[i]);
        }
        
        Log.d("db", "Reading");
        List<SwingStatistics> swingArray = db.getAllSwingStats();
        
        for(SwingStatistics stat : swingArray)
        {
        	String log = "ID:" + stat.getID() 
        				+ " Date:" + stat.getDate() 
        				+ " Time:" + stat.getTime()
        				+ " X_Max:" + stat.getXPositivePeak()
        				+ " X_Max_Time:" + stat.getXPositivePeakTime()
        				+ " Y_Min:" + stat.getYNegativePeak()
        				+ " Y_Min_Time:" + stat.getYNegativePeakTime();
        	
        	Log.d("db", log);
        }
        
    }
    
    public void initSwingStatsData()
    {
    	swingStats[0] = new SwingStatistics("2012-4-20", "01:01:01", 
    							"19.001", "1000", "-4.123", "1500",
    							"5.123", "2000", "-10.123", "2500");
    	
    	swingStats[1] = new SwingStatistics("2012-4-20", "02:02:02",
    							"19.100", "1100", "-4.456", "1600",
    							"6.123", "2100", "-11.123", "2600");
    	
    	swingStats[2] = new SwingStatistics("2012-4-21", "03:03:03",
								"19.200", "1110", "-5.456", "1610",
								"6.200", "1900", "-9.123", "2400");
    	
    	swingStats[3] = new SwingStatistics("2012-4-21", "05:00:00",
    							"17.100", "2500", "-6.456", "2600",
    							"6.123", "2010", "-11.123", "2050");
    	
    	swingStats[4] = new SwingStatistics("2012-4-22", "10:05:00",
								"10.123", "2600", "-2.456", "2700",
								"7.123", "2800", "-8.123", "2850");    	
    }    
}

class StatsRuler extends View
{
	int mScale=0;		
	int mInterval = 0;
	final static int MARGIN = 5;
	
	float mXMax = 0;
	float mXMin = 0;
	float mYMax = 0; 
	float mYMin = 0;
	
	
	public StatsRuler(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}
	
	public StatsRuler(Context context, AttributeSet attrs){
		super(context, attrs);
	}
	
	public StatsRuler(Context context) {
		super(context);
	}
	
	public void setScale(int scale, int collection_time) {
		mScale = scale;
		mInterval = collection_time / mScale;
		Log.i("scale", "mInterval:" + mInterval);
		
		invalidate();
	}
	
	public void setStatData(String maxX, String minX, String maxY, String minY)
	{
		mXMax = Float.parseFloat(maxX);
		mXMin = Float.parseFloat(minX);
		
		mYMax = Float.parseFloat(maxY);
		mYMin = Float.parseFloat(minY);
	}
	
	
	protected void onDraw(Canvas canvas)
	{
		
		int x = 0;
		int x1 = 0;
		int y = 0;
		int width = 0;
		int height = 0;
		int textSize = 0;
		int scaleSize = 0;
				
		String text = "";
		
		
		canvas.drawColor(Color.BLACK);
		Paint Pnt = new Paint();
		Pnt.setColor(Color.WHITE);
		Pnt.setTextAlign(Paint.Align.CENTER);
		
		Resources res = getResources();
		DisplayMetrics dm = res.getDisplayMetrics();
		
		// Textsize = 10dp
		textSize = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, dm);
		Pnt.setTextSize(textSize);
		
		scaleSize = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, dm);	
		
		width = getWidth() - 20;		
		height = getHeight();
		//width = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, getWidth()-20, dm);
		//height = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, getHeight(), dm);
		Log.i("scale", "Width:" + width + ", Height:" + height);
		
		for(int i=0; i< mScale; i++)
		{
			Pnt.setAntiAlias(false);
			if(i==0)
				x = MARGIN;
			else
				x = ((width/mScale) * i) + MARGIN;
			
			if(i < mScale)
			{
				x1 = ((width/mScale) *(i+1)) + 5;
			}
			canvas.drawLine(x, 30, x1, 30, Pnt);
		}
		
		// (mScale+1) is needed to draw "|"
		for(int unit=0; unit <= mScale; unit++)
		{
			Pnt.setAntiAlias(false);
			
			if(unit == 0)
				x = MARGIN;
			else
				x = ((width/mScale) * unit) + MARGIN;			
			
			y = scaleSize;
			canvas.drawLine(x, 20, x, y+textSize+20, Pnt);			
			
			Pnt.setAntiAlias(true);
			text = "" + (mInterval * unit);			
				
			canvas.drawText(text, x, y, Pnt);			
		}
		
	}
}