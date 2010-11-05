package ca.jkiv.RingLoudContacts;

import java.util.LinkedList;
import java.util.List;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.text.InputType;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

/**
 * ContactListActivity
 * 
 * The activity for editing your contact list.
 * 
 * @author Jon Kivinen <android@jkiv.ca>
 */
public class ContactListActivity extends ListActivity
{
    private static final String LOG_TAG = "ContactListActivity";
    List<PhoneNumber> listData;
    
    private static final int CHOOSE_CONTACT_RESULT = 1001;
    
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Populate list view
        listData = ContactListPersistence.getContactList(this);

        // Register context menu
        registerForContextMenu(getListView());
        
        // Create an array adapter for listData
        ArrayAdapter<PhoneNumber> adapter = new ArrayAdapter<PhoneNumber>(this, android.R.layout.simple_list_item_1, listData);

        // Create observer to update SharedPreferences when the list changes
        adapter.registerDataSetObserver(new DataSetObserver()
        {
            @Override
            public void onChanged()
            {
                // Update shared preferences
                ContactListPersistence.setContactList(ContactListActivity.this, listData);
            }
        });
        
        // Set list adapter
        setListAdapter(adapter);
        
        if (listData.size() == 0)
        {
        	Toast.makeText(this, R.string.ContactList_EmptyToastText, Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    /**
     * Edit a list item when it is clicked.
     */
    protected void onListItemClick (ListView l, View v, int position, long id)
    {
    	editNumberByDialog(id);
    }
    
    @Override
    /**
     * Called when context menu is created.
     */
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo)  
    {
        super.onCreateContextMenu(menu, view, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.contactlist_context_menu, menu);
        menu.setHeaderTitle("Context menu title");
    }
    
    @Override
    /**
     * Handle context menu item selected.
     */
    public boolean onContextItemSelected(MenuItem item)
    {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        
        switch (item.getItemId())
        {
          case R.id.ContactListContextMenu_Edit:
            editNumberByDialog(info.id);
            return true;
          case R.id.ContactListContextMenu_Delete:
            deleteListItem(info.id);
            return true;
          default:
            return super.onContextItemSelected(item);
        }
    }
    
    /**
     * Called when menu is first created.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.contactlist_menu, menu);
        return true;
    }
    
    @Override
    /**
     * Handle menu item selection.
     */
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle item selection
        switch (item.getItemId())
        {
          // Manually add an item to the list
          case R.id.ContactListMenu_Add:
            addNumberByDialog();
            return true;
            
          // Add a contact via contact picker
          case R.id.ContactListMenu_AddContact:
        	addNumberByContactPicker();
        	return true;
          
          // Clear the list
          case R.id.ContactListMenu_Clear:
        	  clearList();
        	  return true;
        	  
          default:
            return super.onOptionsItemSelected(item);
        }
    }
    
    /**
     * Delete list item with given id.
     */
    private void deleteListItem(long id)
    {
        if (id >= 0 && id < listData.size())
        {
            listData.remove((int) id);
            
            ArrayAdapter<PhoneNumber> adapter = (ArrayAdapter<PhoneNumber>) getListAdapter();
            adapter.notifyDataSetChanged();
            
            if (listData.size() > 0)
            {
            	Toast.makeText(this, R.string.ContactList_RemovedNumberToastText, Toast.LENGTH_SHORT).show();
            }
            else
            {
            	Toast.makeText(this, R.string.ContactList_EmptyToastText, Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Edit list item with given id.
     */
    private void editNumberByDialog(final long id)
    {
        // Inflate layout
        LayoutInflater inflater= LayoutInflater.from(this);
        View editTextView = inflater.inflate(R.layout.edittextdialog, null);
        
        // Set the EditText properties
        EditText editText = (EditText) editTextView.findViewById(R.id.EditTextDialog_EditText); 
        editText.setInputType(InputType.TYPE_CLASS_PHONE);
        if (id < listData.size())
        {
        	editText.setText(listData.get((int) id));
        }
        
        // Wrap layout so we can access it later
        final EditTextDialogWrapper wrapper = new EditTextDialogWrapper(editTextView);
        
        // Show dialog
		new AlertDialog.Builder(this)
			.setTitle(R.string.ContactList_EditNumberTitleText)
			.setIcon(android.R.drawable.ic_menu_call)
			.setView(editTextView)
			.setPositiveButton(R.string.Dialog_OKText, new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int whichButton)
				{
					changeNumberAtIndex(id, wrapper.getText());
				}
			})
			.setNegativeButton(R.string.Dialog_CancelText, null)
			.show();
    }
    
    /**
     * Start contact picker activity.
     */
    private void addNumberByContactPicker()
    {
        Intent contactPickerIntent = new Intent(Intent.ACTION_PICK, Contacts.CONTENT_URI);
        startActivityForResult(contactPickerIntent, CHOOSE_CONTACT_RESULT);
    }
    
    /**
     * Start 
     */
    private void addNumberByDialog()
    {
    	// Inflate layout
        LayoutInflater inflater= LayoutInflater.from(this);
        View editTextView = inflater.inflate(R.layout.edittextdialog, null);
        
        // Set the EditText type to phone number
        ((EditText) editTextView.findViewById(R.id.EditTextDialog_EditText)).setInputType(InputType.TYPE_CLASS_PHONE);
        
        // Wrap layout so we can access it later
        final EditTextDialogWrapper wrapper = new EditTextDialogWrapper(editTextView);
        
        // Show dialog
		new AlertDialog.Builder(this)
			.setTitle(R.string.ContactList_AddNumberTitleText)
			.setIcon(android.R.drawable.ic_menu_call)
			.setView(editTextView)
			.setPositiveButton(R.string.Dialog_OKText, new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int whichButton)
				{
					addNumber(wrapper.getText());
				}
			})
			.setNegativeButton(R.string.Dialog_CancelText, null)
			.show();
    }
    
    /**
     * Called when an activity returns with a result.
     */
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (resultCode == RESULT_OK)
		{
			switch (requestCode)
			{
			  // Contact-choosing Activity returned result
			  case CHOOSE_CONTACT_RESULT:
				Cursor cursor = null;
				String phoneNumber = "";
				
				try
				{
					Uri result = data.getData();
					
					// get the contact id from the Uri
					String id = result.getLastPathSegment();
					
					// query for everything email
					cursor = getContentResolver().query(Phone.CONTENT_URI, null, Phone.CONTACT_ID + "=?", new String[] { id }, null);
					
					int phoneIdx = cursor.getColumnIndex(Phone.DATA);
					
					// let's just get the first email
					if (cursor.moveToFirst())
					{						
						if (cursor.getCount() > 1)
						{
							LinkedList<CharSequence> numbers = new LinkedList<CharSequence>();
							
							// Add all the numbers
							do
							{
								numbers.addLast(cursor.getString(phoneIdx));
							} while(cursor.moveToNext());
							
							// Ask the user to select one of the numbers
							AlertDialog.Builder builder = new AlertDialog.Builder(this);
							builder.setTitle(R.string.ContactList_WhichNumberTitleText)
								   .setSingleChoiceItems(numbers.toArray(new CharSequence[0]), 0, null)
								   .setPositiveButton("OK", new DialogInterface.OnClickListener()
								   {
									    // User clicked "OK" 
					                    public void onClick(DialogInterface dialog, int whichButton)
					                    {
					                    	// Get the item
					                    	ListView listView = ((AlertDialog) dialog).getListView();
					                    	int selectedItem = listView.getCheckedItemPosition();
					                    	
					                    	// Add the item to the list
					                    	if (selectedItem != ListView.INVALID_POSITION)
					                    	{
					                    		String selectedPhoneNumber = (String) listView.getItemAtPosition(selectedItem);
						                    	addNumber(selectedPhoneNumber);
					                    	}
					                    }
								   }).create();
							
							builder.show();
						}
						else
						{
							// One phone number for contact, add it.
							phoneNumber = cursor.getString(phoneIdx);
							addNumber(phoneNumber);
						}
					}
					else
					{
						// No phone number for contact
						Toast.makeText(this, R.string.ContactList_EmptyToastText, Toast.LENGTH_LONG).show();
					}
				}
				catch (Exception e)
				{
					Log.e(LOG_TAG, "Failed to get contact data.", e);
				}
				finally
				{
					if (cursor != null)
					{
						cursor.close();
					}
				}
				
				break;
			}
		}
		else
		{
			Log.e(LOG_TAG, "Contact-choosing activity not OK.");
		}
	}
    
	/**
	 * Given a phone number, try to add it to the list.
	 * @param phoneNumber The phone number to be added.
	 */
	private void addNumber(String phoneNumberString)
	{
		if (phoneNumberString == null || phoneNumberString.length() == 0) return;
		
		PhoneNumber phoneNumber = new PhoneNumber(phoneNumberString);
		
		if (listData.contains(phoneNumber))
        {
			// Number already in list
            Toast.makeText(this, R.string.ContactList_NumberExistsToastText, Toast.LENGTH_SHORT).show();
        }
        else
        {
            // New item, add it to the list
            listData.add(phoneNumber);

            // Notify of changes to underlying data
            ArrayAdapter<PhoneNumber> adapter = (ArrayAdapter<PhoneNumber>) getListAdapter();
            adapter.notifyDataSetChanged();

            Toast.makeText(this, "Added number '" + phoneNumber + "'", Toast.LENGTH_SHORT).show();
        }
	}
	
	private void changeNumberAtIndex(long id, String text)
	{
		PhoneNumber previousPhoneNumber = listData.get((int) id);
		PhoneNumber newPhoneNumber = new PhoneNumber(text);
		
        // Replace item at `id` with `text`
        listData.set((int) id, newPhoneNumber);
        
        // Notify of data change
        ArrayAdapter<PhoneNumber> adapter = (ArrayAdapter<PhoneNumber>) getListAdapter();
        adapter.notifyDataSetChanged();
	}
	

    private void clearList()
    {
    	// Ask the user if we should clear the list
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        
        builder.setMessage(R.string.ContactList_AreYouSureText)
        	   .setTitle(R.string.ContactList_AreYouSureTitleText)
        	   .setIcon(android.R.drawable.ic_dialog_alert)
        	   .setNegativeButton(R.string.Dialog_NoText, null)
        	   .setPositiveButton(R.string.Dialog_YesText, new OnClickListener()
        	   {
					public void onClick(DialogInterface dialog, int which)
					{
				    	// Clear numbers and primary contact
				    	listData.clear();
				    	
				    	// Update adapter
				    	ArrayAdapter<PhoneNumber> adapter = (ArrayAdapter<PhoneNumber>) getListAdapter();
						adapter.notifyDataSetChanged();
						
						Toast.makeText(ContactListActivity.this, R.string.ContactList_EmptyToastText, Toast.LENGTH_SHORT).show();
					}
        	   }).show();
    }
}
