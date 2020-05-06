package com.color.sms.messages.theme.dialog;

import android.content.Context;

/**
 * @author Thien Stevie
 * Created on 03/04/2019.
 */
public class MyAlertDialog {
    public static MyAlertDialog sShare = new MyAlertDialog();

    private MyAlertDialog() {
    }

    public DialogCustom dialogOnlyOk(Context context, String title, String message) {
        return dialogOnlyOk(context, title, message, null);
    }

    public DialogCustom dialogOnlyOk(Context context, String message) {
        return dialogOnlyOk(context, null, message, null);
    }

    public DialogCustom dialogOnlyOk(Context context, String message, DialogCustom.OnClickListener okOnClick) {
        return dialogOnlyOk(context, null, message, okOnClick);
    }

    public DialogCustom dialogOnlyOk(Context context, String title, String message, DialogCustom.OnClickListener okOnClick) {
        return dialog(context, true, title, message, okOnClick, null);
    }

    public DialogCustom dialog(Context context, String message, DialogCustom.OnClickListener okOnClick) {
        return dialog(context, null, message, okOnClick);
    }

    public DialogCustom dialog(Context context, String title, String message, DialogCustom.OnClickListener okOnClick) {
        return dialog(context, false, title, message, okOnClick, null);
    }

    public DialogCustom dialog(Context context, String title, String message, DialogCustom.OnClickListener okOnClick, DialogCustom.OnClickListener cancelClick) {
        return dialog(context, false, title, message, okOnClick, cancelClick);
    }

    public DialogCustom dialog(Context context, boolean isOnlyOk, String title, String message, DialogCustom.OnClickListener okOnClick, DialogCustom.OnClickListener cancelOnClick) {
        DialogCustom dialog = new DialogCustom(context);

        dialog.setTextTitle(title)
                .setContent(message)
                .hideTitle(title == null)
                .isOnlyOk(isOnlyOk);

        if (cancelOnClick != null) {
            dialog.setCancelOnClick(cancelOnClick);
        } else {
            dialog.setCancelOnClick(v -> dialog.dismiss());
        }

        if (okOnClick != null) {
            dialog.setOkOnClick(okOnClick);
        } else {
            dialog.setOkOnClick(v -> dialog.dismiss());
        }

        return dialog;
    }
}