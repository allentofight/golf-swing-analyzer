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
	final static int TIME_SCALE = 10;
	
	int mScale=0;		
	int mInterval = 0;
	int mStart = 0;
	int mEnd = 0;
	
	int mMaxTextValue[] = new int[TIME_SCALE];
	
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
	
	public void setScale(int scale, int collection_time) 
	{
		mScale = scale;
		mInterval = collection_time / mScale;
		mStart = 0;
		mEnd = collection_time;
	
		for(int i=0; i<TIME_SCALE; i++)
			mMaxTextValue[i] = 0;
		
		invalidate();
	}
	
	public void setFeedbackScale(int scale, int start, int end)
	{
		mScale = scale;
		mStart = start;
		mEnd = end;
		
		mInterval = (mEnd - mStart) / mScale;
		
		for(int i=0; i<TIME_SCALE; i++)
			mMaxTextValue[i] = 0;
		
		Log.i("timescale", "Scale:" + mScale + ", Interval:" + mInterval 
							+ ", Start:" + mStart + ", End:" + mEnd);
		invalidate();
	}
	
	public void drawMaxValueText(int[] maxTextValue)
	{
		for(int i=0; i<maxTextValue.length; i++)
		{
			mMaxTextValue[i] = maxTextValue[i];
		}
		
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
		int interval = 0;	
		String text = "";
		String textMaxValue = "";
		
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

		/*=======================================================================
		 * 
		 *       0   300  600                         3000
		 *       |----|----|----|----| .. --------------|
		 *  
		 *
		 *  (mScale+1) is needed to draw "|"
		 *======================================================================*/		
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
		 *  Draw vertical lines("|") and values(0, 300, 600, ...)
		 *  
		 *       0   300  600                         3000
		 *       |----|----|----|----| .. --------------|
		 *  
		 *  Text coordination: X = ((width/mScale) * unit + MARGIN
		 *  				   Y = 10 
		 *  Vertical Lines:    X = ((width/mScale) * unit + MARGIN
		 *                     Y = 40
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
			//Log.i("scale", "| = " + x);
			
			Pnt.setAntiAlias(true);
			
			
			
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
			
			/*
			if((unit % 2 == 0) || (unit == mScale))
			{
				if(unit == 0)
					canvas.drawText(text, x+10, y, Pnt);
				else
					canvas.drawText(text, x, y, Pnt);
			}
			*/
			if(unit == 0)
				canvas.drawText(text, x+4, y, Pnt);
			else
				canvas.drawText(text, x, y, Pnt);

		}
		
		/*=======================================================================
		 * Draw a maximum text in the middle of each time slot 
		 * 
		 *      
		 *       |   1   |  10   |
		 *       |-------|-------| .. --------------|
		 *       5   20  35  50  65
		 *  
		 *  X-axis: ((width/mScale) * i)/2 + MARGIN = 20, 50, 80
		 *  Y-axis: 25
		 *======================================================================*/
		for(int j=0; j < mScale; j++)
		{
			interval = ((width/mScale) * (j+1)) + MARGIN;
			
			x = interval - 15;
			y = 25;
			
			//Log.i("scale", "Max X :" + x);
			
			Pnt.setAntiAlias(true);
			textMaxValue = Integer.toString(mMaxTextValue[j]);			
			canvas.drawText(textMaxValue, x, y, Pnt);
		}
		
	}

}
