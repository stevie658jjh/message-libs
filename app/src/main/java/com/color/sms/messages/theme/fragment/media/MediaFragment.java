package com.color.sms.messages.theme.fragment.media;


import android.Manifest;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.snackbar.Snackbar;
import com.sucho.placepicker.AddressData;
import com.sucho.placepicker.Constants;
import com.sucho.placepicker.MapType;
import com.sucho.placepicker.PlacePicker;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import com.color.sms.messages.theme.R;
import com.color.sms.messages.theme.activity.NewMessageActivity;
import com.color.sms.messages.theme.adapter.GifCategoryAdapter;
import com.color.sms.messages.theme.adapter.ImageAdapter;
import com.color.sms.messages.theme.adapter.ImageGifAdapter;
import com.color.sms.messages.theme.base.BaseActivity;
import com.color.sms.messages.theme.base.BaseFragment;
import com.color.sms.messages.theme.databinding.FragmentMediaBinding;
import com.color.sms.messages.theme.model.Contact;
import com.color.sms.messages.theme.model.ImageModel;
import com.color.sms.messages.theme.utils.DateTimeUtility;
import com.color.sms.messages.theme.utils.PermissionsUtils;

import static android.content.Context.LOCATION_SERVICE;

/**
 * A simple {@link Fragment} subclass.
 */
public class MediaFragment extends BaseFragment implements
        GifCategoryAdapter.GifCategorySelectListener, ImageAdapter.ImageSelectListener, ImageGifAdapter.ImageGifSelectListener {
    private static final int PLACE_PICKER_REQUEST = 1521;
    private static final int ATTACH_FILE_PICKER = 1522;
    private static final int REQUEST_GET_CONTACT = 212;
    private FragmentMediaBinding binding;
    private GradientDrawable gradientDrawable;
    private GoogleMap mMaps;
    private Location currentLocation;
    private MediaRecorder recorder;
    private int mTimeRecord; // For timer
    private boolean isRecording;

    private long timeStartRecord = -1; // for checking
    private boolean isStopRecord;

    private MediaListener listener;
    private String audioFile;

    public MediaFragment() {
        // Required empty public constructor
    }

    public void setListener(MediaListener listener) {
        this.listener = listener;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_media, container, false);
        binding.setFr(this);
        initView();

        setupGifList();
        setupImageList();

        setUpMaps();
        setUpMic();

        return binding.getRoot();
    }

    private void setUpMaps() {
        if (PermissionsUtils.hasPermissions(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)) {
            binding.btnTurnOnLocation.setVisibility(View.GONE);
            showMaps();
        } else {
            binding.btnTurnOnLocation.setVisibility(View.VISIBLE);
            binding.map.setVisibility(View.GONE);
        }
    }

    private void setUpMic() {
        if (PermissionsUtils.hasPermissions(getContext(), Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            binding.btnTurnOnMic.setVisibility(View.GONE);
            binding.btnRecord.setVisibility(View.VISIBLE);
            binding.btnRecord.setOnTouchListener(btnRecordOnTouch(binding.btnRecord));
        } else {
            binding.btnTurnOnMic.setVisibility(View.VISIBLE);
            binding.btnRecord.setVisibility(View.GONE);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private View.OnTouchListener btnRecordOnTouch(View view) {
        return (v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    timeStartRecord = System.currentTimeMillis();
                    isStopRecord = false;
                    startRecording();
                    return true;
                case MotionEvent.ACTION_UP:
                    if (timeStartRecord != -1 && System.currentTimeMillis() - timeStartRecord > 1000) {
                        stopRecording();
                        break;
                    } else {
                        isStopRecord = true;
                    }
            }
            return false;
        };
    }

    @SuppressLint("MissingPermission")
    private void showMaps() {
        binding.btnTurnOnLocation.setVisibility(View.GONE);
        binding.map.setVisibility(View.VISIBLE);
        binding.mapOverplay.setVisibility(View.VISIBLE);

        LocationListener mLocationListener = new MyLocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                currentLocation = location;
                setMapLocation(currentLocation);
            }

            @Override
            public void onProviderDisabled(String provider) {
                Snackbar.make(binding.getRoot(), "Please Enable GPS and Internet", Snackbar.LENGTH_LONG);
            }
        };

        LocationManager mLocationManager = (LocationManager) getBaseActivity().getSystemService(LOCATION_SERVICE);
        if (mLocationManager != null && PermissionsUtils.hasPermissions(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)) {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000,
                    20, mLocationListener);
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000,
                    20, mLocationListener);
        }

        new Handler().postDelayed(() -> {
            SupportMapFragment supportMapFragment = new SupportMapFragment();
            FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.map, supportMapFragment);
            fragmentTransaction.commit();
            supportMapFragment.getMapAsync(googleMap -> {
                mMaps = googleMap;
                setMapLocation(currentLocation);
            });
        }, 1000);
    }

    private void setMapLocation(Location location) {
        if (mMaps == null) return;
        if (location == null) {
            LatLng latLng = new LatLng(16.0471659, 108.1716865);
            mMaps.addMarker(new MarkerOptions().position(latLng).title(""));
            mMaps.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f));
            return;
        }
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
        String adrName = "";
        try {
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            Address obj = addresses.get(0);
            adrName = obj.getAddressLine(0);
        } catch (IOException ignored) {
        }
        mMaps.addMarker(new MarkerOptions().position(latLng).title(adrName));
        mMaps.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f));
    }

    private void initView() {
        gradientDrawable = new GradientDrawable();
        gradientDrawable.setCornerRadius(30.0f);
        gradientDrawable.setShape(GradientDrawable.RECTANGLE);

        binding.container.post(() -> binding.container.setLayoutParams(new LinearLayoutCompat.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, BaseActivity.sKeyBoardHeight)));
    }

    private void setupImageList() {
        ImageAdapter adapter = new ImageAdapter(getContext(), this);
        binding.rvImage.setAdapter(adapter);
    }

    private void setupGifList() {
        GifCategoryAdapter adapter = new GifCategoryAdapter(getContext(), this);
        binding.rvGif.setAdapter(adapter);
    }

    @Override
    public void onImageGifCategorySelected(ImageModel.Gif imageCategoryModel) {
        ImageGifAdapter adapter = new ImageGifAdapter(getContext(), imageCategoryModel.getName(), this);
        binding.rvGif.setAdapter(adapter);
    }

    private void makeCircle(View view) {
        ObjectAnimator cornerAnimation =
                ObjectAnimator.ofFloat(gradientDrawable, "cornerRadius", 30f, 200.0f);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setDuration(500);
        animatorSet.play(cornerAnimation);
        animatorSet.start();
    }

    private void makeSquare(View view) {
        ObjectAnimator cornerAnimation =
                ObjectAnimator.ofFloat(gradientDrawable, "cornerRadius", 200.0f, 30f);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setDuration(500);
        animatorSet.play(cornerAnimation);
        animatorSet.start();
    }


    public void grantLocationPermissions() {
        checkUserPermission(new BaseActivity.PermissionListener() {
            @Override
            public void OnAcceptedAllPermission() {
                showMaps();
            }

            @Override
            public void OnCancelPermission() {

            }

            @Override
            public void OnNeverRequestPermission() {

            }
        }, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION});
    }

    public void grantMicPermissions() {
        checkUserPermission(new BaseActivity.PermissionListener() {
            @Override
            public void OnAcceptedAllPermission() {
                setUpMic();
            }

            @Override
            public void OnCancelPermission() {

            }

            @Override
            public void OnNeverRequestPermission() {

            }
        }, new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE});
    }

    public void openPlacePicker() {
        LatLng latLng;
        if (currentLocation == null) {
            latLng = new LatLng(16.0471659, 108.1716865);
        } else
            latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        try {
            Intent intent = new PlacePicker.IntentBuilder()
                    .setLatLong(latLng.latitude, latLng.longitude)  // Initial Latitude and Longitude the Map will load into
                    .showLatLong(false)  // Show Coordinates in the Activity
                    .setAddressRequired(false) // Set If return only Coordinates if cannot fetch Address for the coordinates. Default: True
                    .hideMarkerShadow(true) // Hides the shadow under the map marker. Default: False
                    .setMarkerImageImageColor(R.color.colorPrimary)
                    .disableBootomSheetAnimation(true)
                    .setMapType(MapType.TERRAIN)
                    .build(getBaseActivity());
            startActivityForResult(intent, Constants.PLACE_PICKER_REQUEST);
        } catch (Exception ignore) {
        }
    }

    public void onSelectAttachFile() {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.setType("*/*");
        startActivityForResult(i, ATTACH_FILE_PICKER);
    }

    public void onSelectContact() {
        startActivityForResult(new Intent(getContext(), NewMessageActivity.class), REQUEST_GET_CONTACT);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            if (requestCode == Constants.PLACE_PICKER_REQUEST) {
                AddressData addressData = data.getParcelableExtra(Constants.ADDRESS_INTENT);
                if (addressData != null && listener != null) {
                    listener.onAddressAdded(addressData);
                }
            }

            if (requestCode == REQUEST_GET_CONTACT) {
                Contact contact = data.getParcelableExtra(com.color.sms.messages.theme.utils.Constants.PUT_OBJ);
                listener.onContactAdded(contact);
            }

            if (requestCode == ATTACH_FILE_PICKER) {
                String attachFileAdded = data.getDataString();
                if (listener != null) {
                    listener.onAttachFileAdded(attachFileAdded);
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void startRecording() {
        if (isRecording) {
            stopRecording();
            return;
        }
        new Handler().postDelayed(() -> {
            if (isStopRecord) return;
            audioFile = getFilename();
            recorder = new MediaRecorder();
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            recorder.setOutputFile(audioFile);
            try {
                recorder.prepare();
                recorder.start();
                isRecording = true;
                binding.tvTimer.setText(DateTimeUtility.formatTime(0));
                timeUp();
            } catch (IllegalStateException | IOException e) {
                e.printStackTrace();
            }
        }, 1000);
    }

    private void stopRecording() {
        try {
            isRecording = false;
            binding.tvTimer.setText(getString(R.string.touch_and_hold));
            if (null != recorder) {
                if (listener != null && audioFile != null) {
                    listener.onAudioAdded(audioFile);
                }
                recorder.stop();
                recorder.reset();
                recorder.release();
                recorder = null;
                audioFile = null;
            }
        } catch (Exception ignore) {
        }
    }

    private void timeUp() {
        new Handler().postDelayed(() -> {
            mTimeRecord += 1;
            if (isRecording) {
                binding.tvTimer.setText(DateTimeUtility.formatTime(mTimeRecord * 1000));
                timeUp();
            } else {
                // reset value
                mTimeRecord = 0;
                isStopRecord = false;
            }
        }, 1000);
    }

    private String getFilename() {
        String filepath = getBaseActivity().getCacheDir().getPath();
        File file = new File(filepath, "audioRecorder");
        if (!file.exists()) {
            file.mkdirs();
        }
        return (file.getAbsolutePath() + "/" + System.currentTimeMillis() + ".amr");
    }

    @Override
    public void onImageSelected(String path) {
        if (listener != null) {
            listener.onImageAdded(path);
        }
    }

    @Override
    public void onImageGifSelected(String path) {
        if (listener != null) {
            listener.onImageGifAdded(path);
        }
    }

    public interface MediaListener {
        void onImageAdded(String path);

        void onImageGifAdded(String path);

        void onAudioAdded(String audioFile);

        void onAttachFileAdded(String filePath);

        void onContactAdded(Contact contact);

        void onAddressAdded(AddressData addressData);
    }
}
