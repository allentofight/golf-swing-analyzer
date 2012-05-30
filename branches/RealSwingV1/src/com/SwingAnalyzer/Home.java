/*-----------------------------------------------------------------------------------------
  File:   Home.java

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

public class Home extends Activity implements SensorEventListener{
	private static final String PREFERENCE_SETTING = "settings";
	private static final String PREF_COLLECTION_TIME = "collection_time";
	private static final String PREF_BEEP_METHOD = "beep_method";
	private static final String PREF_MAX_THRESHOLD = "max_threshold";
	private static final String PREF_MIN_THRESHOLD = "min_threshold";
	private static final String PREF_PHONE_FRONT_PLACEMENT = "placement";
	private static final String PREF_DATA_KEEPING_TIME = "keeping_time";
	
	private static final int DEFAULT_COLLECTION_TIME 	= 3;
	private static final int DEFAULT_MAX_THRESHOLD 		= 5;
	private static final int DEFAULT_MIN_THRESHOLD 		= -5;
	

	/*
	 * Constant variables
	 */
	final static int COUNTDOWN_TIMER = (1000 * 3);		// 3 seconds timeout value
	final static int TIMEOUT_SEC = 3;
	
	final static String GOLFSWING_DATA_DIR = "/data/GolfSwingAnalyzer";
	final static String EXTERNAL_SWING_DIR = "/externalswing/";
	final static String COLLECTED_SWING_DIR = "/collectedswing/";
	
	final static String COLLECTED_SWING_PATH = GOLFSWING_DATA_DIR + COLLECTED_SWING_DIR;
	
	final static String OUTPUT_FILENAME = "swing";
	final static String OUTPUT_FILE_EXT = ".dat";
	
	final static String OUTPUT_TEXT_FILE = "swing";
	final static String OUTPUT_TEXTFILE_EXT = ".txt";
	
	final static float GRAVITY = 9.81f;
	
	final static int DELETE_FILE_DATE = 7;
	/*
	 * Class member variables
	 */
	private SensorManager mSensorManager;
	private WindowManager mWindowManager;
	private Display	mDisplay;
	
	boolean isRecording;
	boolean isSoundPlayed;
	
	Handler mCollectingDataHandler;
	Handler mCountDownTimer;
	
	String mSDCardPath;
	String mOutputFileName;
	
	SoundPool mSoundPool;
	int mOneSoundId;
	int mTwoSoundId;
	int mThreeSoundId;
	
	int mBeepSoundId;
	int mAccelerationCount;
	
	long mStartTimeMillis;
	long mEndTimeMillis;
	
	private String mStartDateString;
	private String mStartTimeString;

	private String mDateTimeString;
	
	/* 
	 * SharedPreference Values 
	 */
	private int mCollectionTime = 0;
	private boolean mBeepChecked = true;
	
	private int mMaxThreshold = 0;		// Threshold of X-axis
	private int mMinThreshold = 0;		// Threshold of Y-axis
	private boolean mPhoneFrontPlaced = false;
	private int mDataKeepingTime = 0;
	/*
	 * Widgets
	 */
	ImageButton mStartButton;
	ImageButton mSettingsButton;
	
	ImageButton mExternalSwingButton;
	ImageButton mPastSwingButton;
	ImageButton mStatsButton;
	
	TextView mTimeTextView;
	TextView mPhonePlacementTextView;
	
	private ArrayList<AccelerationData> mSwingDataArrayList;
	
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.home);
		
		readPreferenceValues();
		makeGolfSwingDataFolders();
		
		mStartButton = (ImageButton)findViewById(R.id.collect_start_button);
		mStartButton.setOnClickListener(mClickListener);
		mStartButton.setEnabled(true);
		
		mSettingsButton = (ImageButton)findViewById(R.id.collect_settings_button);
		mSettingsButton.setOnClickListener(mClickListener);

		mExternalSwingButton = (ImageButton)findViewById(R.id.collect_external_swing_button);
		mExternalSwingButton.setOnClickListener(mClickListener);
		
		mPastSwingButton = (ImageButton)findViewById(R.id.collect_past_swing_button);
		mPastSwingButton.setOnClickListener(mClickListener);
		
		mStatsButton = (ImageButton)findViewById(R.id.collect_stats_button);
		mStatsButton.setOnClickListener(mClickListener);
		
		mTimeTextView = (TextView)findViewById(R.id.time_text_view);
		mTimeTextView.setText("Data Collection time: " + mCollectionTime + " seconds.");
		
		mPhonePlacementTextView = (TextView)findViewById(R.id.collecting_placement);
		
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);		
		
		mWindowManager = (WindowManager)getSystemService(WINDOW_SERVICE);
		
		mDisplay = mWindowManager.getDefaultDisplay();
		
		initMemberVariables();
		makeOutputFolder();
		initCollectionSoundPool();
		
		if(mDataKeepingTime != 0)
			deletePreviousSwingData(mDataKeepingTime,COLLECTED_SWING_PATH);
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
		
		if(mPhoneFrontPlaced)
			mPhonePlacementTextView.setText("Phone Placement : Front side");
		else
			mPhonePlacementTextView.setText("Phone Placement : Back side");
		
		mSensorManager.registerListener(this, 
								mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), 
								SensorManager.SENSOR_DELAY_FASTEST);
	}

	Button.OnClickListener mClickListener = new View.OnClickListener() {		
		public void onClick(View v) {
			switch(v.getId())
			{
			case R.id.collect_start_button:
				mAccelerationCount = 0;
				clearTextView();
				clearArrayList();
				startCollectingData();
				mStartButton.setEnabled(false);
				break;			
			case R.id.collect_settings_button:
				startActivity(new Intent(Home.this, SettingsActivity.class));
				finish();
				break;
			case R.id.collect_external_swing_button:
				startActivity(new Intent(Home.this, RealSwingAnalysisActivity.class));
				finish();
				break;
			case R.id.collect_past_swing_button:
				startActivity(new Intent(Home.this, SwingPastDataFeedback.class));
				finish();
				break;
			case R.id.collect_stats_button:
				startActivity(new Intent(Home.this, StatisticsActivity.class));
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
	 * Name: readPreferenceValues
	 * 
	 * Description:
	 * 		Read setting values which are stored in the shared preferences
	 * 		- COLLECTION_TIME
	 * 		- MAX_THRESHOLD
	 * 		- MIN_THRESHOLD
	 * 		- BEEP_METHOD
	 * 		- PHONE_FRONT_PLACEMENT
	 * Return:
	 * 		None
	 *=============================================================================*/		
	private void readPreferenceValues()
	{
		SharedPreferences pref = getSharedPreferences(PREFERENCE_SETTING, MODE_PRIVATE);
		mCollectionTime = pref.getInt(PREF_COLLECTION_TIME, DEFAULT_COLLECTION_TIME);
		mMaxThreshold = pref.getInt(PREF_MAX_THRESHOLD, DEFAULT_MAX_THRESHOLD);
		mMinThreshold = pref.getInt(PREF_MIN_THRESHOLD, DEFAULT_MIN_THRESHOLD);
		mBeepChecked = pref.getBoolean(PREF_BEEP_METHOD, false);
		mPhoneFrontPlaced = pref.getBoolean(PREF_PHONE_FRONT_PLACEMENT,	false);
		mDataKeepingTime = pref.getInt(PREF_DATA_KEEPING_TIME, 0);
		
		Log.i("collect", "==== readPreferenceValues ====");
		Log.i("collect", "'PREF_COLLECTION_TIME     : " + mCollectionTime);
		Log.i("collect", "PREF_BEEP_METHOD          : " + mBeepChecked);
		Log.i("collect", "PREF_MAX_THRESHOLD        : " + mMaxThreshold);
		Log.i("collect", "PREF_MIN_THRESHOLD        : " + mMinThreshold);
		Log.i("collect", "PREF_PHONE_FRONT_PLACEMENT: " + mPhoneFrontPlaced);
		Log.i("collect", "PREF_DATA_KEEPING_TIME    : " + mDataKeepingTime);
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
	public void makeGolfSwingDataFolders()
	{
		
        String ext = Environment.getExternalStorageState();
        
        if(ext.equals(Environment.MEDIA_MOUNTED))
        {
        	mSDCardPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        	makeOutputDir(mSDCardPath, GOLFSWING_DATA_DIR + EXTERNAL_SWING_DIR);
        	makeOutputDir(mSDCardPath, GOLFSWING_DATA_DIR + COLLECTED_SWING_DIR);
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
	 * 		Make a output directory for writing output files in a SD Card
	 * 		(/mnt/sdcard/data/ + "folder name")		
	 * 		
	 * 
	 * Return:
	 * 		None
	 *=============================================================================*/	    
    public void makeOutputDir(String path, String dir)
    {
    	File outputDir = new File(path + dir);
    	
    	if(!outputDir.exists())
    	{
	    	if(outputDir.mkdirs() == true)
	    		Log.i("home", "mkdir is successful: " + path + dir);
	    	else
	    		Log.i("home", "mkdir failed: " + path + dir);
    	}
    	else
    	{
    		Log.i("home", "The" + path + dir + " exists.");
    	}
    }

	/*=============================================================================
	 * Name: initSoundPool
	 * 
	 * Description:
	 * 		Initialize Sound Pool and load two sounds
	 * 		- Normal points sound
	 * 		- Peak point sound
	 * 
	 * Return:
	 * 		None
	 *=============================================================================*/				
	public void initCollectionSoundPool()
	{
		mSoundPool = new SoundPool(4, AudioManager.STREAM_MUSIC, 0);
	
		mOneSoundId = mSoundPool.load(this, R.raw.one, 1);
		mTwoSoundId = mSoundPool.load(this, R.raw.two, 1);
		mThreeSoundId = mSoundPool.load(this, R.raw.three, 1);
		mBeepSoundId = mSoundPool.load(this, R.raw.ding, 1);
	
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
		
		
		mDateTimeString = getDateTimeString();
		Log.i("collect", "mDateTimeString:" + mDateTimeString);
		
		startCountDownTimer();
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
			if(mPhoneFrontPlaced == true)
			{
				/*
				 *	 Attached to the front part of a body (Chest)
				 *		. accelData.mXvalue = values[0]
				 * 	           ^
				 *             | +y       
				 *          +-----+
				 *          |O    |
				 *          |     |
				 *    <---  |Back | ---> : () : sensor coordinate values
				 *   -x(+x) |     |  +x(-x)
				 *          +-----+
				 *            | -y
				 *            V
				 */            

				//accelData.mXvalue = values[0];
				accelData.mXvalue = -values[0];	// Right direction
				Log.i("collect", "Front(Chest) " + ", Time=" + accelData.mTimestamp
						+ ", X=" + accelData.mXvalue);

			}
			else
			{
				 /* 
				 *     
				 *     
				 *     Attached to the back part of a body (Waist)
				 * 
				 * 		. accelData.mXvalue = -values[0]
				 * 		. X-axis values should be converted to negative values
				 * 
				 * 	           ^
				 *             | +y       
				 *          +-----+
				 *          |     |
				 *          |     |
				 *    <---  |_____| ---> : () : sensor coordinate values
				 *   +x(-x) |  O  |  -x(+x)
				 *          +-----+
				 *            | -y
				 *            V
				 */
				// When a smart phone is attached to the back part of a body (Waist)
				// X values should be changed.

				
				//accelData.mXvalue = -values[0];
				accelData.mXvalue = values[0];	// Right direction
				
				Log.i("collect", "Back(Waist) " + ", Time=" + accelData.mTimestamp
						+ ", X=" + accelData.mXvalue);

			}
			
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
		/*
		Log.i("collect", "C:" + accelData.mIndex 
						+ " T:" + accelData.mTimestamp
						+ " X:" + accelData.mXvalue
						+ " Y:" + accelData.mYvalue
						+ " Z:" + accelData.mZvalue);
		*/
		mSwingDataArrayList.add(accelData);
		
		
	}
	/*=============================================================================
	 * Name: playCountDownSound
	 * 
	 * Description:
	 * 		Play countdown sounds (Three-Two-One)		
	 * 
	 * Return:
	 * 		None
	 *=============================================================================*/
	public void playCountDownSound(int num)
	{
		switch(num)
		{
		case 4:
			mSoundPool.play(mThreeSoundId, 1, 1, 0, 0, 1);
			break;
		case 3:
			mSoundPool.play(mTwoSoundId, 1, 1, 0, 0, 1);
			break;
		case 2:
			mSoundPool.play(mOneSoundId, 1, 1, 0, 0, 1);
			break;
		case 1:
			mSoundPool.play(mBeepSoundId, 1, 1, 0, 0, 1);
			
			break;
		}

	}
	
	/*=============================================================================
	 * Name: startCountDownTimer
	 * 
	 * Description:
	 * 		Count down before starting to collect acceleration data
	 * 		(Three - Two - One - Beep)		
	 * 
	 * Return:
	 * 		None
	 *=============================================================================*/
	public void startCountDownTimer()
	{
		mCountDownTimer = new Handler()
		{
			int count = 4;
			int wait = 0;
			public void handleMessage(Message msg)
			{
				if(wait < COUNTDOWN_TIMER + 1000)
				{
					mCountDownTimer.sendEmptyMessageDelayed(1, 1000);
					wait += 1000;
					
					playCountDownSound(count);
					count--;
				}
				else
				{
					mStartTimeMillis = System.currentTimeMillis();
					Log.i("collect", "Start Time: " + mStartTimeMillis);
					isSoundPlayed = true;
					isRecording = true;
					startCollectingDataCounter();
				}
			}
		};
		
		mCountDownTimer.sendEmptyMessage(1);
	}
	/*=============================================================================
	 * Name: startCollectingDataCounter
	 * 
	 * Description:
	 * 		3 seconds Timer		
	 * 
	 * Return:
	 * 		None
	 *=============================================================================*/	    
	public void startCollectingDataCounter()
	{
		mCollectingDataHandler = new Handler()
		{
			int wait = 0;
			
			public void handleMessage(Message msg)
			{
				//if(wait < TIMEOUT && isSoundPlayed == true)
				if(wait < (mCollectionTime * 1000) && isSoundPlayed == true)
				{
					
					mCollectingDataHandler.sendEmptyMessageDelayed(0, 1000);
					wait += 1000;
					
					displaySecond(wait);
					
				}
				else
				{
					mSoundPool.play(mBeepSoundId, 1, 1, 0, 0, 1);
					
					isRecording = false;
					mEndTimeMillis = System.currentTimeMillis();
					writeAccelerationDataToFile();
					writeOutputTextFile();		// For debugging
					
					//mStartButton.setEnabled(true);
					try {
						Thread.sleep(10);
						goFeedbackActivity();

					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		};
		
		mCollectingDataHandler.sendEmptyMessage(0);
	}
	
	private void goFeedbackActivity()
	{
		Intent intent = new Intent(Home.this, 
									SwingFeedback.class);
		//intent.putExtra("SWING_FILE_NAME", mOutputFileName);
		
		startActivity(intent);
		finish();

	}
	/*=============================================================================
	 * Name: getDateTimeString
	 * 
	 * Description:
	 * 		Get the start date and time for writing a result file		
	 * 
	 * Return:
	 * 		String
	 *=============================================================================*/	
	private String getDateTimeString()
	{
		String stringDateTime = "";
		
		mStartDateString = getDateString();
		AccelerationData.setSwingStartDate(mStartDateString);
		
		mStartTimeString = getTimeString();
		AccelerationData.setSwingStartTime(mStartTimeString);
		
		stringDateTime = mStartDateString + mStartTimeString;
		
		return stringDateTime;
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
		mBeepSoundId = 0;
		mAccelerationCount = 0;
		
		mStartTimeMillis = 0;
		mEndTimeMillis = 0;
		
		mStartDateString = "";
		mStartTimeString = "";
		
		mSwingDataArrayList = new ArrayList<AccelerationData>();
		
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
    	File outputDir = new File(dir + COLLECTED_SWING_PATH);
    	
    	if(outputDir.mkdir() == true)
    		Log.i("Convert", "mkdir is successful: " + dir + COLLECTED_SWING_PATH);
    	else
    		Log.i("Convert", "mkdir failed: " + dir + COLLECTED_SWING_PATH);
    }
	/*=============================================================================
	 * Name: displaySecond
	 * 
	 * Description:
	 * 		Display a remaining second until timeout.		
	 * 		
	 * 
	 * Return:
	 * 		String
	 *=============================================================================*/	        
    public void displaySecond(int time)
    {
    	int second = 0;
    	
    	//second = (TIMEOUT_SEC) - (time/1000);
    	second = mCollectionTime - (time/1000);
    	String timeString = Integer.toString(second);
    	mTimeTextView.setText("Now data is collecting. " + timeString + " seconds left.");
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
		String outFileName = "";
		
		mOutputFileName = OUTPUT_FILENAME +"_" + mDateTimeString + OUTPUT_FILE_EXT;		
		outFileName = mSDCardPath + COLLECTED_SWING_PATH + mOutputFileName;
		
		Log.i("collect", "mOutputFileName:" + mOutputFileName);
		try
		{
			outFile = new FileOutputStream(outFileName);
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
    	String textOutFileName = "";
    	String content = "";
    	String outFileName = "";
    	
    	AccelerationData element = new AccelerationData();
    	File textFile = null;
    	FileOutputStream fout = null;
    	
    	if(!mSDCardPath.isEmpty())
    	{
    		outFileName = mDateTimeString + OUTPUT_TEXTFILE_EXT;
    		 
    		textOutFileName = mSDCardPath + COLLECTED_SWING_PATH 
    						+ OUTPUT_FILENAME + "_" + outFileName;
    		
    		Log.i("collect", "OutFileName" + OUTPUT_FILENAME + "_" + outFileName);
    		try 
    		{
    			textFile = new File(textOutFileName);
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
    	String stringMonthDate = "";
    	String stringMonth = "";
    	String stringDate = "";
    	
    	Calendar today = Calendar.getInstance();
    	
    	/*
    	stringDate = (today.get(Calendar.YEAR) + "-" 
    				+ (today.get(Calendar.MONTH) + 1) + "-"
    				+ today.get(Calendar.DATE));
    	*/
    	
    	int month = (today.get(Calendar.MONTH) +1); 
    	if(month < 10)
    		stringMonth = "0" + month; 
    	else
    		stringMonth = "" + month;
    	
    	int date = today.get(Calendar.DATE);
    	if(date < 10)
    		stringDate = "0" + date;
    	else
    		stringDate = "" + date;
    	
    	stringMonthDate = stringMonth + stringDate;
    	
    	return stringMonthDate;
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
    	if(hour < 10)
    		stringTime += "0" + hour;
    	else
    		stringTime += hour;
    	
    	min = today.get(Calendar.MINUTE);
    	if(min < 10)
    		stringTime += "0" + min;
    	else
    		stringTime += min;    	
    	
    	sec = today.get(Calendar.SECOND);
    	if(sec < 10)
    		stringTime += "0" + sec;
    	else
    		stringTime += sec;
    	
    	return stringTime;
    }
    /*=============================================================================
	 * Name: getSDPathName
	 * 
	 * Description:
	 * 		Check whether a SD card is mounted or not		
	 * 		If mounted, return the absolute SD card path name
	 * 
	 * Return:
	 * 		String
	 *=============================================================================*/	
    public String getSDPathName()
    {
        String ext = Environment.getExternalStorageState();
        String sdPath = "";
        if(ext.equals(Environment.MEDIA_MOUNTED))
        {
        	sdPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        	//mSdPath = sdPath;
        }
        else
        {
        	sdPath = Environment.MEDIA_UNMOUNTED;
        	
        	//mSdPath = "";
        	Toast.makeText(this, "SD card is not mounted", Toast.LENGTH_LONG).show();
        }
        
    	return sdPath;
    }
    
	/*=============================================================================
	 * Name: deletePreviousSwingData
	 * 
	 * Description:
	 * 		Delete files older than the day to save memory space
	 * 
	 * Return:
	 * 		None
	 *=============================================================================*/ 
    public void deletePreviousSwingData(int day, String dir)
    {
    	String sdPathName = getSDPathName();
    	
    	if(sdPathName == Environment.MEDIA_UNMOUNTED)
    		return;
    	
    	File directory = new File(sdPathName + dir);
    	Log.i("collect", "Swing data keeping time is " + day + " days.");
    	
    	int i;
    	if(directory.exists())
    	{
    		File[] listFiles = directory.listFiles();
    		long delTime = System.currentTimeMillis() - (day * 24 * 60 * 60 * 1000);
    		for(i=0; i< listFiles.length; i++)
    		{
    			if(listFiles[i].lastModified() < delTime)
    			{
    				Log.i("collect", "Delete file: " + listFiles[i]);
    				listFiles[i].delete();
    			}
    		}
    	}
    }
}
