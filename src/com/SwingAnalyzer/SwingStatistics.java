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

	
	public void setID(int id)
	{
		this.id = id;
	}
	
	public int getID()
	{
		return this.id;
	}
	
	public void setDateTime(String date, String time)
	{
		this.date = date;
		this.time = time;
	}
	
	public String getDate()
	{
		return this.date;
	}
	
	public String getTime()
	{
		return this.time;
	}
	
	public void setXPeakPoint(String max, String max_time, String min, String min_time)
	{
		
		this.x_max = max;
		this.x_max_timestamp = max_time;
		this.x_min = min;
		this.x_min_timestamp = min_time;
	}
	
	public void setYPeakPoint(String max, String max_time, String min, String min_time)
	{
		this.y_max = max;
		this.y_max_timestamp = max_time;
		this.y_min = min;
		this.y_min_timestamp = min_time;		
	}
	
	public float getXPositivePeak()
	{
		return Float.parseFloat(this.x_max);
	}
	
	public float getXNegativePeak()
	{
		return Float.parseFloat(this.x_min);
	}

	public float getYPositivePeak()
	{
		return Float.parseFloat(this.y_max);
	}
	
	public float getYNegativePeak()
	{
		return Float.parseFloat(this.y_min);
	}
	
	// Timestamps of Positive and negative peak points
	public int getXPositivePeakTime()
	{
		return Integer.parseInt(this.x_max_timestamp);
	}
	
	public int getXNegativePeakTime()
	{
		return Integer.parseInt(this.x_min_timestamp);
	}
	
	public int getYPositivePeakTime()
	{
		return Integer.parseInt(this.y_max_timestamp);
	}
	
	public int getYNegativePeakTime()
	{
		return Integer.parseInt(this.y_min_timestamp);
	}

}
