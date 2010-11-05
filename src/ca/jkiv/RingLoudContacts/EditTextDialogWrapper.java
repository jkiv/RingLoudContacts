package ca.jkiv.RingLoudContacts;

import android.view.View;
import android.widget.EditText;

/**
 * EditTextDialogWrapper
 * 
 * Holds {@link EditText} and parent {@link View} for retrieving the value of a dialog with an EditText. 
 * 
 * @author Jon Kivinen <android@jkiv.ca>
 */
class EditTextDialogWrapper
{
	EditText editText = null;
	View parentView = null;
	
	public EditTextDialogWrapper(View parentView)
	{
		this.parentView = parentView;
		
		if (parentView != null)
		{
			this.editText = (EditText) parentView.findViewById(R.id.EditTextDialog_EditText);
		}
	}
	
	public String getText()
	{
		if (this.editText != null)
		{
			return editText.getText().toString();
		}
		
		return "";
	}
}
