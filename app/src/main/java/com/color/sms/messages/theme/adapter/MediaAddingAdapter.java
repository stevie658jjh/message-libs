package com.color.sms.messages.theme.adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import com.color.sms.messages.theme.R;
import com.color.sms.messages.theme.databinding.ListItemMediaAudioBinding;
import com.color.sms.messages.theme.databinding.ListItemMediaContactBinding;
import com.color.sms.messages.theme.databinding.ListItemMediaFileBinding;
import com.color.sms.messages.theme.databinding.ListItemMediaImageBinding;
import com.color.sms.messages.theme.model.Contact;
import com.color.sms.messages.theme.model.MediaAddingModel;
import com.color.sms.messages.theme.utils.Glide4Engine;

public class MediaAddingAdapter extends RecyclerView.Adapter<MediaAddingAdapter.ViewHolder> {
    private static final int EMOJI = 0, GIF = 1, CONTACT = 2, AUDIO = 3, ATTACH_IMAGE = 4, ATTACH_VIDEO = 5, ATTACH_FILE = 6;
    private List<MediaAddingModel> listAddingModels;
    private MediaAddingListener listener;
    private Glide4Engine glide4Engine;

    public MediaAddingAdapter(MediaAddingListener listener) {
        this.listener = listener;
        listAddingModels = new ArrayList<>();
        glide4Engine = Glide4Engine.getInstance();
    }

    public void addMedia(MediaAddingModel mediaAddingModel) {
        listAddingModels.add(mediaAddingModel);
        notifyItemInserted(listAddingModels.size() - 1);
    }

    public List<MediaAddingModel> getListAddingModels() {
        return listAddingModels;
    }

    public void addMedia(List<MediaAddingModel> listAddingModels) {
        int old = this.listAddingModels.size() - 1;
        this.listAddingModels.addAll(listAddingModels);
        notifyItemRangeInserted(old, listAddingModels.size());
    }

    private void removeMedia(int adapterPosition) {
        listAddingModels.remove(adapterPosition);
        notifyItemRemoved(adapterPosition);
        if (listAddingModels.size() <= 0) {
            listener.onMediaEmpty();
        }
    }

    public void removeTheLastOne() {
        if (listAddingModels != null && listAddingModels.size() != 0)
            removeMedia(listAddingModels.size() - 1);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case GIF:
                ListItemMediaImageBinding listItemMediaImageBinding = DataBindingUtil.inflate(inflater, R.layout.list_item_media_image, parent, false);
                return new GIFViewHolder(listItemMediaImageBinding);
            case CONTACT:
                ListItemMediaContactBinding listItemMediaContactBinding = DataBindingUtil.inflate(inflater, R.layout.list_item_media_contact, parent, false);
                return new ContactViewHolder(listItemMediaContactBinding);
            case AUDIO:
                ListItemMediaAudioBinding listItemMediaAudioBinding = DataBindingUtil.inflate(inflater, R.layout.list_item_media_audio, parent, false);
                return new AudioViewHolder(listItemMediaAudioBinding);
            case ATTACH_VIDEO:
                ListItemMediaImageBinding listItemMediaImageBinding1 = DataBindingUtil.inflate(inflater, R.layout.list_item_media_image, parent, false);
                return new VideoViewHolder(listItemMediaImageBinding1);
            case ATTACH_FILE:
                ListItemMediaFileBinding listItemMediaFileBinding = DataBindingUtil.inflate(inflater, R.layout.list_item_media_file, parent, false);
                return new FileAttachViewHolder(listItemMediaFileBinding);
        }
        ListItemMediaImageBinding listItemMediaImageBinding = DataBindingUtil.inflate(inflater, R.layout.list_item_media_image, parent, false);
        return new EmojiViewHolder(listItemMediaImageBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(listAddingModels.get(position));
    }

    @Override
    public int getItemCount() {
        return listAddingModels.size();
    }

    @Override
    public int getItemViewType(int position) {
        int type = EMOJI;
        switch (listAddingModels.get(position).getType()) {
            case GIF:
                type = GIF;
                break;
            case CONTACT:
                type = CONTACT;
                break;
            case AUDIO:
                type = AUDIO;
                break;
            case ATTACH_IMAGE:
                type = ATTACH_IMAGE;
                break;
            case ATTACH_VIDEO:
                type = ATTACH_VIDEO;
                break;
            case ATTACH_FILE:
                type = ATTACH_FILE;
                break;
        }
        return type;
    }

    public void removeAllItem() {
        listAddingModels.clear();
        notifyDataSetChanged();
    }

    abstract class ViewHolder extends RecyclerView.ViewHolder {

        ViewHolder(View view) {
            super(view);
        }

        abstract void bind(MediaAddingModel mediaAddingModel);

        void setImgClose(ImageView imgClose) {
            imgClose.setOnClickListener(v -> removeMedia(getAdapterPosition()));
        }
    }

    class EmojiViewHolder extends ViewHolder {
        private ListItemMediaImageBinding binding;

        EmojiViewHolder(ListItemMediaImageBinding view) {
            super(view.getRoot());
            binding = view;
            setImgClose(binding.imgClose);
        }

        @Override
        void bind(MediaAddingModel mediaAddingModel) {
            if (!TextUtils.isEmpty(mediaAddingModel.getPath()))
                glide4Engine.loadImage(binding.img, mediaAddingModel.getPath());
            else
                glide4Engine.loadImageBitmap(binding.img, mediaAddingModel.getBitmap());
        }
    }

    class VideoViewHolder extends EmojiViewHolder {
        private ListItemMediaImageBinding binding;

        VideoViewHolder(ListItemMediaImageBinding view) {
            super(view);
            binding = view;
            setImgClose(binding.imgClose);
        }

        @Override
        void bind(MediaAddingModel mediaAddingModel) {
            glide4Engine.loadImage(binding.img, mediaAddingModel.getPath());
            binding.imgVideoPlay.setVisibility(View.VISIBLE);
        }
    }

    class GIFViewHolder extends ViewHolder {
        private ListItemMediaImageBinding binding;

        GIFViewHolder(ListItemMediaImageBinding view) {
            super(view.getRoot());
            binding = view;
            setImgClose(binding.imgClose);
        }

        @Override
        void bind(MediaAddingModel mediaAddingModel) {
            glide4Engine.loadGifImage(binding.img, mediaAddingModel.getPath());
        }
    }

    class AudioViewHolder extends ViewHolder {
        private ListItemMediaAudioBinding binding;

        AudioViewHolder(ListItemMediaAudioBinding view) {
            super(view.getRoot());
            binding = view;
            setImgClose(binding.imgClose);
        }

        @Override
        void bind(MediaAddingModel mediaAddingModel) {

        }
    }

    class FileAttachViewHolder extends ViewHolder {
        private ListItemMediaFileBinding binding;

        FileAttachViewHolder(ListItemMediaFileBinding view) {
            super(view.getRoot());
            binding = view;
            setImgClose(binding.imgClose);
        }

        @Override
        void bind(MediaAddingModel mediaAddingModel) {

        }
    }

    class ContactViewHolder extends ViewHolder {
        private ListItemMediaContactBinding binding;

        ContactViewHolder(ListItemMediaContactBinding view) {
            super(view.getRoot());
            binding = view;
            setImgClose(binding.imgClose);
        }

        @Override
        void bind(MediaAddingModel mediaAddingModel) {
            Contact contact = mediaAddingModel.getContact();
            binding.tvName.setText(contact.getName());
            binding.tvPhone.setText(contact.getPhone());
            binding.tvImage.setText(String.valueOf(contact.getName().charAt(0)));
        }
    }

    public interface MediaAddingListener {
        void onMediaEmpty();
    }
}
