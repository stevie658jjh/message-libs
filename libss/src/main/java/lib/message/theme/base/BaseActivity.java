package lib.message.theme.base;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.provider.Telephony;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;

import lib.message.theme.MainActivity;
import lib.message.theme.R;
import lib.message.theme.utils.Function;

public abstract class BaseActivity extends AppCompatActivity {
    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    public static final String TAG = "TAG =>>";
    public static int sKeyBoardHeight = Function.getScreenHeight() / 3;

    public static final int REQUEST_PERMISSION_SMS = 777;
    private static final int ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE = 2311;
    private PermissionListener permissionListener;
    private PermissionOverlayListener permissionOverlayListener;
    private long mLastClickTime;

    protected boolean isDefaultSmsApp() {
        return getPackageName().equals(Telephony.Sms.getDefaultSmsPackage(this));
    }

    protected void showDefaultSmsDialog() {
        Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
        if (!Telephony.Sms.getDefaultSmsPackage(this).equals(getPackageName())) {
            intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, getPackageName());
        }
        this.startActivityForResult(intent, REQUEST_PERMISSION_SMS);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            setTheme(R.style.DarkTheme);
        } else {
            setTheme(R.style.LightTheme);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isDefaultSmsApp() && !(getParent() instanceof MainActivity)) {
            showDefaultSmsDialog();
        }
    }

    @SuppressLint("WrongConstant")
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == 0) {

            View localView = getCurrentFocus();

            // This code using to close Keyboard when we click outside of EditText
            if ((localView instanceof EditText)) {
                Object localObject = new Rect();
                localView.getGlobalVisibleRect((Rect) localObject);
                if (!((Rect) localObject).contains((int) ev.getRawX(), (int) ev.getRawY())) {
                    localView.clearFocus();
                    localObject = getSystemService("input_method");
                    assert (localObject != null);
                    ((InputMethodManager) localObject).hideSoftInputFromWindow(localView.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    public void showToast(String s) {
        // TODO create custom Toast layout
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    public void openActivity(Class<?> destination) {
        startActivity(new Intent(this, destination));
    }

    public boolean canClick() {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
            return false;
        }
        mLastClickTime = SystemClock.elapsedRealtime();
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void openOverlaySetting() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getPackageName()));
        startActivityForResult(intent, ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE);
    }

    /**
     * checkUserPermission runtime
     *
     * @param paramPermissionListener PermissionListener
     * @param paramArrayOfString      any permission want to ask the user
     */
    public void checkUserPermission(@NonNull PermissionListener paramPermissionListener, @NonNull String[] paramArrayOfString) {
        if (paramArrayOfString.length == 0)
            return;

        this.permissionListener = paramPermissionListener;
        if (Build.VERSION.SDK_INT >= 23) {
            ActivityCompat.requestPermissions(this, paramArrayOfString, 200);
        } else {
            this.permissionListener.OnAcceptedAllPermission();
        }
    }

    public void showKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    public void closeKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (permissions.length == 0 || grantResults.length == 0) return;
        int numRequestCode = 0;
        do {
            if ((!shouldShowRequestPermissionRationale(permissions[numRequestCode])) && (grantResults[numRequestCode] != 0)) {
                if (permissionListener != null) {
                    permissionListener.OnNeverRequestPermission();
                }
            } else if (grantResults[numRequestCode] != 0) {
                if (this.permissionListener == null) {
                    break;
                }
                this.permissionListener.OnCancelPermission();
                return;
            } else if (permissions.length == grantResults.length) {
                this.permissionListener.OnAcceptedAllPermission();
            }
            numRequestCode += 1;
        } while (this.permissionListener == null);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Check permission draw overlay
        if (requestCode == ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(this) && permissionOverlayListener != null) {
                    permissionOverlayListener.OnNotAccepted();
                } else if (permissionOverlayListener != null) {
                    permissionOverlayListener.OnAccepted();
                }
            }
        }
    }

    @Override
    public void finish() {
        putInt(this, "keyBack1102231i", 0);
        super.finish();
    }

    public void backClicked(View view) {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public static int getInt(Context context, final String key, int defaultValue) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getInt(key, defaultValue);
    }

    public static void putInt(Context context, String key, int value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putInt(key, value).apply();
    }

    public interface PermissionActionListener {
        void OnAccepted();
    }

    public interface PermissionListener {
        void OnAcceptedAllPermission();

        void OnCancelPermission();

        void OnNeverRequestPermission();
    }

    public interface PermissionOverlayListener extends PermissionActionListener {
        void OnNotAccepted();
    }
}
