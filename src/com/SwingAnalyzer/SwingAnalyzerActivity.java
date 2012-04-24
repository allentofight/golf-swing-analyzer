package com.SwingAnalyzer;


import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.os.*;

import android.util.Log;
import android.view.View;
import android.widget.*;

public class SwingAnalyzerActivity extends TabActivity {
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
        TabHost tabHost = getTabHost();
        TabHost.TabSpec spec;
		
		
		/*
		Intent intent;
		intent = new Intent().setClass(this, ConvertDataActivity.class);
		spec = tabHost.newTabSpec("convert").setIndicator("Converting").setContent(intent);
		tabHost.addTab(spec);
		
		intent = new Intent().setClass(this, DetectingPointActivity.class);
		spec = tabHost.newTabSpec("detect").setIndicator("Detection").setContent(intent);
		tabHost.addTab(spec);
		*/
        
        Intent intent;
		intent = new Intent().setClass(this, CollectingAccelerationData.class);
		spec = tabHost.newTabSpec("collection").setIndicator("Collection").setContent(intent);
		tabHost.addTab(spec);
		
		intent = new Intent().setClass(this, SwingFeedback.class);
		spec = tabHost.newTabSpec("feedback").setIndicator("Feedback").setContent(intent);
		tabHost.addTab(spec);
		
		intent = new Intent().setClass(this, StatisticsActivity.class);
		spec = tabHost.newTabSpec("statistics").setIndicator("Statistics").setContent(intent);
		tabHost.addTab(spec);
		
		tabHost.setCurrentTab(0);
		
		for ( int tab = 0; tab < tabHost.getTabWidget().getChildCount(); ++tab )
		{
			tabHost.getTabWidget().getChildAt(tab).getLayoutParams().height = 46;
		}	

	}
}

