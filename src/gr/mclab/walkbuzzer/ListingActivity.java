package gr.mclab.walkbuzzer;

import java.util.ArrayList;

import com.google.android.maps.MapView;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ListingActivity extends ListActivity {
		private SQLiteDatabase db = null;
		private ArrayList<String> namesArr;
    	private ArrayList<String> idsArr;
    	
	/** Called when the activity is first created. */
		
		public void onCreate(Bundle icicle) {
			super.onCreate(icicle);
			
			db = new EventDataSQLHelper(this).getReadableDatabase();
		    populateList(db);
		}
		
		private void populateList(SQLiteDatabase db) {
			Cursor cursor = db.query(EventDataSQLHelper.TABLE,
		    		null, null, null, null, null, null);
		    
		    startManagingCursor(cursor);
		    
		    // If results found
	        if (cursor.getCount() > 0) {
	        	
	        	// Create an array of Strings, that will be put to our ListActivity
	        	namesArr = new ArrayList<String>();
	        	idsArr = new ArrayList<String>();
	        	
	        	
	        	// Iterate through results
	        	while (cursor.moveToNext()) {
	    	    	String wb_al_title = cursor.getString(
	    	    			cursor.getColumnIndex(EventDataSQLHelper.WB_AL_TITLE)
	    	    	);
	    	    	int wb_al_status = cursor.getInt(
	    	    			cursor.getColumnIndex(EventDataSQLHelper.WB_AL_STATUS)
	    	    	);
	    	    	int wb_al_id = cursor.getInt(
	    	    			cursor.getColumnIndex(EventDataSQLHelper.WB_AL_ID)
	    	    	);
	    	    	if (wb_al_status != EventDataSQLHelper.WB_AL_STATUS_DELETED) {
	    	    		namesArr.add(wb_al_title);
	    	    		idsArr.add(Integer.toString(wb_al_id));
	    	    	}
	        	}
	        }

			// Create an ArrayAdapter, that will actually make the Strings above
			// appear in the ListView
	        if (namesArr != null) {
	        	this.setListAdapter(new ArrayAdapter<String>(this,
						android.R.layout.simple_list_item_1, namesArr));
	        }
		}
		
		@Override
		protected void onDestroy() {
		    super.onDestroy();
		    if (db != null) {
		        db.close();
		    }
		}

		@Override
		protected void onListItemClick(ListView l, View v, int position, long id) {
			super.onListItemClick(l, v, position, id);
			// Get the item that was clicked
			Object o = this.getListAdapter().getItem(position);
			
			final CharSequence[] items = {"Activate", "Deactivate", "Delete"};
			final int eventId = Integer.parseInt(idsArr.get(position));
			
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(o.toString());
			builder.setItems(items, new DialogInterface.OnClickListener() {
			    public void onClick(DialogInterface dialog, int item) {
			    	if (item == 0) {
			    		ContentValues values = new ContentValues();
			    		// Update notification status
			    	    values.put(EventDataSQLHelper.WB_AL_STATUS, EventDataSQLHelper.WB_AL_STATUS_ENABLED);
			    	    db.update(
			    	    		EventDataSQLHelper.TABLE, values, // Table values
			    	    		EventDataSQLHelper.WB_AL_ID + "=" + eventId, // Where clause
			    	    		null
			    	    );
			    	} else if (item == 1) {
			    		ContentValues values = new ContentValues();
			    		// Update notification status
			    	    values.put(EventDataSQLHelper.WB_AL_STATUS, EventDataSQLHelper.WB_AL_STATUS_DISABLED);
			    	    db.update(
			    	    		EventDataSQLHelper.TABLE, values, // Table values
			    	    		EventDataSQLHelper.WB_AL_ID + "=" + eventId, // Where clause
			    	    		null
			    	    );
			    	} else if (item == 2) {
			    		ContentValues values = new ContentValues();
			    		// Update notification status
			    	    values.put(EventDataSQLHelper.WB_AL_STATUS, EventDataSQLHelper.WB_AL_STATUS_DELETED);
			    	    db.update(
			    	    		EventDataSQLHelper.TABLE, values, // Table values
			    	    		EventDataSQLHelper.WB_AL_ID + "=" + eventId, // Where clause
			    	    		null
			    	    );
			    	    populateList(db);
			    	}
			    }
			}).show();
			AlertDialog alert = builder.create();
		}
		@Override
		public boolean onKeyDown(int keyCode, KeyEvent event) {
			if (keyCode == KeyEvent.KEYCODE_BACK) {
				moveTaskToBack(true);
				return true;
			}
			return super.onKeyDown(keyCode, event);
		}
	}
