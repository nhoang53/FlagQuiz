package edu.orangecoastcollege.cs273.nhoang53.flagquiz;

import android.preference.PreferenceFragment;
import android.os.Bundle;


/**
 * SettingsActivityFragment is a subclass of PreferenceFragment for managing
 * the app settings, such as number of guesses and regions to select fglag from
 */
public class SettingActivityFragment extends PreferenceFragment {
    // create preferences GUI from preferences.xml file in res/xml
    @Override
    public void onCreate(Bundle bundle){
        super.onCreate(bundle);

        addPreferencesFromResource(R.xml.preferences); // load from XML
    }
}
