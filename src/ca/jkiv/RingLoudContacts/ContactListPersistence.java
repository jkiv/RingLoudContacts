package ca.jkiv.RingLoudContacts;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * ContactListPersistence
 * 
 * Methods for saving contact list to phone to remember.
 * 
 * @author Jon Kivinen <android@jkiv.ca>
 */
public class ContactListPersistence
{
	private static final String CONTACT_LIST_FILE = "RingLoudContacts";
	private static final String CONTACT_LIST_PREF = "";
	
	private static final String LIST_SEPARATOR = "|";
	private static final String LIST_SEPARATOR_EXPR = "\\|";
	
	private ContactListPersistence() {} // Class not meant to be instantiated

	/**
	 * Save list of phone numbers as contact list.
	 * @param context
	 * @param contacts
	 */
	public static void setContactList(Context context, List<PhoneNumber> contacts)
	{
		StringBuilder concatlist = new StringBuilder();
		
		for(PhoneNumber contact : contacts)
		{
			concatlist.append(contact.toString());
			concatlist.append(LIST_SEPARATOR);
		}
		
		SharedPreferences sharedPreferences = context.getSharedPreferences(CONTACT_LIST_FILE, 0);
		Editor editor = sharedPreferences.edit();
		editor.putString(CONTACT_LIST_PREF, concatlist.toString());
		editor.commit();
	}
	
	public static List<PhoneNumber> getContactList(Context context)
	{
		CopyOnWriteArrayList<PhoneNumber> contacts = new CopyOnWriteArrayList<PhoneNumber>();
		
		SharedPreferences sharedPreferences = context.getSharedPreferences(CONTACT_LIST_FILE, 0);
		String concatlist = sharedPreferences.getString(CONTACT_LIST_PREF, "");
		
		for(String contact : concatlist.split(LIST_SEPARATOR_EXPR))
		{
			if (contact.length() > 0)
				contacts.add(new PhoneNumber(contact));
		}
		
		return contacts;
	}
}
