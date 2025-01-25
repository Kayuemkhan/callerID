package com.chromatics.caller_id.ui.contacts

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.database.Cursor
import android.provider.ContactsContract
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ContactsViewModel : ViewModel() {

    private val _contacts = MutableLiveData<List<String>>()
    val contacts: LiveData<List<String>> get() = _contacts

    @SuppressLint("Range")
    fun loadContacts(contentResolver: ContentResolver) {
        val contactsList = mutableListOf<String>()

        val cursor: Cursor? = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME),
            null,
            null,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        )

        cursor?.let {
            while (it.moveToNext()) {
                val name = it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                contactsList.add(name)
            }
            it.close()
        }

        _contacts.value = contactsList
    }
}
