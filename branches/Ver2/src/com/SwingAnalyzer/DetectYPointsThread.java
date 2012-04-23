/*----------------------------------------------------------------------------
  File:   DetectYPointThread.java

  Author: Jung Chang Su
  ----------------------------------------------------------------------------
  Copyright (C) 2012 SICS.
  
  
  This is a source for a thread to detect some critical points such as impact
  and peak points from Y-axis values collected from an accelerometer in 
  an Android smart phone. 
  *--------------------------------------------------------------------------*/
package com.SwingAnalyzer;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class DetectYPointsThread extends Thread
{
	/*
	 * Message ID for Y-axis
	 */
	final static int MSG_DETECT_Y 		= 0x10;
	final static int MSG_PEAK_Y 		= 0x20;
	final static int MSG_IMPACT_Y		= 0x30;
	final static int MSG_DETECT_DONE_Y	= 0x40;
	
	final static int Y_THRESHOLD_HI = 5;
	final static int Y_THRESHOLD_LOW = -10;
	
	String mFileName;
	String mOutputFile;
	boolean mFinished;
	Handler mHandler;

	int mCount;
	int mImpactCount, mImpactPoint;
	int mNegativePeakCount;
	int mPositivePeakCount;
	
	int mThresholdHi = 0;
	int mThresholdLow = 0;
	
	private ArrayList<AccelerationData> AccelDataList = null;
	ArrayList<AccelerationData> mYImpactPointArray = null;

	DetectYPointsThread(String filename, String outfile, int max, int min, Handler handler)
	{
		
		mFileName = filename;
		mOutputFile = outfile;
		mHandler = handler;
		
		mCount = 0;
		mImpactCount = 0;
		mImpactPoint = 0;
		
		mNegativePeakCount = 0;
		mPositivePeakCount = 0;
		
		mThresholdHi = max;
		mThresholdLow = min;
		
		AccelDataList = new ArrayList<AccelerationData>();		
		mYImpactPointArray = new ArrayList<AccelerationData>();

	}
	
	public void run()
	{
		doDetectingYImpactPoint();
	}
	/*=============================================================================
	 * Name: clearArrayList
	 * 
	 * Description:
	 * 		If there is an item in AccelDataList, then clear the ArrayList
	 * 
	 * Return:
	 * 		None
	 *=============================================================================*/    	
	public void clearArrayList()
	{
		if(AccelDataList.size() > 0)
			AccelDataList.clear();
		
		if(mYImpactPointArray.size() > 0)
			mYImpactPointArray.clear();
		
	}
	/*=============================================================================
	 * Name: readArrayListFile
	 * 
	 * Description:
	 * 		Read objects from the file which was converted to the AccelerationData format
	 * 		Save the object to  ArrayList(AccelDataList)	
	 * 
	 * Return:
	 * 		None
	 *=============================================================================*/     	
	public void readArrayListFile() 
	{
    	FileInputStream inputStream = null;
    	ObjectInputStream objInputStream = null;

		try
		{
			inputStream = new FileInputStream(mFileName);
			objInputStream = new ObjectInputStream(inputStream);
			
			//Log.i("Impact", "FileName: " + mFileName);
			AccelDataList = new ArrayList<AccelerationData>();			
			AccelDataList = (ArrayList<AccelerationData>)objInputStream.readObject();
			
			Log.i("Impact", "AccelDataList.size: " + AccelDataList.size());
			
			objInputStream.close();
		} 
		catch(Exception e)
		{
			Log.e("Debug", e.getMessage());
		}

	}
	/*=============================================================================
	 * Name: doDetectingImpactPoint
	 * 
	 * Description:
	 * 		When the DetectImpactThread starts, this function executes first.		
	 * 
	 * Return:
	 * 		None
	 *=============================================================================*/     	  	
	protected void doDetectingYImpactPoint()
	{
		clearArrayList();
		readArrayListFile();
		
		if(detectYImpact() == true)
		{
			//printArrayList();
			mFinished = true;
		}
		
		sendMessageToHandler(MSG_DETECT_DONE_Y, mImpactCount, 0);
	}
	
	/*=============================================================================
	 * Name: detectYImpact
	 * 
	 * Description:
	 * 		Detecting impact points and peak points from the swings		
	 * 
	 * Return:
	 * 		boolean
	 *=============================================================================*/    
    public boolean detectYImpact()
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
    	
    	size = AccelDataList.size();
    	isIncreasing = isDecreasing = isPeakFound = isZero = false;  
    	mFinished = false;
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
    		timestamp = AccelDataList.get(i).mTimestamp;
    		
    		y2 = AccelDataList.get(i).mYvalue;        		
    		if(i> 0)
    			y1 = AccelDataList.get(i-1).mYvalue;
    		if(i < size-1)
    			y3 = AccelDataList.get(i+1).mYvalue;
    		
    		if((y2 < mThresholdLow) && (i < size-1))
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
    					mNegativePeakCount++;
    					mImpactCount++;
    					
    					mYImpactPointArray.add(AccelDataList.get(i));
    					
    					Log.i("DetectY", "Negative Peak Time: " + AccelDataList.get(i).mTimestamp
    							+ ", y: " + y2 + ", Min:" + minValue);
    					    
    					minValue = y2;
    					minIndex = i;        					
    					negativePeakTimestamp = AccelDataList.get(i).mTimestamp;    					
    					sendMessageToHandler(MSG_IMPACT_Y, mNegativePeakCount, timestamp);
    					
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
    		if((isPeakFound) && (y2 > mThresholdHi))
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
        				mPositivePeakCount++;
        				
        				Log.i("DetectY", "Positive Peak Time: " + AccelDataList.get(i).mTimestamp
        							+ ", y: " + y2 + ", Max:" + maxValue);
        				    
        				maxValue = y2;
        				maxIndex = i;        					
        				positivePeakTimestamp = AccelDataList.get(i).mTimestamp;
        				sendMessageToHandler(MSG_PEAK_Y, mPositivePeakCount, timestamp);
        				
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
    	
    	mFinished = true;
    	
    	//writeObjectToFile();		// Not necessary
    	
    	if(mImpactCount > 0)
    		return true;
    	else
    		return false;
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
	/*=============================================================================
	 * Name: printArrayList
	 * 
	 * Description:
	 * 		Print debug messages of the detected ArrayList		
	 * 
	 * Return:
	 * 		None
	 *=============================================================================*/        
    public void printArrayList()
    {
    	if(mYImpactPointArray.size() > 0)
    	{
        	for(int j=0; j<mYImpactPointArray.size(); j++)
        	{
        		Log.i("Impact", "index: " + j + ", " + mYImpactPointArray.get(j).mTimestamp 
        									 + ", " + mYImpactPointArray.get(j).mXvalue
        									 + ", " + mYImpactPointArray.get(j).mYvalue
        									 + ", " + mYImpactPointArray.get(j).mZvalue);
        	}
    	}
    	else
    	{
    		Log.e("Impact", "mImpactPointArray size is zero.");
    	}
    }
	/*=============================================================================
	 * Name: writeObjectToFile
	 * 
	 * Description:
	 * 		Write the detected ArrayList data to an output file		
	 * 		- Currently this function is not used.
	 * 
	 * Return:
	 * 		None
	 *=============================================================================*/    
    public void writeObjectToFile()
    {
    	FileOutputStream mOutFileStream = null;
    	ObjectOutputStream mObjectOutputStream = null;

    	try
    	{
			mOutFileStream = new FileOutputStream(mOutputFile);
			mObjectOutputStream = new ObjectOutputStream(mOutFileStream);

			Log.i("Detect", "writeObject: " + mYImpactPointArray.size());
			
			mObjectOutputStream.writeObject(mYImpactPointArray);
			mObjectOutputStream.flush();				
			mObjectOutputStream.close();
			
			mOutFileStream.close();
			
    		
    	}
    	catch(Exception e)
    	{
    		System.out.println(e.getMessage());
    	}
    }	

}
