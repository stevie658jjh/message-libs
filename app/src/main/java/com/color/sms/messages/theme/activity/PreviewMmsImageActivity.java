package com.color.sms.messages.theme.activity;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.bumptech.glide.Glide;

import java.io.IOException;

import com.color.sms.messages.theme.R;
import com.color.sms.messages.theme.activity.search.model.Search;
import com.color.sms.messages.theme.databinding.ActivityPreviewMmsImageBinding;
import com.color.sms.messages.theme.utils.DateTimeUtility;
import com.color.sms.messages.theme.utils.MessageUtils;

public class PreviewMmsImageActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    private ActivityPreviewMmsImageBinding binding;

    private String addressIntent;
    private Uri imageIntent;
    private long dateIntent;
    private String typeImage;
    private SurfaceHolder surfaceHolder;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview_mms_image);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_preview_mms_image);

        getData();
        initView();
        setData();
        onClick();
    }

    private void onClick() {
        binding.btnBack.setOnClickListener(v -> finish());
        binding.imgPlayVideo.setOnClickListener(v -> {
            if (mediaPlayer == null)
                mediaPlayer = new MediaPlayer();

            try {
                mediaPlayer.setDataSource(PreviewMmsImageActivity.this, imageIntent);
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mediaPlayer.prepare();
                mediaPlayer.start();

                binding.rlThumbVideo.setVisibility(View.GONE);
                binding.videoView.setVisibility(View.VISIBLE);
                mediaPlayer.setOnCompletionListener(mp -> {
                    binding.rlThumbVideo.setVisibility(View.VISIBLE);
                    binding.videoView.setVisibility(View.GONE);
                    releaseMedia();
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        binding.videoView.setOnClickListener(v -> {
            binding.rlThumbVideo.setVisibility(View.VISIBLE);
            binding.videoView.setVisibility(View.GONE);
            releaseMedia();
        });
    }

    private void setData() {
        if (typeImage.contains(Search.NAME.Images.name())) {
            Glide.with(this).load(imageIntent).into(binding.imgPreview);
        } else {
            Glide.with(this).load(imageIntent).into(binding.imgBgVideo);
        }
        binding.tvDate.setText(DateTimeUtility.formatMessageTime(dateIntent));
        binding.tvAddress.setText(MessageUtils.getContactbyPhoneNumber(this, addressIntent));
    }

    private void getData() {
        typeImage = getIntent().getStringExtra("type");
        addressIntent = getIntent().getStringExtra("address");
        imageIntent = Uri.parse(getIntent().getStringExtra("image"));
        dateIntent = getIntent().getLongExtra("date", 0);
    }

    private void initView() {
        surfaceHolder = binding.videoView.getHolder();
        surfaceHolder.addCallback(this);

        if (typeImage.contains(Search.NAME.Images.name())) {
            binding.rlVideo.setVisibility(View.GONE);
            binding.imgPreview.setVisibility(View.VISIBLE);
            binding.imgPreview.setMaxZoom(3f);
        } else {
            binding.rlVideo.setVisibility(View.VISIBLE);
            binding.imgPreview.setVisibility(View.GONE);
        }
    }

    private void releaseMedia() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (mediaPlayer == null)
            mediaPlayer = new MediaPlayer();
        mediaPlayer.setDisplay(surfaceHolder);

        int videoWidth = mediaPlayer.getVideoWidth();
        int videoHeight = mediaPlayer.getVideoHeight();

        int screenWidth = getWindowManager().getDefaultDisplay().getWidth();
        android.view.ViewGroup.LayoutParams lp = binding.videoView.getLayoutParams();

        lp.width = screenWidth;
        lp.height = (int) (((float) videoHeight / (float) videoWidth) * (float) screenWidth);

        binding.videoView.setLayoutParams(lp);
        binding.imgBgVideo.setLayoutParams(lp);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseMedia();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseMedia();
    }
}
