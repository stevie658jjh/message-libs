package com.color.sms.messages.theme.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;

import com.color.sms.messages.theme.R;
import com.color.sms.messages.theme.databinding.DialogDeleteConversationBinding;
import com.color.sms.messages.theme.utils.MessageUtils;

public class DeleteConversationDialog extends Dialog {
    private DialogDeleteConversationBinding binding;
    private DeleteConversationListener listener;
    private long mThreadId;

    public DeleteConversationDialog(@NonNull Activity context, long mThreadId, DeleteConversationListener listener) {
        super(context);
        this.mThreadId = mThreadId;
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        binding = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.dialog_delete_conversation, null, false);
        setContentView(binding.getRoot());
        Window window = this.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.CENTER;
        wlp.width = WindowManager.LayoutParams.MATCH_PARENT;
        wlp.flags &= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        window.setAttributes(wlp);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        this.getWindow().setAttributes(wlp);
        initViewDialog();
    }

    private void initViewDialog() {
        binding.btnCancel.setOnClickListener(v -> onCancelClick());
        binding.btnOk.setOnClickListener(v -> onOkClick());
    }

    private void onCancelClick() {
        cancel();
    }

    private void onOkClick() {
        binding.progressLoading.setVisibility(View.VISIBLE);
        MessageUtils.deleteConversation(mThreadId);
        new Handler().postDelayed(() -> {
            listener.onDelete();
            cancel();
        }, 1500);
    }

    public interface DeleteConversationListener {
        void onDelete();
    }
}
