package ca.jkiv.RingLoudContacts;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.media.AudioManager;

/**
 * Methods for saving, maximizing and restoring the phone's ringer volume.
 * 
 * @author Jon Kivinen <android@jkiv.ca>
 */
public class VolumeControl
{
	private static final String VOLUME_FILE = "RingLoudContacts_Volume";
	private static final String VOLUME_MODE_PREF = "VolumeMode";
	private static final String VOLUME_LEVEL_PREF = "VolumeLevel";
	
	// Default remembered state is silent (this is the assumed operation)
	private static final int VOLUME_MODE_PREF_DEF = AudioManager.RINGER_MODE_SILENT;
	private static final int VOLUME_LEVEL_PREF_DEF = 0;
	
	private static final int stream = AudioManager.STREAM_RING;
	
	private VolumeControl() {} // Class not meant to be instantiated
	
	/**
	 * Restore volume and ringer mode to saved value.
	 * @param context
	 */
	public static void resumeVolume(Context context)
	{
		AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		SharedPreferences sharedPreferences = context.getSharedPreferences(VOLUME_FILE, 0);
		
		int ringerMode = sharedPreferences.getInt(VOLUME_MODE_PREF, VOLUME_MODE_PREF_DEF);
		int volumeLevel = sharedPreferences.getInt(VOLUME_LEVEL_PREF, VOLUME_LEVEL_PREF_DEF);
		int flags = 0;
		
		audioManager.setRingerMode(ringerMode);
		audioManager.setStreamVolume(stream, volumeLevel, flags);
	}

	/**
	 * Remember current volume level and ringer mode.
	 * @param context
	 */
	public static void saveVolume(Context context)
	{
		AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		SharedPreferences sharedPreferences = context.getSharedPreferences(VOLUME_FILE, 0);

		int ringerMode = audioManager.getRingerMode();
		int volumeLevel = audioManager.getStreamVolume(stream);
		
		Editor editor = sharedPreferences.edit();
		editor.putInt(VOLUME_MODE_PREF, ringerMode);
		editor.putInt(VOLUME_LEVEL_PREF, volumeLevel);
		editor.commit();		
	}

	/**
	 * Turn on the volume and maximize it.
	 * @param context
	 */
	public static void maxVolume(Context context)
	{
		AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		
		int stream = AudioManager.STREAM_RING;
		int maxVolume = audioManager.getStreamMaxVolume(stream);
		int flags = 0;

		audioManager.setStreamVolume(stream, maxVolume, flags);
		audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);		
	}
}
