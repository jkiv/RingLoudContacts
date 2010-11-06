package ca.jkiv.RingLoudContacts;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Methods for getting settings values from {@link SharedPreferences}.<br/>
 * Preference names need to be the same as they are in res/xml/preferences.xml
 * 
 * @author Jon Kivinen <android@jkiv.ca>
 * @see res/xml/preferences.xml
 */
public class SettingsPersistence
{
	public static String SETTINGS_PREF_FILE = "RingLoudContacts_Settings";
	public static String SETTINGS_PREF_ADJUST_RINGER = "AdjustRingerVolume";
	private static boolean SETTINGS_PREF_ADJUST_RINGER_DEF = true;

	private SettingsPersistence() {} // Class not meant to be instantiated

	public static boolean shouldAdjustVolume(Context context)
	{
		SharedPreferences sharedPreferences = context.getSharedPreferences(SETTINGS_PREF_FILE, 0);
		return sharedPreferences.getBoolean(SETTINGS_PREF_ADJUST_RINGER, SETTINGS_PREF_ADJUST_RINGER_DEF );
	}
}
