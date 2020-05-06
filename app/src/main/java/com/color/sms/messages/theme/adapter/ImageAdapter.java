package com.color.sms.messages.theme.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import com.color.sms.messages.theme.R;
import com.color.sms.messages.theme.databinding.ListItemImageOnlyBinding;
import com.color.sms.messages.theme.model.ImageModel;
import com.color.sms.messages.theme.utils.Glide4Engine;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {
    private List<String> imageCategoryModelList;
    private Context context;
    private ImageSelectListener listener;
    private Glide4Engine glide4Engine;
    private boolean isLoadGif;

    public ImageAdapter(Context context, ImageSelectListener imageSelectListener) {
        this.context = context;
        imageCategoryModelList = new ImageModel().emojiList();
        listener = imageSelectListener;
        glide4Engine = Glide4Engine.getInstance();
    }

    public void setLoadGif(boolean loadGif) {
        isLoadGif = loadGif;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        ListItemImageOnlyBinding view = DataBindingUtil.inflate(inflater, R.layout.list_item_image_only, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(imageCategoryModelList.get(position));
    }

    @Override
    public int getItemCount() {
        return imageCategoryModelList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private ListItemImageOnlyBinding binding;

        ViewHolder(ListItemImageOnlyBinding view) {
            super(view.getRoot());
            binding = view;

            itemView.setOnClickListener(v -> listener.onImageSelected(imageCategoryModelList.get(getAdapterPosition())));
        }

        void bind(String string) {
            if (isLoadGif){
                glide4Engine.loadGifImage(binding.img, string);
            } else {
                glide4Engine.loadImageItem(binding.img, string);
            }
        }
    }

    public interface ImageSelectListener {
        void onImageSelected(String path);
    }
}
