/*-----------------------------------------------------------------------------------------
  File:   StatisticsActivity.java

  Author: Jung Chang Su
  -----------------------------------------------------------------------------------------
  Copyright (C) 2012 SICS.
  
  Draw a bar chart graph with the database which contains maximum, minimum values 
  of two axis.
  
  This source uses the "achartengine-1.0.0" library which is under 
  the Apache License, Version 2.0.
  *----------------------------------------------------------------------------------------*/

package com.SwingAnalyzer;

import java.util.*;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.BarChart;
import org.achartengine.model.CategorySeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.SimpleSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.*;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;

public class StatisticsActivity extends Activity{
	/* 
	 * Database Handler
	 */
	DatabaseHandler mDatabaseHandler;
	
	protected GraphicalView mChartView = null;
	private Context mContext;
	
	LinearLayout mChartLayout;
	
	Button mStatsDeleteAllButton;
	Button mStatsBackButton;
	Button mStatsHomeButton;
	
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.statistics);
		
		mContext = getApplicationContext();
		
		mStatsDeleteAllButton = (Button)findViewById(R.id.stats_delete_all_button);
		mStatsDeleteAllButton.setOnClickListener(mClickListener);
		
		
		mStatsHomeButton = (Button)findViewById(R.id.stats_home_button);
		mStatsHomeButton.setOnClickListener(mClickListener);
		
		/*
		 * Ready to use the database
		 */
		mDatabaseHandler = new DatabaseHandler(this);
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
		
		/*
		 * AChartEngine for Bar Graph
		 */
		if(mChartView == null)
		{
			createBarChartWithData();
		}
		else
		{
			mChartView.repaint();
		}
		
	}

	Button.OnClickListener mClickListener = new View.OnClickListener() {
		public void onClick(View v) {
			switch(v.getId())
			{
			case R.id.stats_delete_all_button:
				deleteAllDataFromDB();
				mChartView.repaint();
				break;
			case R.id.stats_home_button:
				startActivity(new Intent(StatisticsActivity.this, Home.class));
				finish();				
				break;
			}
		}
	};
	
	public void createBarChartWithData()
	{
		mChartLayout = (LinearLayout)findViewById(R.id.chart);
		
		XYMultipleSeriesRenderer renderer = getBarChartRenderer();
		setChartSettings(renderer);
		
		mChartView = ChartFactory.getBarChartView(mContext, addBarChartDataset(), 
							renderer, BarChart.Type.DEFAULT);

		mChartLayout.addView(mChartView);

	}
	/*=============================================================================
	 * Name: readSwingStatisticsData
	 * 
	 * Description:
	 * 		Read all data from the database table ("SWING_TABLE_NAME")
	 * 		- For debugging
	 * Return:
	 * 		None 
	 *=============================================================================*/	
	public void readSwingStatisticsData()
	{	
        List<SwingStatistics> swingArray = mDatabaseHandler.getAllSwingStats();
        
        Log.d("db", "Reading: count=" + swingArray.size());
        for(SwingStatistics stat : swingArray)
        {
        	String log = "ID:" + stat.getID() 
        				+ " Date:" + stat.getDate() 
        				+ " Time:" + stat.getTime()
        				+ " X_Max:" + stat.getXPositivePeak()
        				+ " X_Max_Time:" + stat.getXPositivePeakTime()
        				+ " Y_Min:" + stat.getYNegativePeak()
        				+ " Y_Min_Time:" + stat.getYNegativePeakTime();
        	
        	Log.d("db", log);
        }
	}
	
	public void deleteAllDataFromDB()
	{
		mDatabaseHandler.deleteSwingTable();
		startActivity(getIntent()); 
		finish();
		
	}
	/*=============================================================================
	 * Name: addBarChartDataset
	 * 
	 * Description:
	 * 		1. Read all data from the database table ("SWING_TABLE_NAME")
	 * 		2. Add data to the bar chart
	 * 		3. Return XYMutilpleSeriesDataset for drawing a bar chart
	 * 		4. Use the achartengine-1.0.0 library which is under Apache license 2.0
	 * Return:
	 * 		XYMultipleSeriesDataset
	 *=============================================================================*/	
	public XYMultipleSeriesDataset addBarChartDataset()
	{
		XYMultipleSeriesDataset dataSet = new XYMultipleSeriesDataset();
		CategorySeries series_x = new CategorySeries("X-Max");
		CategorySeries series_y = new CategorySeries("Y-Max");
		
		double doubleXValue = 0;
		double doubleYValue = 0;
		
		List<SwingStatistics> swingArray = mDatabaseHandler.getAllSwingStats();        
        Log.d("db", "Reading: count=" + swingArray.size());        
		
		for(int i=0; i<swingArray.size(); i++)
		{
			doubleXValue = Double.parseDouble(swingArray.get(i).x_max);
			doubleYValue = Double.parseDouble(swingArray.get(i).y_min);
			
			series_x.add(doubleXValue);
			series_y.add(doubleYValue);			
		}
		
		dataSet.addSeries(series_x.toXYSeries());
		dataSet.addSeries(series_y.toXYSeries());
		
		return dataSet;
	}
	/*=============================================================================
	 * Name: XYMultipleSeriesDataset
	 * 
	 * Description:
	 * 		Add data to X and Y category for the bar char dataset
	 * 
	 * Return:
	 * 		XYMultipleSeriesDataset (using "achartengine-1.0.0.jar")
	 *=============================================================================*/	
	public XYMultipleSeriesDataset getBarChartDataset()
	{
		XYMultipleSeriesDataset accelDataSet = new XYMultipleSeriesDataset();
		
		CategorySeries series_x = new CategorySeries("X-Max");
		CategorySeries series_y = new CategorySeries("Y-Max");
		
		series_x.add(19.001);
		series_x.add(18.100);
		series_x.add(17.100);
		series_x.add(16.123);
		series_x.add(15.5);
		series_x.add(14.001);
		series_x.add(13.100);
		series_x.add(12.100);
		series_x.add(11.123);
		series_x.add(10.5);
		series_x.add(19.001);
		series_x.add(18.100);
		series_x.add(17.100);
		series_x.add(16.123);
		series_x.add(15.5);
		series_x.add(14.001);
		series_x.add(13.100);
		series_x.add(12.100);
		series_x.add(11.123);
		series_x.add(10.5);

		
		series_y.add(-10.123);
		series_y.add(-8.123);
		series_y.add(-11.20);
		series_y.add(-12.50);
		series_y.add(-5.987);
		series_y.add(-10.123);
		series_y.add(-8.123);
		series_y.add(-11.20);
		series_y.add(-12.50);
		series_y.add(-5.987);
		series_y.add(-10.123);
		series_y.add(-8.123);
		series_y.add(-11.20);
		series_y.add(-12.50);
		series_y.add(-5.987);
		series_y.add(-10.123);
		series_y.add(-8.123);
		series_y.add(-11.20);
		series_y.add(-12.50);
		series_y.add(-5.987);
		
		accelDataSet.addSeries(series_x.toXYSeries());
		accelDataSet.addSeries(series_y.toXYSeries());
		
		return accelDataSet;
	}
	/*=============================================================================
	 * Name: getBarChartRenderer
	 * 
	 * Description:
	 * 		Configure settings for the bar char
	 * 		- X, Y data color, font sizes, margins of the graph
	 * Return:
	 * 		None
	 *=============================================================================*/		
	public XYMultipleSeriesRenderer getBarChartRenderer() 
  	{
		XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
		renderer.setAxisTitleTextSize(14);
		renderer.setChartTitleTextSize(20);		// The font size of Title
		renderer.setLabelsTextSize(15);
		renderer.setLegendTextSize(15);		
		renderer.setMargins(new int[] {20, 20, 15, 10});
		
		SimpleSeriesRenderer renderX = new SimpleSeriesRenderer();
		renderX.setColor(Color.parseColor("#3C78C0"));	// X-axis	
		renderX.setDisplayChartValues(true);		// Display the acceleration value
		renderX.setChartValuesSpacing(2);
		renderer.addSeriesRenderer(renderX);
		
		SimpleSeriesRenderer renderY = new SimpleSeriesRenderer();
		renderY = new SimpleSeriesRenderer();
		renderY.setColor(Color.parseColor("#B93936"));	// Y-axis
		renderY.setDisplayChartValues(true);			// Display the acceleration value
		renderY.setChartValuesSpacing(2);
		renderer.addSeriesRenderer(renderY);
		
		return renderer;
	}
	/*=============================================================================
	 * Name: setChartSettings
	 * 
	 * Description:
	 * 		Set maximum and minimum values of the bar char(X, Y) 
	 * 
	 * Return:
	 * 		None
	 *=============================================================================*/	
	private void setChartSettings(XYMultipleSeriesRenderer renderer) 
	{
		renderer.setChartTitle("Acceleration Max/Min");
		renderer.setXTitle("date");
		renderer.setYTitle("acceleration");
		renderer.setXAxisMin(0.5);
		renderer.setXAxisMax(10.5);
		renderer.setYAxisMin(-25);
		renderer.setYAxisMax(25);
		
		renderer.setZoomButtonsVisible(true);
		renderer.setZoomEnabled(true);
	}


}
