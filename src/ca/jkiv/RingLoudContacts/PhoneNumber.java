package ca.jkiv.RingLoudContacts;

import android.telephony.PhoneNumberUtils;

/**
 * A string wrapper which uses PhoneNumberUtils to compare.
 * 
 * @author Jon Kivinen <android@jkiv.ca>
 * @see PhoneNumberUtils#compare(String, String);
 */
public class PhoneNumber implements CharSequence
{
    private String value = "";
    
    public PhoneNumber(String s)
    {
        setString(s);
    }

    public String toString()
    {
        return value;
    }
    
    public void setString(String s)
    {
        value = (s == null) ? "" : s; // Disallow this.value to be null
    }
    
    public boolean equals(Object o)
    {
        if (o == null) return value == null;
        
        // Compare a string straight up
        if (o instanceof String)
            return PhoneNumberUtils.compare(value, (String) o);
        
        // Compare with a string of another phone number
        if (o instanceof PhoneNumber)
            return PhoneNumberUtils.compare(value, ((PhoneNumber) o).toString());
        
        return false;
    }

    public char charAt(int index)
    {
        return value.charAt(index);
    }

    public int length()
    {
        return value.length();
    }

    public CharSequence subSequence(int start, int end)
    {
        return value.subSequence(start, end);
    }
}
