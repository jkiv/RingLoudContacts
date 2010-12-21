package ca.jkiv.RingLoudContacts;

import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;

/**
 * BroadcastReceiver responsible for listening to the phone's state.<br/><br/> 
 * <pre>
 * I've seen some examples that register a PhoneStateListener when onReceive()
 * is invoked.  I'm under the impression onReceive() is called when the phone's
 * state changes and the information you'd get with a PhoneStateListener is
 * either available through TelephonyManager or encoded in the intent.  So
 * instead of using a PhoneStateListener, I just determine the phone state
 * and take action from within onReceive().
 *
 *                                                     -- Jon Kivinen
 * </pre>
 * @author Jon Kivinen <android@jkiv.ca>
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
            List<PhoneNumber> contacts = ContactsListPersistence.getContactList(context);
            
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
