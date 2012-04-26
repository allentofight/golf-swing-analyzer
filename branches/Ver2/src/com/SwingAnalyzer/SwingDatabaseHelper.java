package com.SwingAnalyzer;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class SwingDatabaseHelper extends SQLiteOpenHelper
{
	private static final String SWING_DB_NAME = "swing_acceleration.db";
	private static final int SWING_DB_VERSION = 1;
	private static final String SWING_TABLE_NAME = "swingstats";
	
	/*
    private static final String CREATE_REMOTE_SITE_TABLE = 
			" create table " + RemoteSiteDB.TABLE_NAME + 
			" (" + RemoteSiteDB.ID + " integer primary key autoincrement," +
				RemoteSiteDB.LOCATION + " text not null, " +
				RemoteSiteDB.PHONE_NUMBER + " text not null, " +
				RemoteSiteDB.DEVICE_ID + " text not null);";
	*/
	
    /* Table: table_acceleration
     *         1      2      3       4       5       6       7      8        9      10      11
     * 		+-----+------+------+-------+-------+-------+-------+-------+-------+-------+-------+
     * 		| id  | Date | Time | X-Max | X-Max | X-Min | X-Min | Y-Max | Y-Max | Y-Min | Y-Min |
     * 		|     |      |      |       | time  |       | time  |       | time  |       | time  |
     * 		+-----+------+------+-------+-------+-------+-------+-------+-------+-------+-------+
     * 		| INT | TEXT | TEXT | TEXT  | TEXT  |  TEXT | TEXT  | TEXT  | TEXT  | TEXT  | TEXT  |
     *      +-----+------+------+-------+-------+-------+-------+-------+-------+-------+-------+
     * 
     */
    private static final String CREATE_TABLE = 
    		"create table " + SWING_TABLE_NAME + "";
	public SwingDatabaseHelper(Context context)
	{
		super(context, SWING_DB_NAME, null, SWING_DB_VERSION);
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		  db.execSQL("DROP TABLE IF EXISTS SWING_DB_NAME") ;
		  onCreate(db) ;
	}

}
