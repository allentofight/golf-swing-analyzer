package com.SwingAnalyzer;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.StringTokenizer;

import android.os.Handler;
import android.os.Message;
import android.util.Log;


public class FileConversionThread extends Thread{
	/* 
	 *	Message ID for converting raw data to AccelerationData format 
	 */
	
	final static int MSG_CONVERSION_DONE	= 0x600;
	final static int MSG_CONVERSION_ERROR	= 0x601;
	final static int MSG_CONVERTION_WORKING	= 0x602;

	public ArrayList<AccelerationData> mAccelerationArray;
	
	int mConvertedCount;
	String mSrcFileName;
	String mOutputFile;
	boolean mFinished;
	boolean mRemoveGChecked;
	static Handler mHandler;
	
	FileConversionThread(String srcfile, String outfile, Handler handler)
	{    		
		mSrcFileName = srcfile;
		mOutputFile = outfile;
		mHandler = handler;
		
		initMemberVariables();
	}
	
	FileConversionThread(String srcfile, Handler handler)
	{
		mSrcFileName = srcfile;
		mHandler = handler;
		mOutputFile = "";
		
		initMemberVariables();
	}

	FileConversionThread(String srcfile, Handler handler, 
						ArrayList<AccelerationData> array, boolean isRemoveGChecked)
	{
		mSrcFileName = srcfile;
		mHandler = handler;
		mRemoveGChecked = isRemoveGChecked;
		mOutputFile = "";
		
		//initMemberVariables();
		mAccelerationArray = new ArrayList<AccelerationData>();
		mAccelerationArray = array;
		
		if(mAccelerationArray.size() > 0)
		{
			Log.i("conversion", "mAccelerationArray.size:" + mAccelerationArray.size());
			mAccelerationArray.clear();
		}
	}

	public void initMemberVariables()
	{
		mFinished = false;
		
		mConvertedCount = 0;
		
		mAccelerationArray = new ArrayList<AccelerationData>();
		
		if(mAccelerationArray.size() > 0)
			mAccelerationArray.clear();		
	}
	
	public void run()
	{
		convertSwingDataToArrayList();
	}
	
	/*=============================================================================
	 * Name: convertSwingDataToArrayList
	 * 
	 * Description:
	 * 		Convert a raw golf swing data to AccelerationData format
	 * 		Save the converted data to an ArrayList (not file)
	 * 
	 * 		  int       int        float     float     float 
	 * 		+-------+-----------+---------+---------+---------+
	 * 		| index | timestamp | x value | y value | z value |
	 * 		+-------+-----------+---------+---------+---------+
	 * 		 
	 * Return:
	 * 		None
	 *=============================================================================*/    	
	public void convertSwingDataToArrayList()
	{
    	FileReader fileReader = null;
    	BufferedReader bufReader = null;

    	String accelStringData = "";
    	int count = 0;    	

    	try 
		{
			fileReader = new FileReader(mSrcFileName);
			bufReader = new BufferedReader(fileReader);
			
			mFinished = true;
			Log.i("conversion", "RawData: " + mSrcFileName);			
			
			while((accelStringData = bufReader.readLine()) != null)
			{
				count++;
				++mConvertedCount;
				
				StringTokenizer token = new StringTokenizer(accelStringData, ";");
				
				if(token != null)
				{
					int tokenNum = token.countTokens();
					if(tokenNum == 4)
					{
						String[] element = new String[tokenNum];
						for(int i=0; i<tokenNum; i++)
						{
							element[i] = (String)token.nextToken();
						}
						addArrayList(count, element);
					}
					else
					{
						mFinished = false;
						break;
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

			}
		
			
			bufReader.close();				
			fileReader.close();        	
			
		}
		catch(Exception e)
		{	
			Log.e("conversion", e.getMessage());
			System.out.println(e.getMessage());
		}
		
    	if(mFinished == false)
    	{
    		Log.e("conversion", "File conversion failed: " + mConvertedCount);
	    	
	    	Message msg = Message.obtain();
	    	msg.what = MSG_CONVERSION_DONE;
	    	msg.arg1 = -1;
	    	
	    	mHandler.sendMessage(msg);
    	}
    	else
    	{
	    	//mFinished = true;
	    	Log.i("conversion", "File conversion finished. count=" + mConvertedCount);
	    	
	    	Message msg = Message.obtain();
	    	msg.what = MSG_CONVERSION_DONE;
	    	msg.arg1 = mConvertedCount;
	    	
	    	mHandler.sendMessage(msg);
    	}
	}
	
	/*=============================================================================
	 * Name: convertingRawDataToFile
	 * 
	 * Description:
	 * 		Convert a raw golf swing data to AccelerationData format
	 * 		Write the converted data to an output file
	 * 		
	 * 		  int       int        float     float     float 
	 * 		+-------+-----------+---------+---------+---------+
	 * 		| index | timestamp | x value | y value | z value |
	 * 		+-------+-----------+---------+---------+---------+
	 * 		
	 * 
	 * Return:
	 * 		None
	 *=============================================================================*/    
    public void convertingRawDataToFile()
    {
    	FileReader fileReader = null;
    	BufferedReader bufReader = null;
    	FileOutputStream outFile = null;
    	ObjectOutputStream objectOutput = null;
    	
    	String accelStringData = "";
    	int count = 0;    	

    	try 
		{
			fileReader = new FileReader(mSrcFileName);
			bufReader = new BufferedReader(fileReader);
			
			if(!mOutputFile.isEmpty())
			{
				outFile = new FileOutputStream(mOutputFile);
				objectOutput = new ObjectOutputStream(outFile);
			}
			
			mFinished = false;
			Log.i("conversion", "RawData: " + mSrcFileName);			
			
			while((accelStringData = bufReader.readLine()) != null)
			{
				count++;
				++mConvertedCount;
				
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
	        	msgtmp.what = MSG_CONVERTION_WORKING;
	        	msgtmp.arg1 = mConvertedCount;
	        	mHandler.sendMessage(msgtmp);

			}
			
			if(!mOutputFile.isEmpty())
			{
				objectOutput.writeObject(mAccelerationArray);			
				objectOutput.flush();
				objectOutput.close();
				
				outFile.close();		
			}
			
			bufReader.close();				
			fileReader.close();        	
			
		}
		catch(Exception e)
		{				
			System.out.println(e.getMessage());
		}
		
    	mFinished = true;
    	Log.i("conversion", "File conversion finished. count=" + mConvertedCount);
    	
    	Message msg = Message.obtain();
    	msg.what = MSG_CONVERSION_DONE;
    	msg.arg1 = mConvertedCount;
    	
    	mHandler.sendMessage(msg);
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
    	
    	if(mRemoveGChecked)
    		element.mYvalue -= 9.81;
    	
    	element.mZvalue = Float.parseFloat(data[3]);
    	
    	/*
    	Log.i("convert", "["+element.mIndex+"]: T=" + element.mTimestamp 
    										+ ", X= " + element.mXvalue 
    										+ ", Y= " + element.mYvalue 
    										+ ", Z= " + element.mZvalue);
    	*/
    	mAccelerationArray.add(element);
    	
    }
}
