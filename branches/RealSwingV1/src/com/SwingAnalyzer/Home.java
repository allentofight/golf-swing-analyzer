package com.SwingAnalyzer;

import java.io.File;

import android.app.*;
import android.content.*;
import android.os.*;
import android.widget.*;
import android.util.Log;
import android.view.*;

public class Home extends Activity implements View.OnClickListener{
	final static String GOLFSWING_DATA_DIR = "/data/GolfSwingAnalyzer";
	final static String EXTERNAL_SWING_DIR = "/externalswing/";
	final static String COLLECTED_SWING_DIR = "/collectedswing/";
	
	final static String COLLECTED_SWING_PATH = GOLFSWING_DATA_DIR + COLLECTED_SWING_DIR;
	/*
	 * Widgets
	 */
	ImageButton mAnalyzeSwingButton;
	ImageButton mCollectingDataButton;
	ImageButton mStatisticsButton;
	
	ImageButton mSettingsButton;
	
	/*
	 * Member Variables
	 */
	String mSDCardPath= "";
	

	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.home);
		
		mAnalyzeSwingButton = (ImageButton)findViewById(R.id.home_real_swing_button);
		mAnalyzeSwingButton.setOnClickListener(this);
		
		mCollectingDataButton = (ImageButton)findViewById(R.id.home_collecting_data_button);
		mCollectingDataButton.setOnClickListener(this);
		
		mStatisticsButton = (ImageButton)findViewById(R.id.home_statistics_button);
		mStatisticsButton.setOnClickListener(this);
		
		mSettingsButton = (ImageButton)findViewById(R.id.home_settings_button);
		mSettingsButton.setOnClickListener(this);
		
		makeGolfSwingDataFolders();
		
	}

	public void onClick(View v)
	{
		switch(v.getId())
		{
		case R.id.home_real_swing_button:
			startActivity(new Intent(Home.this, RealSwingAnalysisActivity.class));
			finish();
			break;
		case R.id.home_collecting_data_button:
			startActivity(new Intent(Home.this, CollectingAccelerationData.class));
			finish();
			break;
		case R.id.home_settings_button:
			startActivity(new Intent(Home.this, SettingsActivity.class));
			finish();
			break;
		case R.id.home_statistics_button:
			startActivity(new Intent(Home.this, StatisticsActivity.class));
			finish();
			break;
		}
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

}
