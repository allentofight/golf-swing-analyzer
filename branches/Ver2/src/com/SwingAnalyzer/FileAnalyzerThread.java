package com.SwingAnalyzer;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.StringTokenizer;

import android.os.Handler;
import android.os.Message;
import android.util.Log;


public class FileAnalyzerThread extends Thread{
	final static int MSG_CONVERT 	= 0;
	
	public ArrayList<AccelerationData> mAccelerationArray;
	
	int mCount;
	String mFileName;
	String mOutputFile;
	boolean mFinished;
	static Handler mHandler;
	
	FileAnalyzerThread(String filename, String outfile, Handler handler)
	{    		
		mFileName = filename;
		mOutputFile = outfile;
		mFinished = false;
		mHandler = handler;
		mCount = 0;
		mAccelerationArray = new ArrayList<AccelerationData>();
		
		//if(mAccelerationArray.size() > 0)
		//	mAccelerationArray.clear();

	}
	
	public void run()
	{

		convertRawData();    		
	}
	
	/*=============================================================================
	 * Name: convertRawData
	 * 
	 * Description:
	 * 		Convert a raw golf swing data to AccelerationData format
	 * 		- AccelerationData format
	 * 		
	 * 		  int       int        float     float     float 
	 * 		+-------+-----------+---------+---------+---------+
	 * 		| index | timestamp | x value | y value | z value |
	 * 		+-------+-----------+---------+---------+---------+
	 * 		 
	 * Return:
	 * 		None
	 *=============================================================================*/    
    public void convertRawData()
    {
    	FileReader fileReader = null;
    	BufferedReader bufReader = null;
    	FileOutputStream outFile = null;
    	ObjectOutputStream objectOutput = null;
    	
    	String accelStringData = "";
    	int count = 0;    	

    	try 
		{
			fileReader = new FileReader(mFileName);
			bufReader = new BufferedReader(fileReader);
			outFile = new FileOutputStream(mOutputFile);
			objectOutput = new ObjectOutputStream(outFile);
			
			mFinished = false;
			Log.i("Convert", "RawData: " + mFileName);
			Log.i("Convert", "OutFile: " + mOutputFile);
			
			while((accelStringData = bufReader.readLine()) != null)
			{
				count++;
				++mCount;
				
				StringTokenizer token = new StringTokenizer(accelStringData, ";");
				
				if(token != null)
				{
					int tokenNum = token.countTokens();
					
					String[] element = new String[tokenNum];
					for(int i=0; i<tokenNum; i++)
					{
						element[i] = (String)token.nextToken();
					}
					addArrayList(count, element);
				}
				
				try 
				{
					Thread.sleep(1);
				} 
				catch(InterruptedException e)
				{
					System.out.println(e.getMessage());
				}
				
				//if(mFinished)
				//	return;
				
	        	Message msgtmp = Message.obtain();
	        	msgtmp.what = MSG_CONVERT;
	        	msgtmp.arg1 = mCount;
	        	mHandler.sendMessage(msgtmp);

			}
			
			objectOutput.writeObject(mAccelerationArray);
			//objectOutput.reset();
			objectOutput.flush();
			objectOutput.close();
			
			outFile.close();		
			
			bufReader.close();				
			fileReader.close();			
        	
			try 
			{
				Thread.sleep(10);
			} 
			catch(InterruptedException e)
			{
				System.out.println(e.getMessage());
			}

        	Log.i("Convert", "mFinished = true");        	
			
		}
		catch(Exception e)
		{				
			System.out.println(e.getMessage());
		}
		
    	mFinished = true;
    	
    	Message msg = Message.obtain();
    	msg.what = MSG_CONVERT;
    	msg.arg1 = mCount;
    	
    	mHandler.sendMessage(msg);
    	
    	/*
		if(mAccelerationArray.size() > 0)
			mAccelerationArray.clear();
		*/
	}

	/*=============================================================================
	 * Name: addArrayList
	 * 
	 * Description:
	 * 		Convert a raw string data to AccelerationData format
	 * 		and add the converted data to an ArrayList(mAccelerationArray)	
	 * 
	 * Return:
	 * 		None
	 *=============================================================================*/     	    
    public void addArrayList(int index, String[] data)
    {
    	AccelerationData element = new AccelerationData();
    	
    	element.mIndex = index;
    	element.mTimestamp = Integer.parseInt(data[0]);
    	element.mXvalue = Float.parseFloat(data[1]);    	
    	
    	element.mYvalue = Float.parseFloat(data[2]);
    	element.mYvalue -= 9.81;
    	
    	element.mZvalue = Float.parseFloat(data[3]);
    	
    	//Log.i("Debug", "index= " + element.mIndex + " timestamp: " + element.mTimestamp);
    	//Log.i("Debug", "X= " + element.mXvalue + " Y= " + element.mYvalue + " Z= " + element.mZvalue);
    	
    	mAccelerationArray.add(element);
    	
    }
}
