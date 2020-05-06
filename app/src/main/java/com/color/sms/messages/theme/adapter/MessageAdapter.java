package com.color.sms.messages.theme.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.color.sms.messages.theme.R;
import com.color.sms.messages.theme.activity.PreviewMmsImageActivity;
import com.color.sms.messages.theme.activity.search.model.Search;
import com.color.sms.messages.theme.databinding.MessageListItemInBinding;
import com.color.sms.messages.theme.databinding.MessageListItemOutBinding;
import com.color.sms.messages.theme.model.Contact;
import com.color.sms.messages.theme.model.Message;
import com.color.sms.messages.theme.utils.Constants;
import com.color.sms.messages.theme.utils.DateTimeUtility;
import com.color.sms.messages.theme.utils.Function;
import com.color.sms.messages.theme.utils.Glide4Engine;
import com.color.sms.messages.theme.utils.SharedPreferenceHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static int TYPE_MESSAGE_IN = 1;
    private static int TYPE_MESSAGE_OUT = 2;
    private Context mContext;
    private List<Message> mMessages = new ArrayList<>();
    private MediaPlayer mediaPlayer, mediaPlayerVideo;
    private Glide4Engine glide4Engine;
    private int userColor;
    private int backgroundTheme;

    public MessageAdapter(Context context, int color) {
        this.userColor = color;
        mContext = context;
        glide4Engine = new Glide4Engine();
    }

    public void releaseMedia() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }

    public void setData(List<Message> messages) {
        if (messages == null) {
            return;
        }
        mMessages.addAll(messages);
        notifyDataSetChanged();
    }

    public void addNewMessage(Message message) {
        mMessages.add(message);
        notifyDataSetChanged();
    }

    public void updateMessageStatus(long id, int type, long time) {
        for (int i = 0; i < mMessages.size(); i++) {
            Message message = mMessages.get(i);
            if (message.getId() == id) {
                message.setType(type);
                message.setDate(time);
                notifyItemChanged(i);
                return;
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (mMessages.get(position).getType() == Constants.MESSAGE_TYPE_INBOX) {
            return TYPE_MESSAGE_IN;
        } else {
            return TYPE_MESSAGE_OUT;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        if (viewType == TYPE_MESSAGE_IN) {
            MessageListItemInBinding messageListItemInBinding = DataBindingUtil.inflate(LayoutInflater.from(mContext),
                    R.layout.message_list_item_in, viewGroup, false);
            return new MessageInHolder(messageListItemInBinding);
        } else {
            MessageListItemOutBinding messageListItemOutBinding = DataBindingUtil.inflate(LayoutInflater.from(mContext),
                    R.layout.message_list_item_out, viewGroup, false);
            return new MessageOutHolder(messageListItemOutBinding);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        Message message = mMessages.get(position);
        backgroundTheme = SharedPreferenceHelper.getInstance(mContext).getInt(Constants.BACKGROUND_THEME_MAIN);
        if (getItemViewType(position) == TYPE_MESSAGE_IN) {
            MessageInHolder v = ((MessageInHolder) viewHolder);
            v.setMessageIn(message);
            v.bind(message);
        } else {
            MessageOutHolder v = ((MessageOutHolder) viewHolder);
            v.setMessageOut(message);
            v.bind(message);
        }
    }

    @Override
    public int getItemCount() {
        return mMessages != null ? mMessages.size() : 0;
    }

    class MessageInHolder extends RecyclerView.ViewHolder implements SurfaceHolder.Callback {
        MessageListItemInBinding item;
        SurfaceHolder surfaceHolder;

        MessageInHolder(@NonNull MessageListItemInBinding itemView) {
            super(itemView.getRoot());
            item = itemView;
            surfaceHolder = itemView.videoView.getHolder();
            surfaceHolder.addCallback(this);

            item.lnAudio.setOnClickListener(v -> playMedia(mMessages.get(getAdapterPosition()).getAudio()));

            item.imageMms.setOnClickListener(v -> {
                Intent intent = new Intent(mContext, PreviewMmsImageActivity.class);
                intent.putExtra("date", mMessages.get(getAdapterPosition()).getDate());
                intent.putExtra("address", mMessages.get(getAdapterPosition()).getUser());
                intent.putExtra("image", mMessages.get(getAdapterPosition()).getImage().toString());
                intent.putExtra("type", String.valueOf(Search.NAME.Images));
                mContext.startActivity(intent);
            });

            item.imgPlayVideo.setOnClickListener(view -> {
                if (mediaPlayerVideo == null)
                    mediaPlayerVideo = new MediaPlayer();

                try {
                    mediaPlayerVideo.setDataSource(mContext, mMessages.get(getAdapterPosition()).getVideo());
                    mediaPlayerVideo.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    mediaPlayerVideo.prepare();
                    mediaPlayerVideo.start();

                    item.rlThumbVideo.setVisibility(View.GONE);
                    item.videoView.setVisibility(View.VISIBLE);
                    mediaPlayerVideo.setOnCompletionListener(mp -> {
                        item.rlThumbVideo.setVisibility(View.VISIBLE);
                        item.videoView.setVisibility(View.GONE);
                        if (mediaPlayerVideo != null) {
                            mediaPlayerVideo.release();
                            mediaPlayerVideo = null;
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            item.videoView.setOnClickListener(v -> {
                item.rlThumbVideo.setVisibility(View.VISIBLE);
                item.videoView.setVisibility(View.GONE);
                if (mediaPlayerVideo != null) {
                    mediaPlayerVideo.stop();
                    mediaPlayerVideo.release();
                    mediaPlayerVideo = null;
                }
            });
        }

        private void setMessageIn(Message message) {
            if (backgroundTheme != 0) {
                item.tvBody.setTextColor(mContext.getResources().getColor(R.color.color_black));
                item.tvBody.setBackgroundTintList(ColorStateList.valueOf(mContext.getResources().getColor(R.color.color_white)));
                item.tvTimestamp.setTextColor(mContext.getResources().getColor(R.color.white));
            }
            item.tvBody.setText(message.getBody());

            if (message.isGif()) {
                glide4Engine.loadGifImage(item.imageMms, message.getImage());
            } else if (message.getBitmap() != null) {
                glide4Engine.loadImageBitmap(item.imageMms, message.getBitmap());
            } else {
                glide4Engine.loadImage(item.imageMms, message.getImage());
            }

            String thumbAvatar = getAvatar(message.getAddress());
            item.imgAvatar.setBorderColor(userColor);
            item.imgAvatar.setCircleBackgroundColor(userColor);
            if (TextUtils.isEmpty(thumbAvatar)) {
                item.imgAvatar.setBorderWidth(10);
                glide4Engine.loadImageDefault(item.imgAvatar);
            } else {
                item.imgAvatar.setBorderWidth(1);
                glide4Engine.loadImage(item.imgAvatar, thumbAvatar);
            }

            if (getAdapterPosition() - 1 != -1) {
                Message lastMessage = mMessages.get(getAdapterPosition() - 1);
                if (lastMessage != null
                        && lastMessage.getType() == Constants.MESSAGE_TYPE_INBOX
                        && (message.getDate() - lastMessage.getDate()) < 30000) {
                    item.tvTimestamp.setVisibility(View.GONE);
                    item.imgAvatar.setVisibility(View.INVISIBLE);
                } else {
                    item.imgAvatar.setVisibility(View.VISIBLE);
                    item.tvTimestamp.setVisibility(View.VISIBLE);
                    item.tvTimestamp.setText(DateTimeUtility.formatMessageTime(message.getDate()));
                }
            } else {
                item.imgAvatar.setVisibility(View.VISIBLE);
                item.tvTimestamp.setVisibility(View.VISIBLE);
                item.tvTimestamp.setText(DateTimeUtility.formatMessageTime(message.getDate()));
            }
            glide4Engine.loadImage(item.imgBgVideo, message.getVideo());
        }

        private String getAvatar(String address) {
            Contact contact = new Contact(address, address);
            if (Constants.contactList.contains(contact)) { // This one to down the loop action
                for (Contact c :
                        Constants.contactList) {
                    if (c.getPhone().equalsIgnoreCase(address) && !TextUtils.isEmpty(c.getPhotoUri())) {
                        return c.getPhotoUri();
                    }
                }
            }
            return null;
        }

        public void bind(Message message) {
            if (message.getImage() != null || message.getBitmap() != null) {
                item.imageMms.setVisibility(View.VISIBLE);
                item.tvBody.setVisibility(View.GONE);
                item.lnAudio.setVisibility(View.GONE);
                item.rlVideo.setVisibility(View.GONE);
                item.videoView.setVisibility(View.GONE);
            } else if (message.getAudio() != null) {
                item.lnAudio.setVisibility(View.VISIBLE);
                item.imageMms.setVisibility(View.GONE);
                item.tvBody.setVisibility(View.GONE);
                item.videoView.setVisibility(View.GONE);
                item.rlVideo.setVisibility(View.GONE);
                item.tvAudioDuration.setText(Function.getDuration(message.getAudio()));
            } else if (message.getVideo() != null) {
                item.rlVideo.setVisibility(View.VISIBLE);
                item.imageMms.setVisibility(View.GONE);
                item.tvBody.setVisibility(View.GONE);
                item.lnAudio.setVisibility(View.GONE);
            } else {
                item.imageMms.setVisibility(View.GONE);
                item.tvBody.setVisibility(View.VISIBLE);
                item.lnAudio.setVisibility(View.GONE);
                item.videoView.setVisibility(View.GONE);
                item.rlVideo.setVisibility(View.GONE);
            }
        }

        private void playMedia(Uri audio) {
            if (mediaPlayer == null || !mediaPlayer.isPlaying()) {
                onCountDuration(audio);
                onPlayMedia(audio);
            }
        }

        private void onPlayMedia(Uri audio) {
            item.imgPlayPause.setImageResource(R.drawable.ic_pause_black_24dp);
            if (mediaPlayer == null)
                mediaPlayer = new MediaPlayer();
            try {
                mediaPlayer.setDataSource(mContext, audio);
                mediaPlayer.prepare();
                mediaPlayer.start();
                mediaPlayer.setOnCompletionListener(mp -> {
                    item.imgPlayPause.setImageResource(R.drawable.ic_play_arrow_black_24dp);
                    item.tvAudioDuration.setText(Function.getDuration(audio));
                    mediaPlayer.stop();
                    mediaPlayer.reset();
                });

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void onCountDuration(Uri audio) {
            new CountDownTimer(Function.getDurationInt(audio), 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    if (item != null)
                        item.tvAudioDuration.setText(DateTimeUtility.formatVideoTime(millisUntilFinished));
                }

                @Override
                public void onFinish() {
                }
            }.start();
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            if (mediaPlayerVideo == null)
                mediaPlayerVideo = new MediaPlayer();

            mediaPlayerVideo.setDisplay(surfaceHolder);
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
        }
    }

    class MessageOutHolder extends RecyclerView.ViewHolder implements SurfaceHolder.Callback {
        MessageListItemOutBinding itemView;
        SurfaceHolder surfaceHolder;

        MessageOutHolder(@NonNull MessageListItemOutBinding itemView) {
            super(itemView.getRoot());
            this.itemView = itemView;
            surfaceHolder = itemView.videoView.getHolder();
            surfaceHolder.addCallback(this);

            itemView.lnAudio.setOnClickListener(v -> playMedia(mMessages.get(getAdapterPosition()).getAudio()));

            itemView.imageMms.setOnClickListener(v -> {
                Intent intent = new Intent(mContext, PreviewMmsImageActivity.class);
                intent.putExtra("date", mMessages.get(getAdapterPosition()).getDate());
                intent.putExtra("address", mMessages.get(getAdapterPosition()).getUser());
                intent.putExtra("image", mMessages.get(getAdapterPosition()).getImage().toString());
                intent.putExtra("type", String.valueOf(Search.NAME.Images));
                mContext.startActivity(intent);
            });

            itemView.imgPlayVideo.setOnClickListener(view -> {
                if (mediaPlayerVideo == null)
                    mediaPlayerVideo = new MediaPlayer();

                try {
                    mediaPlayerVideo.setDataSource(mContext, mMessages.get(getAdapterPosition()).getVideo());
                    mediaPlayerVideo.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    mediaPlayerVideo.prepare();
                    mediaPlayerVideo.start();

                    itemView.rlThumbVideo.setVisibility(View.GONE);
                    itemView.videoView.setVisibility(View.VISIBLE);
                    mediaPlayerVideo.setOnCompletionListener(mp -> {
                        itemView.rlThumbVideo.setVisibility(View.VISIBLE);
                        itemView.videoView.setVisibility(View.GONE);
                        if (mediaPlayerVideo != null) {
                            mediaPlayerVideo.release();
                            mediaPlayerVideo = null;
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            itemView.videoView.setOnClickListener(v -> {
                itemView.rlThumbVideo.setVisibility(View.VISIBLE);
                itemView.videoView.setVisibility(View.GONE);
                if (mediaPlayerVideo != null) {
                    mediaPlayerVideo.stop();
                    mediaPlayerVideo.release();
                    mediaPlayerVideo = null;
                }
            });
        }

        private void setMessageOut(Message message) {
            if (backgroundTheme != 0) {
                itemView.tvBody.setTextColor(mContext.getResources().getColor(R.color.white));
                itemView.tvTimestamp.setTextColor(mContext.getResources().getColor(R.color.white));
                itemView.tvStatus.setTextColor(mContext.getResources().getColor(R.color.white));
            }

            if (message.isGif()) {
                glide4Engine.loadGifImage(itemView.imageMms, message.getImage());
            } else if (message.getBitmap() != null) {
                glide4Engine.loadImageBitmap(itemView.imageMms, message.getBitmap());
            } else {
                glide4Engine.loadImage(itemView.imageMms, message.getImage());
            }
            if (getAdapterPosition() - 1 != -1) {
                Message lastMessage = mMessages.get(getAdapterPosition() - 1);
                if (lastMessage != null
                        && lastMessage.getType() != Constants.MESSAGE_TYPE_INBOX
                        && (message.getDate() - lastMessage.getDate()) < 30000) {
                    itemView.tvTimestamp.setVisibility(View.GONE);
                } else {
                    itemView.tvTimestamp.setVisibility(View.VISIBLE);
                    itemView.tvTimestamp.setText(DateTimeUtility.formatMessageTime(message.getDate()));
                }
            } else {
                itemView.tvTimestamp.setVisibility(View.VISIBLE);
                itemView.tvTimestamp.setText(DateTimeUtility.formatMessageTime(message.getDate()));
            }
            itemView.tvBody.setText(message.getBody());
            itemView.tvBody.getBackground().

                    setColorFilter(userColor, PorterDuff.Mode.SRC_OVER);
            glide4Engine.loadImage(itemView.imgBgVideo, message.getVideo());
        }

        void bind(Message message) {
            if (message.getImage() != null || message.getBitmap() != null) {
                itemView.imageMms.setVisibility(View.VISIBLE);
                itemView.tvBody.setVisibility(View.GONE);
                itemView.lnAudio.setVisibility(View.GONE);
                itemView.rlVideo.setVisibility(View.GONE);
                itemView.videoView.setVisibility(View.GONE);
            } else if (message.getAudio() != null) {
                itemView.lnAudio.setVisibility(View.VISIBLE);
                itemView.imageMms.setVisibility(View.GONE);
                itemView.tvBody.setVisibility(View.GONE);
                itemView.videoView.setVisibility(View.GONE);
                itemView.rlVideo.setVisibility(View.GONE);
                itemView.tvAudioDuration.setText(Function.getDuration(message.getAudio()));
            } else if (message.getVideo() != null) {
//                itemView.videoView.setVisibility(View.VISIBLE);
                itemView.rlVideo.setVisibility(View.VISIBLE);
                itemView.imageMms.setVisibility(View.GONE);
                itemView.tvBody.setVisibility(View.GONE);
                itemView.lnAudio.setVisibility(View.GONE);
            } else {
                itemView.imageMms.setVisibility(View.GONE);
                itemView.tvBody.setVisibility(View.VISIBLE);
                itemView.lnAudio.setVisibility(View.GONE);
                itemView.videoView.setVisibility(View.GONE);
                itemView.rlVideo.setVisibility(View.GONE);
                itemView.tvStatus.setVisibility(View.VISIBLE);
                if (message.getType() == Constants.MESSAGE_TYPE_OUTBOX) {
                    itemView.tvStatus.setText("Sending...");
                } else if (message.getType() == Constants.MESSAGE_TYPE_FAILED) {
                    itemView.tvStatus.setText("Send failed");
                } else if (message.getType() == Constants.MESSAGE_TYPE_DRAFT) {
                    itemView.tvStatus.setText("Draft");
                } else {
                    itemView.tvStatus.setVisibility(View.GONE);
                }
            }
        }

        private void playMedia(Uri audio) {
            if (mediaPlayer == null || !mediaPlayer.isPlaying()) {
                onCountDuration(audio);
                onPlayMedia(audio);
            }
        }

        private void onPlayMedia(Uri audio) {
            itemView.imgPlayPause.setImageResource(R.drawable.ic_pause_black_24dp);
            if (mediaPlayer == null)
                mediaPlayer = new MediaPlayer();
            try {
                mediaPlayer.setDataSource(mContext, audio);
                mediaPlayer.prepare();
                mediaPlayer.start();
                mediaPlayer.setOnCompletionListener(mp -> {
                    itemView.imgPlayPause.setImageResource(R.drawable.ic_play_arrow_black_24dp);
                    itemView.tvAudioDuration.setText(Function.getDuration(audio));
                    mediaPlayer.stop();
                    mediaPlayer.reset();
                });

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void onCountDuration(Uri audio) {
            new CountDownTimer(Function.getDurationInt(audio), 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    if (itemView != null)
                        itemView.tvAudioDuration.setText(DateTimeUtility.formatVideoTime(millisUntilFinished));
                }

                @Override
                public void onFinish() {
                }
            }.start();
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            if (mediaPlayerVideo == null)
                mediaPlayerVideo = new MediaPlayer();

            mediaPlayerVideo.setDisplay(surfaceHolder);
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
        }
    }

}