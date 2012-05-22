package com.SwingAnalyzer;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

public class SettingsActivity extends Activity{
	
	private static final String PREFERENCE_SETTING = "settings";
	private static final String PREF_COLLECTION_TIME = "collection_time";
	private static final String PREF_BEEP_METHOD = "beep_method";
	private static final String PREF_MAX_THRESHOLD = "max_threshold";
	private static final String PREF_MIN_THRESHOLD = "min_threshold";	
	private static final String PREF_PHONE_FRONT_PLACEMENT = "placement";
	private static final String PREF_DATA_KEEPING_TIME = "keeping_time";
	
	private static final int DEFAULT_MAX_THRESHOLD = 5;
	private static final int DEFAULT_MIN_THRESHOLD = -5;
	
	final static int LIST_COLLECTION_TIME 	= 0;
	final static int LIST_FEEDBACK_SOUND 	= 1;
	final static int LIST_SWING_THRESHOLD 	= 2;
	final static int LIST_PHONE_PLACEMENT 	= 3;
	final static int LIST_KEEPING_TIME 		= 4;
	
	final static int MENU_ITEM_NUMBER 		= 5;
	/*
	 * Widgets
	 */
	ListView mSettingListView;
	Button mSettingBackButton;

	
	ArrayList<SettingItem> mSettingItemArrayList;
	SettingItem mSettingItem;
	SettingListAdapter mSettingListAdapter;
	
	int [] mIconItems = {R.drawable.setting_icon_time,
						 R.drawable.setting_icon_beep,
						 R.drawable.setting_icon_threshold,
						 R.drawable.setting_icon_placement,
						 R.drawable.setting_icon_keepingtime};
	
	String[] mMenuItems = {"Swing collection time", 
							"Feedback sound",
							"Swing threshold for detection", 
							"Placement of your body",
							"Swing data keeping time"
							};
	
	String[] mValueItems = {"3 seconds", 
							"Beep", 
							"Max=5, Min=-5", 
							"Back (Waist)", 
							"No limitation"
							};
	
	
	String[] mOptionCollectionTime = {"3 seconds", "4 seconds", "5 seconds"};
	int[] 	mIntCollectionTime = {3,4,5};
	
	String[] mOptionFeedbackBeep = {"Beep", "Musical Notes"};
	boolean[] mBoolFeedbackBeep = {false, true};
	
	String[] mOptionPlacement = {"Front (Chest)", "Back (Waist)"};
	boolean[] mBoolPlacement = {false, true};
	
	String[] mOptionKeepingTime = {"No limitation", "1 days", "7 days", "15 days", "30 days", "180 days"};
	int[] mIntKeepingTime = {0, 1, 7, 15, 30, (30*6)};
	
	int mSelectedItem = 0;
	int mSwingTime = 0;
	boolean mMusicalNote = false;
	
	
	int mThresholdX = 0;
	int mThresholdY = 0;
	
	int mPositionListItem = 0;
	LinearLayout mThresholdLayout;
	EditText mDlgMaxThreshold;
	EditText mDlgMinThreshold;
	
	/*=================================================
	 * Pref Variables
	 *====================================================*/
	private int mCollectionTime = 0;	
	private int mMaxThreshold = 0;
	private int mMinThreshold = 0;
	private boolean mMusicalNotesChecked = false;
	private boolean mFrontPlacement = false;
	private int mDataKeepingTime = 0;
	
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings);
		
        loadSettingValues();
        //String[] mValueItems = {"3 seconds", "Beep", "Max=5, Min=-5", "Back (Waist)"};
        mSettingItemArrayList = new ArrayList<SettingItem>();        
        
        for(int i=0; i<MENU_ITEM_NUMBER; i++)
        {
        	SettingItem item = new SettingItem(mIconItems[i], mMenuItems[i], mValueItems[i]);
        	mSettingItemArrayList.add(item);
        }
        
       
        mSettingListAdapter = new SettingListAdapter(this,
        												R.layout.setting_icon_text, 
        												mSettingItemArrayList);
        
        mSettingListView = (ListView)findViewById(R.id.setting_listItem);
        mSettingListView.setAdapter(mSettingListAdapter);
        mSettingListView.setOnItemClickListener(mItemClickListener);
        
        mSettingBackButton = (Button)findViewById(R.id.setting_back);
        mSettingBackButton.setOnClickListener(mClickListener);
	}
    Button.OnClickListener mClickListener = new Button.OnClickListener()
    {

		public void onClick(View v) {
			if(v.getId() == R.id.setting_back)
			{
				saveSettingValues();
				startActivity(new Intent(SettingsActivity.this, Home.class));
				finish();
			}
			
		}
    	
    };
    
	AdapterView.OnItemClickListener mItemClickListener = new AdapterView.OnItemClickListener() 
	{

		public void onItemClick(AdapterView<?> parent, View view, int position,	long id) 
		{
			
			switch(position)
			{
			case LIST_COLLECTION_TIME:				
				showDialogCollectionTime();
				break;
			case LIST_FEEDBACK_SOUND:
				showDialogFeedbackSound();				
				break;
			case LIST_SWING_THRESHOLD:
				showDialogThreshold();
				break;
			case LIST_PHONE_PLACEMENT:
				showDialogPhonePlacement();
				break;
			case LIST_KEEPING_TIME:
				showDialogDataKeepingTime();
				break;
			
			}
			
		}
		
	};
	
	public void showDialogCollectionTime()
	{
		int selection = 0;
		
		selection = mCollectionTime - 3;
		
		AlertDialog.Builder dlg = new AlertDialog.Builder(this);
		
		dlg.setTitle("Select a Swing Collection Time");		
		dlg.setIcon(R.drawable.ic_launcher);
		dlg.setSingleChoiceItems(mOptionCollectionTime, selection, 
			new DialogInterface.OnClickListener() 
			{
				public void onClick(DialogInterface dialog, int which) 
				{
					mSelectedItem = which;
					mCollectionTime = mSelectedItem + 3;
					String value = Integer.toString(mCollectionTime) + " seconds";
					Log.i("setting", "Collection Time: " + mCollectionTime + " seconds" + " Value=" + value);
					
					updateSettingItemValue(LIST_COLLECTION_TIME, value);
					dialog.dismiss();
				}
		});
		
		dlg.show();
	}

	public void showDialogFeedbackSound()
	{
		int selection = 0;
		
		if(mMusicalNotesChecked == false)
			selection = 0;
		else
			selection =1;
		
		AlertDialog.Builder dlg = new AlertDialog.Builder(this);
		dlg.setTitle("Select Feedback Sound");
		dlg.setIcon(R.drawable.ic_launcher);
		dlg.setSingleChoiceItems(mOptionFeedbackBeep, selection, 
				new DialogInterface.OnClickListener() 
				{
					public void onClick(DialogInterface dialog, int which) 
					{
						String value = "";
						if(which == 0)
						{
							mMusicalNotesChecked = false;
							value = "Beep";
						}
						else
						{
							mMusicalNotesChecked = true;
							value = "Musical Notes";
						}
						updateSettingItemValue(LIST_FEEDBACK_SOUND, value);
						dialog.dismiss();
					}
				});
		dlg.show();
	}
	
	public void showDialogThreshold()
	{
		mThresholdLayout = (LinearLayout)View.inflate(this, 
														R.layout.threshold, null);

		mDlgMaxThreshold = (EditText)mThresholdLayout.findViewById(R.id.settings_threshold_hi);
		mDlgMinThreshold = (EditText)mThresholdLayout.findViewById(R.id.settings_threshold_low);

		AlertDialog.Builder dlg = new AlertDialog.Builder(this);
		dlg.setTitle("Type Threshold values");
		dlg.setIcon(R.drawable.ic_launcher);
		dlg.setView(mThresholdLayout);
		dlg.setPositiveButton("Ok", mClickDialogButton);
		dlg.setNegativeButton("Cancel", mClickDialogButton);
		
		Log.i("setting", "mMaxThreshold= " + mMaxThreshold + "mMinThreshold= " + mMinThreshold);
		mDlgMaxThreshold.setText(String.valueOf(mMaxThreshold));
		mDlgMaxThreshold.setSelection(mDlgMaxThreshold.getText().length());	
		
		mDlgMinThreshold.setText(String.valueOf(mMinThreshold));
		
		dlg.show();
		
	}
	public void showDialogPhonePlacement()
	{
		int selection = 0;
		
		if(mFrontPlacement == true)
			selection = 0;
		else
			selection = 1;
		
		AlertDialog.Builder dlg = new AlertDialog.Builder(this);
		dlg.setTitle("Select Phone Location");
		dlg.setIcon(R.drawable.ic_launcher);
		dlg.setSingleChoiceItems(mOptionPlacement, selection, 
				new DialogInterface.OnClickListener() {
					
					public void onClick(DialogInterface dialog, int which) {
						String value = "";
						if(which == 0)
						{
							mFrontPlacement = true;
							value = mOptionPlacement[which];
						}
						else
						{
							mFrontPlacement = false;
							value = mOptionPlacement[1];
						}
						updateSettingItemValue(LIST_PHONE_PLACEMENT, value);
						dialog.dismiss();

					}
				});
		dlg.show();
		
	}
	
	public void showDialogDataKeepingTime()
	{
		int selection = 0;
		
		if(mDataKeepingTime == 0)
			selection = 0;
		else if(mDataKeepingTime == 1)
			selection = 1;
		else if(mDataKeepingTime == 7)
			selection = 2;
		else if(mDataKeepingTime == 15)
			selection = 3;
		else if(mDataKeepingTime == 30)
			selection = 4;
		else
			selection = 5;
		
		AlertDialog.Builder dlg = new AlertDialog.Builder(this);
		
		dlg.setTitle("Select Data Keeping Time");		
		dlg.setIcon(R.drawable.ic_launcher);
		dlg.setSingleChoiceItems(mOptionKeepingTime, selection, 
			new DialogInterface.OnClickListener() 
			{
				public void onClick(DialogInterface dialog, int which) 
				{
					mDataKeepingTime = mIntKeepingTime[which];					
					String value = mOptionKeepingTime[which];
					Log.i("setting", "Data Keeping Time: " + value + ". Days=" + mDataKeepingTime);
					
					updateSettingItemValue(LIST_KEEPING_TIME, value);
					dialog.dismiss();
				}
		});
		
		dlg.show();

	}
    public void updateSettingItemValue(int itemIndex, String value)
    {
    	mSettingItemArrayList.get(itemIndex).mValue = value;
    	mSettingListAdapter.notifyDataSetChanged();
    }
    
    DialogInterface.OnClickListener mClickDialogButton = new DialogInterface.OnClickListener() {
		
		public void onClick(DialogInterface dialog, int which) {
			if(which == DialogInterface.BUTTON1)
			{
				String value = "";
				
				String maxValue = mDlgMaxThreshold.getText().toString();
				mMaxThreshold = Integer.parseInt(maxValue);
				
				String minValue = mDlgMinThreshold.getText().toString();
				mMinThreshold = Integer.parseInt(minValue);
				
				value = "Max=" + mMaxThreshold + ", Min=" + mMinThreshold;
				updateSettingItemValue(LIST_SWING_THRESHOLD, value);
			}
			
		}
	};
	public void loadSettingValues()
	{
		SharedPreferences pref = getSharedPreferences(PREFERENCE_SETTING, MODE_PRIVATE);
		String stringCollectionTime = "";
		String stringMusicalNotes = "";
		String stringThreshold = "";
		String stringPhonePlacement = "";
		String stringKeepingTime = "";
		
		mCollectionTime = pref.getInt(PREF_COLLECTION_TIME, 3);
		
		mMusicalNotesChecked = pref.getBoolean(PREF_BEEP_METHOD, true);
		mMaxThreshold = pref.getInt(PREF_MAX_THRESHOLD, DEFAULT_MAX_THRESHOLD);
		mMinThreshold = pref.getInt(PREF_MIN_THRESHOLD, DEFAULT_MIN_THRESHOLD);
		mFrontPlacement = pref.getBoolean(PREF_PHONE_FRONT_PLACEMENT,	false);	
		mDataKeepingTime = pref.getInt(PREF_DATA_KEEPING_TIME, 0);
		
		Log.i("setting", "loadSettingValues======");
		Log.i("setting", "'PREF_COLLECTION_TIME: " + mCollectionTime);
		Log.i("setting", "PREF_BEEP_METHOD: " + mMusicalNotesChecked);
		Log.i("setting", "PREF_MAX_THRESHOLD: " + mMaxThreshold);
		Log.i("setting", "PREF_MIN_THRESHOLD: " + mMinThreshold);
		Log.i("setting", "PREF_PHONE_FRONT_PLACEMENT: " + mFrontPlacement);
		Log.i("setting", "PREF_DATA_KEEPING_TIME: " + mDataKeepingTime);

		stringCollectionTime = Integer.toString(mCollectionTime) + " seconds";
		mValueItems[LIST_COLLECTION_TIME] = stringCollectionTime;

		if(mMusicalNotesChecked == false)
			stringMusicalNotes = "Beep";
		else
			stringMusicalNotes = "Musical Notes";
		mValueItems[LIST_FEEDBACK_SOUND] = stringMusicalNotes;
		
		stringThreshold = "Max=" + mMaxThreshold + ", Min=" + mMinThreshold;
		mValueItems[LIST_SWING_THRESHOLD] = stringThreshold;
		
		if(mFrontPlacement == true)
			stringPhonePlacement = "Front (Chest)";
		else
			stringPhonePlacement = "Back (Waist)";
		mValueItems[LIST_PHONE_PLACEMENT] = stringPhonePlacement;
		
		if(mDataKeepingTime == 0)
			stringKeepingTime = mOptionKeepingTime[0];
		else
		{
			stringKeepingTime = mDataKeepingTime + " days";
		}
		mValueItems[LIST_KEEPING_TIME] = stringKeepingTime;
		
	}
	
	public void saveSettingValues()
	{
		SharedPreferences pref = getSharedPreferences(PREFERENCE_SETTING, MODE_PRIVATE);
		SharedPreferences.Editor editor = pref.edit();
		
		editor.putInt(PREF_COLLECTION_TIME, mCollectionTime);
		editor.putBoolean(PREF_BEEP_METHOD, mMusicalNotesChecked);
		editor.putBoolean(PREF_PHONE_FRONT_PLACEMENT, mFrontPlacement);
		
		editor.putInt(PREF_MAX_THRESHOLD, mMaxThreshold);
		editor.putInt(PREF_MIN_THRESHOLD, mMinThreshold);
		editor.putInt(PREF_DATA_KEEPING_TIME, mDataKeepingTime);
		
		editor.commit();
		
		Log.i("setting", "saveSettingValues======");
		Log.i("setting", "'PREF_COLLECTION_TIME: " + mCollectionTime);
		Log.i("setting", "PREF_BEEP_METHOD: " + mMusicalNotesChecked);
		Log.i("setting", "PREF_MAX_THRESHOLD: " + mMaxThreshold);
		Log.i("setting", "PREF_MIN_THRESHOLD: " + mMinThreshold);
		Log.i("setting", "PREF_PHONE_FRONT_PLACEMENT: " + mFrontPlacement);
		Log.i("setting", "PREF_DATA_KEEPING_TIME: " + mDataKeepingTime);
	}
	

}

/*=============================================================================
 * Name: class SettingItem
 * 
 * Description:
 * 		- For the custom list items(icon + text + icon) 		
 *=============================================================================*/	
class SettingItem 
{
	int mIcon;
	int mArrowIcon;
	String mItem;
	String mValue;
	
	SettingItem(int icon, String item, String value)
	{
		mIcon = icon;
		mItem = item;
		mValue = value;
	}
	
}

class SettingListAdapter extends BaseAdapter
{
	Context mainContext;
	LayoutInflater Inflater;
	ArrayList<SettingItem> arItem;
	int mLayout;
	
	public SettingListAdapter(Context context, int layout, ArrayList<SettingItem> src)
	{
		mainContext = context;
		Inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		arItem = src;
		mLayout = layout;
	}
	
	public int getCount() 
	{
		return arItem.size();
	}

	public Object getItem(int position) 
	{
		return arItem.get(position).mItem;
	}

	public long getItemId(int position) 
	{	
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) 
	{
	
		if(convertView == null)
		{
			convertView = Inflater.inflate(mLayout, parent, false);
		}

		ImageView img = (ImageView)convertView.findViewById(R.id.setting_image);
		img.setImageResource(arItem.get(position).mIcon);
		
		TextView txtItem = (TextView)convertView.findViewById(R.id.setting_menu);
		txtItem.setText(arItem.get(position).mItem);
		
		TextView txtValue = (TextView)convertView.findViewById(R.id.setting_value);
		txtValue.setText(arItem.get(position).mValue);

		return convertView;
	}
	
}
