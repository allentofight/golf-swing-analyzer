/*-----------------------------------------------------------------------------------------
  File:   AccelerationData.java

  Author: Jung Chang Su
  -----------------------------------------------------------------------------------------
  Copyright (C) 2012 SICS.
  
    
  
  *----------------------------------------------------------------------------------------*/

package com.SwingAnalyzer;

import java.io.Serializable;
import java.util.*;

public class AccelerationData implements Serializable{
	int mIndex;
	int mTimestamp;
	float mXvalue;
	float mYvalue;
	float mZvalue;	
	
	static String mSwingStartDate = "";
	static String mSwingStartTime = "";
	static boolean mEnabledCollection = false;
	
	AccelerationData()
	{
		mIndex = 0;
		mTimestamp = 0;
		
		mXvalue = mYvalue = mZvalue = 0;
		mSwingStartDate = "";
		mSwingStartTime = "";
	}
	
	
	/*=================================================================
	 * Return the maximum value from an array
	 *=================================================================*/
	public float getMaxValue()
	{
		float maxValue = 0;
		
		
		return maxValue;
	}
	/*=================================================================
	 * Return the minimum value from an array
	 *=================================================================*/	
	public float getMinValue()
	{
		float minValue = 0;		
		
		return minValue;
	}
	
	public static void setSwingStartDate(String date)
	{
		mSwingStartDate = date;		
	}
	
	public static void setSwingStartTime(String time)
	{
		mSwingStartTime = time;
	}
	
	public static String getSwingStartDate()
	{
		return mSwingStartDate;
	}
	
	public static String getSwingStartTime()
	{
		return mSwingStartTime;
	}
	
	public static void setEnabledSwing(boolean enabled)
	{
		mEnabledCollection = enabled;
	}
	
	public static boolean getEnabledSwing()
	{
		return mEnabledCollection;
	}
	
	public static void setSwingStaticVariables(String date, String time, boolean enabled)
	{
		mSwingStartDate = date;
		mSwingStartTime = time;
		mEnabledCollection = enabled;
	}
}
