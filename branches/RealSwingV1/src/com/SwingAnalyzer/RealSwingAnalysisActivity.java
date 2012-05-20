package com.SwingAnalyzer;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.prefs.Preferences;

import android.app.Activity;
import android.app.AlertDialog;
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
import android.widget.*;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.*;

public class RealSwingAnalysisActivity extends Activity{
	final static String GOLFSWING_DATA_DIR = "/data/GolfSwingAnalyzer";
	final static String EXTERNAL_SWING_DIR = "/externalswing/";
	final static String COLLECTED_SWING_DIR = "/collectedswing/";
		
	final static String SWING_EXT = ".txt";

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
	final static int COLOR_BLACK = 0;		// Default
	final static int COLOR_GREEN = 1;		// 
	final static int COLOR_RED	 = 2;		// Maximum
	final static int COLOR_YELLOW = 3;		// Minimum
	
	/*
	 * Timeout and time scale to calculate
	 */
	final private static int TIME_SCALE = 10;
	final private static int COLLECTION_TIME = (3*1000);
	final private static int TIME_INTERVAL = (COLLECTION_TIME / TIME_SCALE);	
	final private static int TIMEOUT = COLLECTION_TIME;

	/*
	 * Peak point
	 */
	final static int NORMAL_POINT = 0;
	final static int MIN_POINT = 1;
	final static int MAX_POINT = 2;
	
	final static int MUSICAL_NOTE_NUM = 35;
	
	/*
	 * Widgets
	 */
	Button mRealSwingAnalyzeButton;
	Button mRealSwingHomeButton;
	
	Spinner mRealSwingFileSpinner;
	
	TextView mRealSwingResultTextView;
	TextView mRealSwingXTextView;
	TextView mRealSwingYTextView;
	
	SwingTimeScale mXTimeScale;
	SwingTimeScale mYTimeScale;

	/*
	 * Member Variables 
	 */

	// Thread for file conversion and detecting critical points
	FileConversionThread mFileConversionThread;
	DetectSwingThread mDetectSwingThread;
	
	String mSelectedFile = "";
	
	private boolean mIsAboveThresholdX;
	private boolean mIsAboveThresholdY;
	

	// Variables for the analyzed swing data
	int mStartIndex;	// The start point of a swing
	int mSwingStartTime;
	int mEndIndex;		// The end point of a swing
	int mSwingEndTime;
	
	int mXMaxIndex;		// The maximum point of X-axis
	int mXMinIndex;		// The minimum point of X-axis
	int mYMaxIndex;		// The maximum point of Y-axis	
	int mYMinIndex;		// The minimum point of Y-axis
	
	
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
	
	SoundPool mSoundPool;
	
	// Sound IDs for Beep sound setting 
	int mNormalSoundId;
	int mMaxSoundId;
	int mMinSoundId;
	

	
	List<AccelerationData> mRealSwingDataList = null;
	List<AccelerationData> mConvertedSwingList = null;
	
	/* 
	 * SharedPreference Values 
	 */
	private int mCollectionTime = 0;
	private boolean mMusicalNoteChecked = true;
	
	private int mMaxThreshold = 0;		// Threshold of X-axis
	private int mMinThreshold = 0;		// Threshold of Y-axis
	private boolean mPhoneFrontPlaced = false;
	
	//////////////////////////////////////////////////////////////////////////////
	
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.real_swing_analysis);
		
		mRealSwingAnalyzeButton = (Button)findViewById(R.id.real_swing_analysis_button);
		mRealSwingAnalyzeButton.setOnClickListener(mClickListener);
		
		mRealSwingHomeButton = (Button)findViewById(R.id.real_swing_home_button);
		mRealSwingHomeButton.setOnClickListener(mClickListener);
		
		mRealSwingFileSpinner = (Spinner)findViewById(R.id.real_swing_file_spinner);
		mRealSwingFileSpinner.setOnItemSelectedListener(mItemSelectedListener);
		
		LinearLayout layout_xscale = (LinearLayout)findViewById(R.id.timescale_x);
		layout_xscale.addView(mXTimeScale = new SwingTimeScale(this));
		mXTimeScale.setScale(TIME_SCALE, COLLECTION_TIME);

		LinearLayout layout_yscale = (LinearLayout)findViewById(R.id.timescale_y);
		layout_yscale.addView(mYTimeScale = new SwingTimeScale(this));
		mYTimeScale.setScale(TIME_SCALE, COLLECTION_TIME);
		
		mRealSwingResultTextView = (TextView)findViewById(R.id.realswing_result);
		mRealSwingXTextView = (TextView)findViewById(R.id.realswing_x_textview);
		mRealSwingXTextView.setText("X-axis");
		
		mRealSwingYTextView = (TextView)findViewById(R.id.realswing_y_textview);
		mRealSwingYTextView.setText("Y-axis");
		
		readPreferenceValues();
		initMemberVariables();
		//searchSwingFiles();
		
		setImageViewResource();
		initSoundPool();

	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		
		if(mRealSwingDataList.size() > 0)
			mRealSwingDataList.clear();
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
			case R.id.real_swing_analysis_button:
				clearResultText();
				startAnalysisProcess();
				break;
			case R.id.real_swing_home_button:
				startActivity(new Intent(RealSwingAnalysisActivity.this, Home.class));
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
			Log.i("realswing", "Selected File: " + mSelectedFile);
		}

		public void onNothingSelected(AdapterView<?> arg0) {
			// TODO Auto-generated method stub
			
		}
		
	};
	
	/*=============================================================================
	 * Name: initMemberVariables
	 * 
	 * Description:
	 * 		Initialize class member variables
	 * 
	 * Return:
	 * 		None
	 *=============================================================================*/	
	public void initMemberVariables()
	{
		mStartIndex = 0;	
		mSwingStartTime = 0;
		mEndIndex = 0;		
		mSwingEndTime = 0;

		mXMaxIndex = mXMinIndex = 0;
		mYMaxIndex = mYMinIndex = 0;
		mIsAboveThresholdX = true;
		mIsAboveThresholdY = true;
		
		mRealSwingDataList = new ArrayList<AccelerationData>();
		clearSwingResult();
		
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
		
		clearResultText();
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
		if(mMusicalNoteChecked)
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
		
		
		if(timestamp >= start)
			index = (timestamp - start)/interval;
		else
			index = 0;
		
		if(index >= TIME_SCALE)
			index = (TIME_SCALE-1);
		
		
		if(axis == X_AXIS)
		{		
			mSwingXResult[index] = type;
		}
		else
		{
			mSwingYResult[index] = type;
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
			
			/*
			if(mSwingYResult[index] == MIN_POINT)
				displayResultWithSoundIcon(index, COLOR_RED, Y_AXIS);
			else if(mSwingYResult[index] == MAX_POINT)
				displayResultWithSoundIcon(index, COLOR_YELLOW, Y_AXIS);
			else
				displayResultWithSoundIcon(index, COLOR_GREEN, Y_AXIS);
			*/
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
				Log.i("realswing", "X soundIndex=" + matchedIndex + ", isFound:" + isFound);
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
				Log.i("realswing", "Y soundIndex=" + matchedIndex + ", isFound:" + isFound);
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
		mMusicalNoteChecked = pref.getBoolean(PREF_BEEP_METHOD, true);
		mPhoneFrontPlaced = pref.getBoolean(PREF_PHONE_FRONT_PLACEMENT,	false);
		
		Log.i("setting", "==== readPreferenceValues ====");
		Log.i("setting", "'PREF_COLLECTION_TIME: " + mCollectionTime);
		Log.i("setting", "PREF_BEEP_METHOD: " + mMusicalNoteChecked);
		Log.i("setting", "PREF_MAX_THRESHOLD: " + mMaxThreshold);
		Log.i("setting", "PREF_MIN_THRESHOLD: " + mMinThreshold);
		Log.i("setting", "PREF_PHONE_FRONT_PLACEMENT: " + mPhoneFrontPlaced);

	}
    /*=============================================================================
	 * Name: searchSwingFiles
	 * 
	 * Description:
	 * 		Check whether a SD card is mounted or not.
	 * 		Search for swing raw data files under a specific folder
	 * 		("/mnt/sdcard/data/GolfSwingAnalyzer/externalswing/")
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
    		searchFilesinSdPath(stringSdPath + GOLFSWING_DATA_DIR + EXTERNAL_SWING_DIR);    		
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
    	
    	
    	Log.i("realswing", "Real Swing Dir Path: " + swingDataPath);
    	
    	if(swingDir.isDirectory())
    	{
        	String[] fileNameList = swingDir.list(new FilenameFilter()
        	{
        		public boolean accept(File dir, String name)
        		{
        			return name.endsWith(SWING_EXT);
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
	 * 		Example) j4.txt -> j8.txt -> j9.txt -> 
	 * 				... -> j113.txt
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
		
		for(int i=0; i< (array.length-1); i++)
		{
		
			for(int j=0; j<= (array.length-2); j++)
			{
				s1 = array[j].substring(1, array[j].indexOf("."));
				s2 = array[j+1].substring(1, array[j+1].indexOf("."));

				i1 = Integer.parseInt(s1);
				i2 = Integer.parseInt(s2);
				
				if(i1 > i2)
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
    	ArrayAdapter<String> spinnerAdapter = 
    						new ArrayAdapter<String>(mRealSwingFileSpinner.getContext(),
    											android.R.layout.simple_spinner_item,
    											filenames);
    	
    	spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    	mRealSwingFileSpinner.setAdapter(spinnerAdapter);
    }

	/*=============================================================================
	 * Name: startAnalysisProcess
	 * 
	 * Description:
	 * 		1. File Conversion Procedure
	 * 			- FileConversionThread() sends a "MSG_CONVERSION_DONE" to Handler 
	 * 		2. Swing analysis procedure 
	 * 			- startAnalyzeSwingData() is called from the Handler	 * 		  	
	 * 
	 * Return:
	 * 		None
	 *=============================================================================*/ 
	public void startAnalysisProcess()
	{
		resetIconColor(X_AXIS);
		resetIconColor(Y_AXIS);
		clearSwingResult();
		
		if(mSelectedFile.isEmpty())
			return;
		
		String sdPath = getSDPathName();

		String srcFileName = sdPath + GOLFSWING_DATA_DIR 
							+ EXTERNAL_SWING_DIR + mSelectedFile;   
		
		Log.i("realswing", "srcFileName: " + srcFileName);
		
		if(mRealSwingDataList.size()>0)
			mRealSwingDataList.clear();
			

		mFileConversionThread = new FileConversionThread(srcFileName, 
														RealSwingAnalysisHandler,
														(ArrayList)mRealSwingDataList);
		
		mFileConversionThread.setDaemon(true);
		mFileConversionThread.start();
		
		
	}
	/*=============================================================================
	 * Name: startAnalyzeSwingData
	 * 
	 * Description:
	 * 		1. Swing analysis procedure 
	 * 		- Detect Start, Peak and End points 		  	
	 * 
	 * Return:
	 * 		None
	 *=============================================================================*/ 
	public void startAnalyzeSwingData()
	{
		mIsAboveThresholdX = true;
		mIsAboveThresholdY = true;
		
		mConvertedSwingList = new ArrayList<AccelerationData>(mRealSwingDataList);
		
		Collections.copy(mConvertedSwingList, mRealSwingDataList);
		Log.i("realswing", "mConvertedSwingList.size:" + mConvertedSwingList.size());
		
		/*
		mDetectSwingThread = new DetectSwingThread(RealSwingAnalysisHandler,
													(ArrayList)mConvertedSwingList);
		
		*/
		mDetectSwingThread = new DetectSwingThread(RealSwingAnalysisHandler,
													(ArrayList)mConvertedSwingList,
													mMaxThreshold, mMinThreshold);
		mDetectSwingThread.setDaemon(true);
		mDetectSwingThread.start();
	}
	/*=============================================================================
	 * Name: startRealSwingFeedback
	 * 
	 * Description:
	 * 		1. Prepare the feedback of a real swing 
	 * 		2. Check the timeslot of each critical points' index 		  	
	 * 
	 * Return:
	 * 		None
	 *=============================================================================*/ 	
	public void startRealSwingFeedback()
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
			mSwingStartTime = mConvertedSwingList.get(mStartIndex).mTimestamp;
			mSwingEndTime = mConvertedSwingList.get(mEndIndex).mTimestamp;

			mXMaxTime = mConvertedSwingList.get(mXMaxIndex).mTimestamp;			
			mXMaxValue = mConvertedSwingList.get(mXMaxIndex).mXvalue;
			
			mXMinTime = mConvertedSwingList.get(mXMinIndex).mTimestamp;
			
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
				if(mMusicalNoteChecked == false)
				{
					findPeakTimeIndex(mSwingStartTime, mSwingEndTime, 
										mXMaxTime, X_AXIS, MAX_POINT);
					findPeakTimeIndex(mSwingStartTime, mSwingEndTime, 
										mXMinTime, X_AXIS, MIN_POINT);
				}
			}
			else
			{
				mIsAboveThresholdX = false;
			}
			
			mYMaxTime = mConvertedSwingList.get(mYMaxIndex).mTimestamp;
			mYMinTime = mConvertedSwingList.get(mYMinIndex).mTimestamp;
			mYMinValue = mConvertedSwingList.get(mYMinIndex).mYvalue;
			
			/*
			 * When only the maximum value of Y-axis is less than Threshold(-5),
			 * find a peak point.
			 */
			if((int)mYMinValue <= mMinThreshold)
			{
				mIsAboveThresholdY = true;
				if(mMusicalNoteChecked == false)
				{
					// Make a different beep sound in the max peak and the min peak point
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
			// Calculate each time slots maximum values
			calculateMaxValuePerTimeslot(TIME_SCALE, mStartIndex, mEndIndex);
			
			mXTimeScale.drawMaxValueText(mSwingXAccelTextResult);
			mYTimeScale.drawMaxValueText(mSwingYAccelTextResult);
			
			displayAnalysisResult();
			
			// Display absolute value in scale
		}
	}
	
	/*=============================================================================
	 * Name: calculateMaxValuePerTimeslot
	 * 
	 * Description:
	 * 		Find a maximum value of each time slot 		  	
	 * 		if(abs|max| < abs|min|), then add maxX = (minX)
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
    	maxX = mConvertedSwingList.get(sIndex).mXvalue;
    	maxY = mConvertedSwingList.get(sIndex).mYvalue;
    	
    	minX = maxX;
    	minY = maxY;
    	
    	startTimestamp = mConvertedSwingList.get(sIndex).mTimestamp;
    	endTimestamp = mConvertedSwingList.get(eIndex).mTimestamp;
    	
    	interval = (endTimestamp - startTimestamp) / timescale;
    	
    	for(int i=sIndex; i<=eIndex; i++)
    	{
    		timestamp = mConvertedSwingList.get(i).mTimestamp;
    		x = mConvertedSwingList.get(i).mXvalue;
    		y = mConvertedSwingList.get(i).mYvalue;
    		
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
    				if(mMusicalNoteChecked == true)
    					mSwingXResult[prevIndex] = (int)minX;
    				
    				mSwingXAccelTextResult[prevIndex] = (int)minX;
    			}
    			else
    			{
    				Log.i("realswing", "[" + prevIndex +"] " 
							+ "Abs|X|: maxX=" + Math.abs(maxX) 
							+ " minX=" + Math.abs(minX));
    				
    				if(mMusicalNoteChecked == true)
    					mSwingXResult[prevIndex] = (int)maxX;
    				
    				mSwingXAccelTextResult[prevIndex] = (int)maxX;
    			}

    			if(Math.abs(maxY) < Math.abs(minY))
    			{
    				Log.i("realswing","[" + prevIndex +"] " 
    									+ "Abs|Y|: maxY=" + Math.abs(maxY) 
    									+ " minY=" + Math.abs(minY));
    				
    				if(mMusicalNoteChecked == true)
    					mSwingYResult[prevIndex] = (int)minY;
    				
    				mSwingYAccelTextResult[prevIndex] = (int)minY;

    			}
    			else
    			{
    				Log.i("realswing","[" + prevIndex +"] " 
    								+ "Abs|Y|: maxY=" + Math.abs(maxY) 
    								+ " minY=" + Math.abs(minY));
    				
    				if(mMusicalNoteChecked == true)
    					mSwingYResult[prevIndex] = (int)maxY;
    				
    				mSwingYAccelTextResult[prevIndex] = (int)maxY;
    			}

    			// Initialize maximum and minimum values in each time slot
    			maxX = mConvertedSwingList.get(i).mXvalue;
    			minX = mConvertedSwingList.get(i).mXvalue;
    			
    			maxY = mConvertedSwingList.get(i).mYvalue;
    			minY = mConvertedSwingList.get(i).mYvalue;
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
    		Log.i("realswing", "mSwingXResult["+j+"] = " + mSwingXResult[j]);
    	}

    	for(int j=0; j<TIME_SCALE; j++)
    	{
    		Log.i("realswing", "mSwingYResult["+j+"] = " + mSwingYResult[j]);
    	}

    }
    
    public int getTimestampFromArrayList(int index)
    {
    	int timestamp = 0;
    	
    	timestamp = mConvertedSwingList.get(index).mTimestamp;
    	
    	return timestamp;
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
		= new AlertDialog.Builder(RealSwingAnalysisActivity.this);
	
		alertDlg.setTitle(title);
		alertDlg.setMessage(message);
		alertDlg.setIcon(R.drawable.golf_analyzer_icon);
		alertDlg.setPositiveButton("Close", null);
		alertDlg.show();
	}
	
    
	/*=============================================================================
	 * Name: RealSwingAnalysisHandler
	 * 
	 * Description:
	 * 		Process handler messages for detecting some points	
	 * 		- arg1: index of the peak point
	 * 		- arg2: timestamp of the peak point
	 * 
	 * 		MSG_CONVERSION_DONE -> Start to detect some points from the golf swings
	 * Return:
	 * 		None
	 *=============================================================================*/    
    Handler RealSwingAnalysisHandler = new Handler()
    {
    	public void handleMessage(Message msg)
    	{
    		String handlerText = "";
    		int value = 0;
    		switch(msg.what)
    		{
    		case MSG_CONVERSION_DONE:
    			handlerText = "MSG_CONVERSION_DONE: count=" + msg.arg1;
    			displayResultText(handlerText);
    			
    			startAnalyzeSwingData();	// Start to analyze the converted swing data
    			break;    		
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
    			value = (int)mConvertedSwingList.get(mXMaxIndex).mXvalue;
    			handlerText = "X-axis [Threshold=" + mMaxThreshold+"] " 
    						+  "Peak:" + value + ", Time:" + msg.arg2;
    			
    			mRealSwingXTextView.setText(handlerText);
    			
    			//displayResultText(handlerText);
    			break;
    		case MSG_PEAK_X_MIN:
    			mXMinIndex = msg.arg1;    			
    			value = (int)mConvertedSwingList.get(mXMinIndex).mXvalue;
    			
    			handlerText = "X_MIN: Time:" + msg.arg2 + ", X=" + value;
    			displayResultText(handlerText);
    			break;
    		case MSG_PEAK_Y_MAX:
    			mYMaxIndex = msg.arg1;    			
    			value = (int)mConvertedSwingList.get(mYMaxIndex).mYvalue;
    			
    			handlerText = "Y_MAX: Time:" + msg.arg2 + ", Y=" + value;
    			displayResultText(handlerText);
    			break;    			
    		case MSG_PEAK_Y_MIN:
    			mYMinIndex = msg.arg1;    			
    			value = (int)mConvertedSwingList.get(mYMinIndex).mYvalue;

    			handlerText = "Y-axis [Threshold:" + mMinThreshold+"]" 
						+  " Peak Y:" + value + ", Time:" + msg.arg2;    			
    			
    			mRealSwingYTextView.setText(handlerText);
    			//displayResultText(handlerText);
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
    			if(msg.arg1 == -1)
    			{
    				showErrorDialog("Detection Fail", 
    								"Swing is too weak to detect any points!");
    			}
    			else
    			{
    				startRealSwingFeedback();	// Start do display the feedback result
    			}
    			break;
    		}
    		
    		
    	}
    };
	
    
    public void printArrayListItem()
    {
    	int len = mRealSwingDataList.size();
    	String element = "";
    	
    	Log.i("realswing", "mRealSwingDataList.size: " + len);
    	
    	for(int i = 0; i<len; i++)
    	{
    		element = "index" + mRealSwingDataList.get(i).mIndex
    					+ ", T:" + mRealSwingDataList.get(i).mTimestamp
    					+ ", X:" + mRealSwingDataList.get(i).mXvalue
    					+ ", Y:" + mRealSwingDataList.get(i).mYvalue
    					+ ", Z:" + mRealSwingDataList.get(i).mZvalue;
    		
    		Log.i("realswing", element);
    	}
    }

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
						if(mMusicalNoteChecked)
							showTimeSlotWithMusicalNotes(timeIndex, X_AXIS);
						else
							showTimeSlotWithBeep(timeIndex, X_AXIS);

					}
					else
					{
						if(mMusicalNoteChecked)
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
    	mRealSwingResultTextView.append(text + "\n");
    	Log.i("realswing", text);
    }
    
    public void clearResultText()
    {
    	mRealSwingXTextView.setText("");
    	mRealSwingYTextView.setText("");
    	
    	mRealSwingResultTextView.setText("");
    }
    

}	// End of RealSwingAnalysisActivity

//////////////////////////////////////////////////////////////////////////////////////////////
