/*-----------------------------------------------------------------------------------------
  File:   DetectSwingThread.java

  Author: Jung Chang Su
  -----------------------------------------------------------------------------------------
  Copyright (C) 2012 SICS.
  
  Detect some critical points from one golf swing data such as 
  a Start, an End, a Max peak and a Min peak points.
      	
   (Not a minimum value of a swing)
    	      	  
    	         (Max Peak)
    	  		      /\
    	  		     /  \ 
    	  	        /    \          /\
    	  	       /      \        /  \
    	  0 ------/--------\------/----\--------/\/\/\/\/\--------
    	     (Start point)  \    /      \      /  (End point)
    	                     \  /        \    /
    	                      \/          \  /
    	  			       (Min Peak)      \/
    	                              Minimum point(Not consider)

  *----------------------------------------------------------------------------------------*/
package com.SwingAnalyzer;

import java.util.ArrayList;

import android.os.*;
import android.util.Log;

public class DetectSwingThread extends Thread{
	final static int PEAK_DETECTION_FAIL = -1;
	final static int PEAK_DETECTION_SUCCESS = 0;
	/*
	 *	Message ID for X-axis 
	 */
	final static int MSG_DETECT_X 		= 0x01;
	final static int MSG_PEAK_X_MAX		= 0x02;
	final static int MSG_PEAK_X_MIN		= 0x03;
	final static int MSG_IMPACT_X 		= 0x04;
	final static int MSG_DETECT_DONE_X 	= 0x05;
	
	/*
	 * Message ID for Y-axis
	 */
	final static int MSG_DETECT_Y 		= 0x10;
	final static int MSG_PEAK_Y_MAX 	= 0x20;
	final static int MSG_PEAK_Y_MIN		= 0x30;
	final static int MSG_IMPACT_Y		= 0x40;
	final static int MSG_DETECT_DONE_Y	= 0x50;
	
	final static int MSG_START_POINT	= 0x100;
	final static int MSG_END_POINT		= 0x200;

	final static int MSG_DETECT_DONE_ALL	= 0x300;
	final static int MSG_BELOW_THRESHOLD_X	= 0x400;
	final static int MSG_BELOW_THRESHOLD_Y	= 0x500;


	/*
	 * X-axis Threshold Value
	 */
	final static int X_THRESHOLD_MAX = 10;
	final static int X_THRESHOLD_MIN = -10;

	/*
	 * Y-axis Threshold Values
	 */
	final static int Y_THRESHOLD_MAX = 5;
	final static int Y_THRESHOLD_MIN = -10;

	final static int END_CRITERION = 4;
	
	final static int DETECT_FAIL = -1;
	/*
	 * The selected axis for analysis and detection 
	 */
	final static int X_AXIS_DETECTION = 0;
	final static int Y_AXIS_DETECTION = 1;
	
	
	final static int NEW_METHOD = 1;
	/*
	 * Member Variables
	 */
	Handler mHandler;
	ArrayList<AccelerationData> mSwingArrayList = null;
	
	int mXThresholdMax = 0;
	int mYThresholdMin = 0;
	
	int mCount;
	int mStartPointIndex, mMaxPeakIndex, mMinPeakIndex, mEndPointIndex;
	int mMaxPeakTimestamp, mMinPeakTimestamp;
	int mStartTimestamp, mEndTimestamp;
	
	int mType = 0;
	boolean mIsAboveThresholdX;
	boolean mIsAboveThresholdY;
	
	public DetectSwingThread(Handler handler, ArrayList<AccelerationData>array) 
	{		
		mHandler = handler;
		
		mSwingArrayList = new ArrayList<AccelerationData>();		
		mSwingArrayList = array;
		
		initMemberVariables();
	}

	public DetectSwingThread(Handler handler, ArrayList<AccelerationData>array, 
							int maxThreshold, int minThreshold)
	{
		mHandler = handler;
		
		mSwingArrayList = new ArrayList<AccelerationData>();
		mSwingArrayList = array;
		
		mXThresholdMax = maxThreshold;
		mYThresholdMin = minThreshold;
		Log.i("detectswing", "X_Threshold=" + mXThresholdMax + ", Y_Threshold=" + mYThresholdMin);
		
		initMemberVariables();
	}

	public DetectSwingThread(Handler handler, ArrayList<AccelerationData>array, 
							int maxThreshold, int minThreshold, int type)
	{
		mHandler = handler;
		
		mSwingArrayList = new ArrayList<AccelerationData>();
		mSwingArrayList = array;
		
		mXThresholdMax = maxThreshold;
		mYThresholdMin = minThreshold;
		
		mType = type;
		Log.i("detectswing", "X_Threshold=" + mXThresholdMax + ", Y_Threshold=" + mYThresholdMin);
		
		initMemberVariables();
	}

	private void initMemberVariables()
	{
		mCount = 0;
		
		mStartTimestamp = mEndTimestamp = 0;
		mStartPointIndex = mMaxPeakIndex = mMinPeakIndex = mEndPointIndex = 0;
		mMaxPeakTimestamp = mMinPeakTimestamp = 0;
		
		mIsAboveThresholdX = false;
		mIsAboveThresholdY = false;
	}
	
	public void run()
	{	
		detectAllPeakPoints();
	}
	/*=============================================================================
	 * Name: detectAllPeakPoints
	 * 
	 * Description:
	 * 		When the DetectImpactThread starts, this function executes first.		
	 *    	Detect 4 points from a golf swing (Not considered a minimum value of a swing)
	 *    	- a Start point
	 *    	- a End point
	 *    	- a Positive Peak point
	 *    	- a Negative Peak point
	 *    	
    	      	  
    	         (Max Peak)
    	  		      /\
    	  		     /  \ 
    	  	        /    \          /\
    	  	       /      \        /  \
    	  0 ------/--------\------/----\--------/\/\/\/\/\--------
    	     (Start point)  \    /      \      /  (End point)
    	                     \  /        \    /
    	                      \/          \  /
    	  			       (Min Peak)      \/
    	                              Minimum point(Not consider)

	 * Return:
	 * 		None
	 *=============================================================================*/
	public void detectAllPeakPoints()
	{
		int result = 0;
		/*===========================================================================
		 *  Detecting critical points of X-axis data
		 *  1. Using threshold 				=> detectXPointsUsingThreshold()
		 *  2. Using Maximum/Minimum values => detectXPointsUsingMaxMin()
		 *===========================================================================*/
		result = detectXPointUsingThreshold();
		if(result == PEAK_DETECTION_FAIL)
		{
			sendMessageToHandler(MSG_DETECT_DONE_ALL, -1, -1);
			return;
		}
		
		//detectXPointsUsingMaxMin();
		
		try {
			Thread.sleep(10);
		} catch(InterruptedException e) {
			System.out.println(e.getMessage());
		}
		
		/*
		 *  Detection of Y-axis data
		 */
		
		//detectYPointsUsingThreshold();
		detectYPointsUsingMaxMin();
		
		sendMessageToHandler(MSG_DETECT_DONE_ALL, 0, 0);
	}

	
	/*=============================================================================
	 * Name: detectXPointsUsingThreshold
	 * 
	 * Description:
	 * 		Detect 4 critical points from an X-axis data with the defined threshold
	 * 		- X_THRESHOLD_MAX = 10;
	 * 		- X_THRESHOLD_MIN = -10;
	 * 
	 * Return:
	 * 		None
	 *=============================================================================*/   
	public int detectXPointUsingThreshold()
	{
	    
		mMaxPeakIndex = detectXMaxPeakPoint();
		if(mMaxPeakIndex == PEAK_DETECTION_FAIL)
		{
			return PEAK_DETECTION_FAIL;
		}
		else
		{
			mMaxPeakTimestamp = mSwingArrayList.get(mMaxPeakIndex).mTimestamp;
			
			/*=============================================================================
	    	 * Detect a minimum peak point from a swing data (Not a minimum value of a swing)
	    	 *============================================================================*/
	    	mMinPeakIndex = detectXMinPeakPoint(mMaxPeakIndex);
	    	mMinPeakTimestamp = mSwingArrayList.get(mMinPeakIndex).mTimestamp;
	    	/*=======================================================================
	    	 * Detect a start point from a swing data
	    	 *=======================================================================*/
	    	if(mType == NEW_METHOD)
	    	{
	    		mStartPointIndex = findSwingStartPoint1(mMaxPeakIndex);
		    	if(mStartPointIndex != -1)
		    	{
		    		mStartTimestamp = mSwingArrayList.get(mStartPointIndex).mTimestamp;
		    		sendMessageToHandler(MSG_START_POINT, mStartPointIndex, mStartTimestamp);
		    	}
		    	else
		    	{
		    		sendMessageToHandler(MSG_START_POINT, -1, -1);
		    	}

	    	}
	    	else
	    	{
		    	mStartPointIndex = findSwingStartPoint(mMaxPeakIndex);
		    	if(mStartPointIndex != -1)
		    	{
		    		mStartTimestamp = mSwingArrayList.get(mStartPointIndex).mTimestamp;
		    		sendMessageToHandler(MSG_START_POINT, mStartPointIndex, mStartTimestamp);
		    	}
		    	else
		    	{
		    		sendMessageToHandler(MSG_START_POINT, -1, -1);
		    	}
	    	}
	    	/*=======================================================================
	    	 * Send MSG_PEAK_X_MAX, MSG_PEAK_X_MIN message to a handler
	    	 *=======================================================================*/    	
	    	sendMessageToHandler(MSG_PEAK_X_MAX, mMaxPeakIndex, mMaxPeakTimestamp);
	    	sendMessageToHandler(MSG_PEAK_X_MIN, mMinPeakIndex, mMinPeakTimestamp);
	    	
	    	/*=======================================================================
	    	 * Detect an end point from a swing data
	    	 *=======================================================================*/
	    	mEndPointIndex = findSwingEndPoint(mMaxPeakIndex);
	    	if(mEndPointIndex != -1)
	    	{
	    		mEndTimestamp = mSwingArrayList.get(mEndPointIndex).mTimestamp;
	    		sendMessageToHandler(MSG_END_POINT, mEndPointIndex, mEndTimestamp);
	    	}
	    	else
	    	{
	    		// Return the end point of a swing
	    		//mEndPointIndex = mSwingArrayList.get(arrSize).
	    		sendMessageToHandler(MSG_END_POINT, -1, -1);
	    	}
		}
		
		return PEAK_DETECTION_SUCCESS;
	}
	
	/*=============================================================================
	 * Name: detectXPointsUsingMaxMin
	 * 
	 * Description:
	 * 		Detect the maximum and minimum points from the X-axis data 
	 * 
	 * Return:
	 * 		None
	 *=============================================================================*/	
	public void detectXPointsUsingMaxMin()
	{
		float maxValue, minValue;
		float x = 0;
		int maxIndex, minIndex;
		int maxTimestamp, minTimestamp;
		int i = 0;
		int timestamp = 0;
		int arrSize = 0;
		
		maxValue = minValue = 0;
		maxIndex = minIndex = 0;
		maxTimestamp = minTimestamp = 0;
		
		arrSize = mSwingArrayList.size();
		
		do
		{
			x = mSwingArrayList.get(i).mXvalue;
			timestamp = mSwingArrayList.get(i).mTimestamp;
			
			if(x >= maxValue)
			{
				maxValue = x;
				maxIndex = i;
				maxTimestamp = timestamp;
			}
			
			if(x <= minValue)
			{
				minValue = x;
				minIndex = i;
				minTimestamp = timestamp;
			}
			
			i++;
		}while(i< arrSize);
		
		Log.i("detectmax", "Max index:" + maxIndex 
										+ ", T:" + maxTimestamp 
										+ ", X:" + maxValue);
		
		Log.i("detectmax", "Min index:" + minIndex 
										+ ", T:" + minTimestamp
										+ ", X:" + minValue);
		
		mMaxPeakIndex = maxIndex; 
		mMaxPeakTimestamp = maxTimestamp;
		
		mMinPeakIndex = minIndex; 
		mMinPeakTimestamp = minTimestamp;

    	/*=======================================================================
    	 * Detect a start point from a swing data
    	 *=======================================================================*/
    	mStartPointIndex = findSwingStartPoint(mMaxPeakIndex);
    	if(mStartPointIndex != -1)
    	{
    		mStartTimestamp = mSwingArrayList.get(mStartPointIndex).mTimestamp;
    		sendMessageToHandler(MSG_START_POINT, mStartPointIndex, mStartTimestamp);
    	}
    	else
    	{
    		sendMessageToHandler(MSG_START_POINT, -1, -1);
    	}
    	
		sendMessageToHandler(MSG_PEAK_X_MAX, maxIndex, maxTimestamp);
		sendMessageToHandler(MSG_PEAK_X_MIN, minIndex, minTimestamp);

    	/*=======================================================================
    	 * Detect an end point from a swing data
    	 *=======================================================================*/
    	mEndPointIndex = findSwingEndPoint(mMaxPeakIndex);
    	if(mEndPointIndex != -1)
    	{
    		mEndTimestamp = mSwingArrayList.get(mEndPointIndex).mTimestamp;
    		sendMessageToHandler(MSG_END_POINT, mEndPointIndex, mEndTimestamp);
    	}
    	else
    	{
    		sendMessageToHandler(MSG_END_POINT, -1, -1);
    	}		
	
	}
	
	/*=============================================================================
	 * Name: detectXMaxPeakPoint
	 * 
	 * Description:
	 * 		Detect a maximum peak point from X-axis data
	 * Return:
	 * 		None
	 *=============================================================================*/	
	public int detectXMaxPeakPoint()
	{
		int i = 0;		
		int x1, x2, x3;
		
		int arrSize = 0;
		int maxIndex = 0;
		int maxValue = 0;
		boolean isPeakFound = false;
		
		x1 = x2 = x3 = 0;
		
		arrSize = mSwingArrayList.size();

		do
    	{
    		
    		x2 = getRoundDownValue(mSwingArrayList.get(i).mXvalue);        		
    		if(i> 0)
    			x1 = getRoundDownValue(mSwingArrayList.get(i-1).mXvalue);
    		if(i < arrSize-1)
    			x3 = getRoundDownValue(mSwingArrayList.get(i+1).mXvalue);

    		if((x2 > mXThresholdMax))
    		{
    			if(x2 >= maxValue)
    			{
    				maxValue = x2;
    				maxIndex = i;
    			}
    			
				if(x2 == x3)
				{	
					i++;					
					continue;
				}    
				
    			if((x2 > x3) && (x2 >= x1))
    			{
    				// In some case, the peak values are the same in the different timestamp. 
    				if(x2 >= maxValue)
    				{
    					isPeakFound = true;    
    					maxValue = x2;    					    
    					maxIndex = i;
    					Log.i("detectswing", "isPeakFound ["+i+"]" 
    										+ " x1=" + x1 + ", x2=" + x2 + ", x3=" + x3);
    				}
    			}        			

    		}
    		
    		if((isPeakFound == true) && (x2 >= 0) && (x3 < 0))
    		{
   				Log.i("detectswing", "Transition Point=" + i + ", x2=" + x2 + ", x3=" + x3);
   				break;
    		}

    		i++;
    	}while(i<= (arrSize-1));
    	
    	Log.i("detectswing", "Max Peak point[" + maxIndex +"]:" + maxValue 
    						+ ", isPeakFound=" + isPeakFound);
    	if(isPeakFound == false)
    	{
    		maxIndex = PEAK_DETECTION_FAIL;
    		Log.e("detectswing", "PEAK_DETECTION_FAIL");
    	}
    	
    	return maxIndex;
	}
	
	/*=============================================================================
	 * Name: detectXMinPeakPoint
	 * 
	 * Description:
	 * 		Detect a minimum peak point from X-axis data
	 * Return:
	 * 		None
	 *=============================================================================*/
	public int detectXMinPeakPoint(int peakIndex)
	{
		int i=peakIndex;		
		int minIndex = 0;
		int minValue = 0;
		
		int x1, x2, x3;
		int arrSize = mSwingArrayList.size();
		
		x1 = x2 = x3 = 0;
		
    	do
    	{
    		mCount++;
    		
    		x2 = getRoundDownValue(mSwingArrayList.get(i).mXvalue);        		
    		if(i> 0)
    			x1 = getRoundDownValue(mSwingArrayList.get(i-1).mXvalue);
    		if(i < arrSize-1)
    			x3 = getRoundDownValue(mSwingArrayList.get(i+1).mXvalue);
    
    		/*****************************************
    		 *  Step 5: The negative peak point (value < 0)
    		 *  
    		 *  	1) (x1 < 0) && (x2 < 0) && ((x3 < 0)
    		 *   	2) (x1 >= x2) && (x2 <= x3)
    		 *****************************************/       			
    		if((x2 < 0) && (x1 < 0) && (x3 < 0))
    		{
   			
    			if(x2 <= minValue)
    			{
    				minValue = x2;
    				minIndex = i;
    			}
    			
    			if(x2 == x3)
    			{
    				i++;
    				continue;
    			}
    			
    		}
    		
			if((x2<0) && (x3 >=0))
			{
				Log.i("detectswing", "Transition Point[" + i + "] x2=" + x2 + " x3=" + x3);
				break;
			}

    		i++;
    		
    	}while(i <= (arrSize-1));
		
		Log.i("detectswing", "Min Peak point[" + minIndex +"]:" + minValue);

		return minIndex;
	}
	
	// Y-AXIS
	/*=============================================================================
	 * Name: detectYPointsUsingThreshold
	 * 
	 * Description:
	 * 		Detect peak point from the golf swing Y-axis data with threshold
	 * 		- Y_THRESHOLD_MAX = 5
	 * 		- Y_THRESHOLD_MIN = -10
	 * Return:
	 * 		None
	 *=============================================================================*/    				
	public void detectYPointsUsingThreshold()
	{
    	boolean isFound = false;
    	float y1, y2, y3;
    	int i=0;
    	int impactIndex = 0;
    	int size;
    	boolean isIncreasing, isDecreasing, isPeakFound, isZero;
    	int timestamp = 0;
    	
    	// For detecting the positive peak point
    	float maxValue = 0;
    	int maxIndex = 0;
    	
    	// For detecting the negative peak point
    	float minValue = 0;
    	int minIndex = 0;
    	
    	int negativePeakTimestamp = 0;
    	int positivePeakTimestamp = 0;
    	
    	y1 = y2 = y3 = 0;
    	
    	size = mSwingArrayList.size();
    	isIncreasing = isDecreasing = isPeakFound = isZero = false;  
    	
    	mCount = 0;
    	
    	/*********************************************************  
    	 *  
    	 * 	    Y-axis Analysis
    	 * 	                     Step 4
    	 * 	5 ---------------------/\----- THRESHOLD_HI(5)
    	 * 	                      /  \
    	 *  0 -----------\-------/----\--- 
    	 *          Step1 \    _/Step 3
    	 * -10 ------------\--/---------   THRESHOLD_LOW (-10)
    	 *                  \/    
    	 *                Step 2
    	 *********************************************************/
    	do
    	{
    		mCount++;
    		timestamp = (int)mSwingArrayList.get(i).mTimestamp;
    		
    		y2 = mSwingArrayList.get(i).mYvalue;        		
    		if(i> 0)
    			y1 = mSwingArrayList.get(i-1).mYvalue;
    		if(i < size-1)
    			y3 = mSwingArrayList.get(i+1).mYvalue;
    		
    		if((y2 < Y_THRESHOLD_MIN) && (i < size-1))
    		{
        		/*****************************************
        		 *  Step 1: Decreasing point
        		 *  
        		 *  	1) value < THRESHOLD_LOW(-10)
        		 *   	2) y1 > y2 > y3 (Not equal condition among y1, y2 and y3) 
        		 *****************************************/
    			if((y1 >= y2) && (y2 >= y3))	// Not equal condition
    			{
    				isDecreasing = true;
        			if(y3 < minValue)
        			{
            			minValue = y3;
            			minIndex = i;
            			
        				Log.i("DetectY", "MinValue T:" + timestamp + ", minValue: " + minValue 
        						+ ", y2:" + y2 + ", y1:" + y1);
        				        				
        			}

    			}
    		
        		/*****************************************
        		 *  Step 2: The negative peak point
        		 *  
        		 *  	1) y2 < THRESHOLD_LOW
        		 *   	2) (y2 <= y3) && (y2 <= y1)
        		 *   	 
        		 *****************************************/
    			if((y2 <= y3) && (y2 <= y1))
    			{
        			// If the same values exist, skip and continue the loop
    				if(y2 == y3)
    				{	
    					i++;
    					continue;
    				}    			
    			}
    			// The same values exist(between y[i] and y[i-1].
    			if((y2 < y3) && (y2 <= y1))
    			{
    				if(y2 <= minValue)
    				{
    					isPeakFound = true;	// or Impact point (??): changsu
    					//mNegativePeakCount++;
    					//mImpactCount++;
    					
    					//mYImpactPointArray.add(mSwingArrayList.get(i));
    					
    					Log.i("DetectY", "Negative Peak Time: " + mSwingArrayList.get(i).mTimestamp
    							+ ", y: " + y2 + ", Min:" + minValue);
    					    
    					minValue = y2;
    					minIndex = i;        					
    					negativePeakTimestamp = (int)mSwingArrayList.get(i).mTimestamp;    					
    					sendMessageToHandler(MSG_PEAK_Y_MIN, minIndex, negativePeakTimestamp);
    					
    				}
    			}        			
    		}	
    		// Step 3
    		/*****************************************
    		 *  Step 3: The increasing point (value > 0)
    		 *  
    		 *  	1) (y2 >= 0) && ((y3 >= 0) || (x3 < 0))
    		 *   	2) (y2 > y1) && (y3 > y2)
    		 *****************************************/       			
    		if((isPeakFound) && (y2 > Y_THRESHOLD_MAX))
    		{
  			
    			if((y3 >= y2) && (y2 >= y1))        				
    			{
    				isIncreasing = true;
    				if(y3 > maxValue)
    				{
    					maxValue = y3;
    					maxIndex = i;
    					Log.i("DetectY", "MaxValue T:" + timestamp + "maxValue: " + y3);
    				}
    			}
    			
    			// For the positive peak point
    			if((y2 >= y3) && (y2 >= y1))
    			{
    				if(y2 == y3)
    				{	i++;
    					continue;
    				}
    			}
        			// The same values exist.
        		if((y2 > y3) && (y2 >= y1))
        		{
        		 
        			if(y2 >= maxValue)
        			{
        				//isPeakFound = true;
        				//mPositivePeakCount++;
        				
        				Log.i("DetectY", "Positive Peak Time: " + mSwingArrayList.get(i).mTimestamp
        							+ ", y: " + y2 + ", Max:" + maxValue);
        				    
        				maxValue = y2;
        				maxIndex = i;        					
        				positivePeakTimestamp = (int)mSwingArrayList.get(i).mTimestamp;
        				sendMessageToHandler(MSG_PEAK_Y_MAX, maxIndex, positivePeakTimestamp);
        				
            			minValue = 0;
            			minIndex = 0;
            			maxValue = 0;
            			maxIndex = 0;
            			isPeakFound = false;

        			}
        		}        			
        	}	
    		    		
			try 
			{
				Thread.sleep(1);
			} 
			catch(InterruptedException e)
			{
				System.out.println(e.getMessage());
			}
			
			// To display the counter processed 
			sendMessageToHandler(MSG_DETECT_Y, mCount, 0);
    		i++;
    		
    	}while(i <= size-1);
    	
    	

	}

	/*=============================================================================
	 * Name: detectYPointsUsingMaxMin
	 * 
	 * Description:
	 * 		Detect the maximum and minimum points from the Y-axis data 
	 * 
	 * Return:
	 * 		None
	 *=============================================================================*/		
	public void detectYPointsUsingMaxMin()
	{
		float maxValue, minValue;
		float y = 0;
		int maxIndex, minIndex;
		int maxTimestamp, minTimestamp;
		int i = 0;
		int timestamp = 0;
		int arrSize = 0;
		
		maxValue = minValue = 0;
		maxIndex = minIndex = 0;
		maxTimestamp = minTimestamp = 0;
		
		
		arrSize = mSwingArrayList.size();
		
		do
		{
			
			y = mSwingArrayList.get(i).mYvalue;
			timestamp = mSwingArrayList.get(i).mTimestamp;
			
			if(y >= maxValue)
			{
				maxValue = y;
				maxIndex = i;
				maxTimestamp = timestamp;
			}
			
			if(y <= minValue)
			{
				minValue = y;
				minIndex = i;
				minTimestamp = timestamp;
			}
			
			i++;
		}while(i< arrSize);
		
		Log.i("detectswing", "Max index:" + maxIndex 
										+ ", T:" + maxTimestamp 
										+ ", Y:" + maxValue);
		
		Log.i("detectswing", "Min index:" + minIndex 
										+ ", T:" + minTimestamp
										+ ", Y:" + minValue);
		
		mIsAboveThresholdY = true;
		mMaxPeakIndex = maxIndex; mMaxPeakTimestamp = maxTimestamp;
		mMinPeakIndex = minIndex; mMinPeakTimestamp = minTimestamp;
		
		sendMessageToHandler(MSG_PEAK_Y_MIN, minIndex, minTimestamp);
		sendMessageToHandler(MSG_PEAK_Y_MAX, maxIndex, maxTimestamp);

	}
	
	/*=============================================================================
	 * Name: findSwingStartPoint
	 * 
	 * Description:
	 * 		Find a start point from the peak in reverse direction    	 
	 * 
	 *  A start point can be the first point which the Math.floor(x[peak_point--]) is
	 *  zero from the peak point.
	 * 
	 * Return:
	 * 		int
	 *=============================================================================*/
	public int findSwingStartPoint(int peakIndex)
	{
		int startIndex = 0;
		int i=0;
		int timestamp = 0;		 
		float x = 0;
		boolean isFound = false;
		
    	/*********************************************************  
    	 *         Peak point
    	 * 		      /\
    	 * 		     /  \  
    	 * 	        /    \
    	 * 	Start  /      \
    	 * --____@/--------\------/- 
    	 *                  \    /
    	 *          	     \  /
    	 *                    \/
    	 *                    
    	 * A start point can be the first point which the Math.floor(x[peak_point--]) is 
    	 * zero from the peak point.
    	 *********************************************************/
		Log.i("detectswing", "findSwingStartPoint() peakIndex=" + peakIndex);
		
		i = peakIndex;
		
		do{
			
    		timestamp = (int)mSwingArrayList.get(i).mTimestamp;
    		
    		x = mSwingArrayList.get(i).mXvalue;        		
    		
    		double d = Math.floor((double)x);
    		
    		//Log.i("detectswing", "[" + i + "] d=" + d + ", Time=" + timestamp);
    		
    		if((x > 0) && (d==0))
    		{		
    			isFound = true;
    			startIndex = i;
    			Log.i("detectswing", "Start Index:" + startIndex + ", Time:" + timestamp);
    			break;    		
    		}
    		
    		i--;
		}while(i> 0);
		
		if(isFound)
		{
			return startIndex;
		}
		else
		{
			// If not found a start point, then return the first index
			return 0;
		}
	}
	/*=============================================================================
	 * Name: findSwingStartPoint1
	 * 
	 * Description:
	 * 		Find a start point from the peak in reverse direction    	 
	 * 
	 *  	The function starts to search a start point from the beginning.
	 *  	It will choose the first point when x value is greater than 0 for the first time.
	 * 
	 * Return:
	 * 		int
	 *=============================================================================*/
	public int findSwingStartPoint1(int peakIndex)
	{
		int startIndex = 0;
		int i=0;
		int timestamp = 0;		 
		float x = 0;
		boolean isFound = false;
		int positiveCount = 0;
    	/*********************************************************  
    	 *         Peak point
    	 * 		      /\
    	 * 		     /  \  
    	 * 	        /    \
    	 * 	Start  /      \
    	 * --____@/--------\------/- 
    	 *                  \    /
    	 *          	     \  /
    	 *                    \/
    	 *                    
    	 * A start point can be the first point which the Math.floor(x[peak_point--]) is 
    	 * zero from the peak point.
    	 *********************************************************/
		Log.i("detectswing", "findSwingStartPoint1()");
		
		//i = peakIndex;		
		
		for(i = 0; i< mSwingArrayList.size(); i++)
		{
			
    		timestamp = (int)mSwingArrayList.get(i).mTimestamp;
    		
    		x = mSwingArrayList.get(i).mXvalue;        		
    		
    		int d = (int)Math.ceil((double)x);
    		
    		Log.i("detectswing", "Ceil [" + i + "] d=" + d + ", Time=" + timestamp);
    		
    		if((x > 0) && (d > 0))
    		{
    			positiveCount++;
    			
    			if(positiveCount > 1)
    			{
    				isFound = true;
    				startIndex = i;
    				Log.i("detectswing", "Start Index:" + startIndex + ", Time:" + timestamp);
    				break;
    			}	
    		}
    		else
    		{
    			positiveCount = 0;
    		}
		}
		
		if(isFound)
		{
			return startIndex;
		}
		else
		{
			// If not found a start point, then return the first index
			return 0;
		}
		
	}
	/*=============================================================================
	 * Name: findSwingEndPoint
	 * 
	 * Description:
	 * 		Find a end point from the peak    	 
	 * 
	 *		A end point can be the points 
	 *		which (Math.floor(x[i])==0) && (Math.floor(y[i])==0) at least 5 times in series.
	 *     
	 * Return:
	 * 		None
	 *=============================================================================*/	
	public int findSwingEndPoint(int peakIndex)
	{
		int endIndex = 0;
		int i=0;
		int count = 0;
		int timestamp = 0;
		int arrSize = 0; 
		float x, y;
		int zero_count = 0;
		boolean isFoundEnd = false;
		int dx, dy;
		
		x = y = 0;
		dx = dy = 0;
		
		arrSize = mSwingArrayList.size();
    	/*********************************************************  
    	 *         Peak point
    	 * 		      /\
    	 * 		     /  \  
    	 * 	        /    \
    	 * 	Start  /      \        End point (X[i]==0 && Y[i]==0 for 5 timestamps)
    	 * --____@/--------\-------______ 
    	 *                  \    /
    	 *          	     \  /
    	 *                    \/
    	 *                    
    	 * A end point can be the points which (Math.floor(x[i])==0) && (Math.floor(y[i])==0)
    	 * at least 5 times in series.
    	 * 
    	 *********************************************************/
		//i = peakIndex;
		Log.i("detectswing", "findSwingEndPoint: " + i + ", size=" + arrSize);
		
	
		for(count = peakIndex; count< mSwingArrayList.size(); count++)
		{
			
    		timestamp = mSwingArrayList.get(count).mTimestamp;
    		
    		x = mSwingArrayList.get(count).mXvalue;
    		y = mSwingArrayList.get(count).mYvalue;
    		
    		if(x > 0)	// 내림
    			dx = (int)Math.floor((double)x);
    		else		// 올림
    			dx = (int)Math.ceil((double)x);
    		
    		if(y > 0)
    			dy = (int)Math.floor((double)y);
    		else
    			dy = (int)Math.ceil((double)y);
    		/*
    		Log.i("detectswing", "Finding End [" + count + "] " + "T:" + timestamp
    							+ ", dx=" + dx + "/" + x 
    							+ ", dy=" + dy + "/" + y);
    		*/
    		if((dx == 0) && (dy == 0))
    		{
    			++zero_count;
    			Log.i("detectswing", "Zero [" + zero_count + "]" + " dx=" + dx + ", dy=" + dy);
    			
    			if(zero_count == END_CRITERION)
    			{
    				endIndex = count-(END_CRITERION-1);
    				timestamp = (int)mSwingArrayList.get(endIndex).mTimestamp;
    				isFoundEnd = true;    				
    				Log.i("detectswing", "End Index:" + endIndex + ", Time:" + timestamp + ", i:" + i);
    				break;
    			}
    		}
    		else
    		{
    			zero_count = 0;
    		}
    		   
		}


		if(isFoundEnd == false)
		{
			//endIndex = -1;
			endIndex = arrSize-1;	// The last index of mSwingArrayList
		}
			
		return endIndex;
	}
	
	/*=============================================================================
	 * Name: getRoundDownValue
	 * 
	 * Description:
	 * 		Remove decimal point values for making data smooth	
	 * 
	 * Return:
	 * 		int
	 *=============================================================================*/    	
	public int getRoundDownValue(float x)
	{
		int value = 0;
		
		if(x > 0)
			value = (int)Math.floor((double)x);
		else
			value = (int)Math.ceil((double)x);
		
		return value;
	}	
	
	/*=============================================================================
	 * Name: sendMessageToHandler
	 * 
	 * Description:
	 * 		Send a message to a Handler for printing some results		
	 * 
	 * Return:
	 * 		None
	 *=============================================================================*/    
    public void sendMessageToHandler(int id, int arg1, int arg2)
    {
    	Message msg = Message.obtain();
    	msg.what = id;
    	msg.arg1 = arg1;
    	msg.arg2 = arg2;
    	
    	mHandler.sendMessage(msg);
    }

    
    ////////////////////////////////////////////////////////////////////////////////////
	// Old version's function
	public void detectXPeakPointUsingThreshold1()
	{
		int i = 0;
		
		float x1, x2, x3;
		int dx1, dx2, dx3;
		int timestamp = 0;
		int arrSize = 0;
		int maxIndex = 0;
		int minIndex = 0;
		float maxValue, minValue;
		boolean isPeakFound = false;
		
		x1 = x2 = x3 = 0;
		dx1 = dx2 = dx3 = 0;
		
		maxValue = minValue = 0;
		
		mCount = 0;
		arrSize = mSwingArrayList.size();
		
		
    	/*********************************************************  
    	 *         Step2
    	 * 		      /\
    	 * 		     /  \  Step 3
    	 * 	        /    \
    	 * 	Step 1 /      \
    	 *0 ---------------\------/- 
    	 *                  \    /
    	 *            Step4  \  /
    	 *                    \/
    	 * 					Step 5
    	 *********************************************************/
    	do
    	{
    		mCount++;
    		timestamp = mSwingArrayList.get(i).mTimestamp;
    		
    		x2 = mSwingArrayList.get(i).mXvalue;        		
    		if(i> 0)
    			x1 = mSwingArrayList.get(i-1).mXvalue;
    		if(i < arrSize-1)
    			x3 = mSwingArrayList.get(i+1).mXvalue;
    		

    		if((x2 > X_THRESHOLD_MAX) && (i < arrSize-1))
    		{
        		/*****************************************
        		 *  Step 1: Increasing point
        		 *  
        		 *  	1) value > IMPACT_HI_VALUE
        		 *   	2) x3 > x2 > x1
        		 *****************************************/
    			
    			if((x3 >= x2) && (x2 >= x1))
    			{
    				
        			if(x3 > maxValue)
        			{
            			maxValue = x3;
            			maxIndex = i;
            			/*
        				Log.i("Debug", "T:" + timestamp + ", maxValue: " + maxValue 
        						+ ", x3:" + x3 + ", x2:" + x2 + ", x1:" + x1);
        				*/
        			}

    			}
        		/*****************************************
        		 *  Step 2: The peak point
        		 *  
        		 *  	1) x2 > IMPACT_HI_VALUE
        		 *   	2) (x2 > x3) && (x2 > x1)
        		 *****************************************/   
    			if((x2 >= x3) && (x2 >= x1))
    			{
    				if(x2 == x3)
    				{	i++;
    					continue;
    				}    				
    			}
    			// The same values exist.
    			if((x2 > x3) && (x2 >= x1))
    			{
    				// In some case, the peak values are the same in the different timestamp. 
    				if(x2 >= maxValue)
    				{
    					isPeakFound = true;
    					
    					Log.i("detectswing", "Peak Time: " + mSwingArrayList.get(i).mTimestamp
    							+ ", x: " + x2 + ", Max:" + maxValue);
    					    
    					maxValue = x2;
    					    
    					mMaxPeakIndex = i;
    					mMaxPeakTimestamp = (int)mSwingArrayList.get(i).mTimestamp;
    				}
    			}        			
    		}	
    		// Step 3
    		/*****************************************
    		 *  Step 3: The decreasing point (value > 0)
    		 *  
    		 *  	1) (x2 >= 0) && ((x3 >= 0) || (x3 < 0))
    		 *   	2) (x1 > x2) && (x2 > x3)
    		 *****************************************/       			
    		if((isPeakFound) && (x2 >= 0))
    		{
    			
        		/*****************************************
        		 *  Step 4: The decreasing point (value < 0)
        		 *  
        		 *  	1) (x1 >= 0) && (x2 >= 0) && ((x3 <= 0)
        		 *   	2) (x1 > x2) && (x2 > x3)
        		 *****************************************/
    			if((x2 >= 0) && (x3 <= 0))
    			{
        			
        			//isPeakFound = false;
        			
        			Log.i("detectswing", "MSG_PEAK_X: " + mMaxPeakIndex 
        									+ ", T:" + mMaxPeakTimestamp + ", X:" + maxValue);
        			//sendMessageToHandler(MSG_PEAK_X_MAX, mMaxPeakIndex, mMaxPeakTimestamp);        			
        			maxValue = 0;
        			maxIndex = 0;
    			}
    		}
    		/*****************************************
    		 *  Step 5: The negative peak point (value < 0)
    		 *  
    		 *  	1) (x1 < 0) && (x2 < 0) && ((x3 < 0)
    		 *   	2) (x1 >= x2) && (x2 <= x3)
    		 *****************************************/       			
    		if((isPeakFound) && (x2 < 0) && (x1 < 0) && (x3 < 0))
    		{
    			dx1 = (int)Math.round((double)x1);
    			dx2 = (int)Math.round((double)x2);
    			dx3 = (int)Math.round((double)x3);
    			
    			if(x2 <= minValue)
    			{
    				minValue = x2;
    				minIndex = i;
    			}

    			//if((dx1 >= dx2) && (dx2 < dx3))
    			if((dx1 >= dx2) && (dx2 > dx3))
    			{
    				if(x2 <= minValue)
    				{
    					minValue = x2;
    					minIndex = i;
    				}

    			}
    			if((dx1 > dx2) && (dx2 < dx3))
    			{
    				
    				mMinPeakIndex = i;
    				mMinPeakTimestamp = (int)mSwingArrayList.get(i).mTimestamp;
    				
        			Log.i("detectswing", "MSG_PEAK_X_MIN: " + mMinPeakIndex 
        									+ ", T:" + mMinPeakTimestamp + ", X:" + x2);
        			//sendMessageToHandler(MSG_PEAK_X_MIN, mMinPeakIndex, mMinPeakTimestamp);        			
        			
        			break;
        			
    			}
    			
    			if(dx2 == dx3)
    			{
    				i++;
    				continue;
    			}
    		}
    		
			try 
			{
				Thread.sleep(1);
			} 
			catch(InterruptedException e)
			{
				System.out.println(e.getMessage());
			}
			
			
    		i++;
    		
    	}while(i <= (arrSize-1));

    	if(isPeakFound == false)
    	{
    		Log.e("detectswing", "Peak points are not found.");
    		sendMessageToHandler(MSG_PEAK_X_MAX, -1, -1);
    		sendMessageToHandler(MSG_PEAK_X_MIN, -1, -1);
    	}	
	}

}
