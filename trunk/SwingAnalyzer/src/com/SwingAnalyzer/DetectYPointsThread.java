package com.SwingAnalyzer;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class DetectYPointsThread extends Thread
{
	final static int MSG_DETECT 	= 1;
	final static int MSG_PEAK		= 2;
	final static int MSG_IMPACT 	= 3;
	final static int MSG_DETECT_DONE = 4;
	
	final static int Y_THRESHOLD_HI = 5;
	final static int Y_THRESHOLD_LOW = -10;
	
	String mFileName;
	String mOutputFile;
	boolean mFinished;
	Handler mHandler;

	ArrayList<AccelerationData> AccelDataList = null;
	ArrayList<AccelerationData> mYCriticalPointsArray = null;

	DetectYPointsThread(String filename, String outfile, Handler handler)
	{
		
	}

}
