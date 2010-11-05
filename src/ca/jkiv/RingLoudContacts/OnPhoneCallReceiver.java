package ca.jkiv.RingLoudContacts;

import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
 * OnPhoneCallReceiver
 * 
 * Gets called when a phone call comes in.
 * 
 * @author Jon Kivinen <android@jkiv.ca>
 */
public class OnPhoneCallReceiver extends BroadcastReceiver
{
	@Override
	public void onReceive(Context context, Intent intent)
	{
                // One might be able to check the status here, rather than registering a listener...
                // http://developer.android.com/reference/android/telephony/TelephonyManager.html#getCallState%28%29
                // http://developer.android.com/reference/android/telephony/TelephonyManager.html#ACTION_PHONE_STATE_CHANGED
		TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		telephonyManager.listen(new VolumeChangingPhoneListener(context), PhoneStateListener.LISTEN_CALL_STATE);
	}
	
	private class VolumeChangingPhoneListener extends PhoneStateListener
	{
		private Context context;
		
		public VolumeChangingPhoneListener(Context context)
		{
			this.context = context;
		}
		
		public void onCallStateChanged(int state, String incomingNumber)
		{
			switch(state)
			{
			  case TelephonyManager.CALL_STATE_IDLE:
				// Phone stopped ringing
				VolumeControl.resumeVolume(context);
				TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
				telephonyManager.listen(this, PhoneStateListener.LISTEN_NONE);
				break;
			  case TelephonyManager.CALL_STATE_RINGING:
				// Phone is ringing
				List<PhoneNumber> contacts = ContactListPersistence.getContactList(context);
				
				if (contacts.contains(new PhoneNumber(incomingNumber)))
				{
					VolumeControl.saveVolume(context);
					VolumeControl.maxVolume(context);
				}
				
				break;
			  case TelephonyManager.CALL_STATE_OFFHOOK:
				// Phone answered or dialing?
			  default:
				// Nothing
			}
		}

	}
}
