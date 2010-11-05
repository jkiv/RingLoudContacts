package ca.jkiv.RingLoudContacts;

import android.app.AlertDialog;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class SettingsActivity extends PreferenceActivity
{
    private PreferenceManager preferenceManager;

	public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        setTitle(R.string.Settings_Title);
        
        // Associate Activity with a preferences file
        preferenceManager = getPreferenceManager();
        preferenceManager.setSharedPreferencesName(SettingsPersistence.SETTINGS_PREF_FILE);
        preferenceManager.setSharedPreferencesMode(0);
        
        // Use preferences.xml to define preference schema
        addPreferencesFromResource(R.xml.preferences);
        
        // When we click the "About" item, we want to display an about dialog.
        Preference editWhitelistLink = (Preference) findPreference("_AboutLink");
        editWhitelistLink.setOnPreferenceClickListener(new OnPreferenceClickListener()
        {
            public boolean onPreferenceClick(Preference preference)
            {
            	// Create about dialog

            	new AlertDialog.Builder(SettingsActivity.this)
                .setTitle(R.string.About_Title)
                .setMessage(R.string.About_Message)
                //.setIcon(R.drawable.ic_launcher_ring_loud_contacts) // Icon not made yet.
                .show();
                
                return true;
            }
        });
    }
}
