/*-----------------------------------------------------------------------------------------
File:   SwingPastDataFeedback.java

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

public class SwingPastDataFeedback extends Activity{
	
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
	private static final String PREF_PHONE_FRONT_PLACEMENT = "placement";
	
	private static final int DEFAULT_COLLECTION_TIME = 3;
	private static final int DEFAULT_MAX_THRESHOLD = 5;
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
	final static int COLOR_BLACK 	= 0;		// Reset
	final static int COLOR_GREEN 	= 1;		// Ordinary
	final static int COLOR_RED	 	= 2;		// Maximum
	final static int COLOR_YELLOW 	= 3;		// Minimum
	final static int COLOR_BOTH   	= 4;		// Maximum & Minimum
	
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
	final static int NORMAL_POINT 	= 0X00;
	final static int MIN_POINT 		= 0x01;
	final static int MAX_POINT 		= 0x10;
	final static int BOTH_POINT 	= 0x11;
	
	final static int MUSICAL_NOTE_NUM = 35;
	
	ImageView mXImages[] = new ImageView[TIME_SCALE];
	ImageView mYImages[] = new ImageView[TIME_SCALE];
	
	int mXImageViewIds[] = {R.id.img_x1, R.id.img_x2, R.id.img_x3, R.id.img_x4, R.id.img_x5, 
							R.id.img_x6, R.id.img_x7, R.id.img_x8, R.id.img_x9, R.id.img_x10};
	
	int mYImageViewIds[] = {R.id.img_y1, R.id.img_y2, R.id.img_y3, R.id.img_y4, R.id.img_y5, 
							R.id.img_y6, R.id.img_y7, R.id.img_y8, R.id.img_y9, R.id.img_y10};

	int mSwingXResult[] = new int[TIME_SCALE];
	int mSwingYResult[] = new int[TIME_SCALE];
	
	/*
	 * For display the absolute maximum vales into the scale
	 */
	int mSwingXAccelTextResult[] = new int[TIME_SCALE];
	int mSwingYAccelTextResult[] = new int[TIME_SCALE];

	// MUSICAL_NOTE_NUM = 35
	int mSwingStrength[] = {-14, -13, -12, -11, -10, -9, -8,	// C1 ~ B1 (7ea) 
							-7, -6, -5, -4, -3, -2, -1,			// C2 ~ B2 (7ea) 
							0, 1, 2, 3, 4, 5, 6,				// C3 ~ B3 (7ea) 
							7, 8, 9, 10, 11, 12, 13,			// C4 ~ B4 (7ea) 
							14,	15, 16, 17, 18, 19, 20};		// C5 ~ B5 (7ea)
	
	// MUSICAL_NOTE_NUM = 35
	int mMusicalNoteArray[] =
					{R.raw.c1, R.raw.d1, R.raw.e1, R.raw.f1, R.raw.g1, R.raw.a1, R.raw.b1,
					 R.raw.c2, R.raw.d2, R.raw.e2, R.raw.f2, R.raw.g2, R.raw.a2, R.raw.b2,
					 R.raw.c3, R.raw.d3, R.raw.e3, R.raw.f3, R.raw.g3, R.raw.a3, R.raw.b3,
					 R.raw.c4, R.raw.d4, R.raw.e4, R.raw.f4, R.raw.g4, R.raw.a4, R.raw.b4,
					 R.raw.c5, R.raw.d5, R.raw.e5, R.raw.f5, R.raw.g5, R.raw.a5, R.raw.b5 };
	
	// MUSICAL_NOTE_NUM = 35
	int mSwingStrengthIconArray[] = 
		{R.drawable.n14, R.drawable.n13, R.drawable.n12, R.drawable.n11, R.drawable.n10, 
		R.drawable.n9, R.drawable.n8, R.drawable.n7, R.drawable.n6, R.drawable.n5, 
		R.drawable.n4, R.drawable.n3, R.drawable.n2, R.drawable.n1, R.drawable.p0, 
		R.drawable.p1, R.drawable.p2, R.drawable.p3, R.drawable.p4, R.drawable.p5, 
		R.drawable.p6, R.drawable.p7, R.drawable.p8, R.drawable.p9, R.drawable.p10, 
		R.drawable.p11, R.drawable.p12, R.drawable.p13, R.drawable.p14, R.drawable.p15, 
		R.drawable.p16, R.drawable.p17, R.drawable.p18, R.drawable.p19, R.drawable.p20
	};
	
	int mSoundPoolId[] = new int[MUSICAL_NOTE_NUM];
			
	
	Handler mTimerHandler;
	
	/*
	 * Sound ID
	 */
	SoundPool mSoundPool;	
	int mNormalSoundId;
	int mMaxSoundId;
	int mMinSoundId;
	int mMaxMinSoundId;
	
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

	int mXMaxTime = 0;
	int mXMinTime = 0;
	int mYMaxTime = 0;
	int mYMinTime = 0;
	
	int mSwingDuration = 0;
	
	float mXMaxValue = 0;
	float mXMinValue = 0;
	
	float mYMaxValue = 0;
	float mYMinValue = 0;
	

	ArrayList<AccelerationData> mSwingDataArrayList = null;	
	
	SwingTimeScale mXTimeScale;
	SwingTimeScale mYTimeScale;

	//Ruler mRulerX;
	//Ruler mRulerY;
	
	/*
	 * Widgets 
	 */
	Button mAnalysisButton;
	Button mStatDatabaseButton;
	Button mHomeButton;
	Button mFileManagerButton;
	Button mGraphButton;
	
	TextView mXTextView;
	TextView mYTextView;
	TextView mFeedbackTextView;
	
	Spinner mSwingFileSpinner;
	
	ArrayAdapter<String> spinnerAdapter;
	/* 
	 * SharedPreference Values 
	 */
	private int mCollectionTime = 0;
	private boolean mBeepChecked = true;
	
	private int mMaxThreshold = 0;		// Threshold of X-axis
	private int mMinThreshold = 0;		// Threshold of Y-axis
	private boolean mPhoneFrontPlaced = false;
	
	
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.swing_pastdata_feedback);
		
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

		mAnalysisButton = (Button)findViewById(R.id.past_feedback_result_button);
		mAnalysisButton.setOnClickListener(mClickListener);
		
		mFileManagerButton = (Button)findViewById(R.id.past_feedback_filemananger_button);
		mFileManagerButton.setOnClickListener(mClickListener);
		
		mGraphButton = (Button)findViewById(R.id.past_feedback_graph_button);
		mGraphButton.setOnClickListener(mClickListener);

		mHomeButton = (Button)findViewById(R.id.past_feedback_home_button);
		mHomeButton.setOnClickListener(mClickListener);
		
		mXTextView = (TextView)findViewById(R.id.feedback_x_textview);
		mYTextView = (TextView)findViewById(R.id.feedback_y_textview);
		mFeedbackTextView = (TextView)findViewById(R.id.feedback_result);
		
		readPreferenceValues();
		
		initMemberVariables();
		setImageViewResource();
		
		initSoundPool();
		
		
		//searchSwingFiles();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		
		if(mDatabaseHandler != null)
			mDatabaseHandler.close();
		
		
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
		displayXYResultText(X_AXIS, mMaxThreshold, -1, 0);
		displayXYResultText(Y_AXIS, mMaxThreshold, -1, 0);
	}

	Button.OnClickListener mClickListener = new View.OnClickListener() {
		
		public void onClick(View v) {
			switch(v.getId())
			{
			case R.id.past_feedback_result_button:
				analyzeSwingData();
				break;
/*				
			case R.id.stats_db_button:
				startActivity(new Intent(SwingPastDataFeedback.this, StatisticsActivity.class));
				finish();
				break;
*/				
			case R.id.past_feedback_filemananger_button:
				startActivity(new Intent(SwingPastDataFeedback.this, FileManagerActivity.class));
				finish();
				break;
			case R.id.past_feedback_graph_button:
				if(mSelectedFile.isEmpty())
				{
					Toast.makeText(SwingPastDataFeedback.this, 
									"Error. There is no selected file", Toast.LENGTH_LONG).show();
				}
				else
				{
					Intent intent = new Intent(SwingPastDataFeedback.this, 
												SwingGraphActivity.class);
					intent.putExtra("file", mSelectedFile);
					startActivity(intent);
				}
				break;
			case R.id.past_feedback_home_button:
				startActivity(new Intent(SwingPastDataFeedback.this, Home.class));
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
		mBeepChecked = pref.getBoolean(PREF_BEEP_METHOD, true);
		mPhoneFrontPlaced = pref.getBoolean(PREF_PHONE_FRONT_PLACEMENT,	false);
		
		Log.i("setting", "==== readPreferenceValues ====");
		Log.i("setting", "'PREF_COLLECTION_TIME     : " + mCollectionTime);
		Log.i("setting", "PREF_BEEP_METHOD          : " + mBeepChecked);
		Log.i("setting", "PREF_MAX_THRESHOLD        : " + mMaxThreshold);
		Log.i("setting", "PREF_MIN_THRESHOLD        : " + mMinThreshold);
		Log.i("setting", "PREF_PHONE_FRONT_PLACEMENT: " + mPhoneFrontPlaced);

		
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
			
			mSwingXAccelTextResult[i] = 0;
			mSwingYAccelTextResult[i] = 0;
			
		}
		
		mFeedbackTextView.setText("");
		displayXYResultText(X_AXIS, mMaxThreshold, -1, 0);
		displayXYResultText(Y_AXIS, mMaxThreshold, -1, 0);

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
		}
		else
		{
			Toast.makeText(this, "The file name does not exist", Toast.LENGTH_LONG).show();
		}		
	}
	/*=============================================================================
	 * Name: analyzeSwingData1
	 * 
	 * Description:
	 * 		Create a thread to detect the peak points of X and Y-axis data
	 * 		Another way to detect a start point
	 * Return:
	 * 		None
	 *=============================================================================*/	
	public void analyzeSwingData1()
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
														mMaxThreshold, mMinThreshold, 1);
			mDetectSwingThread.setDaemon(true);
			mDetectSwingThread.start();
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
		
		if(mStartIndex == -1|| mEndIndex == -1)
		{
			if(mStartIndex == -1)
				showMsgDialog("Error", "Cannot detect a start point");
			else
				showMsgDialog("Error", "Cannot detect an end point");
		}
		else
		{
			mSwingStartTime = mSwingDataArrayList.get(mStartIndex).mTimestamp;
			mSwingEndTime = mSwingDataArrayList.get(mEndIndex).mTimestamp;
			mSwingDuration = mSwingEndTime - mSwingStartTime;
			
			displayResultText("Swing Duration: " + mSwingDuration + " msec");
			displayResultText("Time interval : " + mSwingDuration/TIME_SCALE + " msec");
			
			mXMaxTime = mSwingDataArrayList.get(mXMaxIndex).mTimestamp;			
			mXMaxValue = mSwingDataArrayList.get(mXMaxIndex).mXvalue;
			
			mXMinTime = mSwingDataArrayList.get(mXMinIndex).mTimestamp;
			mXMinValue = mSwingDataArrayList.get(mXMinIndex).mXvalue;
			
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
				if(mBeepChecked == true)
				{
					findPeakTimeIndex(mSwingStartTime, mSwingEndTime, mXMaxTime, 
										X_AXIS, MAX_POINT);
					findPeakTimeIndex(mSwingStartTime, mSwingEndTime, mXMinTime, 
										X_AXIS, MIN_POINT);
				}
			}
			else
			{
				mIsAboveThresholdX = false;
			}
			
			mYMaxTime = mSwingDataArrayList.get(mYMaxIndex).mTimestamp;
			mYMaxValue = mSwingDataArrayList.get(mYMaxIndex).mYvalue;
			
			mYMinTime = mSwingDataArrayList.get(mYMinIndex).mTimestamp;
			mYMinValue = mSwingDataArrayList.get(mYMinIndex).mYvalue;
			
			/*
			 * When only the maximum value of Y-axis is less than Threshold(-5),
			 * find a peak point.
			 */
			if((int)mYMinValue <= mMinThreshold)
			{
				mIsAboveThresholdY = true;
				if(mBeepChecked)
				{
					findPeakTimeIndex(mSwingStartTime, mSwingEndTime, 
										mYMaxTime, Y_AXIS, MAX_POINT);
					findPeakTimeIndex(mSwingStartTime, mSwingEndTime, 
										mYMinTime, Y_AXIS, MIN_POINT);
				}
			}
			else
			{
				mIsAboveThresholdY = false;
			}
			
			/*
			 * Display color and make beep sounds according to the values
			 */
			calculateMaxValuePerTimeslot(TIME_SCALE, mStartIndex, mEndIndex);
			
			mXTimeScale.drawMaxValueText(mSwingXAccelTextResult);
			mYTimeScale.drawMaxValueText(mSwingYAccelTextResult);
			
			//displayAnalysisResult();
			displayAnalysisExactTimeResult();
			
			/*
			 * Display All result values
			 * Max, Min, Swing duration, Start time, End time
			 */
		}
	}
	/*=============================================================================
	 * Name: calculateMaxValuePerTimeslot
	 * 
	 * Description:
	 * 		Find a maximum value of each timeslot 		  	
	 * 
	 * Return:
	 * 		None
	 *=============================================================================*/ 	
  public void calculateMaxValuePerTimeslot(int timescale, int sIndex, int eIndex)
  {
  	int interval = 0;
  	float maxX, minX;
  	float maxY, minY;    	
  	int timestamp = 0; 
  	int startTimestamp = 0;
  	int endTimestamp = 0;
  	int arrIndex = 0;
  	int prevIndex = 0;
  	float x, y;
  	
  	x = y = 0;
  	if(eIndex - sIndex == 0)
  	{
  		showMsgDialog("Errpr", "Cannot find critical points.");
  		return;
  	}
  	maxX = mSwingDataArrayList.get(sIndex).mXvalue;
  	maxY = mSwingDataArrayList.get(sIndex).mYvalue;
  	
  	minX = maxX;
  	minY = maxY;

  	startTimestamp = mSwingDataArrayList.get(sIndex).mTimestamp;
  	endTimestamp = mSwingDataArrayList.get(eIndex).mTimestamp;
  	
  	interval = (endTimestamp - startTimestamp) / timescale;
  	
  	for(int i=sIndex; i<=eIndex; i++)
  	{
  		timestamp = mSwingDataArrayList.get(i).mTimestamp;
  		x = mSwingDataArrayList.get(i).mXvalue;
  		y = mSwingDataArrayList.get(i).mYvalue;
  		
  		arrIndex = (timestamp - startTimestamp) / interval;
  		if(arrIndex >= TIME_SCALE)
  			arrIndex = TIME_SCALE -1;
  		
  		if((arrIndex - prevIndex) == 1)
  		{
  			if(Math.abs(maxX) < Math.abs(minX))
  			{
  				Log.i("realswing", "[" + prevIndex +"] " 
  									+ "Abs|X|: maxX=" + Math.abs(maxX) 
  									+ " minX=" + Math.abs(minX));
  				if(!mBeepChecked)
  					mSwingXResult[prevIndex] = (int)minX;
  				
  				mSwingXAccelTextResult[prevIndex] = (int)minX;
  			}
  			else
  			{
  				Log.i("realswing", "[" + prevIndex +"] " 
							+ "Abs|X|: maxX=" + Math.abs(maxX) 
							+ " minX=" + Math.abs(minX));
  				
  				if(!mBeepChecked)
  					mSwingXResult[prevIndex] = (int)maxX;
  				
  				mSwingXAccelTextResult[prevIndex] = (int)maxX;
  			}

  			if(Math.abs(maxY) < Math.abs(minY))
  			{
  				Log.i("realswing","[" + prevIndex +"] " 
  									+ "Abs|Y|: maxY=" + Math.abs(maxY) 
  									+ " minY=" + Math.abs(minY));
  				if(!mBeepChecked)
  					mSwingYResult[prevIndex] = (int)minY;
  				
  				mSwingYAccelTextResult[prevIndex] = (int)minY;
  			}
  			else
  			{
  				Log.i("realswing","[" + prevIndex +"] " 
  								+ "Abs|Y|: maxY=" + Math.abs(maxY) 
  								+ " minY=" + Math.abs(minY));
  				if(!mBeepChecked)
  					mSwingYResult[prevIndex] = (int)maxY;
  				
  				mSwingYAccelTextResult[prevIndex] = (int)maxY;
  			}

  			// Initialize maximum and minimum values in each time slot
  			maxX = mSwingDataArrayList.get(i).mXvalue;
  			minX = mSwingDataArrayList.get(i).mXvalue;
  			
  			maxY = mSwingDataArrayList.get(i).mYvalue;
  			minY = mSwingDataArrayList.get(i).mYvalue;
  			prevIndex = arrIndex;
  		}
  		
  		if(x <= minX)
  		{
  			minX = x;
  		}
  		
  		if(x >= maxX)
  		{
  			maxX = x;
  		}
  		
  		if(y <= minY)
  		{
  			minY = y;
  		}
  		
  		if(y >= maxY)
  		{
  			maxY = y; 
  		}
  	}
  	
  	/* Debug
  	 * 
  	 */
  	for(int j=0; j<TIME_SCALE; j++)
  	{
  		Log.i("feedback", "mSwingXResult["+j+"] = " + mSwingXResult[j]);
  	}

  	for(int j=0; j<TIME_SCALE; j++)
  	{
  		Log.i("feedback", "mSwingYResult["+j+"] = " + mSwingYResult[j]);
  	}

  }

	/*=============================================================================
	 * Name: showMsgDialog
	 * 
	 * Description:
	 * 		Show an alert dialog when an error happens
	 *=============================================================================*/	
	public void showMsgDialog(String title, String message)
	{
		AlertDialog.Builder alertDlg 
		= new AlertDialog.Builder(SwingPastDataFeedback.this);
	
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
		if(!mBeepChecked)
		{
			mSoundPool = new SoundPool(MUSICAL_NOTE_NUM, AudioManager.STREAM_MUSIC, 0);
			for(int i=0; i< MUSICAL_NOTE_NUM; i++)
			{
				mSoundPoolId[i] = mSoundPool.load(this, mMusicalNoteArray[i], 1);
			}

		}
		else
		{
			mSoundPool = new SoundPool(3, AudioManager.STREAM_MUSIC, 0);
			
			mNormalSoundId = mSoundPool.load(this, R.raw.normal_pitch, 1);		
			mMaxSoundId = mSoundPool.load(this, R.raw.high_pitch, 1);
			mMinSoundId = mSoundPool.load(this, R.raw.low_pitch, 1);
			mMaxMinSoundId = mSoundPool.load(this, R.raw.high_low_pitch, 1);
		}
		

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
			case COLOR_BOTH:
				mXImages[index].setImageResource(R.drawable.both_button_30);
				mSoundPool.play(mMaxMinSoundId, 1, 1, 0, 0, 1);
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
			case COLOR_BOTH:
				mYImages[index].setImageResource(R.drawable.both_button_30);
				mSoundPool.play(mMaxMinSoundId, 1, 1, 0, 0, 1);
				
			}

		}
	}
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
		Log.i("feedback", "interval:" + interval);
		
		if(timestamp >= start)
			index = (timestamp - start)/interval;
		else
			index = 0;
		
		if(index >= TIME_SCALE)
			index = (TIME_SCALE-1);
		
		
		if(axis == X_AXIS)
		{
			
			//mSwingXResult[index] = type;
			mSwingXResult[index] = mSwingXResult[index] ^ type;
			Log.i("feedback", "X Type[" + index + "]= " + mSwingXResult[index] );
		}
		else
		{
			
			//mSwingYResult[index] = type;
			mSwingYResult[index] = mSwingYResult[index] ^ type;
			Log.i("feedback", "Y Type[" + index + "]= " + mSwingYResult[index] );
		}		
	}
	/*=============================================================================
	 * Name: showTimeSlotWithBeep
	 * 
	 * Description:
	 * 		Display each timeslot with an Icon and a beep sound
	 * 
	 * Return:
	 * 		None
	 *=============================================================================*/			
	public void showTimeSlotWithBeep(int index, int axis)
	{
		if(axis == X_AXIS)
		{
			if(mSwingXResult[index] == MAX_POINT)
				displayResultWithSoundIcon(index, COLOR_RED, X_AXIS);
			else if(mSwingXResult[index] == MIN_POINT)
				displayResultWithSoundIcon(index, COLOR_YELLOW, X_AXIS);
			else if(mSwingXResult[index] == BOTH_POINT)
				displayResultWithSoundIcon(index, COLOR_BOTH, X_AXIS);
			else
				displayResultWithSoundIcon(index, COLOR_GREEN, X_AXIS);
		}
		else
		{
			if(mSwingYResult[index] == MAX_POINT)
				displayResultWithSoundIcon(index, COLOR_RED, Y_AXIS);
			else if(mSwingYResult[index] == MIN_POINT)
				displayResultWithSoundIcon(index, COLOR_YELLOW, Y_AXIS);
			else if(mSwingYResult[index] == BOTH_POINT)
				displayResultWithSoundIcon(index, COLOR_BOTH, Y_AXIS);			
			else
				displayResultWithSoundIcon(index, COLOR_GREEN, Y_AXIS);			
		}

	}
	
	/*=============================================================================
	 * Name: showTimeSlotWithMusicalNotes
	 * 
	 * Description:
	 * 		Display each timeslot with an Icon and a beep sound
	 * 
	 * Return:
	 * 		None
	 *=============================================================================*/			
	public void showTimeSlotWithMusicalNotes(int index, int axis)
	{
		int strength = 0;
		int matchedIndex = 0;
		boolean isFound = false;
		
		if(axis == X_AXIS)
		{
			strength = mSwingXResult[index];
			
			if(strength < -14)
				strength = -14;
			else if(strength > 20)
				strength = 20;
			
			for(int i=0; i<MUSICAL_NOTE_NUM; i++)
			{
				if(strength == mSwingStrength[i])
				{
					isFound = true;
					matchedIndex = i;
					break;
				}
			}
			if(isFound == false)
			{
				matchedIndex = 0;
				Log.i("feedback", "X soundIndex=" + matchedIndex + ", isFound:" + isFound);
			}
			
			
			mXImages[index].setImageResource(mSwingStrengthIconArray[matchedIndex]);
			mSoundPool.play(mSoundPoolId[matchedIndex], 1, 1, 0, 0, 1);
			
		}
		else
		{
			strength = mSwingYResult[index];
			
			if(strength < -14)
				strength = -14;
			else if(strength > 20)
				strength = 20;
			
			for(int i=0; i<MUSICAL_NOTE_NUM; i++)
			{
				if(strength == mSwingStrength[i])
				{
					isFound = true;
					matchedIndex = i;
					break;
				}
			}
			
			if(isFound == false)
			{
				matchedIndex = 0;
				Log.i("feedback", "Y soundIndex=" + matchedIndex + ", isFound:" + isFound);
			}
			
			
			mYImages[index].setImageResource(mSwingStrengthIconArray[matchedIndex]);
			mSoundPool.play(mSoundPoolId[matchedIndex], 1, 1, 0, 0, 1);

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
  			handlerText = "Start Time: " + msg.arg2;
  			displayResultText(handlerText);
  			break;
  		case MSG_END_POINT:
  			mEndIndex = msg.arg1;
  			handlerText = "End Time  : " + msg.arg2;    			
  			displayResultText(handlerText);
  			break;
  		
  		case MSG_PEAK_X_MAX:    			
  			mXMaxIndex = msg.arg1;    
  			displayXYResultText(X_AXIS, mMaxThreshold, mXMaxIndex, msg.arg2);
  			break;
  		case MSG_PEAK_X_MIN:
  			mXMinIndex = msg.arg1;
  			break;
  		case MSG_PEAK_Y_MAX:
  			mYMaxIndex = msg.arg1;
  			break;    			
  		case MSG_PEAK_Y_MIN:
  			mYMinIndex = msg.arg1;  
  			displayXYResultText(Y_AXIS, mMinThreshold, mYMinIndex, msg.arg2);
  			break;
  		case MSG_DETECT_DONE_X:
  			break;
  		case MSG_DETECT_DONE_Y:
  			break;
  		case MSG_DETECT_DONE_ALL:
  			if((msg.arg1 == -1) && (msg.arg2 == -1))
  			{
  				showMsgDialog("Detection Fail", 
  								"Swing is too weak to detect any points!");

  			}
  			else
  			{
  				startSwingFeedback();
  				//addFeedbackToDatabase();
  			}
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
					{
						if(!mBeepChecked)
							showTimeSlotWithMusicalNotes(timeIndex, X_AXIS);
						else
							showTimeSlotWithBeep(timeIndex, X_AXIS);
						
					}
					else
					{
						if(!mBeepChecked)
							showTimeSlotWithMusicalNotes(timeIndex-TIME_SCALE, Y_AXIS);
						else
							showTimeSlotWithBeep(timeIndex-TIME_SCALE, Y_AXIS);
						
					}
					
					timeIndex++;
				}
				else
				{
					if(mIsAboveThresholdX == false && mIsAboveThresholdY == true)
					{
						showMsgDialog("Weak Swing", "Swing values of X-axis are less than " + mMaxThreshold +".");
					}
					else if(mIsAboveThresholdX == true && mIsAboveThresholdY == false)
					{
						showMsgDialog("Weak Swing", "Swing values of Y-axis are less than " + mMinThreshold + ".");
					}
					else if(mIsAboveThresholdX == false && mIsAboveThresholdY == false)
					{
						showMsgDialog("Weak Swing", "Both swing values(X, Y-axis) are too small.(Max=" 
										+ mMaxThreshold + ", Min=" + mMinThreshold + ")");
					}
					else
					{
						String result = "";
						result = "Swing Duration:" + mSwingDuration + " msec.\n"
								+ "X Max Value: " + (int)mXMaxValue + ", Time: " + mXMaxTime + "\n"
								+ "X Min Value: " + (int)mXMinValue + ", Time: " + mXMinTime + "\n\n"
								+ "Y Max Value: " + (int)mYMaxValue + ", Time: " + mYMaxTime + "\n"
								+ "Y Min Value: " + (int)mYMinValue + ", Time: " + mYMinTime + "\n";
						
						showMsgDialog("Swing Result", result);

					}
				}
			}
		};
		
		mTimerHandler.sendEmptyMessage(0);
	}
	
	/*=============================================================================
	 * Name: displayAnalysisExactTimeResult
	 * 
	 * Description:
	 * 		Display result within the exact time duration. (Starttime - Endtime)		
	 * 
	 * Return:
	 * 		None
	 *=============================================================================*/    
	public void displayAnalysisExactTimeResult()
	{
		mTimerHandler = new Handler()
		{
			int wait = 0;
			int timeIndex = 0;
			int timeInterval = 0;
			public void handleMessage(Message msg)
			{
				/*
				 *  To display two axis icon, it waits for (TIMEOUT *2)
				 */
				timeInterval = mSwingDuration / TIME_SCALE;
				
				if(wait < (mSwingDuration * 2))
				{
					mTimerHandler.sendEmptyMessageDelayed(0, timeInterval);
					wait += timeInterval;
					Log.i("realswing", "Duration:" + mSwingDuration 
										+ ", Index:" + timeIndex 
										+ ", wait:" + wait 
										+ ", Interval:" + timeInterval);
					
					if(timeIndex < TIME_SCALE)
					{
						if(!mBeepChecked)
							showTimeSlotWithMusicalNotes(timeIndex, X_AXIS);
						else
							showTimeSlotWithBeep(timeIndex, X_AXIS);

					}
					else
					{
						if(!mBeepChecked)
						{
							Log.i("realswing", "Musical Notes timeIndex:" + timeIndex);
							if(timeIndex - TIME_SCALE < 10)
							{
								showTimeSlotWithMusicalNotes(timeIndex-TIME_SCALE, Y_AXIS);
							}
						}
						else
						{
							Log.i("realswing", "Beep timeIndex:" + timeIndex);
							if(timeIndex - TIME_SCALE < 10)
							{
								showTimeSlotWithBeep(timeIndex-TIME_SCALE, Y_AXIS);
							}
						}

					}
					
					timeIndex++;
				}
				else
				{
					if(mIsAboveThresholdX == false && mIsAboveThresholdY == true)
					{
						showMsgDialog("Weak Swing", "Swing values of X-axis are less than " + mMaxThreshold +".");
					}
					else if(mIsAboveThresholdX == true && mIsAboveThresholdY == false)
					{
						showMsgDialog("Weak Swing", "Swing values of Y-axis are less than " + mMinThreshold + ".");
					}
					else if(mIsAboveThresholdX == false && mIsAboveThresholdY == false)
					{
						showMsgDialog("Weak Swing", "Both swing values(X, Y-axis) are too small.(Max=" 
										+ mMaxThreshold + ", Min=" + mMinThreshold + ")");
					}
					else
					{
						String result = "";
						result = "Swing Duration :" + mSwingDuration + " msec.\n"
								+ "(From : " + mSwingStartTime + " to : " + mSwingEndTime + " msec)\n\n"
								+ "X Max Value: " + (int)mXMaxValue + ", Time: " + mXMaxTime + "\n"
								+ "X Min Value: " + (int)mXMinValue + ", Time: " + mXMinTime + "\n\n"
								+ "Y Max Value: " + (int)mYMaxValue + ", Time: " + mYMaxTime + "\n"
								+ "Y Min Value: " + (int)mYMinValue + ", Time: " + mYMinTime + "\n";
						
						showMsgDialog("Swing Result", result);

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
  
  public void displayXYResultText(int axis, int threshold, int index, int time)
  {
  	String text = "";
  	int value = 0;
  	
  	if(axis == X_AXIS)
  	{
  		if(index != -1)
  		{
	    		value = (int)mSwingDataArrayList.get(index).mXvalue;
	    	
	    		text = "X-axis(Threshold:" + mMaxThreshold+") " 
					  +  "+Peak:" + value + ", Time:" + time;
  		}
  		else
  		{
      		text = "X-axis(Threshold:" + mMaxThreshold + ") ";
  		}
  		mXTextView.setText(text);
  	}
  	else
  	{
  		if(index != -1)
  		{
  			value = (int)mSwingDataArrayList.get(index).mYvalue;
  			text = "Y-axis(Threshold:" + mMinThreshold+") " 
  					+  "-Peak:" + value + ", Time:" + time;
  		}
  		else
  		{
  			text = "Y-axis(Threshold:" + mMinThreshold +") ";
  		}
  		mYTextView.setText(text);
  		
  	}

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
	 * Name: getDateString
	 * 
	 * Description:
	 * 		Get the current year, month and date
	 * 		Return a string of date like "MM/DD/YYYY"
	 * 
	 * Return:
	 * 		String
	 *=============================================================================*/	                
  public String getDateString()
  {
  	String stringMonthDate = "";
  	String stringMonth = "";
  	String stringDate = "";
  	String stringYear = "";
  	
  	Calendar today = Calendar.getInstance();
  	
  	
  	stringDate = ((today.get(Calendar.MONTH) + 1) + "/"
  					+ today.get(Calendar.DATE) + "/"
  					+ today.get(Calendar.YEAR));
  	
  	return stringDate;
  	
  	/*
  	int month = (today.get(Calendar.MONTH) +1);
  	
  	
  	if(month < 10)
  		stringMonth = "0" + month + "/"; 
  	else
  		stringMonth = "" + month + "/";
  	
  	
  	int date = today.get(Calendar.DATE);
  	
  	if(date < 10)
  		stringDate = "0" + date + "/";
  	else
  		stringDate = "" + date + "/";
  	
  	
  	int year = today.get(Calendar.YEAR);
  	stringYear = "" + year;
  	
  	stringMonthDate = stringMonth + stringDate + stringYear;
  	
  	return stringMonthDate;
  	*/
  	
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
  		stringTime += "0" + hour + ":";
  	else
  		stringTime += hour + ":";
  	
  	min = today.get(Calendar.MINUTE);
  	if(min < 10)
  		stringTime += "0" + min + ":";
  	else
  		stringTime += min + ":";    	
  	
  	sec = today.get(Calendar.SECOND);
  	if(sec < 10)
  		stringTime += "0" + sec;
  	else
  		stringTime += sec;
  	
  	return stringTime;
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
		String stringDate= getDateString();
		String stringTime= getTimeString();
		String x_max = "";
		String x_max_time = "";
		String x_min = "";
		String x_min_time = "";
		String y_max = "";
		String y_max_time = "";
		String y_min = "";
		String y_min_time = "";

		AccelerationData element = new AccelerationData();

		//date = mStartDateString;
		//time = mStartTimeString;
		
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
		
		Log.i("feedback", "addSwingStats: date: " + stringDate + ", time:" + stringTime);
		Log.i("feedback", "x_max: " + x_max + ", time: " + x_max_time);
		Log.i("feedback", "x_min: " + x_min + ", time: " + x_min_time);
		Log.i("feedback", "y_max: " + y_max + ", time: " + y_max_time);
		Log.i("feedback", "y_min: " + y_min + ", time: " + y_min_time);
		
		SwingStatistics swing = new SwingStatistics(stringDate, stringTime, 
												x_max, x_max_time, x_min, x_min_time,
												y_max, y_max_time, y_min, y_min_time);
		
		mDatabaseHandler.addSwingStats(swing);
	}
	
	
}

