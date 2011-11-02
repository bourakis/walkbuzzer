package gr.mclab.walkbuzzer;

import android.os.Bundle;
import android.preference.PreferenceActivity;



public class Settings extends PreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
        
        
    }
    
}