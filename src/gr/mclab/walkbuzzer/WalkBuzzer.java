package gr.mclab.walkbuzzer;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TabHost;
import android.widget.Toast;


public class WalkBuzzer extends TabActivity {
	
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.main);

	    Resources res = getResources(); // Resource object to get Drawables
	    TabHost tabHost = getTabHost();  // The activity TabHost
	    TabHost.TabSpec spec;  // Resusable TabSpec for each tab
	    Intent intent;  // Reusable Intent for each tab

	    // Create an Intent to launch an Activity for the tab (to be reused)
	    intent = new Intent(Intent.ACTION_VIEW).setClass(this, WBMapView.class);

	    // Initialize a TabSpec for each tab and add it to the TabHost
	    spec = tabHost.newTabSpec("Notifications").setIndicator("Notifications",
	                      res.getDrawable(R.drawable.ic_tab_notifications))
	                  .setContent(intent);
	    tabHost.addTab(spec);

	    // Do the same for the other tabs
	    
	    intent = new Intent().setClass(this, ListingActivity.class);
	    spec = tabHost.newTabSpec("List").setIndicator("List",
	                      res.getDrawable(R.drawable.ic_tab_list))
	                  .setContent(intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
	    tabHost.addTab(spec);

	    /*
	    intent = new Intent().setClass(this, FavoritesActivity.class);
	    spec = tabHost.newTabSpec("Favorite").setIndicator("Favorite",
	                      res.getDrawable(R.drawable.ic_tab_favorites))
	                  .setContent(intent);
	    tabHost.addTab(spec);*/
	    tabHost.setCurrentTab(0);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.menu, menu);
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case R.id.bottommenu1:
	        	Intent settingsIntent = new Intent(Intent.ACTION_VIEW).setClass(this, Settings.class);
	        	startActivity(settingsIntent);
	            break;
	        case R.id.bottommenu2: 
	        	this.moveTaskToBack(true);
	            break;
	    }
	    return true;
	}
}