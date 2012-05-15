/*-----------------------------------------------------------------------------------------
  File:   DatabaseHandler.java

  Author: Jung Chang Su
  -----------------------------------------------------------------------------------------
  Copyright (C) 2012 SICS.
  
  This source provides some functions to manage a database table such as insertion, 
  deletion, update and reading all items from the database.  
  
  *----------------------------------------------------------------------------------------*/

package com.SwingAnalyzer;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHandler extends SQLiteOpenHelper
{
	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "swingdata";
	private static final String TABLE_SWINGSTATS = "swingstats";
	
    /* 
     * TABLE_SWINGSTATS: swingstats
     * 
     *     1      2      3       4       5       6       7      8        9      10      11
     * 	+-----+------+------+-------+-------+-------+-------+-------+-------+-------+-------+
     * 	| ID  | DATE | TIME | X-MAX | X-MAX | X-MIN | X-MIN | Y-MAX | Y-MAX | Y-MIN | Y-MIN |
     * 	|     |      |      |       | _TIME |       | _TIME |       | _TIME |       | _TIME |
     * 	+-----+------+------+-------+-------+-------+-------+-------+-------+-------+-------+
     * 	| INT | TEXT | TEXT | TEXT  | TEXT  |  TEXT | TEXT  | TEXT  | TEXT  | TEXT  | TEXT  |
     *  +-----+------+------+-------+-------+-------+-------+-------+-------+-------+-------+
     * 
     */
	
	// The name of columns
	private static final String ID = "id";
	private static final String DATE = "date";
	private static final String TIME = "time";
	
	private static final String X_MAX = "xmax";
	private static final String X_MAX_TIME = "xmax_time";
	private static final String X_MIN = "xmin";
	private static final String X_MIN_TIME = "xmin_time";
	
	private static final String Y_MAX = "ymax";
	private static final String Y_MAX_TIME = "ymax_time";
	private static final String Y_MIN = "ymin";
	private static final String Y_MIN_TIME = "ymin_time";
	
	/*
	 * Index of Columns
	 */
	private static final int INDEX_ID 			= 0;
	private static final int INDEX_DATE 		= 1;
	private static final int INDEX_TIME 		= 2;
	private static final int INDEX_X_MAX 		= 3;
	private static final int INDEX_X_MAX_TIME 	= 4;
	private static final int INDEX_X_MIN		= 5;
	private static final int INDEX_X_MIN_TIME	= 6;
	private static final int INDEX_Y_MAX		= 7;
	private static final int INDEX_Y_MAX_TIME	= 8;
	private static final int INDEX_Y_MIN		= 9;
	private static final int INDEX_Y_MIN_TIME	= 10;
	
	/*
	 * All columns of a row
	 */
	static String[] mAllColumns = new String[] { ID, DATE, TIME, 
												X_MAX, X_MAX_TIME, X_MIN, X_MIN_TIME, 
												Y_MAX, Y_MAX_TIME, Y_MIN, Y_MIN_TIME
												};
	
	public DatabaseHandler(Context context)
	{
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	public void onCreate(SQLiteDatabase db) {
		/*
		 *  CREATE TABLE contacts (id INTEGER PRIMARY KEY AUTOINCREMENT,
		 *  						name TEXT, 
		 *  						phone_number TEXT);
		 */
		String CREATE_SWINGSTATS_TABLE = "CREATE TABLE " + TABLE_SWINGSTATS 
							+ "(" + ID 		+ " INTEGER PRIMARY KEY AUTOINCREMENT," 
							+ DATE 			+ " TEXT,"
							+ TIME 			+ " TEXT,"
							+ X_MAX 		+ " TEXT,"
							+ X_MAX_TIME 	+ " TEXT,"
							+ X_MIN 		+ " TEXT,"
							+ X_MIN_TIME 	+ " TEXT,"
							+ Y_MAX 		+ " TEXT,"
							+ Y_MAX_TIME 	+ " TEXT,"
							+ Y_MIN 		+ " TEXT,"
							+ Y_MIN_TIME 	+ " TEXT"							
							+ ");";
		
		db.execSQL(CREATE_SWINGSTATS_TABLE);
		
		Log.d("db", "Create SWINGSTATS TABLE");
	}

	
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) 
	{
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_SWINGSTATS);
		onCreate(db);
	}
	
	/*=============================================================================
	 * Name: addSwingStats
	 * 
	 * Description:
	 * 		Insert Swing Statistics data to a database
	 * 
	 * Return:
	 * 		None
	 *=============================================================================*/     		
	public void addSwingStats(SwingStatistics swing)
	{
		SQLiteDatabase db = this.getWritableDatabase();
		
		ContentValues values = new ContentValues();
		values.put(DATE, swing.date);
		values.put(TIME, swing.time);
		
		values.put(X_MAX, swing.x_max);
		values.put(X_MAX_TIME, swing.x_max_timestamp);		
		
		values.put(X_MIN, swing.x_min);
		values.put(X_MIN_TIME, swing.x_min_timestamp);
		
		values.put(Y_MAX, swing.y_max);
		values.put(Y_MAX_TIME, swing.y_max_timestamp);
		
		values.put(Y_MIN, swing.y_min);
		values.put(Y_MIN_TIME, swing.y_min_timestamp);
		
		db.insert(TABLE_SWINGSTATS, null, values);
		
		db.close();
		
		Log.i("db", "addSwingStats()");
	}

	/*=============================================================================
	 * Name: getSwingStats
	 * 
	 * Description:
	 * 		Return the matched row from the database
	 * 
	 * Return:
	 * 		None
	 *=============================================================================*/     		
	public SwingStatistics getSwingStats(int id)
	{
		SwingStatistics swingStats = null;		
		
		SQLiteDatabase db = this.getReadableDatabase();
		

		Cursor cursor = db.query(TABLE_SWINGSTATS, mAllColumns, 
								ID + " =? ", new String[] {String.valueOf(id)},
								null, null, null, null);
		if(cursor != null)
			cursor.moveToFirst();
		
		swingStats = new SwingStatistics(Integer.parseInt(cursor.getString(INDEX_ID)),	  
										cursor.getString(INDEX_DATE), 
										cursor.getString(INDEX_TIME), 
										cursor.getString(INDEX_X_MAX), 
										cursor.getString(INDEX_X_MAX_TIME), 
										cursor.getString(INDEX_X_MIN), 
										cursor.getString(INDEX_X_MIN_TIME), 
										cursor.getString(INDEX_Y_MAX), 
										cursor.getString(INDEX_Y_MAX_TIME), 
										cursor.getString(INDEX_Y_MIN), 
										cursor.getString(INDEX_Y_MIN_TIME));
		
		
		return swingStats;
	}
	/*=============================================================================
	 * Name: getAllSwingStats
	 * 
	 * Description:
	 * 		Return all SwingStatistics from the database
	 * 
	 * Return:
	 * 		None
	 *=============================================================================*/     		
	public List<SwingStatistics> getAllSwingStats()
	{
		List<SwingStatistics> swingStatsList = new ArrayList<SwingStatistics>();
		
		String selectQuery = "SELECT * FROM " + TABLE_SWINGSTATS;
		SQLiteDatabase db = this.getWritableDatabase();
		
		Cursor cursor = db.rawQuery(selectQuery, null);
		
		if(cursor.moveToFirst())
		{
			do 
			{
				SwingStatistics swingStats = new SwingStatistics();
				
				swingStats.setID(Integer.parseInt(cursor.getString(INDEX_ID)));
				
				swingStats.setDateTime(cursor.getString(INDEX_DATE), 
										cursor.getString(INDEX_TIME));
				
				swingStats.setXPeakPoint(cursor.getString(INDEX_X_MAX), 
										cursor.getString(INDEX_X_MAX_TIME),
										cursor.getString(INDEX_X_MIN),
										cursor.getString(INDEX_X_MIN_TIME));
				
				swingStats.setYPeakPoint(cursor.getString(INDEX_Y_MAX), 
										cursor.getString(INDEX_Y_MAX_TIME), 
										cursor.getString(INDEX_Y_MIN),
										cursor.getString(INDEX_Y_MIN_TIME));
				
				swingStatsList.add(swingStats);
				
			}while(cursor.moveToNext());
		}
		
		return swingStatsList;
	}
	
	/*=============================================================================
	 * Name: getSwingStatsCount
	 * 
	 * Description:
	 * 		Return the number of SwingStatistics rows in the database
	 * 
	 * Return:
	 * 		None
	 *=============================================================================*/     		
	public int getSwingStatsCount()
	{
		String countQuery = "SELECT * FROM " + TABLE_SWINGSTATS;
		SQLiteDatabase db = this.getReadableDatabase();
		
		Cursor cursor = db.rawQuery(countQuery, null);
		cursor.close();
		
		return cursor.getCount();
		
	}
	/*=============================================================================
	 * Name: updateSwingStats
	 * 
	 * Description:
	 * 		Update a single row in the database
	 * 
	 * Return:
	 * 		int
	 *=============================================================================*/     		
	public int updateSwingStats(SwingStatistics swingStats)
	{
		SQLiteDatabase db = this.getWritableDatabase();
		
		ContentValues values = new ContentValues();
		values.put(ID, swingStats.id);
		values.put(TIME, swingStats.date);
		values.put(TIME, swingStats.time);
		
		values.put(X_MAX, swingStats.x_max);
		values.put(X_MAX_TIME, swingStats.x_max_timestamp);
		
		values.put(X_MIN, swingStats.x_min);
		values.put(X_MIN_TIME, swingStats.x_min_timestamp);
		
		values.put(Y_MAX, swingStats.y_max);
		values.put(Y_MAX_TIME, swingStats.y_max);
		
		values.put(Y_MIN, swingStats.y_min);
		values.put(Y_MIN_TIME, swingStats.y_min_timestamp);
		
		return db.update(TABLE_SWINGSTATS,	values, ID + " =? ", 
						new String[] {String.valueOf(swingStats.getID())});
	}
	/*=============================================================================
	 * Name: deleteSwingStats
	 * 
	 * Description:
	 * 		Delete one row which is matched with the given ID from the database 
	 * 
	 * Return:
	 * 		int
	 *=============================================================================*/
	public void deleteSwingStats(SwingStatistics swingStats)
	{
		SQLiteDatabase db = this.getWritableDatabase();
		
		db.delete(TABLE_SWINGSTATS, ID + " =? ", 
				new String[] {String.valueOf(swingStats.getID())});
		
		db.close();
	}
	
	/*=============================================================================
	 * Name: deleteSwingTable
	 * 
	 * Description:
	 * 		Delete the swing data table(TABLE_SWINGSTATS) 
	 * 
	 * Return:
	 * 		None
	 *=============================================================================*/	
	public void deleteSwingTable()
	{
		String selectQuery = "SELECT * FROM " + TABLE_SWINGSTATS;
		SQLiteDatabase db = this.getWritableDatabase();		
		Cursor cursor = db.rawQuery(selectQuery, null);

		db.delete(TABLE_SWINGSTATS, null, null);
		cursor.requery();
	}
}
