package com.color.sms.messages.theme.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.Objects;

import com.color.sms.messages.theme.R;

/**
 * @author Thien Stevie
 * Created on 03/04/2019.
 */
public class DialogCustom extends Dialog {
    private OnClickListener okOnClick;
    private OnClickListener cancelOnClick;
    private OnClickListener thirdOnclick;
    private CharSequence content, title;
    private String textOk, textCancel, textThird;
    private boolean hideTitle, isOnlyOk, isCancelAble;

    public DialogCustom(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        Objects.requireNonNull(getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setContentView(R.layout.alert_dialog_layout);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        setCancelable(isCancelAble);
        TextView tvContent = findViewById(R.id.tvContent);
        TextView tvTitle = findViewById(R.id.tvTitle);

        TextView btnOk = findViewById(R.id.btnOk);
        TextView btnCancel = findViewById(R.id.btnCancel);
        TextView btnThird = findViewById(R.id.btnThird);

        if ((hideTitle || tvTitle == null) && tvTitle != null) {
            tvTitle.setVisibility(View.GONE);
        }

        if (isOnlyOk && btnCancel != null) {
            btnCancel.setVisibility(View.GONE);
        }

        if (okOnClick != null && btnOk != null) {
            btnOk.setOnClickListener(v -> okOnClick.OnClick(DialogCustom.this));
        }
        if (cancelOnClick != null && btnCancel != null) {
            btnCancel.setOnClickListener(v -> cancelOnClick.OnClick(DialogCustom.this));
        }
        if (thirdOnclick != null && btnThird != null) {
            btnThird.setVisibility(View.VISIBLE);
            btnThird.setOnClickListener(v -> thirdOnclick.OnClick(DialogCustom.this));
        }

        if (tvContent != null && !TextUtils.isEmpty(content)) {
            tvContent.setText(content);
        }
        if (tvTitle != null && !TextUtils.isEmpty(title)) {
            tvTitle.setText(title);
        }

        if (btnCancel != null && !TextUtils.isEmpty(textCancel)) {
            btnCancel.setText(textCancel);
        }
        if (btnOk != null && !TextUtils.isEmpty(textOk)) {
            btnOk.setText(textOk);
        }
        if (btnThird != null && !TextUtils.isEmpty(textThird)) {
            btnThird.setText(textThird);
        }
    }

    public DialogCustom setContent(CharSequence content) {
        this.content = content;
        return this;
    }

    public DialogCustom setTextTitle(CharSequence title) {
        this.title = title;
        return this;
    }

    public DialogCustom setTextOk(String textOk) {
        this.textOk = textOk;
        return this;
    }

    public DialogCustom setTextCancel(String textCancel) {
        this.textCancel = textCancel;
        return this;
    }

    public DialogCustom setTextThird(String textThird) {
        this.textThird = textThird;
        return this;
    }

    public void setCancelAble(boolean cancelAble) {
        isCancelAble = cancelAble;
    }

    public DialogCustom setOkOnClick(OnClickListener okOnClick) {
        this.okOnClick = okOnClick;
        return this;
    }

    public DialogCustom setThirdOnclick(OnClickListener cancelOnClick) {
        this.thirdOnclick = cancelOnClick;
        return this;
    }


    public DialogCustom setCancelOnClick(OnClickListener cancelOnClick) {
        this.cancelOnClick = cancelOnClick;
        return this;
    }

    public DialogCustom hideTitle(boolean hideTitle) {
        this.hideTitle = hideTitle;
        return this;
    }

    public void isOnlyOk(boolean isOnlyOk) {
        this.isOnlyOk = isOnlyOk;
    }

    public interface OnClickListener {
        void OnClick(Dialog dialog);
    }
}
