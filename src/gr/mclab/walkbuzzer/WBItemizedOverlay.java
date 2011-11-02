package gr.mclab.walkbuzzer;

import java.util.ArrayList;

import android.graphics.drawable.Drawable;
import android.content.Context;
import android.graphics.Canvas;
import android.view.MotionEvent;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;
import com.google.android.maps.MapView;

public class WBItemizedOverlay extends ItemizedOverlay<OverlayItem> {

	private ArrayList<OverlayItem> items = new ArrayList<OverlayItem>();
	private Drawable marker = null;
	private Context context = null;
	
	public WBItemizedOverlay(Drawable marker, Context context) {
		super(boundCenterBottom(marker));
		this.marker = marker;
		this.context = context;
	}
	
	@Override
	protected OverlayItem createItem(int i) {
		// TODO Auto-generated method stub
		return items.get(i);
	}

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return items.size();
	}
	
	public void addOverlay(OverlayItem overlay) {
	    items.add(overlay);
	    populate();
	}
	
	public void removeOverlay() {
		items.clear();
		populate();
	}
	
	// Set marker of existing overlay
	public void setMarker(Drawable marker, int i) {
		boundCenterBottom(marker);
		items.get(i).setMarker(marker);
	}
	
	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		super.draw(canvas, mapView, shadow);
		boundCenterBottom(marker);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event, MapView mapView) {
		return false;
	}
}
