package com.color.sms.messages.theme.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import com.color.sms.messages.theme.model.GroupMessage;
import com.color.sms.messages.theme.model.Message;

public class MessageGroupDBHelper extends SQLiteOpenHelper {

    private Context context;

    public static final String DATABASE_NAME = "group_message_list";
    private static final String TABLE_NAME = "group_message";
    private static final String TABLE_MESSAGE = "tb_message";
    private static final String ID = "groupId";
    private static final String GROUP_NAME = "groupName";
    private static final String DATE = "date";
    private static final String GROUP_NUMBER = "groupNumber";
    private static final String BODY = "body";
    private static final String ID_CONTACT = "id_contact";

    private static final String ID_MESSAGE = "id_message";
    private static final String ID_GROUP = "id_group";
    private static final String MESSAGE = "message";

    public MessageGroupDBHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
        Log.d("DBManager", "DBManager: ");
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String tbGroup = "CREATE TABLE " + TABLE_NAME + " (" +
                ID + " integer primary key, " +
                GROUP_NAME + " TEXT, " +
                DATE + " TEXT, " +
                GROUP_NUMBER + " TEXT," +
                ID_CONTACT + " TEXT," +
                BODY + " TEXT)";
        db.execSQL(tbGroup);

        String tbMessage = "CREATE TABLE " + TABLE_MESSAGE + " (" +
                ID_MESSAGE + " integer primary key, " +
                ID_GROUP + " INTEGER, " +
                DATE + " TEXT, " +
                MESSAGE + " TEXT)";
        db.execSQL(tbMessage);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGE);
        onCreate(db);
        Toast.makeText(context, "Drop successfully", Toast.LENGTH_SHORT).show();
    }

    public void addMessage(Message message) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ID_GROUP, message.getId());
        values.put(MESSAGE, message.getBody());
        values.put(DATE, message.getDate());

        db.insert(TABLE_MESSAGE, null, values);
        db.close();
    }

    public void addGroup(GroupMessage groupMessage) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(GROUP_NAME, groupMessage.getGroupName());
        values.put(GROUP_NUMBER, groupMessage.getGroupNumber());
        values.put(DATE, groupMessage.getDate());
        values.put(BODY, groupMessage.getBody());
        values.put(ID_CONTACT, groupMessage.getContactId());
        //Neu de null thi khi value bang null thi loi

        db.insert(TABLE_NAME, null, values);
        db.close();
    }

    public GroupMessage getGroupById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, new String[]{ID,
                        GROUP_NAME, DATE, GROUP_NUMBER, BODY}, ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        GroupMessage student = new GroupMessage(cursor.getString(1), cursor.getLong(2), cursor.getString(3), cursor.getString(4));
        cursor.close();
        db.close();
        return student;
    }

    public int update(GroupMessage groupMessage) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(GROUP_NAME, groupMessage.getGroupName());

        return db.update(TABLE_NAME, values, ID + "=?", new String[]{String.valueOf(groupMessage.getGroupId())});
    }

    public List<GroupMessage> getAllGroupMessage() {
        List<GroupMessage> messageList = new ArrayList<>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_NAME;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                GroupMessage groupMessage = new GroupMessage();
                groupMessage.setGroupId(cursor.getInt(0));
                groupMessage.setGroupName(cursor.getString(1));
                groupMessage.setDate(cursor.getLong(2));
                groupMessage.setGroupNumber(cursor.getString(3));
                groupMessage.setContactId(cursor.getString(4));
                groupMessage.setBody(cursor.getString(5));
                messageList.add(groupMessage);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return messageList;
    }

    public List<Message> getMessages(int groupId) {
        List<Message> messageList = new ArrayList<>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_MESSAGE + " WHERE " + ID_GROUP + " = '" + groupId + "' ";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Message message = new Message();
                message.setId(cursor.getInt(1));
                message.setDate(cursor.getLong(2));
                message.setBody(cursor.getString(3));
                messageList.add(message);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return messageList;
    }

    public void deleteGroup(GroupMessage groupMessage) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, ID + " = ?",
                new String[]{String.valueOf(groupMessage.getGroupId())});
        db.close();
    }

    public int getGroupCount() {
        String countQuery = "SELECT  * FROM " + TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        cursor.close();

        // return count
        return cursor.getCount();
    }

}