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
import org.achartengine.chart.PointStyle;
import org.achartengine.model.CategorySeries;
import org.achartengine.model.TimeSeries;
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
	final static String X_COLOR = "#3C78C0";
	final static String Y_COLOR = "#B93936";
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
	
	private XYMultipleSeriesDataset SwingDataset = new XYMultipleSeriesDataset();
	private XYMultipleSeriesRenderer SwingDataRenderer = new XYMultipleSeriesRenderer();
	private XYSeries SwingCurrentSeries;
	
	List<SwingStatistics> mSwingArrayList = null;
	String[] mTimeChartTitles = new String[] {"X-axis", "Y-axis"};
	
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
		
		mChartLayout = (LinearLayout)findViewById(R.id.chart);
		
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		
		if(mDatabaseHandler != null)
			mDatabaseHandler.close();
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
			//createBarChart();
			//createTimeChart();
			createLineChart();
			
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
	
	private Date getDateFromString(String stringDate)
	{
		Date date = null;
		int index1 = 0;
		int index2 = 0;
		int year = 0;
		int month = 0;
		int day = 0;
		
		
		Log.i("stats", "getDateFromString(): " + stringDate);
		
		index1 = stringDate.indexOf("/");
		index2 = stringDate.indexOf("/", index1+1);
		
		month = Integer.parseInt(stringDate.substring(0, index1));
		day = Integer.parseInt(stringDate.substring(index1+1, index2));
		year = Integer.parseInt(stringDate.substring(index2+1));
		Log.i("stats", "month:" + month + ", day:" + day + ", year:" + year);
		
		date = new Date((year-1900), month, day);
		
		
		return date;
	}
	
	private Date getDateFromInt(int i)
	{
		Date date = new Date(2012-1900, 5, i);
		
		return date;
	}
	/*================================================================================
	 * 
	 *  Bar Chart
	 * 
	 *================================================================================*/
	public void createBarChart()
	{
		
		XYMultipleSeriesRenderer renderer = getBarChartRenderer();
		setBarChartSettings(renderer);
		
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
        
        Log.d("stats", "Reading: count=" + swingArray.size());
        for(SwingStatistics stat : swingArray)
        {
        	String log = "ID:" + stat.getID() 
        				+ " Date:" + stat.getDate() 
        				+ " Time:" + stat.getTime()
        				+ " X_Max:" + stat.getXPositivePeak()
        				+ " X_Max_Time:" + stat.getXPositivePeakTime()
        				+ " Y_Min:" + stat.getYNegativePeak()
        				+ " Y_Min_Time:" + stat.getYNegativePeakTime();
        	
        	Log.d("stats", log);
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
        Log.d("stats", "Reading: count=" + swingArray.size());        
		
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
	private void setBarChartSettings(XYMultipleSeriesRenderer renderer) 
	{
		renderer.setChartTitle("Acceleration Max/Min");
		renderer.setXTitle("Date");
		renderer.setYTitle("Acceleration");
		renderer.setXAxisMin(0.5);
		renderer.setXAxisMax(10.5);
		renderer.setYAxisMin(-25);
		renderer.setYAxisMax(25);
		
		renderer.setZoomButtonsVisible(true);
		renderer.setZoomEnabled(true);
	}
	/*================================================================================
	 * 
	 *  Line Chart
	 *  
	 *================================================================================*/
	private void createLineChart()
	{
		int arrSize = 0;
		XYMultipleSeriesRenderer renderer = getLineChartRenderer();		
		
		mSwingArrayList = mDatabaseHandler.getAllSwingStats();		
		arrSize = mSwingArrayList.size();
		
		Log.i("stats", "Database size = " + arrSize);
		
		if(arrSize > 0)
		{
			setLineChartSettings(renderer);
			mChartView = ChartFactory.getLineChartView(mContext, 
														getLineChartDataset(), renderer);
			
			mChartLayout.addView(mChartView);
		}
		else
		{
			Toast.makeText(this, "Database is empty.", Toast.LENGTH_LONG).show();
		}

	}
	
	private XYMultipleSeriesRenderer getLineChartRenderer()
	{
		XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
		
		renderer.setAxisTitleTextSize(16);
		renderer.setChartTitleTextSize(20);
		renderer.setLabelsTextSize(15);
		renderer.setLegendTextSize(15);
		renderer.setPointSize(5f);
		renderer.setMargins(new int[] {20, 30, 15, 0});

		XYSeriesRenderer r = new XYSeriesRenderer();
		r.setColor(Color.parseColor(X_COLOR));
		r.setPointStyle(PointStyle.CIRCLE);
		r.setFillBelowLine(false);		
		r.setDisplayChartValues(true);		// Display the acceleration value
		r.setChartValuesSpacing(2);
		r.setFillPoints(true);
		renderer.addSeriesRenderer(r);

		r = new XYSeriesRenderer();
		r.setColor(Color.parseColor(Y_COLOR));
		r.setPointStyle(PointStyle.CIRCLE);		
		r.setFillPoints(true);
		r.setDisplayChartValues(true);		// Display the acceleration value
		r.setChartValuesSpacing(2);
		
		renderer.addSeriesRenderer(r);
		renderer.setAxesColor(Color.DKGRAY);
		renderer.setLabelsColor(Color.LTGRAY);

		return renderer;
	}
	
	private void setLineChartSettings(XYMultipleSeriesRenderer renderer)
	{
		renderer.setChartTitle("Acceleration Max/Min");
		renderer.setXTitle("Date");
		renderer.setYTitle("Acceleration");
		renderer.setXAxisMin(0.5);
		renderer.setXAxisMax(10.5);
		renderer.setYAxisMin(-30);
		renderer.setYAxisMax(30);
		
		renderer.setZoomButtonsVisible(true);
		renderer.setZoomEnabled(true);
	}
	
	private XYMultipleSeriesDataset getLineChartDataset() 
  	{
		XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
		XYSeries series_x = new XYSeries("X-axis");
		XYSeries series_y = new XYSeries("Y-axis");
		
		double doubleXValue = 0;
		double doubleYValue = 0;
		
		
		for(int i=0; i < mSwingArrayList.size(); i++)
		{
			
			doubleXValue = Double.parseDouble(mSwingArrayList.get(i).x_max);
			doubleYValue = Double.parseDouble(mSwingArrayList.get(i).y_min);
			
			series_x.add(i, doubleXValue);
			series_y.add(i, doubleYValue);
		}
		
		dataset.addSeries(series_x);
		dataset.addSeries(series_y);

		return dataset;
	}

	/*================================================================================
	 * 
	 *  Time Chart
	 *  (날짜가 동일한 경우 화면에 출력이 안됨)
	 *================================================================================*/
	private void createTimeChart()
	{
		Date firstDate, lastDate;
		String stringFirstDate = "";
		String stringLastDate = "";
		int arrSize = 0;
		
		XYMultipleSeriesRenderer renderer = getTimeChartRenderer();
		mSwingArrayList = mDatabaseHandler.getAllSwingStats();
		
		arrSize = mSwingArrayList.size();
		
		Log.i("stats", "Database size = " + arrSize);
		
		if(arrSize > 0)
		{
			// Date string: MM/DD/YYYY
			stringFirstDate = mSwingArrayList.get(0).date;
			stringLastDate = mSwingArrayList.get(arrSize-1).date;
	
			//firstDate = getDateFromString(stringFirstDate);
			//lastDate = getDateFromString(stringLastDate);
			firstDate = getDateFromInt(mSwingArrayList.get(0).id);
			lastDate = getDateFromInt(mSwingArrayList.get(arrSize-1).id);
			
			setTimeChartSettings(renderer, firstDate.getTime(), lastDate.getTime());
			
			
			mChartView = ChartFactory.getTimeChartView(mContext, 
														getTimeChartDataset(), 
														renderer, 
														"MM/dd/yyyy");
				
			
			mChartLayout.addView(mChartView);
		}
		else
		{
			Toast.makeText(this, "Database is empty.", Toast.LENGTH_LONG).show();
		}
	}
	
	protected void setTimeChartSettings(XYMultipleSeriesRenderer renderer, 
										double startDate, double endDate) 
	{
		renderer.setChartTitle("Swing Acceleration Data");
		renderer.setXTitle("Date");
		renderer.setYTitle("Acceleration");
		
		renderer.setXAxisMin(startDate);	 	// Sets the start value of the X axis range.
		renderer.setXAxisMax(endDate);		//  Sets the end value of the X axis range.
		
		renderer.setYAxisMin(-20);
		renderer.setYAxisMax(20);
		
		renderer.setShowGridX(true);
		renderer.setAxesColor(Color.GRAY);
		renderer.setLabelsColor(Color.LTGRAY);
		
		renderer.setZoomButtonsVisible(true);
		renderer.setZoomEnabled(true);
	}

	private XYMultipleSeriesRenderer getTimeChartRenderer()
	{
		XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
		
		int[] colors = new int[] {Color.parseColor("#3C78C0"), 
									Color.parseColor("#B93936")};
		
		PointStyle[] styles = new PointStyle[] {PointStyle.POINT, 
												PointStyle.POINT};
		
		int length = colors.length;
		
	    renderer.setAxisTitleTextSize(16);
	    renderer.setChartTitleTextSize(20);
	    renderer.setLabelsTextSize(15);
	    renderer.setLegendTextSize(15);
	    renderer.setPointSize(5f);
	    renderer.setMargins(new int[] { 20, 30, 15, 20 });
	    
	    for (int i = 0; i < length; i++) 
	    {
			XYSeriesRenderer r = new XYSeriesRenderer();
			r.setColor(colors[i]);
			r.setPointStyle(styles[i]);
			renderer.addSeriesRenderer(r);
	    }
	    
	    return renderer;
	}

	private XYMultipleSeriesDataset getTimeChartDataset()
	{
		XYMultipleSeriesDataset dataSet = new XYMultipleSeriesDataset();

		TimeSeries series_x = new TimeSeries("X-axis");
        TimeSeries series_y = new TimeSeries("Y-axis");
		
		double doubleXValue = 0;
		double doubleYValue = 0;
		String stringDate = "";
		
		for(int i=0; i < mSwingArrayList.size(); i++)
		{
			//stringDate = mSwingArrayList.get(i).date;			
			//Date chartDate = getDateFromString(Integer.toString(i+1));
			
			Date chartDate = getDateFromInt(mSwingArrayList.get(i).id);
			
			doubleXValue = Double.parseDouble(mSwingArrayList.get(i).x_max);
			doubleYValue = Double.parseDouble(mSwingArrayList.get(i).y_min);
			
			series_x.add(chartDate, doubleXValue);
			series_y.add(chartDate, doubleYValue);
		}
		
		dataSet.addSeries(series_x);
		dataSet.addSeries(series_y);
		
		return dataSet;
	}
	

}
