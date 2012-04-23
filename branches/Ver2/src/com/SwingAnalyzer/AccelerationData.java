package com.SwingAnalyzer;

import java.io.Serializable;
import java.util.*;

public class AccelerationData implements Serializable{
	int mIndex;
	//long mTimestamp;
	int mTimestamp;
	float mXvalue;
	float mYvalue;
	float mZvalue;	
	
	//public ArrayList<AccelerationData> mImpactArrayList;
	//public ArrayList<AccelerationData> mPeakArrayList;
	//public ArrayList<AccelerationData> mYCriticalArrayList;
	
	AccelerationData()
	{
		mIndex = 0;
		mTimestamp = 0;
		
		mXvalue = mYvalue = mZvalue = 0;
		
		initAllArrayList();
	}
	
	public void initAllArrayList()
	{
		
		//mImpactArrayList = new ArrayList<AccelerationData>();
		//mPeakArrayList = new ArrayList<AccelerationData>();
		//mYCriticalArrayList = new ArrayList<AccelerationData>();
	}
	
	public void clearAllArrayList()
	{
		/*
		if(mAccelerationArray.size() > 0)
			mAccelerationArray.clear();
		
		if(mImpactArrayList.size() > 0)
			mImpactArrayList.clear();
		
		if(mPeakArrayList.size() > 0)
			mPeakArrayList.clear();
		
		if(mYCriticalArrayList.size()>0)
			mYCriticalArrayList.clear();
		*/
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
	
	public void addData(AccelerationData data)
	{
		
	}
}
