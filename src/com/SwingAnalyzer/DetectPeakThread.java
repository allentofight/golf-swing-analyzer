package com.SwingAnalyzer;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class DetectPeakThread extends Thread{

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
	
	final static int MSG_DETECT_DONE_ALL	= 0x111;
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

	/*
	 * The selected axis for analysis and detection 
	 */
	final static int X_AXIS_DETECTION = 0;
	final static int Y_AXIS_DETECTION = 1;
	
	/*
	 * Member variables
	 */
	Handler mHandler;
	String mInputFileName = "";
	ArrayList<AccelerationData> mSwingArrayList = null;

	
	int mMaxPeakIndex;
	int mMinPeakIndex;
	
	int mMaxPeakTimestamp;
	int mMinPeakTimestamp;
	
	int mAxis;	// X-axis or Y-axis
	int mCount;
	
	public DetectPeakThread(ArrayList<AccelerationData>array, Handler handler, int axis) 
	{		
		mHandler = handler;
		
		mAxis = axis;
		
		mMaxPeakIndex = 0;
		mMinPeakIndex = 0;
		
		mMaxPeakTimestamp = 0;
		mMinPeakTimestamp = 0;
		
		mCount = 0;
		
		mSwingArrayList = new ArrayList<AccelerationData>();
		
		mSwingArrayList = array;
	}

	public DetectPeakThread(ArrayList<AccelerationData>array, Handler handler) 
	{		
		mHandler = handler;
		
		mMaxPeakIndex = 0;
		mMinPeakIndex = 0;
		
		mMaxPeakTimestamp = 0;
		mMinPeakTimestamp = 0;
		
		mCount = 0;
		
		mSwingArrayList = new ArrayList<AccelerationData>();
		
		mSwingArrayList = array;
	}

	public void run()
	{
		//startDetectingPeakPoint();
		detectAllPeakPoints();
	}
	/*=============================================================================
	 * Name: detectAllPeakPoints
	 * 
	 * Description:
	 * 		When the DetectImpactThread starts, this function executes first.		
	 * 
	 * Return:
	 * 		None
	 *=============================================================================*/
	public void detectAllPeakPoints()
	{
		/*
		 *  Detection of X-axis data
		 */
		detectMaxMinFromX();
		sendMessageToHandler(MSG_DETECT_DONE_X, mMaxPeakIndex, mMinPeakIndex);
		
		try {
			Thread.sleep(10);
		} catch(InterruptedException e) {
			System.out.println(e.getMessage());
		}
		
		/*
		 *  Detection of Y-axis data
		 */
		detectMaxMinFromY();
		sendMessageToHandler(MSG_DETECT_DONE_Y, mMaxPeakIndex, mMinPeakIndex);
		
		try {
			Thread.sleep(10);
		} catch(InterruptedException e) {
			System.out.println(e.getMessage());
		}
		
		sendMessageToHandler(MSG_DETECT_DONE_ALL, 0, 0);
	}
	/*=============================================================================
	 * Name: startDetectingPeakPoint
	 * 
	 * Description:
	 * 		When the DetectImpactThread starts, this function executes first.		
	 * 
	 * Return:
	 * 		None
	 *=============================================================================*/     	  		
	public void startDetectingPeakPoint(int axis)
	{
		//clearArrayList();
		//readArrayListFromFile();	// changsu
		
		if(axis == X_AXIS_DETECTION)
		{
			//detectXPeakPoint();
			detectMaxMinFromX();
			sendMessageToHandler(MSG_DETECT_DONE_X, mMaxPeakIndex, mMinPeakIndex);
		}
		else
		{
			//detectYPeakPoint();
			detectMaxMinFromY();
			sendMessageToHandler(MSG_DETECT_DONE_Y, mMaxPeakIndex, mMinPeakIndex);
		}
		sendMessageToHandler(MSG_DETECT_DONE_ALL, 0, 0);
	}
	
	/*=============================================================================
	 * Name: detectMaxMinFromX
	 * 
	 * Description:
	 * 		Detect the maximum and minimum points from the X-axis data 
	 * 
	 * Return:
	 * 		None
	 *=============================================================================*/	
	public void detectMaxMinFromX()
	{
		float maxValue, minValue;
		float x1, x2, x3;
		int maxIndex, minIndex;
		int maxTimestamp, minTimestamp;
		int i = 0;
		int timestamp = 0;
		int size = 0;
		
		maxValue = minValue = 0;
		maxIndex = minIndex = 0;
		maxTimestamp = minTimestamp = 0;
		
		x1 = x2 = x3 = 0;
		
		size = mSwingArrayList.size();
		
		do
		{
			
			x2 = mSwingArrayList.get(i).mXvalue;
			timestamp = mSwingArrayList.get(i).mTimestamp;
			
			
			if(x2 >= maxValue)
			{
				maxValue = x2;
				maxIndex = i;
				maxTimestamp = timestamp;
			}
			
			if(x2 <= minValue)
			{
				minValue = x2;
				minIndex = i;
				minTimestamp = timestamp;
			}
			
			i++;
		}while(i<= size-1);
		
		Log.i("detectpeak", "Max index:" + maxIndex 
										+ ", T:" + maxTimestamp 
										+ ", X:" + maxValue);
		
		Log.i("detectpeak", "Min index:" + minIndex 
										+ ", T:" + minTimestamp
										+ ", X:" + minValue);
		
		mMaxPeakIndex = maxIndex; mMaxPeakTimestamp = maxTimestamp;
		mMinPeakIndex = minIndex; mMinPeakTimestamp = minTimestamp;

		sendMessageToHandler(MSG_PEAK_X_MAX, maxIndex, maxTimestamp);
		sendMessageToHandler(MSG_PEAK_X_MIN, minIndex, minTimestamp);
	}
	/*=============================================================================
	 * Name: detectMaxMinFromY
	 * 
	 * Description:
	 * 		Detect the maximum and minimum points from the Y-axis data 
	 * 
	 * Return:
	 * 		None
	 *=============================================================================*/		
	public void detectMaxMinFromY()
	{
		float maxValue, minValue;
		float y1, y2, y3;
		int maxIndex, minIndex;
		int maxTimestamp, minTimestamp;
		int i = 0;
		int timestamp = 0;
		int size = 0;
		
		maxValue = minValue = 0;
		maxIndex = minIndex = 0;
		maxTimestamp = minTimestamp = 0;
		
		y1 = y2 = y3 = 0;
		
		size = mSwingArrayList.size();
		
		do
		{
			
			y2 = mSwingArrayList.get(i).mYvalue;
			timestamp = mSwingArrayList.get(i).mTimestamp;
			
			
			if(y2 >= maxValue)
			{
				maxValue = y2;
				maxIndex = i;
				maxTimestamp = timestamp;
			}
			
			if(y2 <= minValue)
			{
				minValue = y2;
				minIndex = i;
				minTimestamp = timestamp;
			}
			
			i++;
		}while(i<= size-1);
		
		Log.i("detectpeak", "Max index:" + maxIndex 
										+ ", T:" + maxTimestamp 
										+ ", Y:" + maxValue);
		
		Log.i("detectpeak", "Min index:" + minIndex 
										+ ", T:" + minTimestamp
										+ ", Y:" + minValue);
		
		mMaxPeakIndex = maxIndex; mMaxPeakTimestamp = maxTimestamp;
		mMinPeakIndex = minIndex; mMinPeakTimestamp = minTimestamp;
		
		sendMessageToHandler(MSG_PEAK_Y_MIN, minIndex, minTimestamp);
		sendMessageToHandler(MSG_PEAK_Y_MAX, maxIndex, maxTimestamp);
	}
	/*=============================================================================
	 * Name: detectXPeakPoint
	 * 
	 * Description:
	 * 		Detect peak point from the golf swing X-axis data with the predefined threshold
	 * 		- X_THRESHOLD_MAX = 10;
	 * 		- X_THRESHOLD_MIN = -10;
	 * 
	 * Return:
	 * 		None
	 *=============================================================================*/    			
	public void detectXPeakPoint()
	{
		int i = 0;
		float x1, x2, x3;
		int timestamp = 0;
		int size = 0;
		int maxIndex = 0;
		float maxValue, minValue;
		boolean isIncreasing, isDecreasing, isPeakFound;
		
		x1 = x2 = x3 = 0;
		isIncreasing = isDecreasing = isPeakFound = false;
		maxValue = minValue = 0;
		
		mCount = 0;
		size = mSwingArrayList.size();
		
		
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
    	 * 
    	 *********************************************************/
    	do
    	{
    		mCount++;
    		timestamp = (int)mSwingArrayList.get(i).mTimestamp;
    		
    		x2 = mSwingArrayList.get(i).mXvalue;        		
    		if(i> 0)
    			x1 = mSwingArrayList.get(i-1).mXvalue;
    		if(i < size-1)
    			x3 = mSwingArrayList.get(i+1).mXvalue;
    		

    		if((x2 > X_THRESHOLD_MAX) && (i < size-1))
    		{
        		/*****************************************
        		 *  Step 1: Increasing point
        		 *  
        		 *  	1) value > IMPACT_HI_VALUE
        		 *   	2) x3 > x2 > x1
        		 *****************************************/
    			
    			if((x3 >= x2) && (x2 >= x1))
    			{
    				isIncreasing = true;
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
    					
    					Log.i("detectpeak", "Peak Time: " + mSwingArrayList.get(i).mTimestamp
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
  			
    			if((x1 >= x2) && (x2 >= x3))        				
    			{
    				isDecreasing = true;
    			}
    			// Step 4
    			if((x2 >= 0) && (x3 <= 0))
    			{
        			
        			isPeakFound = false;
        			
        			Log.i("detectpeak", "MSG_PEAK_X: " + mMaxPeakIndex + ", T:" + mMaxPeakTimestamp);
        			sendMessageToHandler(MSG_PEAK_X_MAX, mMaxPeakIndex, mMaxPeakTimestamp);

        			
        			maxValue = 0;
        			maxIndex = 0;
        			mMaxPeakIndex = 0;
        			mMaxPeakTimestamp = 0;
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
			//sendMessageToHandler(MSG_DETECT_X, mCount, 0);
			
    		i++;
    		
    	}while(i <= size-1);

	}
	
	/*=============================================================================
	 * Name: detectYPeakPoint
	 * 
	 * Description:
	 * 		Detect peak point from the golf swing Y-axis data with threshold
	 * 		- Y_THRESHOLD_MAX = 5
	 * 		- Y_THRESHOLD_MIN = -10
	 * Return:
	 * 		None
	 *=============================================================================*/    				
	public void detectYPeakPoint()
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
    				{	i++;
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
	 * Name: clearArrayList
	 * 
	 * Description:
	 * 		If there are items inside of ArrayList, clear the ArrayList
	 * 
	 * Return:
	 * 		None
	 *=============================================================================*/    		
	public void clearArrayList()
	{
		if(mSwingArrayList.size() > 0)
			mSwingArrayList.clear();
	}
	
	/*=============================================================================
	 * Name: readArrayListFromFile
	 * 
	 * Description:
	 * 		Read objects from the file which was converted to the AccelerationData format
	 * 		Save the object to  ArrayList(AccelDataList)	
	 * 
	 * Return:
	 * 		None
	 *=============================================================================*/     	
	public void readArrayListFromFile() 
	{
    	FileInputStream inputStream = null;
    	ObjectInputStream objInputStream = null;

		try
		{
			inputStream = new FileInputStream(mInputFileName);
			objInputStream = new ObjectInputStream(inputStream);
						
			mSwingArrayList = (ArrayList<AccelerationData>)objInputStream.readObject();
			
			Log.i("detectpeak", "mSwingArrayList.size: " + mSwingArrayList.size());
			
			objInputStream.close();
		} 
		catch(Exception e)
		{
			Log.e("Debug", e.getMessage());
		}

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
	
}
