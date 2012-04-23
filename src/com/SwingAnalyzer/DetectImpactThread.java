/*----------------------------------------------------------------------------
  File:   DetectImpactThread.java

  Author: Jung Chang Su
  ----------------------------------------------------------------------------
  Copyright (C) 2012 SICS.
  
  Detect some critical points from X-axis values collected from an Accelerometer
  in an Android smart phone
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


///////////////////////////////////////////////////////////////////////////////////
/*=============================================================================
 * Name: Class DetectImpactThread
 * 
 * Description:
 * 		A thread for detecting some critical points from golf swings		
 * 
 * Return:
 * 		None
 *=============================================================================*/     	
class DetectImpactThread extends Thread
{

	/*
	 *	Message ID for X-axis 
	 */
	final static int MSG_DETECT_X 		= 0x01;
	final static int MSG_PEAK_X			= 0x02;
	final static int MSG_IMPACT_X 		= 0x03;
	final static int MSG_DETECT_DONE_X 	= 0x04;

	final static int IMPACT_HI_VALUE = 10;		// 
	final static int IMPACT_LOW_VALUE = -5;

	int mImpactCount, mImpactPoint;
	int mPeakCount;
	int mCount;
	String mFileName;
	String mOutputFile;
	boolean mFinished;
	Handler mHandler;
	
	ArrayList<AccelerationData> AccelDataList = null;
	ArrayList<AccelerationData> mImpactPointArray = null;
	
	DetectImpactThread(String filename, String outfile, Handler handler)
	{
		mFileName = filename;
		mOutputFile = outfile;
		mHandler = handler;
		mCount = 0;
		mImpactCount = 0;
		mImpactPoint = 0;
		mPeakCount = 0;
		
		AccelDataList = new ArrayList<AccelerationData>();
		mImpactPointArray = new ArrayList<AccelerationData>();
		
		mFinished = false;
	}

	public void run()
	{
		doDetectingImpactPoint();
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
		
		if(mImpactPointArray.size() > 0)
			mImpactPointArray.clear();
		
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
	protected void doDetectingImpactPoint()
	{
		clearArrayList();
		readArrayListFile();
		
		if(detectedImpact() == true)
		{
			//printArrayList();
			mFinished = true;
		}
		
		sendMessageToHandler(MSG_DETECT_DONE_X, mImpactCount, 0);
	}
	
	/*=============================================================================
	 * Name: detectedImpact
	 * 
	 * Description:
	 * 		Detecting impact points and peak points from the swings		
	 * 
	 * Return:
	 * 		boolean
	 *=============================================================================*/    
    public boolean detectedImpact()
    {
    	boolean isFound = false;
    	float x1, x2, x3;
    	int i=0;
    	int impactIndex = 0;
    	int size;
    	boolean isIncreasing, isDecreasing, isPeakFound, isZero;
    	int timestamp = 0;
    	float maxValue = 0;
    	int maxIndex = 0;
    	int peakTimestamp = 0;
    	
    	x1 = x2 = x3 = 0;
    	
    	size = AccelDataList.size();
    	isIncreasing = isDecreasing = isPeakFound = isZero = false;  
    	mFinished = false;
    	mCount = 0;
    	
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
    		timestamp = (int)AccelDataList.get(i).mTimestamp;
    		
    		x2 = AccelDataList.get(i).mXvalue;        		
    		if(i> 0)
    			x1 = AccelDataList.get(i-1).mXvalue;
    		if(i < size-1)
    			x3 = AccelDataList.get(i+1).mXvalue;
    		
    		if((x2 > IMPACT_HI_VALUE) && (i < size-1))
    		{
        		/*****************************************
        		 *  Step 1: Increasing point
        		 *  
        		 *  	1) value > IMPACT_HI_VALUE
        		 *   	2) x3 > x2 > x1
        		 *****************************************/
    			//if((x3 > x2) && (x2 > x1))
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
    			else
    			{
    				isIncreasing = false;
    				isFound = false;
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
    					mPeakCount++;
    					/*
    					Log.i("Impact", "Peak Time1: " + AccelDataList.get(i).mTimestamp
    							+ ", x: " + x2 + ", Max:" + maxValue);
    					*/    
    					maxValue = x2;
    					maxIndex = i;        					
    					peakTimestamp = (int)AccelDataList.get(i).mTimestamp;
    					//sendMessageToHandler(MSG_PEAK, mPeakCount, timestamp);
    					
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
    				mImpactPoint = i;
        			mImpactPointArray.add(AccelDataList.get(i));
        			mImpactCount++;
        			
        			Log.i("Impact", "Impact[" + mImpactCount + "] " 
        						+ "T:" + AccelDataList.get(mImpactPoint).mTimestamp
        						+ ", x2:" + x2
        						+ ", x3:" + x3);
        			
        			isFound = true;
        			isPeakFound = false;
        			
					Log.i("Impact", "Peak Time: " + peakTimestamp 
								+ ", Value:" + AccelDataList.get(maxIndex).mXvalue 
								+ ", Max:" + maxValue);    

        			//sendMessageToHandler(MSG_PEAK, mPeakCount, peakTimestamp);
        			sendMessageToHandler(MSG_IMPACT_X, mImpactCount, timestamp);
        			
        			maxValue = 0;
        			maxIndex = 0;
        			peakTimestamp = 0;
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
			sendMessageToHandler(MSG_DETECT_X, mCount, 0);
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
    
    public void printArrayList()
    {
    	if(mImpactPointArray.size() > 0)
    	{
        	for(int j=0; j<mImpactPointArray.size(); j++)
        	{
        		Log.i("Impact", "index: " + j + ", " + mImpactPointArray.get(j).mTimestamp 
        									 + ", " + mImpactPointArray.get(j).mXvalue
        									 + ", " + mImpactPointArray.get(j).mYvalue
        									 + ", " + mImpactPointArray.get(j).mZvalue);
        	}
    	}
    	else
    	{
    		Log.e("Impact", "mImpactPointArray size is zero.");
    	}
    }

    public void writeObjectToFile()
    {
    	FileOutputStream mOutFileStream = null;
    	ObjectOutputStream mObjectOutputStream = null;

    	try
    	{
			mOutFileStream = new FileOutputStream(mOutputFile);
			mObjectOutputStream = new ObjectOutputStream(mOutFileStream);

			Log.i("Detect", "writeObject: " + mImpactPointArray.size());
			
			mObjectOutputStream.writeObject(mImpactPointArray);
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
