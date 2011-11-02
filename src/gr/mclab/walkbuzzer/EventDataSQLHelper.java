package gr.mclab.walkbuzzer;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class EventDataSQLHelper extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "events.db";
	private static final int DATABASE_VERSION = 1;

	// Table name
	public static final String TABLE = "events";

	// Column names
	public static final String WB_AL_ID = "wb_al_id";
	public static final String WB_AL_TITLE = "wb_al_name";
	public static final String WB_AL_LON = "wb_al_lon";
	public static final String WB_AL_LAT = "wb_al_lat";
	public static final String WB_AL_STATUS = "wb_al_status";
	
	public static final int WB_AL_STATUS_DISABLED = 0;
	public static final int WB_AL_STATUS_ENABLED = 1;
	public static final int WB_AL_STATUS_DELETED = 2;

	public EventDataSQLHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String sql = "CREATE TABLE IF NOT EXISTS " + TABLE + "(wb_al_id INTEGER PRIMARY KEY AUTOINCREMENT, wb_al_name VARCHAR(50), wb_al_lon VARCHAR(50), wb_al_lat VARCHAR(50), wb_al_status TINYINT);";
		Log.d("EventsData", "onCreate: " + sql);
		db.execSQL(sql);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (oldVersion >= newVersion)
			return;

		String sql = null;
		if (oldVersion == 1)
			sql = "ALTER TABLE " + TABLE + " ADD NOTE TEXT;";
		if (oldVersion == 2)
			sql = "";

		Log.d("EventsData", "onUpgrade	: " + sql);
		if (sql != null)
			db.execSQL(sql);
	}

}
