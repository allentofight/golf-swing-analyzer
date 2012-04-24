package com.SwingAnalyzer;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
	final static int MSG_PEAK_X			= 0x02;
	final static int MSG_IMPACT_X 		= 0x03;
	final static int MSG_DETECT_DONE_X 	= 0x04;
	
	/*
	 * Message ID for Y-axis
	 */
	final static int MSG_DETECT_Y 		= 0x10;
	final static int MSG_PEAK_Y 		= 0x20;
	final static int MSG_IMPACT_Y		= 0x30;
	final static int MSG_DETECT_DONE_Y	= 0x40;
	
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
	final static int X_AXIS_DETECTION = 0;
	final static int Y_AXIS_DETECTION = 1;
	
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
	DetectPeakThread mDetectPeakThread;
	
	int mWhichAxis;
	
	/*
	 * Timestamps and peak values of positive and negative values 
	 */
	int mPositivePeakIndex;
	int mPositivePeakTimestamp;
	
	int mNegativePeakIndex;
	int mNegativePeakTimestamp;
	
	ArrayList<AccelerationData> mSwingDataArrayList = null;	
	
	Ruler mRulerX;
	Ruler mRulerY;
	/*
	 * Widgets 
	 */
	Button mXAnalysisButton;
	Button mYAnalysisButton;
	Button mBackButton;
	
	TextView mXaxisTextView;
	TextView mYaxisTextView;
	TextView mFeedbackTextView;
	
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.swing_feedback);
		
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
		
		mXAnalysisButton = (Button)findViewById(R.id.x_button);
		mXAnalysisButton.setOnClickListener(mClickListener);
		
		mYAnalysisButton = (Button)findViewById(R.id.y_button);
		mYAnalysisButton.setOnClickListener(mClickListener);
		
		mBackButton = (Button)findViewById(R.id.back_button);
		mBackButton.setOnClickListener(mClickListener);
		
		mXaxisTextView = (TextView)findViewById(R.id.feedback_x_textview);
		mYaxisTextView = (TextView)findViewById(R.id.feedback_y_textview);
		mFeedbackTextView = (TextView)findViewById(R.id.feedback_result);
		
		mNormalSoundId = mPeakSoundId = 0;
		
		initMemberVariables();
		setImageViewResource();
		initSoundPool();
		
		if(getResultFileName() == true)
			readArrayListFromFile();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
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
			case R.id.x_button:
				clearSwingResult();
				mWhichAxis = X_AXIS_DETECTION;				
				analyzeSwingFromXvalues();
				break;
			case R.id.y_button:
				clearSwingResult();
				
				mWhichAxis = Y_AXIS_DETECTION;				
				analyzeSwingFromYvalues();
				break;
			case R.id.back_button:
				Intent intent = new Intent(SwingFeedback.this, SwingAnalyzerActivity.class);
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
		
		//mFeedbackTextView.setText("");
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
	 * Name: analyzeSwingFromXvalues
	 * 
	 * Description:
	 * 		Create a thread to detect a peak point from X-axis data
	 * 
	 * Return:
	 * 		None
	 *=============================================================================*/				
	public void analyzeSwingFromXvalues()
	{
		resetIconColor(mWhichAxis);
		
		if(getResultFileName() == false)
		{
			Toast.makeText(this, "The result file does not exist", Toast.LENGTH_LONG).show();
			return;
		}
		
		if(!mResultFileName.isEmpty())
		{
			mDetectPeakThread = new DetectPeakThread(mSwingDataArrayList, 
													FeedbackHandler, mWhichAxis);
			
			mDetectPeakThread.setDaemon(true);
			mDetectPeakThread.start();
		}
		else
		{
			Toast.makeText(this, "The file name does not exist", Toast.LENGTH_LONG).show();
		}
		
		// After completing analysis, display the result with icon and sound
		displayAnalysisResult();
	}
	/*=============================================================================
	 * Name: analyzeSwingFromXvalues
	 * 
	 * Description:
	 * 		Create a thread to detect a peak point from X-axis data
	 * 
	 * Return:
	 * 		None
	 *=============================================================================*/					
	public void analyzeSwingFromYvalues()
	{
		if(getResultFileName() == false)
		{
			Toast.makeText(this, "The result file does not exist", Toast.LENGTH_LONG).show();
			return;
		}

		if(!mResultFileName.isEmpty())
		{
			mDetectPeakThread = new DetectPeakThread(mSwingDataArrayList, 
													FeedbackHandler, mWhichAxis);
			mDetectPeakThread.setDaemon(true);
			mDetectPeakThread.start();

		}
		else
		{
			Toast.makeText(this, "The file name does not exist", Toast.LENGTH_LONG).show();
		}
		displayAnalysisResult();
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
		if(axis == X_AXIS_DETECTION)
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
		
		if(axis == X_AXIS_DETECTION)
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
	public void showResultTimeSlot(int index)
	{
		
		if(mWhichAxis == X_AXIS_DETECTION)
		{
			if(mSwingXResult[index] == PEAK_POINT)
				displayResultWithSoundIcon(index, COLOR_RED, mWhichAxis);
			else
				displayResultWithSoundIcon(index, COLOR_GREEN, mWhichAxis);
		}
		else
		{
			if(mSwingYResult[index] == PEAK_POINT)
				displayResultWithSoundIcon(index, COLOR_RED, mWhichAxis);
			else
				displayResultWithSoundIcon(index, COLOR_GREEN, mWhichAxis);			
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
    		Log.i("feedback", "handleMessage() what:" + msg.what 
    										+ ", arg1:" + msg.arg1 
    										+ ", arg2:" + msg.arg2);
    		int timestamp = msg.arg2;
    		
    		switch(msg.what)
    		{
    		case MSG_PEAK_X:
    			findPeakTimeIndex(timestamp, mWhichAxis);
    			
    			Log.i("feedback", "Peak X: index=" + msg.arg1 + ", Time=" + msg.arg2);
    			mPositivePeakIndex = msg.arg1;
    			mPositivePeakTimestamp = msg.arg2;
    			
    			showFeedbackResultText(msg.arg1, msg.arg2, msg.what);
    			break;
    		case MSG_PEAK_Y:
    			findPeakTimeIndex(timestamp, mWhichAxis);
    			
    			Log.i("feedback", "Peak Y: index=" + msg.arg1 + ", Time=" + msg.arg2);
    			mPositivePeakIndex = msg.arg1;
    			mPositivePeakTimestamp = msg.arg2;
    			
    			showFeedbackResultText(msg.arg1, msg.arg2, msg.what);    			
    			break;
    		case MSG_DETECT_DONE_X:
    			break;
    		case MSG_DETECT_DONE_Y:
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
				if(wait < TIMEOUT)
				{
					mTimerHandler.sendEmptyMessageDelayed(0, TIME_INTERVAL);
					wait += TIME_INTERVAL;
					
					showResultTimeSlot(timeIndex);
					timeIndex++;
				}
				else
				{
					showAllResult(mPositivePeakIndex, mWhichAxis);
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
		
		if(msg == MSG_PEAK_X)
		{				
			result = "X-axis Peak [Time: " + accel.mTimestamp 
										+ ", X: " + accel.mXvalue + "]" + "\n";
			
			mXaxisTextView.setText(result);
		}
		else if(msg == MSG_PEAK_Y)
		{	
			result = "Y-axis Peak [Time: " + accel.mTimestamp 
										+ ", Y: " + accel.mYvalue + "]" + "\n";
			mYaxisTextView.setText(result);
		}

		//mFeedbackTextView.setText(result);
	}
	
	public void showAllResult(int index, int axis)
	{
		String resultString = "";
		
		AccelerationData accel = new AccelerationData();
		
		accel = mSwingDataArrayList.get(index);
		
		Log.i("feedback", "showAllResult index: " + index 
											+ ", T:" + accel.mTimestamp 
											+ ", X:" + accel.mXvalue
											+ ", Y:" + accel.mYvalue);
		if(axis == X_AXIS_DETECTION)
		{
			resultString = "The Peak of X " + "[Time: " + accel.mTimestamp 
											+ ", X: " + accel.mXvalue + "]" + "\n";
		}
		else
		{
			resultString = "The Peak of Y " + "[Time: " + accel.mTimestamp 
											+ ", Y: " + accel.mYvalue + "]" + "\n";
			
		}
		mFeedbackTextView.append(resultString);

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
		//width = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, getWidth()-20, dm);
		//height = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, getHeight(), dm);
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
				
				
				//if(x_axis2 >= width)
				//	x_axis2 = width;
				
				Log.i("scale", "x:" + x + ", x2:" + x1);
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
			Log.i("scale", "X:" + x);
			
			y = scaleSize;
			canvas.drawLine(x, 20, x, y+textSize+20, Pnt);			
			
			Pnt.setAntiAlias(true);
			text = "" + (mInterval * unit);			
				
			canvas.drawText(text, x, y, Pnt);			
		}
		
	}
}	
