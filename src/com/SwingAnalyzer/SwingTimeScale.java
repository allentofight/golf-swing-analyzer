/*-----------------------------------------------------------------------------------------
  File:   SwingTimeScale.java

  Author: Jung Chang Su
  -----------------------------------------------------------------------------------------
  Copyright (C) 2012 SICS.
  
  Draw time scale and text for providing feedback.
  
  *----------------------------------------------------------------------------------------*/
package com.SwingAnalyzer;

import android.content.*;
import android.content.res.Resources;

import android.graphics.*;
import android.util.*;
import android.view.*;

public class SwingTimeScale extends View{
	final static int MARGIN = 5;
	
	int mScale=0;		
	int mInterval = 0;
	int mStart = 0;
	int mEnd = 0;
	
	public SwingTimeScale(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}
	
	public SwingTimeScale(Context context, AttributeSet attrs){
		super(context, attrs);
	}
	
	public SwingTimeScale(Context context) {
		super(context);
	}
	
	public void setScale(int scale, int collection_time) {
		mScale = scale;
		mInterval = collection_time / mScale;
		mStart = 0;
		mEnd = collection_time;
		
		invalidate();
	}
	
	public void setFeedbackScale(int scale, int start, int end)
	{
		mScale = scale;
		mStart = start;
		mEnd = end;
		
		mInterval = (mEnd - mStart) / mScale;
		
		Log.i("timescale", "Scale:" + mScale + ", Interval:" + mInterval 
							+ ", Start:" + mStart + ", End:" + mEnd);
		invalidate();
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
		
		/*=======================================================================
		 * 
		 *       0   300  600                         3000
		 *       |----|----|----|----| .. --------------|
		 *  
		 *
		 *  (mScale+1) is needed to draw "|"
		 *======================================================================*/
		for(int unit=0; unit <= mScale; unit++)
		{
			Pnt.setAntiAlias(false);
			
			if(unit == 0)
				x = MARGIN;
			else
				x = ((width/mScale) * unit) + MARGIN;			
			
			y = scaleSize;
			canvas.drawLine(x, 20, x, y + textSize + 20, Pnt);			
			
			Pnt.setAntiAlias(true);
			
			
			//Log.i("realswing", "Scale: text=" + text);
			if(unit == mScale)
			{
				int lastTime = mStart + (mInterval * unit);
				if(lastTime != mEnd)
					text = "" + mEnd;
				else
					text = "" + lastTime;
			}
			else
				text = "" + (mStart + (mInterval * unit));
			
			if((unit % 2 == 0) || (unit == mScale))
			{
				if(unit == 0)
					canvas.drawText(text, x+10, y, Pnt);
				else
					canvas.drawText(text, x, y, Pnt);
			}
		}
		
	}

}
