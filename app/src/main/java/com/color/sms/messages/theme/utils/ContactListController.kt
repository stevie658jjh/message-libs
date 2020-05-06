package com.color.sms.messages.theme.utils

import android.annotation.SuppressLint
import android.os.AsyncTask
import android.provider.ContactsContract
import android.util.Log
import com.color.sms.messages.theme.base.BaseActivity
import com.color.sms.messages.theme.model.Contact


class ContactListController private constructor() {
    companion object {
        private var INSTANCE: ContactListController = ContactListController()

        @JvmStatic
        fun getInstance(): ContactListController {
            return INSTANCE
        }

        @JvmStatic
        fun getListContact(): List<Contact> {
            return Constants.contactList
        }
    }

    // only call this method once time in MyApplication
    fun init() {
        loadList()
    }

    @SuppressLint("StaticFieldLeak")
    private fun loadList() {
        object : AsyncTask<Void, Void, String>() {
            override fun doInBackground(vararg voids: Void): String {
                getContactList()
                return "abc"
            }
        }.execute()
    }


    @SuppressLint("Recycle")
    private fun getContactList() {
        if (Constants.contactList == null || Constants.contactList.size <= 0) {
            val cr = MyApplication.getInstance().contentResolver
            val cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null)
            val contacts = arrayListOf<Contact>()
            if ((cur?.count ?: 0) > 0) {
                while (cur!!.moveToNext()) {
                    val id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID))
                    val name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))

                    if (cur.getInt(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                        val pCur = cr.query(
                                ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                                arrayOf<String>(id), null)!!
                        while (pCur.moveToNext()) {
                            val phoneNo = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                            val photoUri = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI))
                            val type = pCur.getInt(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE))
                            val postal_uri = ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_URI
                            val postal_cursor = MyApplication.getInstance().contentResolver.query(postal_uri, null, ContactsContract.Data.CONTACT_ID + "=" + id, null, null)
                            Log.d(BaseActivity.TAG, "getContactList: $type")
                            assert(postal_cursor != null)
                            postal_cursor!!.close()
                            contacts.add(Contact(id,
                                    name,
                                    phoneNo, photoUri, type
                            ))
                        }
                        pCur.close()
                    }
                }
                cur.close()
                Constants.contactList = contacts
                Constants.sortList()
            }
        }
    }
}
