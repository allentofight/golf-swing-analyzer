/*-----------------------------------------------------------------------------------------
  File:   SwingFeedback.java

  Author: Jung Chang Su
  -----------------------------------------------------------------------------------------
  Copyright (C) 2012 SICS.
  
    
  
  *----------------------------------------------------------------------------------------*/
package com.SwingAnalyzer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.ObjectInputStream;
import java.util.*;


import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.*;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.*;

public class SwingFeedback extends Activity{
	
	final static String OUTPUT_FILENAME = "swing.dat";
	
	final static String GOLFSWING_DATA_DIR = "/data/GolfSwingAnalyzer";
	final static String EXTERNAL_SWING_DIR = "/externalswing/";
	final static String COLLECTED_SWING_DIR = "/collectedswing/";
	
	final static String COLLECTED_SWING_PATH = GOLFSWING_DATA_DIR + COLLECTED_SWING_DIR;

	private static final String PREFERENCE_SETTING = "settings";
	private static final String PREF_COLLECTION_TIME = "collection_time";
	private static final String PREF_BEEP_METHOD = "beep_method";
	private static final String PREF_MAX_THRESHOLD = "max_threshold";
	private static final String PREF_MIN_THRESHOLD = "min_threshold";

	private static final int DEFAULT_COLLECTION_TIME = 3;
	private static final int DEFAULT_MAX_THRESHOLD = 10;
	private static final int DEFAULT_MIN_THRESHOLD = -5;

	/*
	 *	Message ID for detecting points from X-axis 
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
	
	/*
	 * Message ID for Start point and End point
	 */
	final static int MSG_START_POINT	= 0x100;
	final static int MSG_END_POINT		= 0x200;
	
	final static int MSG_DETECT_DONE_ALL	= 0x300;
	
	final static int MSG_BELOW_THRESHOLD_X	= 0x400;
	final static int MSG_BELOW_THRESHOLD_Y	= 0x500;
	/* 
	 *	Message ID for converting raw data to AccelerationData format 
	 */
	final static int MSG_CONVERSION_DONE	= 0x600;
	final static int MSG_CONVERSION_ERROR	= 0x601;
	final static int MSG_CONVERTION_WORKING	= 0x602;

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
	final static int X_AXIS = 0;
	final static int Y_AXIS = 1;
	
	/*
	 * Button color to be displayed
	 */
	/*
	 * Button color to be displayed
	 */
	final static int COLOR_BLACK = 0;		// Default
	final static int COLOR_GREEN = 1;		// 
	final static int COLOR_RED	 = 2;		// Maximum
	final static int COLOR_YELLOW = 3;		// Minimum
	
	/*
	 * Timeout and time scale to calculate
	 */
	final static int TIME_SCALE = 10;
	final static int COLLECTION_TIME = (3*1000);
	final static int TIME_INTERVAL = (COLLECTION_TIME / TIME_SCALE);	
	final static int TIMEOUT = COLLECTION_TIME;
	
	/*
	 * Peak point
	 */
	final static int NORMAL_POINT = 0;
	final static int MIN_POINT = 1;
	final static int MAX_POINT = 2;
	
	ImageView mXImages[] = new ImageView[TIME_SCALE];
	ImageView mYImages[] = new ImageView[TIME_SCALE];
	
	int mXImageViewIds[] = {R.id.img_x1, R.id.img_x2, R.id.img_x3, R.id.img_x4, R.id.img_x5, 
							R.id.img_x6, R.id.img_x7, R.id.img_x8, R.id.img_x9, R.id.img_x10};
	
	int mYImageViewIds[] = {R.id.img_y1, R.id.img_y2, R.id.img_y3, R.id.img_y4, R.id.img_y5, 
							R.id.img_y6, R.id.img_y7, R.id.img_y8, R.id.img_y9, R.id.img_y10};

	int mSwingXResult[] = new int[TIME_SCALE];
	int mSwingYResult[] = new int[TIME_SCALE];
	
	Handler mTimerHandler;
	
	/*
	 * Sound ID
	 */
	SoundPool mSoundPool;	
	int mNormalSoundId;
	int mMaxSoundId;
	int mMinSoundId;
	
	private boolean mIsAboveThresholdX;
	private boolean mIsAboveThresholdY;

	String mResultFileName;
	String mSwingFileName;
	String mSDCardPath;
	String mSelectedFile;
	
	int mWhichAxis;
	
	private String mStartDateString;
	private String mStartTimeString;
	private boolean mSwingStarted;
	
	//DetectPeakThread mDetectPeakThread;
	
	DetectSwingThread mDetectSwingThread;
	/* 
	 * Database Handler
	 */
	DatabaseHandler mDatabaseHandler;
	
	
	// Variables for the analyzed swing data
	int mStartIndex;	// The start point of a swing
	int mSwingStartTime;
	int mEndIndex;		// The end point of a swing
	int mSwingEndTime;
	
	int mXMaxIndex;		// The maximum point of X-axis
	int mXMinIndex;		// The minimum point of X-axis
	int mYMaxIndex;		// The maximum point of Y-axis	
	int mYMinIndex;		// The minimum point of Y-axis
	

	ArrayList<AccelerationData> mSwingDataArrayList = null;	
	
	SwingTimeScale mXTimeScale;
	SwingTimeScale mYTimeScale;

	//Ruler mRulerX;
	//Ruler mRulerY;
	/*
	 * Widgets 
	 */
	Button mAnalysisButton;
	Button mPreviousSwingButton;
	Button mBackButton;
	
	TextView mXTextView;
	TextView mYTextView;
	TextView mFeedbackTextView;
	
	Spinner mSwingFileSpinner;
	
	ArrayAdapter<String> spinnerAdapter;
	/* 
	 * SharedPreference Values 
	 */
	private int mCollectionTime = 0;
	private boolean mMusicalNoteChecked = false;
	
	private int mMaxThreshold = 0;		// Threshold of X-axis
	private int mMinThreshold = 0;		// Threshold of Y-axis

	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.swing_feedback);
		
		/*=================================================================
		 * Draw X-axis time scales
		 *=================================================================*/		
		LinearLayout layout_xscale = (LinearLayout)findViewById(R.id.feedback_timescale_x);
		layout_xscale.addView(mXTimeScale = new SwingTimeScale(this));
		mXTimeScale.setScale(TIME_SCALE, COLLECTION_TIME);

		/*=================================================================
		 * Draw Y-axis time scales
		 *=================================================================*/		
		LinearLayout layout_yscale = (LinearLayout)findViewById(R.id.feedback_timescale_y);
		layout_yscale.addView(mYTimeScale = new SwingTimeScale(this));
		mYTimeScale.setScale(TIME_SCALE, COLLECTION_TIME);
		
		/*=================================================================
		 * Create DatabaseHandler
		 *=================================================================*/
		mDatabaseHandler = new DatabaseHandler(this);
		
		/* 
		 * Widgets
		 */
		mSwingFileSpinner = (Spinner)findViewById(R.id.feedback_swing_spinner);
		mSwingFileSpinner.setOnItemSelectedListener(mItemSelectedListener);
		
    	spinnerAdapter = new ArrayAdapter<String>(mSwingFileSpinner.getContext(),
									android.R.layout.simple_spinner_item);

		mAnalysisButton = (Button)findViewById(R.id.result_button);
		mAnalysisButton.setOnClickListener(mClickListener);
		
		mPreviousSwingButton = (Button)findViewById(R.id.previous_swing_button);
		mPreviousSwingButton.setOnClickListener(mClickListener);
		
		mBackButton = (Button)findViewById(R.id.back_button);
		mBackButton.setOnClickListener(mClickListener);
		
		mXTextView = (TextView)findViewById(R.id.feedback_x_textview);
		mYTextView = (TextView)findViewById(R.id.feedback_y_textview);
		mFeedbackTextView = (TextView)findViewById(R.id.feedback_result);
		
		
		initMemberVariables();
		setImageViewResource();
		initSoundPool();
		readPreferenceValues();
		
		//searchSwingFiles();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		
		initMemberVariables();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		searchSwingFiles();
	}

	Button.OnClickListener mClickListener = new View.OnClickListener() {
		
		public void onClick(View v) {
			switch(v.getId())
			{
			case R.id.result_button:
				analyzeSwingData();
				break;
			case R.id.previous_swing_button:
				
				break;
			case R.id.back_button:
				Intent intent = new Intent(SwingFeedback.this, CollectingAccelerationData.class);
				startActivity(intent);
				finish();
				break;
			}
			
		}
	};
	
	private AdapterView.OnItemSelectedListener mItemSelectedListener = new AdapterView.OnItemSelectedListener()
	{

		public void onItemSelected(AdapterView<?> parent, View view, int position, long id) 
		{
			mSelectedFile = (String)parent.getSelectedItem();
			Log.i("feedback", "Selected File: " + mSelectedFile);
		}

		public void onNothingSelected(AdapterView<?> arg0) {
			// TODO Auto-generated method stub
			
		}
		
	};

    /*=============================================================================
	 * Name: searchSwingFiles
	 * 
	 * Description:
	 * 		Check whether a SD card is mounted or not.
	 * 		Search for swing raw data files under a specific folder
	 * 		("/mnt/sdcard/data/GolfSwingAnalyzer/collectedswing/")
	 * 
	 * Return:
	 * 		None
	 *=============================================================================*/
	public void searchSwingFiles()
	{
    	String stringSdPath = "";
    	
    	stringSdPath = getSDPathName();
    	
    	if(stringSdPath != Environment.MEDIA_UNMOUNTED)
    	{
    		searchFilesinSdPath(stringSdPath + GOLFSWING_DATA_DIR + COLLECTED_SWING_DIR);    		
    	}
    	else
    		mSelectedFile = "";
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
	 * Name: searchFilesinSdPath
	 * 
	 * Description:
	 * 		Get golf swing file names in the specific folder 
	 * 		- (/mnt/sdcare/data/GolfSwingAnalyzer/externalswing)		
	 * 		Input those file names to a Spinner widget
	 * 
	 * Return:
	 * 		None
	 *=============================================================================*/	    
    public void searchFilesinSdPath(String swingDataPath)
    {
    	File swingDir = new File(swingDataPath);    	
    	
    	Log.i("feedback", "Collected Swing Dir Path: " + swingDataPath);
    	
    	if(swingDir.isDirectory())
    	{
        	String[] fileNameList = swingDir.list(new FilenameFilter()
        	{
        		public boolean accept(File dir, String name)
        		{
        			return name.endsWith("dat");
        		}
        	});
        	
        	
        	String[] sortedFileNameList = new String[fileNameList.length];
        	sortedFileNameList = doNaturalSorting(fileNameList);

        	insertFileNameToSpinner(sortedFileNameList);
    	}
    	
    }
	/*=============================================================================
	 * Name: doNaturalSorting
	 * 
	 * Description:
	 * 		Display filenames according to the file number
	 * 		Example) swing_05134.dat 
	 * 
	 * Return:
	 * 		None
	 *=============================================================================*/	    
    private String[] doNaturalSorting(String[] array)
    {	
		String tmp = "";
		
		String s1 = "";
		String s2 = "";
		int i1, i2;		
		int indexOfDash = 0;
		
		
		for(int i=0; i< (array.length-1); i++)
		{
		
			for(int j=0; j<= (array.length-2); j++)
			{
				indexOfDash = array[j].indexOf("_");
				s1 = array[j].substring(indexOfDash+1, array[j].indexOf("."));
				s2 = array[j+1].substring(indexOfDash+1, array[j+1].indexOf("."));

				i1 = Integer.parseInt(s1);
				i2 = Integer.parseInt(s2);
				
				if(i1 < i2)
				{
					tmp = array[j];
					array[j] = array[j+1];
					array[j+1] = tmp;				
				}				
			}
		}
		
    	return array;
    }

	/*=============================================================================
	 * Name: insertFileNameToSpinner
	 * 
	 * Description:
	 * 		Insert file names to a Spinner widget		
	 * 
	 * Return:
	 * 		None
	 *=============================================================================*/	    
    private void insertFileNameToSpinner(String[] filenames)
    {
    	/*
    	ArrayAdapter<String> spinnerAdapter = 
    						new ArrayAdapter<String>(mSwingFileSpinner.getContext(),
    											android.R.layout.simple_spinner_item,
    											filenames);
    	*/
    	for(int i = 0; i < filenames.length; i++)
    	{
    		spinnerAdapter.add(filenames[i]);
    	}
    	spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    	mSwingFileSpinner.setAdapter(spinnerAdapter);
    }


	/*=============================================================================
	 * Name: initMemberVariables
	 * 
	 * Description:
	 * 		Initialize member variables
	 * 
	 * Return:
	 * 		None
	 *=============================================================================*/						
	public void initMemberVariables()
	{
		mSwingDataArrayList = new ArrayList<AccelerationData>();
		
		mResultFileName = "";
		mSwingFileName = "";
		mSelectedFile = "";
		
		mWhichAxis = 0;
		
		mStartDateString = "";
		mStartTimeString = "";
		mSwingStarted = false;
		
		for(int i=0; i<TIME_SCALE; i++)
		{
			mSwingXResult[i] = 0;
			mSwingYResult[i] = 0;
		}
		
		mFeedbackTextView.setText("");
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
	 * 
	 * Return:
	 * 		None
	 *=============================================================================*/		
	private void readPreferenceValues()
	{
		SharedPreferences pref = getSharedPreferences(PREFERENCE_SETTING, MODE_PRIVATE);
		mCollectionTime = pref.getInt(PREF_COLLECTION_TIME, DEFAULT_COLLECTION_TIME);
		mMaxThreshold = pref.getInt(PREF_MAX_THRESHOLD, DEFAULT_MAX_THRESHOLD);
		mMinThreshold = pref.getInt(PREF_MIN_THRESHOLD, DEFAULT_MIN_THRESHOLD);
		mMusicalNoteChecked = pref.getBoolean(PREF_BEEP_METHOD, false);
		
	}

	/*=============================================================================
	 * Name: clearSwingResult
	 * 
	 * Description:
	 * 		Reset Swing Result array for the next test
	 * 		Clear Feedback Text View
	 * 
	 * Return:
	 * 		None
	 *=============================================================================*/	
	public void clearSwingResult()
	{
		for(int i=0; i<TIME_SCALE; i++)
		{
			mSwingXResult[i] = 0;
			mSwingYResult[i] = 0;
		}
		
		mFeedbackTextView.setText("");
	}
	/*=============================================================================
	 * Name: getResultFileName
	 * 
	 * Description:
	 * 		Check whether a SD card is mounted or not
	 * 		If a SD card is mounted, find the "/data/acceldata/swing.dat" file.
	 * 
	 * Return:
	 * 		None
	 *=============================================================================*/					
	public boolean getResultFileName()
	{
		boolean isFound = false;
        String ext = Environment.getExternalStorageState();
        String resultFile = "";
        
        if(mSelectedFile.isEmpty())
        {
        	return false;
        }
        
        if(ext.equals(Environment.MEDIA_MOUNTED))
        {
        	mSDCardPath = Environment.getExternalStorageDirectory().getAbsolutePath();  
        	
        	resultFile = mSDCardPath + COLLECTED_SWING_PATH + mSelectedFile;
        	
        	File file = new File(resultFile);
        	
        	if(file.exists())
        	{
        		mResultFileName = resultFile;
        		isFound = true;
        		Log.i("feedback", "ResultFileName: " + mResultFileName);
        	}
        	else
        	{
        		resultFile = "";
        		isFound = false;
        		Toast.makeText(this, "The result file does not exist", Toast.LENGTH_LONG).show();
        	}
        }
        else
        {
        	mSDCardPath = Environment.MEDIA_UNMOUNTED;
        	mResultFileName = "";
        	isFound = false;
        	Toast.makeText(this, "SD card is not mounted", Toast.LENGTH_LONG).show();
        }
        
        return isFound;
	}
	
	/*=============================================================================
	 * Name: analyzeSwingData
	 * 
	 * Description:
	 * 		Create a thread to detect the peak points of X and Y-axis data
	 * 
	 * Return:
	 * 		None
	 *=============================================================================*/
	public void analyzeSwingData()
	{
		resetIconColor(X_AXIS);
		resetIconColor(Y_AXIS);
		clearSwingResult();
		
		if(getResultFileName() == true)
			readArrayListFromFile();
		else
		{
			Toast.makeText(this, "The result file does not exist", Toast.LENGTH_LONG).show();
			return;
		}
		
		if(!mResultFileName.isEmpty())
		{
			mDetectSwingThread = new DetectSwingThread(FeedbackHandler,
														(ArrayList)mSwingDataArrayList,
														mMaxThreshold, mMinThreshold);
			mDetectSwingThread.setDaemon(true);
			mDetectSwingThread.start();
			/*
			mDetectPeakThread = new DetectPeakThread(mSwingDataArrayList, 
													FeedbackHandler);
			
			mDetectPeakThread.setDaemon(true);
			mDetectPeakThread.start();
			*/
		}
		else
		{
			Toast.makeText(this, "The file name does not exist", Toast.LENGTH_LONG).show();
		}		
	}
	
	/*=============================================================================
	 * Name: startSwingFeedback
	 * 
	 * Description:
	 * 		1. Prepare the feedback of a real swing 
	 * 		2. Check the timeslot of each critical points' index 		  	
	 * 
	 * Return:
	 * 		None
	 *=============================================================================*/ 	
	public void startSwingFeedback()
	{
		int mXMaxTime = 0;
		int mXMinTime = 0;
		int mYMaxTime = 0;
		int mYMinTime = 0;
		
		float mXMaxValue = 0;
		float mYMinValue = 0;
		
		if(mStartIndex == -1|| mEndIndex == -1)
		{
			if(mStartIndex == -1)
				showErrorDialog("Error", "Cannot detect a start point");
			else
				showErrorDialog("Error", "Cannot detect an end point");
		}
		else
		{
			mSwingStartTime = mSwingDataArrayList.get(mStartIndex).mTimestamp;
			mSwingEndTime = mSwingDataArrayList.get(mEndIndex).mTimestamp;

			mXMaxTime = mSwingDataArrayList.get(mXMaxIndex).mTimestamp;			
			mXMaxValue = mSwingDataArrayList.get(mXMaxIndex).mXvalue;
			
			mXMinTime = mSwingDataArrayList.get(mXMinIndex).mTimestamp;
			
			// Draw lines and text
			mXTimeScale.setFeedbackScale(TIME_SCALE, mSwingStartTime, mSwingEndTime);
			mYTimeScale.setFeedbackScale(TIME_SCALE, mSwingStartTime, mSwingEndTime);
			
			/*
			 * When only the maximum value of X-axis is greater than Threshold(10),
			 * find a peak point.
			 */
			if((int)mXMaxValue >= mMaxThreshold)
			{
				mIsAboveThresholdX = true;
				findPeakTimeIndex(mSwingStartTime, mSwingEndTime, mXMaxTime, X_AXIS, MAX_POINT);
				findPeakTimeIndex(mSwingStartTime, mSwingEndTime, mXMinTime, X_AXIS, MIN_POINT);
			}
			else
			{
				mIsAboveThresholdX = false;
			}
			
			mYMaxTime = mSwingDataArrayList.get(mYMaxIndex).mTimestamp;
			mYMinTime = mSwingDataArrayList.get(mYMinIndex).mTimestamp;
			mYMinValue = mSwingDataArrayList.get(mYMinIndex).mYvalue;
			
			/*
			 * When only the maximum value of Y-axis is less than Threshold(-5),
			 * find a peak point.
			 */
			if((int)mYMinValue <= mMinThreshold)
			{
				mIsAboveThresholdY = true;
				findPeakTimeIndex(mSwingStartTime, mSwingEndTime, mYMaxTime, Y_AXIS, MAX_POINT);
				findPeakTimeIndex(mSwingStartTime, mSwingEndTime, mYMinTime, Y_AXIS, MIN_POINT);
			}
			else
			{
				mIsAboveThresholdY = false;
			}
			
			/*
			 * Display color and make beep sounds according to the values
			 */
			displayAnalysisResult();
		}
	}

	/*=============================================================================
	 * Name: showErrorDialog
	 * 
	 * Description:
	 * 		Show an alert dialog when an error happens
	 *=============================================================================*/	
	public void showErrorDialog(String title, String message)
	{
		AlertDialog.Builder alertDlg 
		= new AlertDialog.Builder(SwingFeedback.this);
	
		alertDlg.setTitle(title);
		alertDlg.setMessage(message);
		alertDlg.setIcon(R.drawable.golf_analyzer_icon);
		alertDlg.setPositiveButton("Close", null);
		alertDlg.show();
	}

	/*=============================================================================
	 * Name: setImageViewResource
	 * 
	 * Description:
	 * 		Connect ImageView resources to an ImageView array
	 * 
	 * Return:
	 * 		None
	 *=============================================================================*/			
	public void setImageViewResource()
	{
		for(int i=0; i<TIME_SCALE; i++)
		{
			mXImages[i] = (ImageView)findViewById(mXImageViewIds[i]);
			mYImages[i] = (ImageView)findViewById(mYImageViewIds[i]);
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
	public void initSoundPool()
	{
		mSoundPool = new SoundPool(2, AudioManager.STREAM_MUSIC, 0);
		
		mNormalSoundId = mSoundPool.load(this, R.raw.normal_pitch, 1);		
		mMaxSoundId = mSoundPool.load(this, R.raw.high_pitch, 1);
		mMinSoundId = mSoundPool.load(this, R.raw.low_pitch, 1);

	}
	
	/*=============================================================================
	 * Name: displayResultWithSoundIcon
	 * 
	 * Description:
	 * 		Change the color of X-axis button icons
	 * 
	 * Return:
	 * 		None
	 *=============================================================================*/		
	public void displayResultWithSoundIcon(int index, int color, int axis)
	{
		if(axis == X_AXIS)
		{
			switch(color)
			{
			case COLOR_BLACK:
				mXImages[index].setImageResource(R.drawable.black_button_30);
				break;
			case COLOR_GREEN:
				mXImages[index].setImageResource(R.drawable.green_button_30);
				mSoundPool.play(mNormalSoundId, 1, 1, 0, 0, 1);
				break;
			case COLOR_RED:
				mXImages[index].setImageResource(R.drawable.red_button_30);
				mSoundPool.play(mMaxSoundId, 1, 1, 0, 0, 1);
				break;
			case COLOR_YELLOW:
				mXImages[index].setImageResource(R.drawable.yellow_button_30);
				mSoundPool.play(mMinSoundId, 1, 1, 0, 0, 1);
				break;
			}
		}
		else
		{
			switch(color)
			{
			case COLOR_BLACK:
				mYImages[index].setImageResource(R.drawable.black_button_30);
				break;
			case COLOR_GREEN:				
				mYImages[index].setImageResource(R.drawable.green_button_30);
				mSoundPool.play(mNormalSoundId, 1, 1, 0, 0, 1);
				break;
			case COLOR_RED:				
				mYImages[index].setImageResource(R.drawable.red_button_30);
				mSoundPool.play(mMaxSoundId, 1, 1, 0, 0, 1);
				break;
			case COLOR_YELLOW:
				mYImages[index].setImageResource(R.drawable.yellow_button_30);
				mSoundPool.play(mMinSoundId, 1, 1, 0, 0, 1);
				break;
			}

		}
	}
	/*=============================================================================
	 * Name: findPeakTimeIndex
	 * 
	 * Description:
	 * 		Find an index using a given timestamp
	 * 
	 * Return:
	 * 		None
	 *=============================================================================*/		
	/*=============================================================================
	 * Name: findPeakTimeIndex
	 * 
	 * Description:
	 * 		Find an index using a given timestamp
	 * 		type: MAX_POINT, MIN_POINT
	 * Return:
	 * 		None
	 *=============================================================================*/		
	public void findPeakTimeIndex(int start, int end, int timestamp, int axis, int type)
	{	
		int index = 0;
		int interval = 0;
		
		if(timestamp == 0)
			timestamp = 1;
		
		interval = (end - start)/TIME_SCALE;
		Log.i("realswing", "interval:" + interval);
		
		if(timestamp >= start)
			index = (timestamp - start)/interval;
		else
			index = 0;
		
		if(index >= TIME_SCALE)
			index = (TIME_SCALE-1);
		
		
		if(axis == X_AXIS)
		{
			Log.i("realswing", "X Type:" + type + ", Index: " + index + ", Time:" + timestamp);
			mSwingXResult[index] = type;
		}
		else
		{
			Log.i("realswing", "Y Type:" + type + ", Index: " + index + ", Time:" + timestamp);
			mSwingYResult[index] = type;
		}		
	}
	/*=============================================================================
	 * Name: showResultTimeSlot
	 * 
	 * Description:
	 * 		Display each timeslot with an Icon and a beep sound
	 * 
	 * Return:
	 * 		None
	 *=============================================================================*/			
	public void showResultTimeSlot(int index, int axis)
	{
		if(axis == X_AXIS)
		{
			if(mSwingXResult[index] == MAX_POINT)
				displayResultWithSoundIcon(index, COLOR_RED, X_AXIS);
			else if(mSwingXResult[index] == MIN_POINT)
				displayResultWithSoundIcon(index, COLOR_YELLOW, X_AXIS);
			else
				displayResultWithSoundIcon(index, COLOR_GREEN, X_AXIS);
		}
		else
		{
			if(mSwingYResult[index] == MAX_POINT)
				displayResultWithSoundIcon(index, COLOR_RED, Y_AXIS);
			else if(mSwingYResult[index] == MIN_POINT)
				displayResultWithSoundIcon(index, COLOR_YELLOW, Y_AXIS);
			else
				displayResultWithSoundIcon(index, COLOR_GREEN, Y_AXIS);			
		}

	}
	/*=============================================================================
	 * Name: resetIconColor
	 * 
	 * Description:
	 * 		Display each timeslot with an Icon and a beep sound
	 * 
	 * Return:
	 * 		None
	 *=============================================================================*/			
	public void resetIconColor(int axis)
	{
		for(int i=0; i<TIME_SCALE; i++)
		{
			displayResultWithSoundIcon(i, COLOR_BLACK, axis);
		}
	}
	/*=============================================================================
	 * Name: FeedbackHandler
	 * 
	 * Description:
	 * 		Process handler messages for detecting some points	
	 * 		- arg1: index of the peak point
	 * 		- arg2: timestamp of the peak point
	 * 
	 * Return:
	 * 		None
	 *=============================================================================*/    
    Handler FeedbackHandler = new Handler()
    {
    	public void handleMessage(Message msg)
    	{
    		String handlerText = "";
    		float value = 0;
    		
    		switch(msg.what)
    		{
    		case MSG_START_POINT:
    			mStartIndex = msg.arg1;
    			handlerText = "START_POINT: index=" + msg.arg1 + ", Time:" + msg.arg2;
    			displayResultText(handlerText);
    			break;
    		case MSG_END_POINT:
    			mEndIndex = msg.arg1;
    			handlerText = "END_POINT: index=" + msg.arg1 + ", Time:" + msg.arg2;
    			displayResultText(handlerText);
    			break;
    		
    		case MSG_PEAK_X_MAX:    			
    			mXMaxIndex = msg.arg1;    	
    			value = mSwingDataArrayList.get(mXMaxIndex).mXvalue;
    			handlerText = "X_MAX: Time:" + msg.arg2 + ", X=" + value;    			
    			mXTextView.setText(handlerText);    			
    			displayResultText(handlerText);  
    			//showFeedbackResultText(msg.arg1, msg.arg2, msg.what);
    			break;
    		case MSG_PEAK_X_MIN:
    			mXMinIndex = msg.arg1;
    			value = mSwingDataArrayList.get(mXMinIndex).mXvalue;
    			handlerText = "X_MIN: Time:" + msg.arg2 + ", X=" + value;
    			displayResultText(handlerText);
    			
    			//showFeedbackResultText(msg.arg1, msg.arg2, msg.what);
    			break;
    		case MSG_PEAK_Y_MAX:
    			mYMaxIndex = msg.arg1;
    			value = mSwingDataArrayList.get(mYMaxIndex).mYvalue;
    			handlerText = "Y_MAX: Time:" + msg.arg2 + ", Y=" + value;
    			displayResultText(handlerText);
    			break;    			
    		case MSG_PEAK_Y_MIN:
    			mYMinIndex = msg.arg1;    			
    			value = mSwingDataArrayList.get(mYMinIndex).mYvalue;
    			handlerText = "Y_MIN: Time:" + msg.arg2 + ", Y=" + value;
    			mYTextView.setText(handlerText);
    			displayResultText(handlerText);

    			//showFeedbackResultText(msg.arg1, msg.arg2, msg.what);    			
    			break;
    		case MSG_DETECT_DONE_X:
    			handlerText = "DETECT_DONE_X";
    			displayResultText(handlerText);
    			break;
    		case MSG_DETECT_DONE_Y:
    			handlerText = "DETECT_DONE_Y";
    			displayResultText(handlerText);
    			break;
    		case MSG_DETECT_DONE_ALL:
    			handlerText = "DETECT_DONE_ALL";
    			displayResultText(handlerText);
    			
    			startSwingFeedback();
    			addFeedbackToDatabase();
    			break;
    		}
    		
    	}
    };
	
	/*=============================================================================
	 * Name: displayAnalysisResult
	 * 
	 * Description:
	 * 		Check timer and send a message to other functions		
	 * 
	 * Return:
	 * 		None
	 *=============================================================================*/    
	public void displayAnalysisResult()
	{
		mTimerHandler = new Handler()
		{
			int wait = 0;
			int timeIndex = 0;
			public void handleMessage(Message msg)
			{
				/*
				 *  To display two axis icon, it waits for (TIMEOUT *2)
				 */
				if(wait < (TIMEOUT * 2))
				{
					mTimerHandler.sendEmptyMessageDelayed(0, TIME_INTERVAL);
					wait += TIME_INTERVAL;
					
					if(timeIndex < TIME_SCALE)
						showResultTimeSlot(timeIndex, X_AXIS);
					else
						showResultTimeSlot(timeIndex-TIME_SCALE, Y_AXIS);
					
					timeIndex++;
				}
				else
				{
					if(mIsAboveThresholdX == false && mIsAboveThresholdY == true)
					{
						showErrorDialog("Weak Swing", "Swing values of X-axis are less than " + mMaxThreshold +".");
					}
					else if(mIsAboveThresholdX == true && mIsAboveThresholdY == false)
					{
						showErrorDialog("Weak Swing", "Swing values of Y-axis are less than " + mMinThreshold + ".");
					}
					else if(mIsAboveThresholdX == false && mIsAboveThresholdY == false)
					{
						showErrorDialog("Weak Swing", "Both swing values(X, Y-axis) are too small.(Max=" 
										+ mMaxThreshold + ", Min=" + mMinThreshold + ")");
					}

				}
			}
		};
		
		mTimerHandler.sendEmptyMessage(0);
	}
	
    public void displayResultText(String text)
    {
    	mFeedbackTextView.append(text + "\n");
    	Log.i("feedback", text);
    }
	
	/*=============================================================================
	 * Name: showFeedbackResultText
	 * 
	 * Description:
	 * 		Display the timestamp and x-axis value of the peak point.
	 * 
	 * Return:
	 * 		None
	 *=============================================================================*/     		
	public void showFeedbackResultText(int index, int timestamp, int msg)
	{
		String result = "";
		
		AccelerationData accel = new AccelerationData();
		
		accel = mSwingDataArrayList.get(index);
		
		Log.i("feedback", "Result index: " + index 
											+ ", T:" + accel.mTimestamp 
											+ ", X:" + accel.mXvalue
											+ ", Y:" + accel.mYvalue);
		
		if(msg == MSG_PEAK_X_MAX)
		{				
			result = "X-axis Peak [Time: " + accel.mTimestamp 
										+ ", X: " + accel.mXvalue + "]" + "\n";
			
			mXTextView.setText(result);
		}
		else if(msg == MSG_PEAK_Y_MIN)
		{	
			result = "Y-axis Peak [Time: " + accel.mTimestamp 
										+ ", Y: " + accel.mYvalue + "]" + "\n";
			mYTextView.setText(result);
		}

		//mFeedbackTextView.setText(result);
	}
	
	public void showAllResult(int maxIndex, int minIndex, int axis)
	{
		String resultStringMax = "";
		String resultStringMin = "";
		
		AccelerationData accelMax = new AccelerationData();
		AccelerationData accelMin = new AccelerationData();
		
		accelMax = mSwingDataArrayList.get(maxIndex);
		accelMin = mSwingDataArrayList.get(minIndex);
		
		if(axis == X_AXIS)
		{
			
			resultStringMax = "The + Peak of X " + "[Time: " + accelMax.mTimestamp 
											+ ", X: " + accelMax.mXvalue + "]" + "\n";
			
			resultStringMin = "The - Peak of X " + "[Time: " + accelMin.mTimestamp 
											+ ", X: " + accelMin.mXvalue + "]" + "\n";
			mFeedbackTextView.append(resultStringMax);
			mFeedbackTextView.append(resultStringMin);
		}
		else
		{
			
			resultStringMin = "The - Peak of Y " + "[Time: " + accelMin.mTimestamp 
											+ ", Y: " + accelMin.mYvalue + "]" + "\n";
			resultStringMax = "The + Peak of Y " + "[Time: " + accelMax.mTimestamp 
											+ ", Y: " + accelMax.mYvalue + "]" + "\n";

			mFeedbackTextView.append(resultStringMin);
			mFeedbackTextView.append(resultStringMax);
		}
		
		
	}
	/*=============================================================================
	 * Name: readArrayListFromFile
	 * 
	 * Description:
	 * 		Read objects from the file which was converted to the AccelerationData format
	 * 		Save the object to  ArrayList<AccelerationData>	
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
			inputStream = new FileInputStream(mResultFileName);
			objInputStream = new ObjectInputStream(inputStream);
						
			mSwingDataArrayList = (ArrayList<AccelerationData>)objInputStream.readObject();
			
			Log.i("detectpeak", "mSwingArrayList.size: " + mSwingDataArrayList.size());
			
			objInputStream.close();
		} 
		catch(Exception e)
		{
			Log.e("Debug", e.getMessage());
		}

	}
	
	/*=============================================================================
	 * Name: addFeedbackToDatabase
	 * 
	 * Description:
	 * 		Insert feedback information to the database
	 * 
	 * Return:
	 * 		None
	 *=============================================================================*/     			
	public void addFeedbackToDatabase()
	{
		String date="";
		String time= "";
		String x_max = "";
		String x_max_time = "";
		String x_min = "";
		String x_min_time = "";
		String y_max = "";
		String y_max_time = "";
		String y_min = "";
		String y_min_time = "";

		AccelerationData element = new AccelerationData();

		date = mStartDateString;
		time = mStartTimeString;
		
		element = mSwingDataArrayList.get(mXMaxIndex);
		x_max = String.valueOf(element.mXvalue);
		x_max_time = String.valueOf(element.mTimestamp);
		
		element = mSwingDataArrayList.get(mXMinIndex);
		x_min = String.valueOf(element.mXvalue);
		x_min_time = String.valueOf(element.mTimestamp);
		
		element = mSwingDataArrayList.get(mYMaxIndex);
		y_max = String.valueOf(element.mYvalue);
		y_max_time = String.valueOf(element.mTimestamp);
		
		element = mSwingDataArrayList.get(mYMinIndex);
		y_min = String.valueOf(element.mYvalue);
		y_min_time = String.valueOf(element.mTimestamp);
		
		Log.i("feedback", "addSwingStats: date:" + date + ", time:" + time);
		Log.i("feedback", "x_max:" + x_max + ", x_max_time:" + x_max_time);
		Log.i("feedback", "x_min:" + x_min + ", x_min_time:" + x_min_time);
		Log.i("feedback", "y_max:" + y_max + ", y_max_time:" + y_max_time);
		Log.i("feedback", "y_min:" + y_min + ", y_min_time:" + y_min_time);
		
		SwingStatistics swing = new SwingStatistics(date, time, 
												x_max, x_max_time, x_min, x_min_time,
												y_max, y_max_time, y_min, y_min_time);
		
		mDatabaseHandler.addSwingStats(swing);
	}
	
	
}

/*=============================================================================
 * Class Name: Ruler
 * 
 * Description:
 * 		Draw lines and texts for time scales
 * 		Derived from View class	
 * 
 * Return:
 * 		None
 *=============================================================================*/     		

class Ruler extends View
{
	int mScale=0;		
	int mInterval = 0;
	final static int MARGIN = 5;
	
	public Ruler(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}
	
	public Ruler(Context context, AttributeSet attrs){
		super(context, attrs);
	}
	
	public Ruler(Context context) {
		super(context);
	}
	
	public void setScale(int scale, int collection_time) {
		mScale = scale;
		mInterval = collection_time / mScale;
		Log.i("scale", "mInterval:" + mInterval);
		
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
				
		String text = "";
		
		
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
		
		// (mScale+1) is needed to draw "|"
		for(int unit=0; unit <= mScale; unit++)
		{
			Pnt.setAntiAlias(false);
			
			if(unit == 0)
				x = MARGIN;
			else
				x = ((width/mScale) * unit) + MARGIN;			
			
			y = scaleSize;
			canvas.drawLine(x, 20, x, y+textSize+20, Pnt);			
			
			Pnt.setAntiAlias(true);
			text = "" + (mInterval * unit);			
				
			canvas.drawText(text, x, y, Pnt);			
		}
		
	}
}	
