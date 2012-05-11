package com.SwingAnalyzer;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;

public class SettingsActivity extends Activity{
	
	private static final String PREFERENCE_SETTING = "settings";
	private static final String PREF_COLLECTION_TIME = "collection_time";
	private static final String PREF_BEEP_METHOD = "beep_method";
	private static final String PREF_MAX_THRESHOLD = "max_threshold";
	private static final String PREF_MIN_THRESHOLD = "min_threshold";
	
	private static final int DEFAULT_MAX_THRESHOLD = 10;
	private static final int DEFAULT_MIN_THRESHOLD = -10;
	
	/*
	 * Widgets
	 */
	Spinner mCollectionTimeSpinner;
	RadioGroup mRadioBeepMethod;
	
	Button mOkButton;
	Button mCancelButton;
	
	EditText mMaxThresholdEditText;
	EditText mMinThresholdEditText;
	
	
	/*
	 * Member Variables
	 */
	//AccelerationData mAccelerationData;
	
	String[] collectionTime = {"3", "4", "5"};	// second
	
	boolean mMusicalNotesChecked = false;
	int mCollectionTime = 0;	
	int mMaxThreshold = 0;
	int mMinThreshold = 0;
	
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings);
		
		mRadioBeepMethod = (RadioGroup)findViewById(R.id.radio_group_beep);
		mRadioBeepMethod.setOnCheckedChangeListener(mCheckedChangeListener);
		
		mOkButton = (Button)findViewById(R.id.settings_ok_button);
		mOkButton.setOnClickListener(mClickListener);
		
		mCancelButton = (Button)findViewById(R.id.settings_cancel_button);
		mCancelButton.setOnClickListener(mClickListener);
		
		mMaxThresholdEditText = (EditText)findViewById(R.id.settings_threshold_hi);
		mMinThresholdEditText = (EditText)findViewById(R.id.settings_threshold_low);
		
		ArrayAdapter<String> arrayItem = new ArrayAdapter<String>(this, 
											android.R.layout.simple_spinner_dropdown_item,
											collectionTime);
		mCollectionTimeSpinner = (Spinner)findViewById(R.id.collection_time_spinner);
		mCollectionTimeSpinner.setPrompt("Select time");
		mCollectionTimeSpinner.setAdapter(arrayItem);		
		mCollectionTimeSpinner.setOnItemSelectedListener(mItemSelectedListener);
		
		loadSettingValues();
	}

	
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}
	
	RadioGroup.OnCheckedChangeListener mCheckedChangeListener =
			new RadioGroup.OnCheckedChangeListener() 
	{
				
		public void onCheckedChanged(RadioGroup group, int checkedId) {
			// TODO Auto-generated method stub
			if(group.getId() == R.id.radio_group_beep)
			{
				switch(checkedId)
				{
				case R.id.radio_beep:
					mMusicalNotesChecked = false;
					AccelerationData.setFeedbackMethod(false);
					break;
				case R.id.radio_musical_notes:
					mMusicalNotesChecked = true;
					AccelerationData.setFeedbackMethod(true);
					break;
				}
			}
		}
	};
	
	Spinner.OnItemSelectedListener mItemSelectedListener = new Spinner.OnItemSelectedListener()
	{

		public void onItemSelected(AdapterView<?> parent, View view, int position,
				long id) {
			// TODO Auto-generated method stub
			mCollectionTime = Integer.parseInt(parent.getItemAtPosition(position).toString());
			AccelerationData.setSwingCollectionTime(mCollectionTime);
			Log.i("setting", "Collectin Time: " + mCollectionTime);
		}
		public void onNothingSelected(AdapterView<?> arg0) {
			// TODO Auto-generated method stub
			
		}
	};
	
	Button.OnClickListener mClickListener = new View.OnClickListener()
	{

		public void onClick(View v) {
			switch(v.getId())
			{
			case R.id.settings_ok_button:
				// Save the changed setting values
				saveSettingValues();
				startActivity(new Intent(SettingsActivity.this, Home.class));
				finish();
				break;
			case R.id.settings_cancel_button:
				startActivity(new Intent(SettingsActivity.this, Home.class));
				finish();
				break;
			}
			
		}
		
	};
	
	public void loadSettingValues()
	{
		SharedPreferences pref = getSharedPreferences(PREFERENCE_SETTING, MODE_PRIVATE);
		
		mCollectionTime = pref.getInt(PREF_COLLECTION_TIME, 3);
		mMusicalNotesChecked = pref.getBoolean(PREF_BEEP_METHOD, false);
		mMaxThreshold = pref.getInt(PREF_MAX_THRESHOLD, DEFAULT_MAX_THRESHOLD);
		mMinThreshold = pref.getInt(PREF_MIN_THRESHOLD, DEFAULT_MIN_THRESHOLD);
		
		mMaxThresholdEditText.setText(String.valueOf(mMaxThreshold));
		mMinThresholdEditText.setText(String.valueOf(mMinThreshold));
		
		if(mMusicalNotesChecked == true)
			mRadioBeepMethod.check(R.id.radio_musical_notes);
		else
			mRadioBeepMethod.check(R.id.radio_beep);
		
	}
	
	public void saveSettingValues()
	{
		SharedPreferences pref = getSharedPreferences(PREFERENCE_SETTING, MODE_PRIVATE);
		SharedPreferences.Editor editor = pref.edit();
		
		editor.putInt(PREF_COLLECTION_TIME, mCollectionTime);
		editor.putBoolean(PREF_BEEP_METHOD, mMusicalNotesChecked);
		
		String max = mMaxThresholdEditText.getText().toString();
		if(!max.isEmpty())
		{
			mMaxThreshold = Integer.parseInt(max);
			Log.i("settings", "Max Threshold: " + mMaxThreshold);
		}
		else
		{
			mMaxThreshold = DEFAULT_MAX_THRESHOLD;
			Log.i("settings", "Max Threshold: " + mMaxThreshold);
		}
		
		editor.putInt(PREF_MAX_THRESHOLD, mMaxThreshold);
		
		String min = mMinThresholdEditText.getText().toString();
		
		if(!min.isEmpty())
		{
			mMinThreshold = Integer.parseInt(min);
			Log.i("settings", "Min Threshold: " + mMinThreshold);
		}
		else
		{
			mMinThreshold = DEFAULT_MAX_THRESHOLD;
			Log.i("settings", "Min Threshold: " + mMinThreshold);
		}
		
		editor.putInt(PREF_MIN_THRESHOLD, mMinThreshold);
		
		editor.commit();
	}
}
