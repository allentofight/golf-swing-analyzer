package com.SwingAnalyzer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Date;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.DefaultRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.app.Activity;
import android.content.*;
import android.graphics.Color;
import android.os.*;
import android.util.*;
import android.view.*;
import android.widget.*;

public class SwingGraphActivity extends Activity
{
	final static String X_AXIS_COLOR = "#3C78C0";
	final static String Y_AXIS_COLOR = "#B93936";

	final static String X_TITLE = "Timestamp";
	final static String Y_TITLE = "Acceleration";
	
	final static double Y_MIN = -25;
	final static double Y_MAX = 25;

	final static String GOLFSWING_DATA_DIR = "/data/GolfSwingAnalyzer";
	final static String EXTERNAL_SWING_DIR = "/externalswing/";
	final static String COLLECTED_SWING_DIR = "/collectedswing/";
	
	final static String COLLECTED_SWING_PATH = GOLFSWING_DATA_DIR + COLLECTED_SWING_DIR;

	protected GraphicalView mChartView = null;
	private Context mContext;
	
	String mSwingDataFileName = "";
	String mResultFileName = "";
	
	ArrayList<AccelerationData> mSwingGraphArrayList = null;
	
	/*==============================================================
	 * Widgets
	 *==============================================================*/
	LinearLayout mChartLayout;

	Button mGraphBackButton;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.swing_graph);
		
		mSwingGraphArrayList = new ArrayList<AccelerationData>();
		
		mContext = getApplicationContext();
		
		mGraphBackButton = (Button)findViewById(R.id.graph_back_button);
		mGraphBackButton.setOnClickListener(mClickListener);
		
		
		mChartLayout = (LinearLayout)findViewById(R.id.graph);

		Bundle extras = getIntent().getExtras();
		mSwingDataFileName = extras.getString("file");		
		Log.i("graph", "mSwingDataFileName: " + mSwingDataFileName);		
		
		drawSwingGraph(mSwingDataFileName);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		
		if(mSwingGraphArrayList.size() > 0)
			mSwingGraphArrayList.clear();
		
		mSwingDataFileName = "";
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
			case R.id.graph_back_button:
				Intent intent = new Intent(SwingGraphActivity.this, SwingPastDataFeedback.class);
				startActivity(intent);
				finish();				
				break;
			}
		}
	};

	
	private void drawSwingGraph(String fileName)
	{
		// Read file
		if(readSwingFile(fileName))
		{
			createLineChart();
			//createTimeChart();
		}
		else
		{
			Toast.makeText(this, "Failed to read the swing file.", Toast.LENGTH_LONG).show();
		}
	}
	
	private boolean readSwingFile(String fileName)
	{
        String sdPath = "";
        boolean bFound = false;
        String fullPathName = "";
    	
        String ext = Environment.getExternalStorageState();
        
        if(ext.equals(Environment.MEDIA_MOUNTED))
        {
        	sdPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        	bFound = searchSwingFileinSdPath(sdPath + COLLECTED_SWING_PATH + fileName);
        	
        	if(bFound)
        	{
        		fullPathName = sdPath + COLLECTED_SWING_PATH + fileName;
        		readArrayListFromFile(fullPathName);
        	}
        }
        else
        {
        	sdPath = Environment.MEDIA_UNMOUNTED;
        	Toast.makeText(this, "SD card is not mounted", Toast.LENGTH_LONG).show();
        }
    	
        return bFound;
	}
	

    public boolean searchSwingFileinSdPath(String swingDataFile)
    {
    	File swingFile = new File(swingDataFile);    	
    	
    	Log.i("graph", "Collected Swing Dir Path: " + swingDataFile);
    	
    	if(swingFile.exists())    	
    		return true;
    	else
    		return false;
    	
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
	public void readArrayListFromFile(String fullPathName) 
	{
    	FileInputStream inputStream = null;
    	ObjectInputStream objInputStream = null;

		try
		{
			Log.i("graph", "fullPathName: " + fullPathName);
			
			inputStream = new FileInputStream(fullPathName);
			objInputStream = new ObjectInputStream(inputStream);
						
			mSwingGraphArrayList = (ArrayList<AccelerationData>)objInputStream.readObject();
			
			Log.i("graph", "mSwingGraphArrayList.size: " + mSwingGraphArrayList.size());
			
			objInputStream.close();
		} 
		catch(Exception e)
		{
			Log.e("graph", e.getMessage() + " :" + fullPathName);
		}

	}
	
	/*=============================================================================
	 * 
	 *	AChartEngine Functions 
	 * 
	 *============================================================================*/

	/*=============================================================================
	 * Name: setChartRenderer
	 * 
	 * Description:
	 * 		Set up X, Y axis' style (text size, color, grid line etc)
	 * 		This function can be called regardless of the types of chart.
	 * 
	 * Return:
	 * 		XYMultipleSeriesRenderer
	 *=============================================================================*/
	private XYMultipleSeriesRenderer setChartRenderer()
	{
		//XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
		
		XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer(3);
	    renderer.setAxisTitleTextSize(16);
	    renderer.setChartTitleTextSize(18);
	    renderer.setLabelsTextSize(15);
	    renderer.setLegendTextSize(15);
	    renderer.setPointSize(5f);
	    //renderer.setMargins(new int[] { 20, 30, 15, 20 });
	    renderer.setMargins(new int[] {20, 30, 15, 0});
	    
	    /*===========================================
	     * X-axis Renderer
	     *===========================================*/
	    XYSeriesRenderer r = new XYSeriesRenderer();
		r.setColor(Color.parseColor(X_AXIS_COLOR));
		r.setPointStyle(PointStyle.POINT);
		r.setFillPoints(false);
		renderer.addSeriesRenderer(r);				// Add X axis renderer
		renderer.setShowGrid(true);					// Show grid line
		renderer.initAxesRange(3);
		//renderer.initAxesRangeForScale(1000);

	    /*===========================================
	     * Y-axis Renderer
	     *===========================================*/
		r = new XYSeriesRenderer();
		r.setColor(Color.parseColor(Y_AXIS_COLOR));
		r.setPointStyle(PointStyle.POINT);		
		r.setFillPoints(false);		
		renderer.addSeriesRenderer(r);				// Add Y axis renderer
		
		renderer.setAxesColor(Color.DKGRAY);
		renderer.setLabelsColor(Color.LTGRAY);
	    
	    return renderer;
	}

	/*=============================================================================
	 * Name: setChartSettings
	 * 
	 * Description:
	 * 		Define a chart settings (title, x-axis title, y-axis title, start value, end value)
	 * 		This function can be called regardless of the types of chart.
	 * 
	 * 		- title: Chart Title
	 * 		- xtitle: X-axis title, ytitle: Y-axis title
	 * 		- xmin: the start value of x-axis, xmax: the end value of x-axis
	 * 		- ymin: the lower limit of y-axis, ymax: the upper limit of y-axis
	 * 
	 * Return:
	 * 		None
	 *=============================================================================*/	
	private void setChartSettings(XYMultipleSeriesRenderer renderer, String title, 
									String xtitle, String ytitle, 
									double xmin, double xmax, 
									double ymin, double ymax)
	{
		
		renderer.setChartTitle(title);
		
		renderer.setXTitle(xtitle);
		renderer.setYTitle(ytitle);
		renderer.setXAxisMin(xmin);		// Start value of X-axis 
		renderer.setXAxisMax(xmax);		// End value of X-axis
		
		renderer.setYAxisMin(-30);		// The lower limit value of Y-axis
		renderer.setYAxisMax(30);		// The upper limit value of Y-axis
		
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
		int arrSize = mSwingGraphArrayList.size();
		XYMultipleSeriesRenderer renderer = setLineChartRenderer();
		
		Log.i("stats", "mSwingGraphArrayList size = " + arrSize);
		
		if(arrSize > 0)
		{
			String title = "";
			title = "Swing Data (" + mSwingDataFileName + ")";
			
			double lastTimestamp = mSwingGraphArrayList.get(arrSize-1).mTimestamp;
			
			setLineChartSettings(renderer, title, "Timestamp", Y_TITLE, 
								(double)0, (double)lastTimestamp+10, Y_MIN, Y_MAX);
			
			XYMultipleSeriesDataset dataset = getLineChartDataset();
			
			mChartView = ChartFactory.getLineChartView(mContext, dataset, renderer);
			
			mChartLayout.addView(mChartView);
		}
		else
		{
			Toast.makeText(this, "Swing data is empty.", Toast.LENGTH_LONG).show();
		}

	}
	
	private XYMultipleSeriesRenderer setLineChartRenderer()
	{
		XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer(3);
	    renderer.setAxisTitleTextSize(16);
	    renderer.setChartTitleTextSize(18);
	    renderer.setLabelsTextSize(15);
	    renderer.setLegendTextSize(15);
	    renderer.setPointSize(5f);	   
	    renderer.setMargins(new int[] {20, 30, 15, 10});
	    
	    /*===========================================
	     * X-axis Renderer
	     *===========================================*/
	    XYSeriesRenderer r = new XYSeriesRenderer();
		r.setColor(Color.parseColor(X_AXIS_COLOR));
		r.setPointStyle(PointStyle.POINT);
		r.setFillPoints(false);
		renderer.addSeriesRenderer(r);				// Add X axis renderer
		renderer.setShowGrid(true);					// Show grid line

	    /*===========================================
	     * Y-axis Renderer
	     *===========================================*/
		r = new XYSeriesRenderer();
		r.setColor(Color.parseColor(Y_AXIS_COLOR));
		r.setPointStyle(PointStyle.POINT);		
		r.setFillPoints(false);		
		renderer.addSeriesRenderer(r);				// Add Y axis renderer
		
		renderer.setAxesColor(Color.DKGRAY);
		renderer.setLabelsColor(Color.LTGRAY);
	    
	    return renderer;

	}
	
	private void setLineChartSettings(XYMultipleSeriesRenderer renderer, String title, 
										String xtitle, String ytitle, 
										double xmin, double xmax, 
										double ymin, double ymax)
	{
	
		renderer.setChartTitle(title);
		
		renderer.setXTitle(xtitle);
		renderer.setYTitle(ytitle);
		renderer.setXAxisMin(xmin);		// Start value of X-axis 
		renderer.setXAxisMax(xmax);		// End value of X-axis
		
		renderer.setYAxisMin(ymin);		// The lower limit value of Y-axis
		renderer.setYAxisMax(ymax);		// The upper limit value of Y-axis
		
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
		double timestamp = 0;
		
		for(int i=0; i < mSwingGraphArrayList.size(); i++)
		{
			timestamp = (double)(mSwingGraphArrayList.get(i).mTimestamp);
			doubleXValue = (double)(mSwingGraphArrayList.get(i).mXvalue);
			doubleYValue = (double)(mSwingGraphArrayList.get(i).mYvalue);

			series_x.add(timestamp, doubleXValue);
			series_y.add(timestamp, doubleYValue);

			/*
			series_x.add(i, doubleXValue);
			series_y.add(i, doubleYValue);
			*/
		}
		
		dataset.addSeries(series_x);
		dataset.addSeries(series_y);

		return dataset;
	}

	
	/*================================================================================
	 * 
	 *  Time Chart
	 *  
	 *================================================================================*/
	private void createTimeChart()
	{
		
		String stringFirstDate = "";
		String stringLastDate = "";
		int arrSize = 0;
		int firstTimestamp = 0;
		int lastTimestamp = 0;
		
		//XYMultipleSeriesRenderer renderer = getTimeChartRenderer();
		XYMultipleSeriesRenderer renderer = setChartRenderer();
		
		arrSize = mSwingGraphArrayList.size();
		
		Log.i("graph", "mSwingGraphArrayList size = " + arrSize);
		
		if(arrSize > 0)
		{
			// Date string: ssss (1298 msec)
			String title = "";
			title = "Swing Acceleration Data (" + mSwingDataFileName + ")";

			firstTimestamp = mSwingGraphArrayList.get(0).mTimestamp;
			lastTimestamp = mSwingGraphArrayList.get(arrSize-1).mTimestamp;
			Log.i("graph", "firstTimestamp:" + firstTimestamp 
							+ ", lastTimestamp:" + lastTimestamp);
			
			Date firstDate = new Date((long)firstTimestamp);
			Date lastDate = new Date((long)lastTimestamp);
			
			Log.i("graph", "fistDate:" + firstDate.getTime() 
						+ ", lastDate:" + lastDate.getTime());
			
			//setTimeChartSettings(renderer, firstDate.getTime(), lastDate.getTime());
			setChartSettings(renderer, title, X_TITLE, Y_TITLE, 
							0, 3000, Y_MIN, Y_MAX);

			
			mChartView = ChartFactory.getTimeChartView(mContext, 
														getTimeChartDataset(), 
														renderer, 
														"s");
				
			
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
		renderer.setXTitle("milliseconds");
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
		
	    renderer.setAxisTitleTextSize(16);
	    renderer.setChartTitleTextSize(18);
	    renderer.setLabelsTextSize(15);
	    renderer.setLegendTextSize(15);
	    renderer.setPointSize(5f);
	    renderer.setMargins(new int[] { 20, 30, 15, 20 });
	    
	    // X-axis Renderer
	    XYSeriesRenderer r = new XYSeriesRenderer();
		r.setColor(Color.parseColor(X_AXIS_COLOR));
		r.setPointStyle(PointStyle.POINT);
		
		renderer.addSeriesRenderer(r);
		renderer.setShowGrid(true);
		
		// Y-axis Renderer
		r = new XYSeriesRenderer();
		r.setColor(Color.parseColor(Y_AXIS_COLOR));
		r.setPointStyle(PointStyle.POINT);		
		r.setFillPoints(true);
		
		renderer.addSeriesRenderer(r);
		renderer.setAxesColor(Color.DKGRAY);
		renderer.setLabelsColor(Color.LTGRAY);
	    
	    return renderer;
	}

	private XYMultipleSeriesDataset getTimeChartDataset()
	{
		XYMultipleSeriesDataset dataSet = new XYMultipleSeriesDataset();

		TimeSeries series_x = new TimeSeries("X-axis");
        TimeSeries series_y = new TimeSeries("Y-axis");
		
		double doubleXValue = 0;
		double doubleYValue = 0;
		
		for(int i=0; i < mSwingGraphArrayList.size(); i++)
		{
		
			Date chartDate = new Date((long)mSwingGraphArrayList.get(i).mTimestamp);
			
			doubleXValue = (double)(mSwingGraphArrayList.get(i).mXvalue);
			doubleYValue = (double)(mSwingGraphArrayList.get(i).mYvalue);
			
			series_x.add(chartDate, doubleXValue);
			series_y.add(chartDate, doubleYValue);
		}
		
		dataSet.addSeries(series_x);
		dataSet.addSeries(series_y);
		
		return dataSet;
	}
	

}
