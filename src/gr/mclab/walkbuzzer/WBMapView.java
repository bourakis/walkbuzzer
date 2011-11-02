package gr.mclab.walkbuzzer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.GestureDetector.OnGestureListener;
import android.view.KeyEvent;
import android.view.View;

import android.view.MotionEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.OverlayItem;

public class WBMapView extends MapActivity implements OnGestureListener, OnDoubleTapListener {
	
	private LocationManager locationManager;
	private LocationListener locationListener;
	private MyLocationOverlay myLocOverlay;
	private MapController mapController;
	private EventDataSQLHelper eventsData;
	private MapView mapView;
	private SharedPreferences settings;
	private boolean isFocused;
	
    
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.mapview);
        
        loadSettings();
        
        // Get location manager
        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        
        // Attach location listener (Subclass of WBMapView)
		locationListener = new WBLocationListener();
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 
        		400, 1, locationListener);
        
        // Create a new mapview
        mapView = (MapView) findViewById(R.id.mapview);
        mapView.setBuiltInZoomControls(true);
        mapController = mapView.getController();

        // Set current location on init
        myLocOverlay = new MyLocationOverlay(this, mapView);
		myLocOverlay.enableMyLocation();
		mapView.getOverlays().add(myLocOverlay);
        
        // Get sql helper, class EventDataSQLHelper
        eventsData = new EventDataSQLHelper(this);
        populateOverlays();
    }
    
    public void loadSettings()
    {
    	/* Load preferences/settings */
        settings = PreferenceManager.getDefaultSharedPreferences(this);  
    }
    
    private double getDistance()
    {
    	loadSettings();
    	double distance = Double.parseDouble(settings.getString("listDistance", "100"));
        Log.d("LOG XML SETTINGS: ", settings.getString("listDistance", "100"));

    	return distance;
    }
    
    private WBItemizedOverlay getItemizedOverlay(GeoPoint point, boolean enabled) {
    	
    	Drawable drawable = null;
    	if (enabled) {
    		drawable = this.getResources()
    			.getDrawable(R.drawable.marker_red_dot);
    	} else {
    		drawable = this.getResources()
    			.getDrawable(R.drawable.marker_grey_dot);
    	}
    	
    	// Create a new WBItemizedOverlay object
    	WBItemizedOverlay itemizedOverlay = new WBItemizedOverlay(
    			drawable, 
    			this
    	);
    	
    	// Add a new overlay item from a GeoPoint
    	itemizedOverlay.addOverlay(new OverlayItem(point, "", ""));
    	return itemizedOverlay;
    }
    
    private void updateOverlays() {
        mapView.getOverlays().clear();
        mapView.getOverlays().add(myLocOverlay);
		populateOverlays();
		mapView.invalidate();
    }
    
    private void populateOverlays() {
    	Cursor cursor = getEvents();
 	   
        // If results found
        if (cursor.getCount() > 0) {
        	
        	// Iterate through results
        	while (cursor.moveToNext()) {
        		
    	    	String wb_al_lat = cursor.getString(
    	    			cursor.getColumnIndex(EventDataSQLHelper.WB_AL_LAT)
    	    	);
    	    	String wb_al_lon = cursor.getString(
    	    			cursor.getColumnIndex(EventDataSQLHelper.WB_AL_LON)
    	    	);
    	    	int wb_al_status = cursor.getInt(
    	    			cursor.getColumnIndex(EventDataSQLHelper.WB_AL_STATUS)
    	    	);
    	    	
    	    	Location location = new Location("location");
                location.setLatitude(Double.parseDouble(wb_al_lat));
                location.setLongitude(Double.parseDouble(wb_al_lon));
                
                int lat = (int) (location.getLatitude() * 1E6);
        		int lng = (int) (location.getLongitude() * 1E6);

                GeoPoint point = new GeoPoint(lat, lng);
                
                if (wb_al_status == EventDataSQLHelper.WB_AL_STATUS_ENABLED) {
                	// Add custom overlay
                    mapView.getOverlays().add(getItemizedOverlay(point, true));
    	    	} else if (wb_al_status == EventDataSQLHelper.WB_AL_STATUS_DISABLED) {
    	    		// Add custom overlay
                    mapView.getOverlays().add(getItemizedOverlay(point, false));
    	    	}
    	    }  	
        }
    }
    
    static final int DIALOG_PAUSED_ID = 0;
    static final int DIALOG_GAMEOVER_ID = 1;
    
    private void setLocationTo(GeoPoint point) {

    	final Dialog dialog = new Dialog(this);
    	dialog.setContentView(R.layout.dialogform);
    	dialog.setTitle("Set location");
    	dialog.show();
    	
    	final GeoPoint newPoint = point;
    	final EditText editText = (EditText) dialog.findViewById(R.id.widget32);
    	Button dialogButton = (Button) dialog.findViewById(R.id.widget33);
    	View.OnClickListener myhandler1 = new View.OnClickListener() {
            public void onClick(View v) {
            	
            	SQLiteDatabase db = eventsData.getWritableDatabase();
        		ContentValues values = new ContentValues();
        		values.put(EventDataSQLHelper.WB_AL_TITLE, editText.getText().toString());
        	    values.put(EventDataSQLHelper.WB_AL_LAT, (double) newPoint.getLatitudeE6()/1E6);
        	    values.put(EventDataSQLHelper.WB_AL_LON, (double) newPoint.getLongitudeE6()/1E6);
        	    values.put(EventDataSQLHelper.WB_AL_STATUS, EventDataSQLHelper.WB_AL_STATUS_ENABLED);
        	    db.insert(EventDataSQLHelper.TABLE, null, values);
        	   
        	    // Add custom overlay
        	    mapView.getOverlays().add(getItemizedOverlay(newPoint, true));
            	mapController.animateTo(newPoint);
                mapView.invalidate();
        	    
        	    /* Close dialog */
        	    dialog.dismiss();
            }
          };
    	dialogButton.setOnClickListener(myhandler1);

    }
    
    private Cursor getEvents() {
	    SQLiteDatabase db = eventsData.getReadableDatabase();
	    Cursor cursor = db.query(EventDataSQLHelper.TABLE,
	    		null, null, null, null, null, null);
	    startManagingCursor(cursor);
	    return cursor;
	}
	
    public void createNotification(String destTitle) {
		NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		Notification notification = new Notification(R.drawable.icon,
				"A WalkBuzzer Notification", System.currentTimeMillis());
		// Hide the notification after its selected
		notification.flags |= Notification.FLAG_AUTO_CANCEL;

		Intent intent = new Intent(this, WalkBuzzer.class);
		PendingIntent activity = PendingIntent.getActivity(this, 0, intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP), PendingIntent.FLAG_CANCEL_CURRENT);
		
		notification.setLatestEventInfo(this, "WalkBuzzer",
				"You have reached: " + destTitle, activity);
		notification.number += 1;
		notificationManager.notify(0, notification);
	}
	
	private void onReachingLocationMsg(String wbAlTitle) {
        AlertDialog.Builder alertbox = new AlertDialog.Builder(this);
        alertbox.setMessage("You have reached your destination! \n" + "\"" + wbAlTitle + "\"");
        alertbox.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
            	Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            	vibrator.cancel();
            }
        });
        alertbox.show();
        //createNotification(wbAlTitle);
        
	}
	
	private void distanceCheck(Location locDestination, Location locCurrent, int overlayIndex, String wbAlTitle, int wbAlId) {
		if(locCurrent.distanceTo(locDestination) < getDistance() ) {
			
			// Show message dialog
			onReachingLocationMsg(wbAlTitle);
			
			// Start vibrating
			Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        	vibrator.vibrate(40000);
			
			// Get writable database
			SQLiteDatabase db = eventsData.getWritableDatabase();
    		ContentValues values = new ContentValues();
    		
    		// Location reached, update table
    	    values.put(EventDataSQLHelper.WB_AL_STATUS, EventDataSQLHelper.WB_AL_STATUS_DISABLED);
    	    db.update(
    	    		EventDataSQLHelper.TABLE, values, // Table values
    	    		EventDataSQLHelper.WB_AL_ID + "=" + wbAlId, // Where clause
    	    		null
    	    );
		
    	    // Get overlay item
    	    WBItemizedOverlay itemizedOverlay = (WBItemizedOverlay) 
    	    	mapView.getOverlays().get(overlayIndex);
    	    
    	    // Set new drawable (disabled)
    	    Drawable disabledMarker = this.getResources()
    	    	.getDrawable(R.drawable.marker_grey_dot);
    	    itemizedOverlay.setMarker(disabledMarker, 0);
    	    
    	    if (!isFocused) {
    	    	createNotification(wbAlTitle);
    	    }
		}
	}
	
    private class WBLocationListener implements LocationListener {
        
    	// Create geopoint from location
        private GeoPoint createGeoPoint(Location loc) {
            int lat = (int) (loc.getLatitude() * 1E6);
            int lng = (int) (loc.getLongitude() * 1E6);
            return new GeoPoint(lat, lng);
        }

        @Override
        public void onLocationChanged(Location loc) {
            if (loc != null) {
            	
            	mapController.setZoom(18);
            	mapController.animateTo(createGeoPoint(loc));
                mapView.invalidate();
            	
                ArrayList<String[]> dataItems = new ArrayList<String[]>();
                
            	Cursor cursor = getEvents();
            	if (cursor.getCount() > 0) {
            		
            		int i = 1;
            		while (cursor.moveToNext()) {
            			int wb_al_id = cursor.getInt(
            					cursor.getColumnIndex(EventDataSQLHelper.WB_AL_ID)
            			);
            			String wb_al_title = cursor.getString(
            	    			cursor.getColumnIndex(EventDataSQLHelper.WB_AL_TITLE)
            	    	);
            	    	String wb_al_lat = cursor.getString(
            	    			cursor.getColumnIndex(EventDataSQLHelper.WB_AL_LAT)
            	    	);
            	    	String wb_al_lon = cursor.getString(
            	    			cursor.getColumnIndex(EventDataSQLHelper.WB_AL_LON)
            	    	);
            	    	int wb_al_status = cursor.getInt(
            	    			cursor.getColumnIndex(EventDataSQLHelper.WB_AL_STATUS)
            	    	);
                        
            	    	if (wb_al_status != EventDataSQLHelper.WB_AL_STATUS_DELETED) {
            	    		if (wb_al_status == EventDataSQLHelper.WB_AL_STATUS_ENABLED) {
                	    		Location locationTo = new Location("LocationTo");
                        	    locationTo.setLatitude(Double.parseDouble(wb_al_lat));
                        	    locationTo.setLongitude(Double.parseDouble(wb_al_lon));
                                distanceCheck(locationTo, loc, i, wb_al_title, wb_al_id);
                	    	}
            	    		i++;
            	    	}
            	    }
        	    }
            }
        }

        @Override
        public void onProviderDisabled(String provider) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onProviderEnabled(String provider) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onStatusChanged(String provider, int status, 
            Bundle extras) {
            // TODO Auto-generated method stub
        }
    }    

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onDown(MotionEvent e) {
		return false;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		
		// Get projection
		GeoPoint p = mapView.getProjection().fromPixels(
				(int)e.getX(), 
				(int)e.getY()
		);
		
		// Set new location
		setLocationTo(p);
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onDoubleTap(MotionEvent e) {
		
		// Get projection
		int xPixel = (int) e.getX();
		int yPixel = (int) e.getY();
		
		// Zoom in by one level
		mapController.zoomInFixing(xPixel, yPixel);
		return true;
	}

	@Override
	public boolean onDoubleTapEvent(MotionEvent arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onSingleTapConfirmed(MotionEvent arg0) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public void onPause() {
		super.onPause();
		isFocused = false;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		updateOverlays();
		isFocused = true;
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
