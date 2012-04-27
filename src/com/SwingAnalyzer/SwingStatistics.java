/*-----------------------------------------------------------------------------------------
  File:   SwingStatistics.java

  Author: Jung Chang Su
  -----------------------------------------------------------------------------------------
  Copyright (C) 2012 SICS.
  
    
  
  *----------------------------------------------------------------------------------------*/
package com.SwingAnalyzer;

/* Table: table_acceleration
 *         1      2      3       4       5       6       7      8        9      10      11
 * 		+-----+------+------+-------+-------+-------+-------+-------+-------+-------+-------+
 * 		| id  | Date | Time | X-Max | X-Max | X-Min | X-Min | Y-Max | Y-Max | Y-Min | Y-Min |
 * 		|     |      |      |       | time  |       | time  |       | time  |       | time  |
 * 		+-----+------+------+-------+-------+-------+-------+-------+-------+-------+-------+
 * 		| INT | TEXT | TEXT | TEXT  | TEXT  | TEXT  | TEXT  | TEXT  | TEXT  | TEXT  | TEXT  |
 *      +-----+------+------+-------+-------+-------+-------+-------+-------+-------+-------+
 * 
 */
public class SwingStatistics
{
	int id;
	String date;
	String time;
	
	// X-axis Maximum value and timestamp
	String x_max;
	String x_max_timestamp;
	
	// X-axis Minimum value and timestamp
	String x_min;
	String x_min_timestamp;
	
	// Y-axis Maximum value and timestamp
	String y_max;
	String y_max_timestamp;
	
	// Y-axis Minimum value and timestamp
	String y_min;
	String y_min_timestamp;
	
	SwingStatistics()
	{
		this.id = 0;
		this.date = ""; this.time = "";
		
		this.x_max = this.x_max_timestamp = "";
		this.x_min = this.x_min_timestamp = "";
		
		this.y_max = this.y_max_timestamp = "";
		this.y_min = this.y_min_timestamp = "";
	}
	
	SwingStatistics(int id, String date, String time, String xmax, String xmax_time,
					String xmin, String xmin_time, 
					String ymax, String ymax_time, String ymin, String ymin_time)
	{
	
		this.id = id;
		this.date = date;
		this.time = time;
		
		this.x_max = xmax;
		this.x_max_timestamp = xmax_time;
		this.x_min = xmin;
		this.x_min_timestamp = xmin_time;
		
		this.y_max = ymax;
		this.y_max_timestamp = ymax_time;
		this.y_min = ymin;
		this.y_min_timestamp = ymin_time;
	}

	SwingStatistics(String date, String time, 
					String xmax, String xmax_time, String xmin, String xmin_time, 
					String ymax, String ymax_time, String ymin, String ymin_time)
	{


		this.date = date;
		this.time = time;

		this.x_max = xmax;
		this.x_max_timestamp = xmax_time;
		this.x_min = xmin;
		this.x_min_timestamp = xmin_time;

		this.y_max = ymax;
		this.y_max_timestamp = ymax_time;
		this.y_min = ymin;
		this.y_min_timestamp = ymin_time;
	}

	/*=============================================================================
	 * Name: setID
	 * 
	 * Description:
	 * 		Set the id with the argument
	 * 
	 * Return:
	 * 		None
	 *=============================================================================*/
	public void setID(int id)
	{
		this.id = id;
	}
	
	/*=============================================================================
	 * Name: getID
	 * 
	 * Description:
	 * 		Return the id value
	 * 
	 * Return:
	 * 		int
	 *=============================================================================*/	
	public int getID()
	{
		return this.id;
	}

	/*=============================================================================
	 * Name: setDateTime
	 * 
	 * Description:
	 * 		Set the date and time strings with the arguments
	 * 
	 * Return:
	 * 		None
	 *=============================================================================*/	
	public void setDateTime(String date, String time)
	{
		this.date = date;
		this.time = time;
	}

	/*=============================================================================
	 * Name: getDate
	 * 
	 * Description:
	 * 		Return the date string
	 * 
	 * Return:
	 * 		String
	 *=============================================================================*/	
	public String getDate()
	{
		return this.date;
	}

	/*=============================================================================
	 * Name: getTime
	 * 
	 * Description:
	 * 		Return the time string
	 * 
	 * Return:
	 * 		String
	 *=============================================================================*/	
	public String getTime()
	{
		return this.time;
	}
	
	/*=============================================================================
	 * Name: setXPeakPoint
	 * 
	 * Description:
	 * 		Set the peak points and timestamps of X-axis
	 * 
	 * Return:
	 * 		None
	 *=============================================================================*/		
	public void setXPeakPoint(String max, String max_time, String min, String min_time)
	{
		
		this.x_max = max;
		this.x_max_timestamp = max_time;
		this.x_min = min;
		this.x_min_timestamp = min_time;
	}

	/*=============================================================================
	 * Name: setYPeakPoint
	 * 
	 * Description:
	 * 		Set the peak points and timestamps of Y-axis
	 * 
	 * Return:
	 * 		None
	 *=============================================================================*/		
	public void setYPeakPoint(String max, String max_time, String min, String min_time)
	{
		this.y_max = max;
		this.y_max_timestamp = max_time;
		this.y_min = min;
		this.y_min_timestamp = min_time;		
	}
	
	/*=============================================================================
	 * Name: getXPositivePeak
	 * 
	 * Description:
	 * 		Return the positive peak point of X-axis
	 * 
	 * Return:
	 * 		float
	 *=============================================================================*/		
	public float getXPositivePeak()
	{
		return Float.parseFloat(this.x_max);
	}
	
	/*=============================================================================
	 * Name: getXNegativePeak
	 * 
	 * Description:
	 * 		Return the negative peak point of X-axis
	 * 
	 * Return:
	 * 		float
	 *=============================================================================*/			
	public float getXNegativePeak()
	{
		return Float.parseFloat(this.x_min);
	}
	
	/*=============================================================================
	 * Name: getYPositivePeak
	 * 
	 * Description:
	 * 		Return the positive peak point of Y-axis
	 * 
	 * Return:
	 * 		float
	 *=============================================================================*/		
	public float getYPositivePeak()
	{
		return Float.parseFloat(this.y_max);
	}
	
	/*=============================================================================
	 * Name: getYNegativePeak
	 * 
	 * Description:
	 * 		Return the negative peak point of Y-axis
	 * 
	 * Return:
	 * 		float
	 *=============================================================================*/			
	public float getYNegativePeak()
	{
		return Float.parseFloat(this.y_min);
	}
	
	/*=============================================================================
	 * Name: getXPositivePeakTime
	 * 
	 * Description:
	 * 		Return the timestamp of the positive peak point of X-axis
	 * 
	 * Return:
	 * 		int
	 *=============================================================================*/		
	public int getXPositivePeakTime()
	{
		return Integer.parseInt(this.x_max_timestamp);
	}

	/*=============================================================================
	 * Name: getXNegativePeakTime
	 * 
	 * Description:
	 * 		Return the timestamp of the negative peak point of X-axis
	 * 
	 * Return:
	 * 		int
	 *=============================================================================*/		
	public int getXNegativePeakTime()
	{
		return Integer.parseInt(this.x_min_timestamp);
	}
	
	/*=============================================================================
	 * Name: getYPositivePeakTime
	 * 
	 * Description:
	 * 		Return the timestamp of the positive peak point of Y-axis
	 * 
	 * Return:
	 * 		int
	 *=============================================================================*/		
	public int getYPositivePeakTime()
	{
		return Integer.parseInt(this.y_max_timestamp);
	}
	
	/*=============================================================================
	 * Name: getYNegativePeakTime
	 * 
	 * Description:
	 * 		Return the timestamp of the negative peak point of Y-axis
	 * 
	 * Return:
	 * 		int
	 *=============================================================================*/			
	public int getYNegativePeakTime()
	{
		return Integer.parseInt(this.y_min_timestamp);
	}

}
