/*-----------------------------------------------------------------------------------------
  File:   SwingFeedback.java

  Author: Jung Chang Su
  -----------------------------------------------------------------------------------------
  Copyright (C) 2012 SICS.
  
    
  
  *----------------------------------------------------------------------------------------*/
package com.SwingAnalyzer;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.*;


import android.app.*;
import android.content.Context;
import android.content.Intent;
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
	final static String ACCELERATION_DIR = "/data/acceldata/";
	final static String OUTPUT_FILENAME = "swing.dat";

	/*
	 *	Message ID for X-axis 
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
	
	final static int MSG_DETECT_DONE_ALL	= 0x111;
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
	final static int COLOR_BLACK = 0;
	final static int COLOR_GREEN = 1;
	final static int COLOR_RED	 = 2;
	
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
	final static int PEAK_POINT = 1;
	
	ImageView mXImages[] = new ImageView[TIME_SCALE];
	ImageView mYImages[] = new ImageView[TIME_SCALE];
	
	int mXImageViewIds[] = {R.id.img_x1, R.id.img_x2, R.id.img_x3, R.id.img_x4, R.id.img_x5, 
							R.id.img_x6, R.id.img_x7, R.id.img_x8, R.id.img_x9, R.id.img_x10};
	
	int mYImageViewIds[] = {R.id.img_y1, R.id.img_y2, R.id.img_y3, R.id.img_y4, R.id.img_y5, 
							R.id.img_y6, R.id.img_y7, R.id.img_y8, R.id.img_y9, R.id.img_y10};

	int mSwingXResult[] = new int[TIME_SCALE];
	int mSwingYResult[] = new int[TIME_SCALE];
	
	Handler mTimerHandler;
	
	SoundPool mSoundPool;
	
	int mNormalSoundId;
	int mPeakSoundId;
	
	String mResultFileName;
	String mSDCardPath;
	
	int mWhichAxis;
	
	private String mStartDateString;
	private String mStartTimeString;
	private boolean mSwingStarted;
	
	DetectPeakThread mDetectPeakThread;
	
	/* 
	 * Database Handler
	 */
	DatabaseHandler mDatabaseHandler;
	
	
	/*
	 * The positive and negative peak points and timestamps of X-axis data 
	 */
	int mXPositivePeakIndex;
	int mXPositivePeakTimestamp;
	
	int mXNegativePeakIndex;
	int mXNegativePeakTimestamp;

	/*
	 * The positive and negative peak points and timestamps of Y-axis data 
	 */

	int mYPositivePeakIndex;
	int mYPositivePeakTimestamp;
	
	int mYNegativePeakIndex;
	int mYNegativePeakTimestamp;

	ArrayList<AccelerationData> mSwingDataArrayList = null;	
	
	Ruler mRulerX;
	Ruler mRulerY;
	/*
	 * Widgets 
	 */
	Button mAnalysisButton;
	Button mStatsButton;
	Button mBackButton;
	
	TextView mXaxisTextView;
	TextView mYaxisTextView;
	TextView mFeedbackTextView;
	
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.swing_feedback);
		
		mDatabaseHandler = new DatabaseHandler(this);
		/*
		 * Draw X-axis time scales
		 */
		mRulerX = (Ruler)findViewById(R.id.ruler_x);		
		mRulerX.setScale(TIME_SCALE, COLLECTION_TIME);
		
		/*
		 * Draw Y-axis time scales
		 */
		mRulerY = (Ruler)findViewById(R.id.ruler_y);
		mRulerY.setScale(TIME_SCALE, COLLECTION_TIME);
		
		mAnalysisButton = (Button)findViewById(R.id.result_button);
		mAnalysisButton.setOnClickListener(mClickListener);
		
		mStatsButton = (Button)findViewById(R.id.stats_button);
		mStatsButton.setOnClickListener(mClickListener);
		
		mBackButton = (Button)findViewById(R.id.back_button);
		mBackButton.setOnClickListener(mClickListener);
		
		mXaxisTextView = (TextView)findViewById(R.id.feedback_x_textview);
		mYaxisTextView = (TextView)findViewById(R.id.feedback_y_textview);
		mFeedbackTextView = (TextView)findViewById(R.id.feedback_result);
		
		mNormalSoundId = mPeakSoundId = 0;
		
		initMemberVariables();
		setImageViewResource();
		initSoundPool();
		
		Bundle extras = getIntent().getExtras();
		
		mSwingStarted = extras.getBoolean("START_SWING");
		mStartDateString = extras.getString("START_DATE");
		mStartTimeString = extras.getString("START_TIME");
		
		Log.i("feedback", "Date:" + mStartDateString + ", Time:" + mStartTimeString);
		
		if(getResultFileName() == true)
			readArrayListFromFile();
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
	}

	Button.OnClickListener mClickListener = new View.OnClickListener() {
		
		public void onClick(View v) {
			switch(v.getId())
			{
			case R.id.result_button:
				analyzeSwingData();
				break;
			case R.id.stats_button:
				//Intent intent1 = new Intent(SwingFeedback.this, StatisticsActivity.class);
				startActivity(new Intent(SwingFeedback.this, StatisticsActivity.class));
				finish();
				break;
			case R.id.back_button:
				Intent intent = new Intent(SwingFeedback.this, CollectingAccelerationData.class);
				startActivity(intent);
				finish();
				break;
			}
			
		}
	};
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
        
        if(ext.equals(Environment.MEDIA_MOUNTED))
        {
        	mSDCardPath = Environment.getExternalStorageDirectory().getAbsolutePath();  
        	String fileName = mSDCardPath + ACCELERATION_DIR + OUTPUT_FILENAME;
        	
        	File file = new File(fileName);
        	
        	if(file.exists())
        	{
        		mResultFileName = fileName;
        		isFound = true;
        		Log.i("feedback", "ResultFileName: " + mResultFileName);
        	}
        	else
        	{
        		mResultFileName = "";
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
		
		if(getResultFileName() == false)
		{
			Toast.makeText(this, "The result file does not exist", Toast.LENGTH_LONG).show();
			return;
		}
		
		if(!mResultFileName.isEmpty())
		{
			mDetectPeakThread = new DetectPeakThread(mSwingDataArrayList, 
													FeedbackHandler);
			
			mDetectPeakThread.setDaemon(true);
			mDetectPeakThread.start();
		}
		else
		{
			Toast.makeText(this, "The file name does not exist", Toast.LENGTH_LONG).show();
		}		
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
		mPeakSoundId = mSoundPool.load(this, R.raw.high_pitch, 1);
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
				mSoundPool.play(mPeakSoundId, 1, 1, 0, 0, 1);
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
				mSoundPool.play(mPeakSoundId, 1, 1, 0, 0, 1);
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
	public void findPeakTimeIndex(int timestamp, int axis)
	{	
		int index = 0;
		
		if(timestamp == 0)
			timestamp = 1;
		
		index = timestamp/TIME_INTERVAL;
		
		if(index >= TIME_SCALE)
			index = (TIME_SCALE-1);
		
		Log.i("feedback", "findPeakTime Index: " + index + ", Time:" + timestamp);
		
		if(axis == X_AXIS)
		{
			mSwingXResult[index] = PEAK_POINT;
		}
		else
		{
			mSwingYResult[index] = PEAK_POINT;
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
			if(mSwingXResult[index] == PEAK_POINT)
				displayResultWithSoundIcon(index, COLOR_RED, X_AXIS);
			else
				displayResultWithSoundIcon(index, COLOR_GREEN, X_AXIS);
		}
		else
		{
			if(mSwingYResult[index] == PEAK_POINT)
				displayResultWithSoundIcon(index, COLOR_RED, Y_AXIS);
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
    		
    		int timestamp = msg.arg2;
    		
    		switch(msg.what)
    		{
    		case MSG_PEAK_X_MAX:
    			findPeakTimeIndex(timestamp, X_AXIS);
    			
    			//Log.i("feedback", "+ Peak X: index=" + msg.arg1 + ", Time=" + msg.arg2);
    			mXPositivePeakIndex = msg.arg1;
    			mXPositivePeakTimestamp = msg.arg2;
    			
    			showFeedbackResultText(msg.arg1, msg.arg2, msg.what);
    			break;
    		case MSG_PEAK_X_MIN:
    			// Don't find the negative X peak point
    			
    			//Log.i("feedback", "- Peak X: index=" + msg.arg1 + ", Time=" + msg.arg2);
    			
    			mXNegativePeakIndex = msg.arg1;
    			mXNegativePeakTimestamp = msg.arg2;
    			
    			break;
    		case MSG_PEAK_Y_MIN:
    			findPeakTimeIndex(timestamp, Y_AXIS);
    			
    			//Log.i("feedback", "- Peak Y: index=" + msg.arg1 + ", Time=" + msg.arg2);
    			mYNegativePeakIndex = msg.arg1;
    			mYNegativePeakTimestamp = msg.arg2;
    			
    			showFeedbackResultText(msg.arg1, msg.arg2, msg.what);    			
    			break;
    		case MSG_PEAK_Y_MAX:
    			
    			//Log.i("feedback", "+ Peak Y: index=" + msg.arg1 + ", Time=" + msg.arg2);
    			mYPositivePeakIndex = msg.arg1;
    			mYPositivePeakTimestamp = msg.arg2;    			
    			break;
    		case MSG_DETECT_DONE_X:
    			// msg.arg1 = Max, msg.arg2 = Min
    			showAllResult(msg.arg1, msg.arg2, X_AXIS);
    			break;
    		case MSG_DETECT_DONE_Y:
    			// msg.arg1 = Max, msg.arg2 = Min    			
    			showAllResult(msg.arg1, msg.arg2, Y_AXIS);
    			break;
    		case MSG_DETECT_DONE_ALL:
    			Log.i("feedback", "MSG_DETECT_DONE_ALL");
    			displayAnalysisResult();
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
			}
		};
		
		mTimerHandler.sendEmptyMessage(0);
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
			
			mXaxisTextView.setText(result);
		}
		else if(msg == MSG_PEAK_Y_MIN)
		{	
			result = "Y-axis Peak [Time: " + accel.mTimestamp 
										+ ", Y: " + accel.mYvalue + "]" + "\n";
			mYaxisTextView.setText(result);
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
		
		element = mSwingDataArrayList.get(mXPositivePeakIndex);
		x_max = String.valueOf(element.mXvalue);
		x_max_time = String.valueOf(element.mTimestamp);
		
		element = mSwingDataArrayList.get(mXNegativePeakIndex);
		x_min = String.valueOf(element.mXvalue);
		x_min_time = String.valueOf(element.mTimestamp);
		
		element = mSwingDataArrayList.get(mYPositivePeakIndex);
		y_max = String.valueOf(element.mYvalue);
		y_max_time = String.valueOf(element.mTimestamp);
		
		element = mSwingDataArrayList.get(mYNegativePeakIndex);
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
