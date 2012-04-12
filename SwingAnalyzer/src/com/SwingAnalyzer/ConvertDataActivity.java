package com.SwingAnalyzer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


import android.app.*;
import android.content.*;
import android.os.*;
import android.util.Log;
import android.view.*;
import android.widget.*;

public class ConvertDataActivity extends Activity{

	final static String GOLFSWING_DIR = "/data/golfswing/";
	final static String ACCELERATION_DIR = "/data/acceldata/";
	
	final static String OUTPUT_FILE = "_out.acc";
	final static String OUT_EXT = "acc";
	
	final static int IMPACT_MAX = 10;		// 
	final static int IMPACT_MIN = -5;
	
	final static int MSG_CONVERT 	= 0;
	final static int MSG_DETECT 	= 1;
	final static int MSG_PEAK		= 2;
	final static int MSG_IMPACT 	= 3;

	List<AccelerationData> mSwingAccelArray;
	FileAnalyzerThread mFileAnalyzerThread;


	/*
	 * Class member variables
	 */
	String mSdPath;
	String mSelectedFile = "";		// the selected file from a spinner for analyzing
	String mConvertFile = "";
	String mOutputFile = "";

	boolean misConverted;
	/* 
	 * Widgets
	 */
	Spinner mFileNameSpinner;

	Button mConvertingButton;
	Button mExitButton;
	
	TextView mResultTextView;
	TextView mImpactTextView;
	TextView mImpactTitleTextView;
	
	ProgressDialog mProgress;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.converting_data);                
        
        mFileNameSpinner = (Spinner)findViewById(R.id.file_spinner);
        mFileNameSpinner.setOnItemSelectedListener(mItemSelectedListener);
        
        mConvertingButton = (Button)findViewById(R.id.converting_button);        
        mConvertingButton.setOnClickListener(mClickListener);
        
        mResultTextView = (TextView)findViewById(R.id.result_text);
        mImpactTextView = (TextView)findViewById(R.id.impactpoint_text);
        mImpactTitleTextView = (TextView)findViewById(R.id.impact_title);
        
        initVariables();        
      
        searchRawFiles();
        
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
		searchRawFiles();
		super.onResume();
	}

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
			case R.id.converting_button:
				startConvertProcess(v.getContext());				
				break;
			}
		}
		
	};
	
	/*=============================================================================
	 * Name: initVariables
	 * 
	 * Description:
	 * 		Initialize member variables
	 * 
	 * Return:
	 * 		None
	 *=============================================================================*/	
    public void initVariables()
    {
        mSdPath = "";
        mSelectedFile = "";
        mConvertFile = "";
        mOutputFile = "";
        
        mResultTextView.setText("");
        
        mImpactTitleTextView.setText("Impact point: Criteria: > " + IMPACT_MAX 
        								+ " or < " + IMPACT_MIN + "\n");
        
        mImpactTextView.setText("");
        
        mSwingAccelArray = new ArrayList<AccelerationData>();
    }
    
	/*=============================================================================
	 * Name: searchRawFiles
	 * 
	 * Description:
	 * 		Set file names for input and output		
	 * 		
	 * 
	 * Return:
	 * 		String
	 *=============================================================================*/	
    public void searchRawFiles()
    {
    	String stringSdPath = "";
    	
    	stringSdPath = getSDCardPath();
    	if(stringSdPath != Environment.MEDIA_UNMOUNTED)
    	{
    		
    		makeOutputDir(stringSdPath);
    		searchFilesinSdPath(stringSdPath);
    		
    		Log.i("Debug", "Filename: " + mSelectedFile);
    		mResultTextView.setText("Selected file: " + mSelectedFile);
    	}
    	else
    		mSelectedFile = "";
    }
    
    public void makeOutputDir(String dir)
    {
    	File outputDir = new File(dir + ACCELERATION_DIR);
    	
    	if(outputDir.mkdir() == true)
    		Log.i("Debug", "mkdir is successful: " + dir + ACCELERATION_DIR);
    	else
    		Log.i("Debug", "mkdir failed: " + dir + ACCELERATION_DIR);
    }
    
	/*=============================================================================
	 * Name: getSDCardPath
	 * 
	 * Description:
	 * 		Check whether a SD card is mounted or not		
	 * 		If mounted, return the absolute SD card path name
	 * 
	 * Return:
	 * 		String
	 *=============================================================================*/	
    public String getSDCardPath()
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
    	File swingDir = new File(sdPath + GOLFSWING_DIR);
    	
    	
    	Log.i("Debug", "PATH: " + sdPath + GOLFSWING_DIR);
    	
    	if(swingDir.isDirectory())
    	{
        	String[] fileNameList = swingDir.list();
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
    						new ArrayAdapter<String>(mFileNameSpinner.getContext(),
    											android.R.layout.simple_spinner_item,
    											filenames);
    	
    	spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    	mFileNameSpinner.setAdapter(spinnerAdapter);
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
		
			mSelectedFile = (String)parent.getSelectedItem();		 
			mOutputFile = "";
			mConvertFile = "";
			Log.i("Debug", "Selected File: " + mSelectedFile);
			
		}

		public void onNothingSelected(AdapterView<?> arg0) {
			// TODO Auto-generated method stub
			
		}
	};
	

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
							mFileAnalyzerThread.mFinished = false;
							mProgress.dismiss();
						}
		});
		
		mProgress.show();
	}

	/*=============================================================================
	 * Name: startConvertProcess
	 * 
	 * Description:
	 * 		When the Convert File button is clicked, run the FileAnalyzerThread  	
	 * 
	 * Return:
	 * 		None
	 *=============================================================================*/ 
	public void startConvertProcess(Context context)
	{
		String fileName = "";
		String extName = "";
		String outFileName = "";
		
		int dotpos = 0;
		
 
		if(mSelectedFile.isEmpty())
			return;

		mConvertFile = mSdPath + GOLFSWING_DIR + mSelectedFile;   
		

		dotpos = mSelectedFile.lastIndexOf(".");
		
		fileName = mSelectedFile.substring(0, dotpos);
		//extName = mSelectedFile.substring(dotpos+1, mSelectedFile.length());
		extName = OUT_EXT;
		
		outFileName = fileName + "_out." + extName;
		
		mOutputFile = mSdPath + ACCELERATION_DIR + outFileName;
		
		Log.i("Debug", "mConvertFile: " + mConvertFile);
		Log.i("Debug", "OutputFile: " + mOutputFile);
		
		initProgressDialog(context, "Converting", "Wait...");
		
		
		
		mFileAnalyzerThread = new FileAnalyzerThread(mConvertFile, 
													mOutputFile, 
													mMsgHandler);
		
		
		mSwingAccelArray = Collections.synchronizedList(mFileAnalyzerThread.mAccelerationArray);
		
		mFileAnalyzerThread.setDaemon(true);
		mFileAnalyzerThread.start();
		
		
	}

    
	/*=============================================================================
	 * Name: mMsgHandler
	 * 
	 * Description:
	 * 		Process handle message		
	 * 
	 * Return:
	 * 		None
	 *=============================================================================*/    
    Handler mMsgHandler = new Handler()
    {
    	public void handleMessage(Message msg)
    	{

    		mProgress.dismiss();
    		if(msg.what == MSG_CONVERT)
    		{
    			if(mFileAnalyzerThread.mFinished == true)
    			{
    				//Log.i("Debug", "Converting is finished");
    				misConverted = true;
    				
        			mResultTextView.setText("Finished converting a file: " + mSelectedFile 
        									+ ", count = " + Integer.toString(msg.arg1));
        			
        			
        			//writeObjectToFile();
    			}
    			else
    			{
    				
    				mResultTextView.setText("Converting a file: " + mSelectedFile + ", count = " 
        					+ Integer.toString(msg.arg1));
        			
    			}
    		}

    	}
    };

    public void writeObjectToFile()
    {
    	FileOutputStream mOutFileStream = null;
    	ObjectOutputStream mObjectOutputStream = null;

    	try
    	{
			mOutFileStream = new FileOutputStream(mOutputFile);
			mObjectOutputStream = new ObjectOutputStream(mOutFileStream);

			Log.i("Debug", "writeObject: " + mSwingAccelArray.size());
			
			mObjectOutputStream.writeObject(mSwingAccelArray);
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
    	Log.i("Debug", "Array size: " + mSwingAccelArray.size());
    	
    	if(mSwingAccelArray.size() > 0)
    		mSwingAccelArray.clear();    	
    }
    
	/*=============================================================================
	 * Name: displayResult
	 * 
	 * Description:
	 * 		Print a message to Result View widget		
	 * 
	 * Return:
	 * 		None
	 *=============================================================================*/    
    public void displayResult(String msg)
    {
    	mResultTextView.setText("");
    	mResultTextView.setText(msg);
    }
    
}
