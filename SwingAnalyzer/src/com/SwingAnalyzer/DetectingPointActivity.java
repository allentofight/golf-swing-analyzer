package com.SwingAnalyzer;

import java.io.*;
import java.util.*;

import android.app.*;
import android.content.*;
import android.os.*;
import android.util.Log;
import android.view.*;
import android.widget.*;

public class DetectingPointActivity extends Activity{
	final static String RESULT_DIR = "/data/acceldata/";	
	final static String SWINGRESULT_DIR = "/data/acceldata/";
	
	final static String X_RESULT_NAME = "_x";
	final static String Y_RESULT_NAME = "_y";
	
	final static String RESULT_EXT = ".dat";
	final static String ACCEL_EXT = ".acc";
	
	// X-axis Threshold Value
	final static int X_THRESHOLD_MAX = 10;
	final static int X_THRESHOLD_MIN = -10;
	
	// Y-axis Threshold Values
	final static int Y_THRESHOLD_MAX = 5;
	final static int Y_THRESHOLD_MIN = -10;
	
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
	 * Message ID for Z-axis
	 */
	final static int MSG_DETECT_Z		= 0x100;
	final static int MSG_PEAK_Z			= 0x200;
	final static int MSG_IMPACT_Z		= 0x300;
	final static int MSG_DETECT_DONE_Z 	= 0x400;
	
	final static int X_AXIS	= 0;
	final static int Y_AXIS = 1;
	/*
	 * Member variables
	 */
	
	// ArrayList for saving the detected points
	List<AccelerationData> mMainImpactArray = null;
	List<AccelerationData> mXImpactArray = null;
	List<AccelerationData> mYImpactArray = null;
	
	// X-axis detection thread
	DetectImpactThread mDetectImpactThread;
	
	// Y-axis detection thread
	DetectYPointsThread mDetectYPointsThread;
		
	String mResultOutputFile;		// Result file name
	String mSelectedFile;		// The selected file in a spinner widget
	String mImpactOutFileName;
	
	String mSdPath;			// SD card path name

	boolean mDetected;
	
	File mImpactFile = null;
	FileOutputStream mFileOutputStream = null;		// For saving the result to text file
	
	long mStartTimeMillis = 0;
	long mEndTimeMillis = 0;
	
	int mMaxThreshold;
	int mMinThreshold;
	
	boolean mXChecked;
	boolean mYChecked;
	/*
	 * Widgets
	 */
	
	Spinner mOutfileSpinner;
	
	RadioGroup mRadioAxisType;
	
	EditText mMaxEditText;
	EditText mMinEditText;

	Button mDetectButton;
	
	TextView mDetectImpactTitleView;
	TextView mDetectImpactResultView;
	
	ProgressDialog mProgress;
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detecting_points);   
        
        mRadioAxisType = (RadioGroup)findViewById(R.id.radio_group_axis);
        mRadioAxisType.setOnCheckedChangeListener(mCheckedChangeListener);
        
        mDetectButton = (Button)findViewById(R.id.detect_button);
        mDetectButton.setOnClickListener(mClickListener);
        
        mMaxEditText = (EditText)findViewById(R.id.threshold_hi_edit);
        mMinEditText = (EditText)findViewById(R.id.threshold_low_edit);
        
        mDetectImpactTitleView = (TextView)findViewById(R.id.detect_impact_title);
        mDetectImpactResultView = (TextView)findViewById(R.id.detect_impactpoint_text);
        
        mOutfileSpinner = (Spinner)findViewById(R.id.outfile_spinner);
        mOutfileSpinner.setOnItemSelectedListener(mItemSelectedListener);
        
        initMemberVariables();
        
        
        searchAccelerationFiles();
        showThresholdValues();
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
		searchAccelerationFiles();
		super.onResume();
	}
	
	/*=============================================================================
	 * Name: mItemSelectedListener
	 * 
	 * Description:
	 * 		Spinner item selection listener		
	 * 
	 * Return:
	 * 		None
	 *=============================================================================*/	    
    private AdapterView.OnItemSelectedListener mItemSelectedListener = 
    		new AdapterView.OnItemSelectedListener()    
    {

		public void onItemSelected(AdapterView<?> parent, View view, int position,
				long id) {
			// TODO Auto-generated method stub
			mSelectedFile = (String)parent.getSelectedItem();
			Log.i("Debug", "Selected File: " + mSelectedFile);
			mDetectImpactTitleView.setText(mSelectedFile);
		}

		public void onNothingSelected(AdapterView<?> arg0) {
			// TODO Auto-generated method stub
			
		}
	};

	RadioGroup.OnCheckedChangeListener mCheckedChangeListener 
		= new RadioGroup.OnCheckedChangeListener()
	{
		public void onCheckedChanged(RadioGroup group, int checkedId)
		{
			if(group.getId() == R.id.radio_group_axis)
			{
				switch(checkedId)
				{
				case R.id.radio_xaxis:
					mXChecked = true;
					mYChecked = false;
					
					mMaxThreshold = X_THRESHOLD_MAX;
					mMinThreshold = X_THRESHOLD_MIN;
					showThresholdValues();
					break;
				case R.id.radio_yaxis:
					mYChecked = true;
					mXChecked = false;
					
					mMaxThreshold = Y_THRESHOLD_MAX;
					mMinThreshold = Y_THRESHOLD_MIN;
					showThresholdValues();
					break;
				}
			}
		}
	};
	/*=============================================================================
	 * Name: mClickListener
	 * 
	 * Description:
	 * 		Button click listener		
	 * 
	 * Return:
	 * 		None
	 *=============================================================================*/	    
	private Button.OnClickListener mClickListener = new View.OnClickListener()
	{

		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch(v.getId())
			{
			case R.id.detect_button:
				clearTextView();
				mDetectImpactTitleView.setText(mSelectedFile 
											+ ", Threshold Max:" + mMaxThreshold
											+ ", Min:" + mMinThreshold);
				startDetectProcess(v.getContext());
				break;
			}
		}
		
	};
	
	/*=============================================================================
	 * Name: initMemberVariables
	 * 
	 * Description:
	 * 		Set file names for input and output		
	 * 		
	 * 
	 * Return:
	 * 		String
	 *=============================================================================*/	
	public void initMemberVariables()
	{
		mMainImpactArray = new ArrayList<AccelerationData>();
		mXImpactArray = new ArrayList<AccelerationData>();
		mYImpactArray = new ArrayList<AccelerationData>();		
		
		mSelectedFile = "";
		mResultOutputFile = "";
		mSdPath = "";
		
		mDetected = false;
		

        mRadioAxisType.check(R.id.radio_yaxis);
        mYChecked = true;
        mXChecked = false;
        
		if(mYChecked)
		{
			mMaxThreshold = Y_THRESHOLD_MAX;
			mMinThreshold = Y_THRESHOLD_MIN;
		}
		else
		{
			mMaxThreshold = X_THRESHOLD_MAX;
			mMinThreshold = X_THRESHOLD_MIN;
		}
	

	}
	/*=============================================================================
	 * Name: showThresholdValues
	 * 
	 * Description:
	 * 		show Max and Min threshold values according to X or Y axis
	 * 		- X-axis: Max = 10, Min= -10
	 * 		- Y-axis: Max = 5, Min = -10
	 * 
	 * Return:
	 * 		None
	 *=============================================================================*/
	public void showThresholdValues()
	{
		mMaxEditText.setText(Integer.toString(mMaxThreshold));
		mMinEditText.setText(Integer.toString(mMinThreshold));
	}
	/*=============================================================================
	 * Name: clearTextView
	 * 
	 * Description:
	 * 		Clear Text view widgets		
	 * 		
	 * 
	 * Return:
	 * 		None
	 *=============================================================================*/	
	public void clearTextView()
	{
		mDetectImpactTitleView.setText("");
		mDetectImpactResultView.setText("");		
	}
	
	/*=============================================================================
	 * Name: searchAccelerationFiles
	 * 
	 * Description:
	 * 		Set file names for input and output		
	 * 		
	 * 
	 * Return:
	 * 		String
	 *=============================================================================*/	
    public void searchAccelerationFiles()
    {
    	String stringSdPath = "";
    	
    	stringSdPath = getSDPathName();
    	
    	if(stringSdPath != Environment.MEDIA_UNMOUNTED)
    	{
    		searchFilesinSdPath(stringSdPath);    		
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
        	mSdPath = sdPath;
        }
        else
        {
        	sdPath = Environment.MEDIA_UNMOUNTED;
        	mSdPath = "";
        	Toast.makeText(this, "SD card is not mounted", Toast.LENGTH_LONG).show();
        }
        
    	return sdPath;
    }
    
    /*=============================================================================
	 * Name: searchFilesinSdPath
	 * 
	 * Description:
	 * 		Get golf swing file names in the specific folder (/mnt/sdcare/data/golfswing)		
	 * 		Input those file names to a Spinner widget
	 * 
	 * Return:
	 * 		None
	 *=============================================================================*/	    
    public void searchFilesinSdPath(String sdPath)
    {
    	File swingDir = new File(sdPath + RESULT_DIR);
    	
    	
    	Log.i("Detect", "Acceleration Dir Path: " + sdPath + RESULT_DIR);
    	
    	if(swingDir.isDirectory())
    	{
        	String[] fileNameList = swingDir.list(new FilenameFilter()
        	{
        		public boolean accept(File dir, String name)
        		{
        			return name.endsWith(ACCEL_EXT);
        		}
        	});
        	
        	for(int i=0; i<fileNameList.length; i++)
        	{
        		Log.i("Detect", "Filelist: " + fileNameList[i]);
        	}
        	
        	insertFileNameToSpinner(fileNameList);
    	}
    	
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
    						new ArrayAdapter<String>(mOutfileSpinner.getContext(),
    											android.R.layout.simple_spinner_item,
    											filenames);
    	
    	spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    	mOutfileSpinner.setAdapter(spinnerAdapter);
    }
    
	/*=============================================================================
	 * Name: initProgressDialog
	 * 
	 * Description:
	 * 		Initialize a progress dialog before running  	
	 * 
	 * Return:
	 * 		None
	 *=============================================================================*/ 
	public void initProgressDialog(Context context, String title, String message)
	{

		mProgress = new ProgressDialog(context);
		mProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		mProgress.setTitle(title);
		mProgress.setMessage(message);
		mProgress.setCancelable(false);
		
		mProgress.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							//mDetectImpactThread.mFinished = false;
							mProgress.dismiss();
						}
		});
		
		mProgress.show();
	}

	/*=============================================================================
	 * Name: startDetectProcess
	 * 
	 * Description:
	 * 		- Run a thread for detecting critical points according to a Radio button checked
	 * 		- If X-axis is checked, execute startDetectProcessOfXvalue()
	 * 		- If Y-axis is checked, execute startDetectProcessOfYvalue()
	 *   	
	 * Return:
	 * 		None
	 *=============================================================================*/
	public void startDetectProcess(Context context)
	{
		
		mMaxThreshold = Integer.parseInt(mMaxEditText.getText().toString());
		mMinThreshold = Integer.parseInt(mMinEditText.getText().toString());

		Log.i("Detect", "Threshold Max: " + mMaxThreshold + ", Min: " + mMinThreshold);
		
		if((mXChecked == true) && (mYChecked == false) )
		{	
			Log.i("Detect", "Start X detection.");
			startDetectProcessOfXvalue(context);
		}
		else
		{
			Log.i("Detect", "Start Y detection.");
			startDetectProcessOfYvalue(context);
		}
	}
	/*=============================================================================
	 * Name: startDetectProcessOfXvalue
	 * 
	 * Description:
	 * 		- The process of detecting critical points from X-axis values
	 * 		- Run a thread (DetectImpactThread)  	
	 * 		- Example 
	 * 			input file: j8_acc_out.txt
	 * 			output file: j8_acc_out_x.dat 
	 * Return:
	 * 		None
	 *=============================================================================*/ 
	public void startDetectProcessOfXvalue(Context context)
	{
		String title = "Detecting X-axis";
		String msg = "Wait ...";
		int dotPos = 0;
		String outFileName = "";
		String outFile = "";

		/*
		 * Input file name: j8_acc.txt
		 * Output file name: j8_acc_out_x.dat
		 */
		String inFile = mSdPath + RESULT_DIR + mSelectedFile;
		
		dotPos = mSelectedFile.lastIndexOf(".");		
		outFileName = mSelectedFile.substring(0, dotPos);		
		outFile = mSdPath + RESULT_DIR + outFileName + X_RESULT_NAME + RESULT_EXT;
		mResultOutputFile = outFile;

		/*
		 * Impact Text File : j8_acc_out_x.txt
		 */		
		setImpactOutFileName(outFileName, X_AXIS);
		
		initProgressDialog(context, title, msg);
		
		/*
		 * Create and run DetectImpactThread 
		 */
		mDetectImpactThread = new DetectImpactThread(inFile, 
													outFile, mXMsgHandler);
		
		// Synchronize ArrayList
		mMainImpactArray = Collections.synchronizedList(mDetectImpactThread.mImpactPointArray);
		
		mDetectImpactThread.setDaemon(true);
		// Start to check time
		mStartTimeMillis = System.currentTimeMillis();
		
		mDetectImpactThread.start();
	}
	/*=============================================================================
	 * Name: startDetectProcessOfYvalue
	 * 
	 * Description:
	 * 		- The process of detecting critical points from Y-axis values
	 * 		- Run a thread (DetectImpactThread)  	
	 * 
	 * Return:
	 * 		None
	 *=============================================================================*/ 
	public void startDetectProcessOfYvalue(Context context)
	{
		String title = "Detecting Y-axis";
		String msg = "Wait ...";
		int dotPos = 0;
		String outFileName = "";
		String outFile = "";

		/*
		 * Input file name: j8_acc.txt
		 * Output file name: j8_acc_out_x.dat
		 */
		String inFile = mSdPath + RESULT_DIR + mSelectedFile;
		
		dotPos = mSelectedFile.lastIndexOf(".");		
		outFileName = mSelectedFile.substring(0, dotPos);		
		outFile = mSdPath + RESULT_DIR + outFileName + Y_RESULT_NAME + RESULT_EXT;
		mResultOutputFile = outFile;

		Log.i("Debug", "outFile: " + outFile);
		/*
		 * Impact Text File : j8_acc_out_y.txt
		 */		
		setImpactOutFileName(outFileName, Y_AXIS);
		
		initProgressDialog(context, title, msg);
		
		/*
		 * Create and run DetectImpactThread 
		 */
		mDetectYPointsThread = new DetectYPointsThread(inFile, outFile, 
												mMaxThreshold, mMinThreshold, mYMsgHandler);
		
		// Synchronize ArrayList
		mYImpactArray = Collections.synchronizedList(mDetectYPointsThread.mYImpactPointArray);
		
		mDetectYPointsThread.setDaemon(true);
		// Start to check time
		mStartTimeMillis = System.currentTimeMillis();
		
		mDetectYPointsThread.start();

	}
	
	
	/*=============================================================================
	 * Name: displayImpactPoint
	 * 
	 * Description:
	 * 		Read impact points from the impact array list and 
	 * 		Display the impact points to a TextView widget  	
	 * 
	 * Return:
	 * 		None
	 *=============================================================================*/ 	
    public void displayImpactPoint(String timeDiff)
    {
    	int impactCount=0;
    	String stringTimeDiff = "";    	
    	
    	if(mXChecked)
    	{
    		impactCount = mMainImpactArray.size();
    	}
    	else
    	{
    		impactCount = mYImpactArray.size();
        	/*
        	for(int i=0; i<impactCount; i++)
        	{
        		String impactString = "";	
        		impactString = "index: " + (i+1) + " " + mYImpactArray.get(i).mTimestamp
        									 + " " + mYImpactArray.get(i).mXvalue
        									 + " " + mYImpactArray.get(i).mYvalue
        									 + " " + mYImpactArray.get(i).mZvalue + "\n";

        		mDetectImpactResultView.append(impactString);
        		
        		Log.i("Detect", impactString);
        	}
        */

    	}
    	
    	mDetectImpactTitleView.setText("The number of impact point: " + impactCount 
    									+ ", "+ timeDiff + "\n");

    	/*
    	for(int i=0; i<impactCount; i++)
    	{
    		String impactString = "";	
    		impactString = "index: " + (i+1) + " " + mMainImpactArray.get(i).mTimestamp
    									 + " " + mMainImpactArray.get(i).mXvalue
    									 + " " + mMainImpactArray.get(i).mYvalue
    									 + " " + mMainImpactArray.get(i).mZvalue + "\n";

    		mDetectImpactResultView.append(impactString);
    		
    		Log.i("Detect", impactString);
    	}
    */
    }
    
	/*=============================================================================
	 * Name: mXMsgHandler
	 * 
	 * Description:
	 * 		Process handle message	(X-axis values)	
	 * 
	 * Return:
	 * 		None
	 *=============================================================================*/    
    Handler mXMsgHandler = new Handler()
    {
    	public void handleMessage(Message msg)
    	{

    		mProgress.dismiss();
    		if(msg.what == MSG_DETECT_X)
    		{
    			
    			mDetectImpactTitleView.setText("Detecting process: count = " 
	    										+ Integer.toString(msg.arg1));
    		}
    		else if(msg.what == MSG_DETECT_DONE_X)
    		{
    			/*
    			 * Save the elapsed time to the output file
    			 */
    			mEndTimeMillis = System.currentTimeMillis();
    			String stringTimeDiff = getTimeDifference(mStartTimeMillis, mEndTimeMillis);
    			writeEventStringToFile(stringTimeDiff);
    			
    			//writeObjectToFile();
    			displayImpactPoint(stringTimeDiff);
    			closeFileOutStrem();
    		}
    		else if(msg.what == MSG_IMPACT_X)
    		{
    			String stringImpact = "";
    			stringImpact = "Impact:" + Integer.toString(msg.arg1) 
    							+ ", Time: " + Integer.toString(msg.arg2) + "\n";
    			
    			mDetectImpactResultView.append(stringImpact);
    			
    			// Write impact information to a file
    			writeEventStringToFile(stringImpact);    			
    			
    		}
    		else if(msg.what == MSG_PEAK_X)
    		{
    			String stringPeak = "";
    			stringPeak = "Peak:" + Integer.toString(msg.arg1)
    								+ ", Time: " + Integer.toString(msg.arg2) + "\n";
    			
    			mDetectImpactResultView.append(stringPeak);	// Debug: changsu
    		}
    	}
    };
    
	/*=============================================================================
	 * Name: mYMsgHandler
	 * 
	 * Description:
	 * 		Process handle message	(X-axis values)	
	 * 
	 * Return:
	 * 		None
	 *=============================================================================*/     
    Handler mYMsgHandler = new Handler()
    {
    	public void handleMessage(Message msg)
    	{

    		mProgress.dismiss();
    		if(msg.what == MSG_DETECT_Y)
    		{
    			
	    		mDetectImpactTitleView.setText("Detecting process: count = " 
	    										+ Integer.toString(msg.arg1));
    		}
    		else if(msg.what == MSG_DETECT_DONE_Y)
    		{
    			/*
    			 * Save the elapsed time to the output file
    			 */
    			mEndTimeMillis = System.currentTimeMillis();
    			String stringTimeDiff = getTimeDifference(mStartTimeMillis, mEndTimeMillis);
    			writeEventStringToFile(stringTimeDiff);
    			
    			//writeObjectToFile();
    			displayImpactPoint(stringTimeDiff);
    			closeFileOutStrem();
    		}
    		else if(msg.what == MSG_IMPACT_Y)
    		{
    			String stringImpact = "";
    			stringImpact = "- Peak:" + Integer.toString(msg.arg1) 
    							+ ", Time: " + Integer.toString(msg.arg2) + "\n";
    			
    			mDetectImpactResultView.append(stringImpact);
    			
    			// Write impact information to a file
    			writeEventStringToFile(stringImpact);    			
    			
    		}
    		else if(msg.what == MSG_PEAK_Y)
    		{
    			String stringPeak = "";
    			stringPeak = "+ Peak:" + Integer.toString(msg.arg1)
    								+ ", Time: " + Integer.toString(msg.arg2) + "\n";
    			
    			writeEventStringToFile(stringPeak);	// Added (2012.04.13)
    			mDetectImpactResultView.append(stringPeak);	// Debug: changsu
    		}
    	}
    };

	/*=============================================================================
	 * Name: writeObjectToFile
	 * 
	 * Description:
	 * 		Write ArrayList object to a file		
	 * 
	 * Return:
	 * 		None
	 *=============================================================================*/    
    public void writeObjectToFile()
    {
    	FileOutputStream mOutFileStream = null;
    	ObjectOutputStream mObjectOutputStream = null;

    	try
    	{
			mOutFileStream = new FileOutputStream(mResultOutputFile);
			mObjectOutputStream = new ObjectOutputStream(mOutFileStream);

			Log.i("Detect", "writeObject: " + mMainImpactArray.size());
			
			mObjectOutputStream.writeObject(mMainImpactArray);
			mObjectOutputStream.reset();				
		
			mOutFileStream.close();
			mObjectOutputStream.close();
    		
    	}
    	catch(Exception e)
    	{
    		System.out.println(e.getMessage());
    	}
    }

	/*=============================================================================
	 * Name: deleteArrayList
	 * 
	 * Description:
	 * 		Detect ArrayList
	 * 		- mAccelerationArray
	 * 		- mImpactPointArray		
	 * 		 
	 * Return:
	 * 		None
	 *=============================================================================*/    
    public void deleteArrayList()
    {
    	Log.i("Debug", "Array size: " + mMainImpactArray.size());
    	
    	if(mMainImpactArray.size() > 0)
    		mMainImpactArray.clear();    	
    }
	/*=============================================================================
	 * Name: setImpactOutFileName
	 * 
	 * Description:
	 * 		Detect ArrayList
	 * 		- mAccelerationArray
	 * 		- mImpactPointArray		
	 * 		 
	 * Return:
	 * 		None
	 *=============================================================================*/    
    public void setImpactOutFileName(String filename, int axis)
    {
    	if(axis == X_AXIS)
    		mImpactOutFileName = mSdPath + RESULT_DIR + filename + X_RESULT_NAME + ".txt";
    	else
    		mImpactOutFileName = mSdPath + RESULT_DIR + filename + Y_RESULT_NAME + ".txt";
    	
    	try
    	{
    		mImpactFile = new File(mImpactOutFileName);
    		mFileOutputStream = new FileOutputStream(mImpactFile);
    	}
    	catch(Exception e)
    	{
    		System.out.println(e.getMessage());
    	}
    	
    }
	/*=============================================================================
	 * Name: writeEventStringToFile
	 * 
	 * Description:
	 * 		Write events to the output file.
	 * 		This function is called by Handler when a thread send a message. 		
	 * 				
	 * 		 
	 * Return:
	 * 		None
	 *=============================================================================*/    
    public void writeEventStringToFile(String string) 
    {
    	try
    	{
    		mFileOutputStream.write(string.getBytes());
    	}
    	catch(IOException e)
    	{
    		Log.e("Debug", "Error: " + e.getMessage());
    	}
    }
	/*=============================================================================
	 * Name: closeFileOutStrem
	 * 
	 * Description:
	 * 		Close the output stream file
	 * 		This function is called by Handler. 		
	 * 		 
	 * Return:
	 * 		None
	 *=============================================================================*/        
    public void closeFileOutStrem()
    {
    	try 
    	{
			mFileOutputStream.close();			
		} 
    	catch (IOException e) 
    	{		
			e.printStackTrace();
		}
    }
    
    public String getTimeDifference(long start, long end)
    {
    	String stringTimeDiff = "";
    	long timeDiff = 0;
    	// Display time    	
    	 timeDiff = end - start;
    	stringTimeDiff = "\nElapsed Time: " + (timeDiff/1000) + "." + ((timeDiff%1000)/10) + " sec";
    	
    	return stringTimeDiff;
    }
}
