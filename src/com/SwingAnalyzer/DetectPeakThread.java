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
	final static int MSG_PEAK_X			= 0x02;
	final static int MSG_IMPACT_X 		= 0x03;
	final static int MSG_DETECT_DONE_X 	= 0x04;
	
	/*
	 * Message ID for Y-axis
	 */
	final static int MSG_DETECT_Y 		= 0x10;
	final static int MSG_PEAK_Y 		= 0x20;
	final static int MSG_IMPACT_Y		= 0x30;
	final static int MSG_DETECT_DONE_Y	= 0x40;
	
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

	
	int mPeakIndex;
	int mPeakTimestamp;
	
	int mAxis;	// X-axis or Y-axis
	int mCount;
	
/*
 * AccelerationData class
 * 
	int mIndex;
	int mTimestamp;
	float mXvalue;
	float mYvalue;
	float mZvalue;	
	
*/
	/*
	public DetectPeakThread(String filename, Handler handler, int axis) {
		// TODO Auto-generated constructor stub
		
		mInputFileName = filename;
		mHandler = handler;
		
		mAxis = axis;
		mPeakIndex = 0;
		mPeakTimestamp = 0;
		
		mCount = 0;
		
		mSwingArrayList = new ArrayList<AccelerationData>();
		
		if(mSwingArrayList.size() > 0)
			mSwingArrayList.clear();
		
	}
	*/
	public DetectPeakThread(ArrayList<AccelerationData>array, Handler handler, int axis) {
		// TODO Auto-generated constructor stub
		
		//mInputFileName = filename;
		mHandler = handler;
		
		mAxis = axis;
		mPeakIndex = 0;
		mPeakTimestamp = 0;
		
		mCount = 0;
		
		mSwingArrayList = new ArrayList<AccelerationData>();
		
		mSwingArrayList = array;
	}
	
	public void run()
	{
		startDetectingPeakPoint(mAxis);
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
			sendMessageToHandler(MSG_DETECT_DONE_X, mPeakIndex, 0);
		}
		else
		{
			//detectYPeakPoint();
			detectMaxMinFromY();
			sendMessageToHandler(MSG_DETECT_DONE_Y, mPeakIndex, 0);
		}
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
		
		sendMessageToHandler(MSG_PEAK_X, maxIndex, maxTimestamp);
	}
	
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
			
			y2 = mSwingArrayList.get(i).mXvalue;
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
										+ ", X:" + maxValue);
		
		Log.i("detectpeak", "Min index:" + minIndex 
										+ ", T:" + minTimestamp
										+ ", X:" + minValue);
		
		sendMessageToHandler(MSG_PEAK_Y, maxIndex, maxTimestamp);
	}
	/*=============================================================================
	 * Name: detectXPeakPoint
	 * 
	 * Description:
	 * 		Detect peak point from the golf swing X-axis data 
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
    					    
    					mPeakIndex = i;
    					mPeakTimestamp = (int)mSwingArrayList.get(i).mTimestamp;
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
        			
        			Log.i("detectpeak", "MSG_PEAK_X: " + mPeakIndex + ", T:" + mPeakTimestamp);
        			sendMessageToHandler(MSG_PEAK_X, mPeakIndex, mPeakTimestamp);

        			
        			maxValue = 0;
        			maxIndex = 0;
        			mPeakIndex = 0;
        			mPeakTimestamp = 0;
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
	 * 		Detect peak point from the golf swing Y-axis data 
	 * 
	 * Return:
	 * 		None
	 *=============================================================================*/    				
	public void detectYPeakPoint()
	{
		
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
