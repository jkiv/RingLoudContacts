package ca.jkiv.RingLoudContacts;

import android.content.Context;
import android.content.SharedPreferences;

public class SettingsPersistence
{
	public static String SETTINGS_PREF_FILE = "RingLoudContacts_Settings";
	public static String SETTINGS_PREF_ADJUST_RINGER = "AdjustRingerVolume"; // Should match res/xml/preferences.xml

	private SettingsPersistence() {} // Class not meant to be instantiated

	public static boolean shouldAdjustVolume(Context context)
	{
		SharedPreferences sharedPreferences = context.getSharedPreferences(SETTINGS_PREF_FILE, 0);
		return sharedPreferences.getBoolean(SETTINGS_PREF_ADJUST_RINGER, false);
	}
}
