package ca.jkiv.RingLoudContacts;

import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;

/**
 * OnPhoneStateChangeReceiver
 * 
 * onReceive() should be called when the phone is receiving calls
 * 
 * @author Jon Kivinen <android@jkiv.ca>
 * 
 * @see BroadcastReceiver#onReceive(Context, Intent)
 * @see TelephonyManager#ACTION_PHONE_STATE_CHANGED
 */
public class OnPhoneStateChangeReceiver extends BroadcastReceiver
{
	@Override
	public void onReceive(Context context, Intent intent)
	{
		// Check adjust ringer volume setting
		if (!SettingsPersistence.shouldAdjustVolume(context)) return;
		
		// Ignore bogus intents
		if (intent == null || intent.getAction() == null) return;
		
		// Handle android.telephony.TelephonyManager.ACTION_PHONE_STATE
		if (intent.getAction().equals(android.telephony.TelephonyManager.ACTION_PHONE_STATE_CHANGED))
		{
			handleCallStateChanged(context, intent);
		}
	}
	
	private void handleCallStateChanged(Context context, Intent intent)
	{
		TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		
		switch(telephonyManager.getCallState())
		{
		  case TelephonyManager.CALL_STATE_IDLE:
			// Phone stopped ringing
			VolumeControl.resumeVolume(context);
			break;
		  case TelephonyManager.CALL_STATE_RINGING:
			// Phone is ringing
			String incomingNumber = intent.getExtras().getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
			List<PhoneNumber> contacts = ContactListPersistence.getContactList(context);
			
			if (contacts.contains(new PhoneNumber(incomingNumber)))
			{
				VolumeControl.saveVolume(context);
				VolumeControl.maxVolume(context);
			}
			
			break;
		  case TelephonyManager.CALL_STATE_OFFHOOK:
			// Phone answered or dialing?
			break;
		  default:
			// Something else we're not concerned with
		}
	}
}
