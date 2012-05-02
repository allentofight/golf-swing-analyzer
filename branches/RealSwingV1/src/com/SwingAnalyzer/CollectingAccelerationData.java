/*-----------------------------------------------------------------------------------------
  File:   CollectingAccelerationData.java

  Author: Jung Chang Su
  -----------------------------------------------------------------------------------------
  Copyright (C) 2012 SICS.
  
    
  
  *----------------------------------------------------------------------------------------*/
package com.SwingAnalyzer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import android.app.*;
import android.content.*;
import android.hardware.*;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.*;
import android.util.*;
import android.view.*;
import android.widget.*;

public class CollectingAccelerationData extends Activity implements SensorEventListener{

	/*
	 * Constant variables
	 */
	final static int TIMEOUT = (1000 * 3);		// 3 seconds timeout value
	final static int TIMEOUT_SEC = 3;
	
	final static String ACCELERATION_DIR = "/data/acceldata/";
	final static String OUTPUT_FILENAME = "swing.dat";
	final static String OUTPUT_TEXT_FILE = "swing.txt";
	
	final static float GRAVITY = 9.81f;
	
	/*
	 * Class member variables
	 */
	private SensorManager mSensorManager;
	private WindowManager mWindowManager;
	private Display	mDisplay;
	
	boolean isRecording;
	boolean isSoundPlayed;
	
	Handler mTimerHandler;
	
	String mSDCardPath;
	String mOutputFileName;
	
	SoundPool mSoundPool;
	int mSoundId;
	int mAccelerationCount;
	
	long mStartTimeMillis;
	long mEndTimeMillis;
	
	private String mStartDateString;
	private String mStartTimeString;

	/*
	 * Widgets
	 */
	ImageButton mStartButton;
	Button mSkipButton;
	
	TextView mTimeTextView;
	
	private ArrayList<AccelerationData> mSwingDataArrayList;
	
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.collecting_acceleration);
		
		mStartButton = (ImageButton)findViewById(R.id.start_button);
		mStartButton.setOnClickListener(mClickListener);
		
		mSkipButton = (Button)findViewById(R.id.skip_button);
		mSkipButton.setOnClickListener(mClickListener);
		
		mTimeTextView = (TextView)findViewById(R.id.time_text_view);
		
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);		
		
		mWindowManager = (WindowManager)getSystemService(WINDOW_SERVICE);
		
		mDisplay = mWindowManager.getDefaultDisplay();
		
		initMemberVariables();
		makeOutputFolder();
		
	}

	
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		
		if(mSwingDataArrayList.size()>0)
			mSwingDataArrayList.clear();
	}

	
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		
		
		mSensorManager.unregisterListener(this);
		Log.i("collect", "unregisterListener() called");
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		
		mSensorManager.registerListener(this, 
								mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), 
								SensorManager.SENSOR_DELAY_FASTEST);
	}

	Button.OnClickListener mClickListener = new View.OnClickListener() {		
		public void onClick(View v) {
			switch(v.getId())
			{
			case R.id.start_button:
				mAccelerationCount = 0;
				clearTextView();
				clearArrayList();
				startCollectingData();
				break;
			case R.id.skip_button:				
				Intent intent = new Intent(CollectingAccelerationData.this, 
											StatisticsActivity.class);
				startActivity(intent);
				finish();				
				break;
			default:
				break;
			}
			
		}
	};

	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}


	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub
				
		if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
		{			
			// Only if Start button is clicked, then collect acceleration data
			if((isRecording == true) && (isSoundPlayed == true))
			{
				collectingAccelerationData(event);
			}
		}
	}
	/*=============================================================================
	 * Name: startCollectingData
	 * 
	 * Description:
	 * 		When Start button is clicked,
	 * 		- make a beep sound for notification
	 * 		- start a 3 seconds timer	
	 * 
	 * Return:
	 * 		None
	 *=============================================================================*/	    			
	public void startCollectingData()
	{
		isRecording = true;
		
		getDateTimeString();
		//mStartDateString = getDateString();
		//mStartTimeString = getTimeString();
		
		if(makeBeepSound() == true)	
		{
			mStartTimeMillis = System.currentTimeMillis();
			Log.i("collect", "Start Time: " + mStartTimeMillis);
			
			startTimeoutCounter();
		}
		
		Log.i("collect", "startTimeoutCounter()");
	}
	
	/*=============================================================================
	 * Name: collectingAccelerationData
	 * 
	 * Description:
	 * 				
	 * 
	 * Return:
	 * 		None
	 *=============================================================================*/	    		
	public void collectingAccelerationData(SensorEvent event)
	{
		AccelerationData accelData = new AccelerationData();		
		int dispRotation = 0;
		
		float[] values = event.values;

		// 방향을 고려해야 함		
		long timeInMillis = System.currentTimeMillis();

		accelData.mIndex = mAccelerationCount++;
		accelData.mTimestamp = (int)(timeInMillis - mStartTimeMillis);
		
		dispRotation = mDisplay.getRotation();
		//Log.i("collect", "Rotation: " + dispRotation);
		
		switch(dispRotation)
		{
		case Surface.ROTATION_0:
			accelData.mXvalue = values[0];
			accelData.mYvalue = values[1] - GRAVITY;
			accelData.mZvalue = values[2];
			break;
			
			/*            ^
			 *            | +y (+x)
			 *   -x   +----------+--+
			 *  <--   |          |o |  -> +x (-y)  : () : sensor coordinate values
			 *   (+y) +----------+--+
			 *            | -y (-x)
			 *            V
			 */
		case Surface.ROTATION_90:	// counter-clockwise
			accelData.mXvalue = -values[1] - GRAVITY;
			accelData.mYvalue = values[0] ;
			accelData.mZvalue = values[2];
			break;
			
			/*             ^
			 *             | +y(-y)       
			 *          +-----+
			 *          |__O__|
			 *          |     |
			 *    <---  |     | ---> : () : sensor coordinate values
			 *   -x(+x) |     |  +x (-x)
			 *          +-----+
			 *            | -y(+y)
			 *            V
			 */
		case Surface.ROTATION_180:
			accelData.mXvalue = -values[0];
			accelData.mYvalue = -values[1] + GRAVITY;
			accelData.mZvalue = values[2];
			break;
			/*             ^
			 *             | +y (-x)
			 *   -x   +--+----------+
			 *  <--   | O|          | --> +x (+y)  : () : sensor coordinate values
			 *   (-y) +--+----------+
			 *             | -y (+x)
			 *             V
			 */

		case Surface.ROTATION_270:
			accelData.mXvalue = values[1] + GRAVITY;
			accelData.mYvalue = -values[0];
			accelData.mZvalue = values[2];
			break;
		}
		
		Log.i("collect", "C:" + accelData.mIndex 
						+ " T:" + accelData.mTimestamp
						+ " X:" + accelData.mXvalue
						+ " Y:" + accelData.mYvalue
						+ " Z:" + accelData.mZvalue);
		
		mSwingDataArrayList.add(accelData);
		
		
	}
	
	/*=============================================================================
	 * Name: makeBeepSound
	 * 
	 * Description:
	 * 		Generate a beep sound for informing start of a user		
	 * 
	 * Return:
	 * 		None
	 *=============================================================================*/	    	
	public boolean makeBeepSound()
	{
		if(mSoundPool.play(mSoundId, 1, 1, 0, 0, 1) != 0)
			isSoundPlayed = true;
		else
			isSoundPlayed = false;
		
		return isSoundPlayed;
	}
	
	/*=============================================================================
	 * Name: writeAccelerationDataToFile
	 * 
	 * Description:
	 * 		If a time is expired, then write all data to an output file.
	 * 		This function is called by mTimeoutHandler		
	 * 
	 * Return:
	 * 		None
	 *=============================================================================*/	    		
	public void writeAccelerationDataToFile()
	{
		FileOutputStream outFile = null;
		ObjectOutputStream objectOutput = null;
		
		mOutputFileName = mSDCardPath + ACCELERATION_DIR + OUTPUT_FILENAME;
		
		try
		{
			outFile = new FileOutputStream(mOutputFileName);
			objectOutput = new ObjectOutputStream(outFile);
			
			objectOutput.writeObject(mSwingDataArrayList);
			objectOutput.flush();
			objectOutput.close();
			outFile.close();
			
			Log.i("collect", "Swing Data ArrayList count: " + mSwingDataArrayList.size());
			//displaySavedCount(mSwingDataArrayList.size());
			
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
		}
	}
	
	/*=============================================================================
	 * Name: startTimeoutCounter
	 * 
	 * Description:
	 * 		3 seconds Timer		
	 * 
	 * Return:
	 * 		None
	 *=============================================================================*/	    
	public void startTimeoutCounter()
	{
		mTimerHandler = new Handler()
		{
			int wait = 0;
			
			public void handleMessage(Message msg)
			{
				if(wait < TIMEOUT && isSoundPlayed == true)
				{
					
					mTimerHandler.sendEmptyMessageDelayed(0, 1000);
					wait += 1000;
					displaySecond(wait);
					
				}
				else
				{
					isRecording = false;
					mEndTimeMillis = System.currentTimeMillis();
					writeAccelerationDataToFile();
					writeOutputTextFile();		// For debugging
					
					try {
						Thread.sleep(1000);
						goFeedbackActivity();

					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		};
		
		mTimerHandler.sendEmptyMessage(0);
	}
	
	private void goFeedbackActivity()
	{
		Intent intent = new Intent(CollectingAccelerationData.this, 
									SwingFeedback.class);
		
		intent.putExtra("START_SWING", true);
		intent.putExtra("START_DATE", mStartDateString);
		intent.putExtra("START_TIME", mStartTimeString);

		startActivity(intent);
		finish();

	}
	
	private void getDateTimeString()
	{
		mStartDateString = getDateString();
		AccelerationData.setSwingStartDate(mStartDateString);
		
		mStartTimeString = getTimeString();
		AccelerationData.setSwingStartTime(mStartTimeString);
	}
	/*=============================================================================
	 * Name: initMemberVariables
	 * 
	 * Description:
	 * 		Initialize all member variables		
	 * 
	 * Return:
	 * 		None
	 *=============================================================================*/	    		
	private void initMemberVariables()
	{
		isRecording = false;
		mSDCardPath = "";
		mOutputFileName = "";
		mSoundId = 0;
		mAccelerationCount = 0;
		
		mStartTimeMillis = 0;
		mEndTimeMillis = 0;
		
		mStartDateString = "";
		mStartTimeString = "";
		
		mSwingDataArrayList = new ArrayList<AccelerationData>();
		
		mSoundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
		mSoundId = mSoundPool.load(this, R.raw.ding, 1);
	}
	
	/*=============================================================================
	 * Name: makeOutputFolder
	 * 
	 * Description:
	 * 		Check whether a SD card is mounted or not		
	 * 		If mounted, return the absolute SD card path name
	 * 
	 * Return:
	 * 		String
	 *=============================================================================*/	
    public void makeOutputFolder()
    {
        String ext = Environment.getExternalStorageState();
        
        if(ext.equals(Environment.MEDIA_MOUNTED))
        {
        	mSDCardPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        	makeOutputDir(mSDCardPath);
        }
        else
        {
        	mSDCardPath = Environment.MEDIA_UNMOUNTED;        	
        	Toast.makeText(this, "SD card is not mounted", Toast.LENGTH_LONG).show();
        }
    }
	/*=============================================================================
	 * Name: makeOutputDir
	 * 
	 * Description:
	 * 		Make a output directory for writing output files
	 * 		(/data/acceldata/)		
	 * 		
	 * 
	 * Return:
	 * 		String
	 *=============================================================================*/	    
    public void makeOutputDir(String dir)
    {
    	File outputDir = new File(dir + ACCELERATION_DIR);
    	
    	if(outputDir.mkdir() == true)
    		Log.i("Convert", "mkdir is successful: " + dir + ACCELERATION_DIR);
    	else
    		Log.i("Convert", "mkdir failed: " + dir + ACCELERATION_DIR);
    }
	/*=============================================================================
	 * Name: displaySecond
	 * 
	 * Description:
	 * 		Make a output directory for writing output files
	 * 		(/data/acceldata)		
	 * 		
	 * 
	 * Return:
	 * 		String
	 *=============================================================================*/	        
    public void displaySecond(int time)
    {
    	int second = 0;
    	
    	second = (TIMEOUT_SEC) - (time/1000);
    	String timeString = Integer.toString(second);
    	mTimeTextView.setText(timeString + " seconds left.");
    }
	/*=============================================================================
	 * Name: displaySavedCount
	 * 
	 * Description:
	 * 		Display the count of ArrayList and the elapsed time for processing
	 * 		
	 * 
	 * Return:
	 * 		None
	 *=============================================================================*/	        
    public void displaySavedCount(int num)
    {
    	String timeDiff = "";
    	
    	timeDiff = getTimeDifference(mStartTimeMillis, mEndTimeMillis);
    	/*
    	mCountTextView.setText("Saved number is " + Integer.toString(num)
    							+ "," + timeDiff);
    	*/
    }
	/*=============================================================================
	 * Name: displayAccelerationData
	 * 
	 * Description:
	 * 		Display all data of mSwingDataArrayList
	 * 		- for debugging
	 * 
	 * Return:
	 * 		None
	 *=============================================================================*/	            
    public void displayAccelerationData()
    {
    	String string = "";
    	int size = mSwingDataArrayList.size();
    	AccelerationData accelData = new AccelerationData();
    	
    	for(int i=0; i<size; i++)
    	{
    		accelData = mSwingDataArrayList.get(i);
    		
    		string = Integer.toString(accelData.mIndex) + "," +
    					Long.toString(accelData.mTimestamp) + "," +
    					Float.toString(accelData.mXvalue) + "," + 
    					Float.toString(accelData.mYvalue) + "," +
    					Float.toString(accelData.mZvalue) + "\n";
    	
    		Log.i("collect", "SwingData:" + string);
    		
    	}

    }
    
	/*=============================================================================
	 * Name: getTimeDifference
	 * 
	 * Description:
	 * 		Display the count of ArrayList
	 * 		
	 * 
	 * Return:
	 * 		String
	 *=============================================================================*/	            
    public String getTimeDifference(long start, long end)
    {
    	String stringTimeDiff = "";
    	long timeDiff = 0;
    	    	
    	timeDiff = end - start;
    	stringTimeDiff = "\nElapsed Time: " + (timeDiff/1000) + "." + ((timeDiff%1000)/10) + " sec";
    	
    	return stringTimeDiff;
    }

	/*=============================================================================
	 * Name: clearTextView
	 * 
	 * Description:
	 * 		Clear all textview widgets
	 * 		
	 * 
	 * Return:
	 * 		None
	 *=============================================================================*/	            
    public void clearTextView()
    {
    	mTimeTextView.setText("3 seconds left.");
    }
    
	/*=============================================================================
	 * Name: clearArrayList
	 * 
	 * Description:
	 * 		Delete elements of ArrayList, if the size is greater than 0
	 * 		
	 * 
	 * Return:
	 * 		None
	 *=============================================================================*/	                
    public void clearArrayList()
    {
		if(mSwingDataArrayList.size()>0)
			mSwingDataArrayList.clear();
    }
    
	/*=============================================================================
	 * Name: writeOutputTextFile
	 * 
	 * Description:
	 * 		Write golf swing data which was collected by an accelerometer to a text file
	 * 		- For analyzing the swing data with Excel
	 * 		
	 * 
	 * Return:
	 * 		None
	 *=============================================================================*/	                
    public void writeOutputTextFile()
    {
    	String textFileName = "";
    	String content = "";
    	
    	AccelerationData element = new AccelerationData();
    	File textFile = null;
    	FileOutputStream fout = null;
    	
    	if(!mSDCardPath.isEmpty())
    	{
    		textFileName = mSDCardPath + ACCELERATION_DIR + OUTPUT_TEXT_FILE;
    		
    		try 
    		{
    			textFile = new File(textFileName);
				fout = new FileOutputStream(textFile);
				
		    	for(int i=0; i< mSwingDataArrayList.size(); i++)
		    	{
		    		element = mSwingDataArrayList.get(i);
		    		
		    		content = element.mTimestamp + ";" 
		    				+ element.mXvalue + ";"
		    				+ element.mYvalue + ";"
		    				+ element.mZvalue + "\n";
		    		
		    		try 
		    		{
						fout.write(content.getBytes());
					} 
		    		catch (IOException e) 
		    		{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		    	}
		    	
		    	try 
		    	{
		    		fout.flush();
					fout.close();
					
				} 
		    	catch (IOException e) 
		    	{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			} 
    		catch (FileNotFoundException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    }
    
	/*=============================================================================
	 * Name: getDateString
	 * 
	 * Description:
	 * 		Get the current year, month and date
	 * 		Return a string of date like "yyyy-mm-dd" (ISO 2014)
	 * 
	 * Return:
	 * 		String
	 *=============================================================================*/	                
    public String getDateString()
    {
    	String stringDate = "";
    	
    	Calendar today = Calendar.getInstance();
    	
    	stringDate = (today.get(Calendar.YEAR) + "-" 
    				+ (today.get(Calendar.MONTH) + 1) + "-"
    				+ today.get(Calendar.DATE));
    	
    	return stringDate;
    }
    
	/*=============================================================================
	 * Name: getTimeString
	 * 
	 * Description:
	 * 		Get the current hour, minute and second
	 * 		Return a string of time like "hh:mm:ss" (ISO 8601)
	 * 
	 * Return:
	 * 		String
	 *=============================================================================*/	                
    public String getTimeString()
    {
    	String stringTime = "";
    	
    	int hour, min, sec;
    	
    	hour = min = sec = 0;
    	
    	Calendar today = Calendar.getInstance();
    	
    	hour = today.get(Calendar.HOUR_OF_DAY);    	
    	stringTime += hour + ":";
    	
    	min = today.get(Calendar.MINUTE);
    	stringTime += min + ":";
    	
    	sec = today.get(Calendar.SECOND);
    	stringTime += sec;
    	
    	return stringTime;
    }
    
}
