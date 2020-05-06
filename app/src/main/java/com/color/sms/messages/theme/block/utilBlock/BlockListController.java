package com.color.sms.messages.theme.block.utilBlock;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import java.util.ArrayList;

import com.color.sms.messages.theme.utils.MyApplication;

public class BlockListController {
    private static SQLiteDatabase db;

    public BlockListController() {
        if (db == null) {
            SQLiteOpenHelper helper = new DBHelper(MyApplication.getInstance(), "TrackerActivity", null, 1);
            db = helper.getWritableDatabase();
        }
    }

    public boolean isInBlacklist(String phoneNumber) {
        if (!TextUtils.isEmpty(phoneNumber)) {
            ArrayList<String> strings = getAllBlacklist();
            return strings.contains(phoneNumber.replaceAll(" ", ""));
        }
        return false;
    }

    private ArrayList<String> getAllBlacklist() {
        ArrayList<String> numberBacked = new ArrayList<>();
        try {
            if (db != null) {
                Cursor c = db.query("blklst_call", null, null, null, null, null, "ph_no desc");
                while (c.moveToNext()) {
                    String num = c.getString(0);
                    numberBacked.add(num.replaceAll(" ", ""));
                }
                c.close();
                return numberBacked;
            } else {
                return numberBacked;
            }
        } catch (Exception e) {
            return numberBacked;
        }
    }

    public boolean deleteOutOfBlacklist(String phoneNumber) {
        try {
            if (!phoneNumber.equals("")) {
                String[] args = new String[1];
                args[0] = phoneNumber;
                db.delete("blklst_call", "ph_no = ? ", args);
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    public boolean addIntoBlacklist(String phoneNumber) {
        try {
            if (!phoneNumber.equals("")) {
                ContentValues val = new ContentValues();
                val.put("ph_no", phoneNumber);
                val.put("ct", "0");
                db.insert("blklst_call", null, val);
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }
}
