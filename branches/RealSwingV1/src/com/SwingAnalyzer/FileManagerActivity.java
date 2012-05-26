package com.SwingAnalyzer;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.*;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.*;

public class FileManagerActivity extends Activity{
	final static String GOLFSWING_DATA_DIR = "/data/GolfSwingAnalyzer";
	final static String EXTERNAL_SWING_DIR = "/externalswing/";
	final static String COLLECTED_SWING_DIR = "/collectedswing/";
	
	final static String COLLECTED_SWING_PATH = GOLFSWING_DATA_DIR + COLLECTED_SWING_DIR;
	final static String OUTPUT_FILENAME = "swing";
	final static String OUTPUT_FILE_EXT = ".dat";
	
	final static String OUTPUT_TEXT_FILE = "swing";
	final static String OUTPUT_TEXTFILE_EXT = ".txt";
	
	ArrayList<String> mFileItems;
	ArrayAdapter<String> mFileListAdapter;
	ListView mFileListView;

	String mSDPathName = "";
	/*
	 * Widgets
	 */
	Button mDeleteAllButton;
	Button mDeleteSelectionButton;
	Button mBackButton;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.filemanager);
		
		mFileItems = new ArrayList<String>();
		
		mFileListView = (ListView)findViewById(R.id.filemanager_list);
		
		mDeleteAllButton = (Button)findViewById(R.id.filemanager_delete_all);
		mDeleteAllButton.setOnClickListener(mClickListener);
		
		mDeleteSelectionButton = (Button)findViewById(R.id.filemanager_delete);
		mDeleteSelectionButton.setOnClickListener(mClickListener);
		
		mBackButton = (Button)findViewById(R.id.filemanager_back);
		mBackButton.setOnClickListener(mClickListener);
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
		
		searchSwingFiles();
	}
	
	Button.OnClickListener mClickListener = new Button.OnClickListener()
	{

		public void onClick(View v) {
			switch(v.getId())
			{
			case R.id.filemanager_delete_all:
				deleteAllItems();
				break;
			case R.id.filemanager_delete:
				deleteSelectedItems();
				break;
			case R.id.filemanager_back:
				startActivity(new Intent(FileManagerActivity.this, SwingPastDataFeedback.class));
				finish();
				break;
			}
			
		}
		
	};
	
	public void deleteSelectedItems()
	{
		SparseBooleanArray sb = mFileListView.getCheckedItemPositions();
		if (sb.size() != 0) 
		{
			for (int i = mFileListView.getCount() - 1; i >= 0 ; i--) 
			{
				if (sb.get(i)) 
				{
					deleteSelectedFiles(mFileItems.get(i));
					mFileItems.remove(i);
				}
			}
			mFileListView.clearChoices();
			mFileListAdapter.notifyDataSetChanged();
		}
	}
	
	public void deleteAllItems()
	{
		/*for (int i = 0; i < mFileListView.getCount() - 1; i++) 
		{
			mFileItems.remove(i);
			Log.i("filemanager", "deleteAllItems: " + i + " mFileListView:" + mFileListView.getCount());
		}
		*/
		deleteAllFiles();
		mFileItems.clear();		
		mFileListAdapter.notifyDataSetChanged();

	}
	
	/*=============================================================================
	 * Name: deleteFiles
	 * 
	 * Description:
	 * 		Delete files older than the day to save memory space
	 * 
	 * Return:
	 * 		None
	 *=============================================================================*/ 
    public void deleteAllFiles()
    {
    	if(mSDPathName.isEmpty())
    		mSDPathName = getSDPathName();
    	
    	File directory = new File(mSDPathName + COLLECTED_SWING_PATH);
    	int i;
    	if(directory.exists())
    	{
    		File[] listFiles = directory.listFiles();

    		for(i=0; i< listFiles.length; i++)
    		{
				Log.i("filemanager", "Delete All Files: " + listFiles[i]);
				listFiles[i].delete();			
    		}
    	}
    }

    public void deleteSelectedFiles(String fileName)
    {
    	String txtFileName = fileName.substring(0, fileName.indexOf("."));
    	
    	txtFileName += OUTPUT_TEXTFILE_EXT;
    	
    	if(mSDPathName.isEmpty())
    		mSDPathName = getSDPathName();

    	File delFile = new File(mSDPathName + COLLECTED_SWING_PATH + fileName);    	
    	File delTxtFile = new File(mSDPathName + COLLECTED_SWING_PATH + txtFileName);
    	
    	if(delFile.exists())
    	{
    		Log.i("filemanager", "Delete File:" + delFile + " txt file:" + delTxtFile);
    		delFile.delete();
    		if(delTxtFile.exists())
    			delTxtFile.delete();
    	}
    	else
    	{
    		Toast.makeText(this, fileName + " does not exist.", Toast.LENGTH_LONG).show();
    	}

    }

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
    	
    	mSDPathName = getSDPathName();
    	
    	if(mSDPathName != Environment.MEDIA_UNMOUNTED)
    	{
    		searchFilesinSdPath(mSDPathName + COLLECTED_SWING_PATH);    		
    	}
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
    	
    	Log.i("filemanager", "Collected Swing Dir Path: " + swingDataPath);
    	
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

        	insertFileNameToListView(sortedFileNameList);
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
    private void insertFileNameToListView(String[] filenames)
    {

    	for(int i = 0; i < filenames.length; i++)
    	{
    		mFileItems.add(filenames[i]);
    	}
    	mFileListAdapter = new ArrayAdapter<String>(this, 
    												android.R.layout.simple_list_item_multiple_choice,
    												mFileItems);
    	mFileListView.setAdapter(mFileListAdapter);
    	mFileListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
    	
    	mFileListView.setOnItemClickListener(mItemClickListener);
    	
    }

    AdapterView.OnItemClickListener mItemClickListener = 
    		new AdapterView.OnItemClickListener()
    {

		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			// TODO Auto-generated method stub
			
		}
    	
    };
}
