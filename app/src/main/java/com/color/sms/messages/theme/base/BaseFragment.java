package com.color.sms.messages.theme.base;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class BaseFragment extends Fragment {
    protected static final String TAG = "Log =>>> ";
    protected BaseActivity shareit_mBaseActivity;

    protected boolean mIsDestroyed;

    public void showToast(String text) {
        shareit_mBaseActivity.showToast(text);
    }

    public BaseActivity getBaseActivity() {
        return shareit_mBaseActivity;
    }

    protected void openActivity(Class<?> destination) {
        shareit_mBaseActivity.openActivity(destination);
    }

    protected void startService(Intent intent) {
        shareit_mBaseActivity.startService(intent);
    }

    protected void checkUserPermission(@NonNull BaseActivity.PermissionListener paramPermissionListener, @NonNull String[] paramArrayOfString) {
        shareit_mBaseActivity.checkUserPermission(paramPermissionListener, paramArrayOfString);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        shareit_mBaseActivity = (BaseActivity) getActivity();
    }


    @Override
    public void onResume() {
        super.onResume();
        mIsDestroyed = false;
        if (shareit_mBaseActivity == null) {
            shareit_mBaseActivity = (BaseActivity) getActivity();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mIsDestroyed = true;
    }

    public boolean canClick() {
        if (shareit_mBaseActivity == null) return false;
        return shareit_mBaseActivity.canClick();
    }
}
