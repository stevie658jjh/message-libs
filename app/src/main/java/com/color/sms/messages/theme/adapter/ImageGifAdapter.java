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
import com.color.sms.messages.theme.model.*;
import com.color.sms.messages.theme.utils.Glide4Engine;

public class ImageGifAdapter extends RecyclerView.Adapter<ImageGifAdapter.ViewHolder> {
    private List<String> imageCategoryModelList;
    private Context context;
    private ImageGifSelectListener listener;
    private Glide4Engine glide4Engine;

    public ImageGifAdapter(Context context, String category, ImageGifSelectListener imageGifSelectListener) {
        this.context = context;
        imageCategoryModelList = new ImageModel().getListGif(category);
        listener = imageGifSelectListener;
        glide4Engine = Glide4Engine.getInstance();
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

            itemView.setOnClickListener(v -> listener.onImageGifSelected(imageCategoryModelList.get(getAdapterPosition())));
        }

        void bind(String string) {
            glide4Engine.loadGifImage(binding.img, string);
        }
    }

    public interface ImageGifSelectListener {
        void onImageGifSelected(String path);
    }
}
