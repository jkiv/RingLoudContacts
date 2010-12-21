package ca.jkiv.RingLoudContacts;

import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

/**
 * The activity for changing the settings.
 * 
 * @author Jon Kivinen <android@jkiv.ca>
 */
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

        // Show version number
        Preference aboutItem = findPreference("_AboutLink");
        
        try
        {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            aboutItem.setSummary("Version " + packageInfo.versionName);
        } 
        catch (NameNotFoundException e)
        {
            aboutItem.setSummary("Version ???");
        }
        
        // When we click the "About" item, we want to display an about dialog.
        aboutItem.setOnPreferenceClickListener(new OnPreferenceClickListener()
        {
            public boolean onPreferenceClick(Preference preference)
            {
                // Create about dialog
                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View layout = inflater.inflate(R.layout.about_dialog, null);
                TextView aboutTextView = (TextView) layout.findViewById(R.id.AboutDialog_TextView);
                
                final SpannableString s = new SpannableString(getText(R.string.AboutDialog_Message));
                Linkify.addLinks(s, Linkify.WEB_URLS);
                aboutTextView.setText(s);
                aboutTextView.setMovementMethod(LinkMovementMethod.getInstance());

                new AlertDialog.Builder(SettingsActivity.this)
                    .setTitle(R.string.AboutDialog_Title)
                    .setIcon(R.drawable.ic_launcher_ring_loud_contacts)
                    .setPositiveButton(R.string.Dialog_OK, null)
                    .setView(layout)
                    .show();

                return true;
            }
        });

    }
}
