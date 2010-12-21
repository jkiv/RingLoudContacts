package ca.jkiv.RingLoudContacts;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.PhoneLookup;
import android.text.InputType;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * The activity for editing the contacts list.
 * 
 * @author Jon Kivinen <android@jkiv.ca>
 */
public class ContactsListActivity extends ListActivity
{
    private static final String LOG_TAG = "ContactsListActivity";
    private static final int CHOOSE_CONTACT_RESULT = 1001;
    private List<PhoneNumber> listData;
    
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Set empty contact list layout
        setContentView(R.layout.contacts_list_empty);
        
        // Register context menu
        registerForContextMenu(getListView());

        // Populate list
        listData = ContactsListPersistence.getContactList(this);

        // Create an array adapter for listData
        ContactsListAdapter adapter = new ContactsListAdapter(this, listData);

        // Create observer to update SharedPreferences when the list changes
        adapter.registerDataSetObserver(new DataSetObserver()
        {
            @Override
            public void onChanged()
            {
                // Update shared preferences
                ContactsListPersistence.setContactList(ContactsListActivity.this, listData);
            }
        });
        
        // Set list adapter
        setListAdapter(adapter);
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
        inflater.inflate(R.menu.contacts_list_context_menu, menu);
        
        // Get the selected number
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
        PhoneNumber selectedNumber = listData.get((int) info.id);
        
        menu.setHeaderTitle(selectedNumber.toString());
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
          case R.id.ContactsList_ContextMenu_Edit:
            editNumberByDialog(info.id);
            return true;
          case R.id.ContactsList_ContextMenu_Delete:
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
        inflater.inflate(R.menu.contacts_list_menu, menu);
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
          case R.id.ContactsList_Menu_Add:
            addNumberFromDialog();
            return true;
            
          // Add a contact via contact picker
          case R.id.ContactsList_Menu_AddContact:
            addNumberFromContactPicker();
            return true;

          // Jump to settings activity
          case R.id.ContactsList_Menu_Settings:
              // Go to settings activity
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            
          // Clear the list
          case R.id.ContactsList_Menu_Clear:
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
            
            ContactsListAdapter adapter = (ContactsListAdapter) getListAdapter();
            adapter.notifyDataSetChanged();
            
            if (listData.size() > 0)
            {
                Toast.makeText(this, R.string.ContactsList_RemovedNumber, Toast.LENGTH_SHORT).show();
            }
            else
            {
                Toast.makeText(this, R.string.ContactsList_Empty, Toast.LENGTH_LONG).show();
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
        View editTextView = inflater.inflate(R.layout.edittext_dialog, null);
        
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
            .setTitle(R.string.ContactsList_EditNumberTitle)
            .setIcon(android.R.drawable.ic_menu_call)
            .setView(editTextView)
            .setPositiveButton(R.string.Dialog_OK, new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int whichButton)
                {
                    changeNumberAtIndex(id, wrapper.getText());
                }
            })
            .setNegativeButton(R.string.Dialog_Cancel, null)
            .show();
    }
    
    /**
     * Start contact picker activity.
     */
    private void addNumberFromContactPicker()
    {
        Intent contactPickerIntent = new Intent(Intent.ACTION_PICK, Contacts.CONTENT_URI);
        startActivityForResult(contactPickerIntent, CHOOSE_CONTACT_RESULT);
    }
    
    /**
     * Add a number to the contacts list manually
     */
    private void addNumberFromDialog()
    {
        // Inflate layout
        LayoutInflater inflater= LayoutInflater.from(this);
        View editTextView = inflater.inflate(R.layout.edittext_dialog, null);
        
        // Set the EditText type to phone number
        ((EditText) editTextView.findViewById(R.id.EditTextDialog_EditText)).setInputType(InputType.TYPE_CLASS_PHONE);
        
        // Wrap layout so we can access it later
        final EditTextDialogWrapper wrapper = new EditTextDialogWrapper(editTextView);
        
        // Show dialog
        new AlertDialog.Builder(this)
            .setTitle(R.string.ContactsList_AddNumberTitle)
            .setIcon(android.R.drawable.ic_menu_call)
            .setView(editTextView)
            .setPositiveButton(R.string.Dialog_OK, new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int whichButton)
                {
                    addNumber(wrapper.getText());
                }
            })
            .setNegativeButton(R.string.Dialog_Cancel, null)
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
                handleContactChooserResult(data);
                break;
              default:
                // Nothing
            }
        }
    }
    
    private void handleContactChooserResult(Intent data)
    {
        Cursor cursor = null;
        String phoneNumber = "";
        
        try
        {
            // Uri returned from contact chooser
            Uri contactResult = data.getData();
            
            // Get contact ID as string
            String contactId = contactResult.getLastPathSegment();
            
            // Query phone number for selected contact ID
            cursor = getContentResolver().query(Phone.CONTENT_URI,
                                                new String[] { Phone.NUMBER },
                                                Phone.CONTACT_ID + "=?",
                                                new String[] { contactId },
                                                null);
            
            int phoneNumberIdx = cursor.getColumnIndexOrThrow(Phone.NUMBER);
            
            // Get the first entry for the query
            if (cursor.moveToFirst())
            {
                // If there is more than one number, show a list and have the user select a number
                if (cursor.getCount() > 1)
                {
                    LinkedList<String> numbers = new LinkedList<String>();
                    
                    // Add all the numbers to a list
                    do
                    {
                        numbers.addLast(cursor.getString(phoneNumberIdx));
                    } while(cursor.moveToNext());
                    
                    // Ask the user to select one of the numbers
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle(R.string.ContactsList_SelectNumberTitle)
                           .setSingleChoiceItems(numbers.toArray(new String[0]), 0, null)
                           .setPositiveButton(R.string.Dialog_OK, new DialogInterface.OnClickListener()
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
                           })
                           .show();
                }
                else
                {
                    // One phone number for contact, add it.
                    phoneNumber = cursor.getString(phoneNumberIdx);
                    addNumber(phoneNumber);
                }
            }
            else
            {
                // No number found for contact
                Toast.makeText(this, R.string.ContactsList_NoNumberFound, Toast.LENGTH_SHORT).show();
            }
        }
        catch (Exception e)
        {
            // Something went wrong
            Toast.makeText(this, R.string.ContactsList_CouldNotGetContactData, Toast.LENGTH_SHORT).show();
            
            Log.e(LOG_TAG, getString(R.string.ContactsList_CouldNotGetContactData));
        }
        finally
        {
            if (cursor != null)
                cursor.close();
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
            Toast.makeText(this, R.string.ContactsList_NumberAlreadyInList, Toast.LENGTH_SHORT).show();
        }
        else
        {
            // New item, add it to the list
            listData.add(phoneNumber);

            // Notify of changes to underlying data
            ContactsListAdapter adapter = (ContactsListAdapter) getListAdapter();
            adapter.notifyDataSetChanged();

            Toast.makeText(this, this.getString(R.string.ContactsList_AddedNumber) + " '" + phoneNumber + "'", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void changeNumberAtIndex(long id, String text)
    {
        PhoneNumber newPhoneNumber = new PhoneNumber(text);
        
        // Replace item at `id` with `text`
        listData.set((int) id, newPhoneNumber);
        
        // Notify of data change
        ContactsListAdapter adapter = (ContactsListAdapter) getListAdapter();
        adapter.notifyDataSetChanged();
    }
    

    private void clearList()
    {
        // Ask the user if we should clear the list
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        
        builder.setMessage(R.string.ContactsList_AreYouSure)
               .setTitle(R.string.ContactsList_AreYouSureTitle)
               .setIcon(android.R.drawable.ic_dialog_alert)
               .setNegativeButton(R.string.Dialog_No, null)
               .setPositiveButton(R.string.Dialog_Yes, new OnClickListener()
               {
                    public void onClick(DialogInterface dialog, int which)
                    {
                        // Clear numbers and primary contact
                        listData.clear();
                        
                        // Update adapter
                        ContactsListAdapter adapter = (ContactsListAdapter) getListAdapter();
                        adapter.notifyDataSetChanged();
                        
                        Toast.makeText(ContactsListActivity.this, R.string.ContactsList_EmptyList, Toast.LENGTH_SHORT).show();
                    }
               }).show();
    }
    
    /**
     * A custom {@link ArrayAdapter} to change the appearance of certain special elements.
     */
    private class ContactsListAdapter extends ArrayAdapter<PhoneNumber>
    {
        private Context context;
        private final int DEFAULT_CONTACT_IMAGE = R.drawable.ic_contact_unknown;
        
        public ContactsListAdapter(Context context, List<PhoneNumber> phoneNumbers)
        {
            super(context, R.layout.contacts_list, phoneNumbers);
            
            this.context = context;
        }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            // Get phone number value
            
            View itemView = convertView;
            if (itemView == null) {
                LayoutInflater inflator = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                itemView = inflator.inflate(R.layout.contacts_list, parent, false);
            }
            
            // Get phone number for list item
            PhoneNumber phoneNumber = getItem(position);
            
            // Get layout items
            TextView nameTextView = (TextView) itemView.findViewById(R.id.ContactsList_ListItem_Name);
            TextView numberTextView = (TextView) itemView.findViewById(R.id.ContactsList_ListItem_Number);
            ImageView photoImageView = (ImageView) itemView.findViewById(R.id.ContactsList_ListItem_Photo);
            
            // Query contact data
            ContentResolver contentResolver = context.getContentResolver();
            
            Uri lookupUri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber.toString()));
            Uri contactUri = Contacts.lookupContact(contentResolver, lookupUri);
            
            // Values to put into views
            Bitmap photoBitmap = null;
            String displayName = "";
            
            // Get contact's photo
            if (contactUri != null)
            {
                InputStream photoInputStream = Contacts.openContactPhotoInputStream(contentResolver, contactUri);

                if (photoInputStream != null)
                    photoBitmap = BitmapFactory.decodeStream(photoInputStream);
            }
            
            // Get contact's display name
            if (contactUri != null)
            {
                Cursor cursor = contentResolver.query(contactUri, new String[]{ Contacts.DISPLAY_NAME }, null, null, null);
                
                try
                {
                    // Get display name from first element at cursor
                    if (cursor.moveToFirst())
                        displayName = cursor.getString(cursor.getColumnIndexOrThrow(Contacts.DISPLAY_NAME));
                }
                finally
                {
                    if (cursor != null)
                        cursor.close();
                }
            }
            
            // Set display picture
            if (photoBitmap != null)
            {
                photoImageView.setImageBitmap(photoBitmap);
            }
            else
            {
                photoImageView.setImageDrawable(context.getResources().getDrawable(DEFAULT_CONTACT_IMAGE));
            }
            
            // Set display name and phone number
            if (!displayName.equals(""))
            {
                nameTextView.setText(displayName);
                numberTextView.setText(phoneNumber.toString());
            }
            else
            {
                nameTextView.setText(phoneNumber.toString());
                numberTextView.setText(context.getString(R.string.ContactsList_UnknownContact));
            }
            
            return itemView;
        }
    }
}