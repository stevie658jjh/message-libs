package lib.message.theme.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import lib.message.theme.R;
import lib.message.theme.custom.onTouch.DragViewListener;
import lib.message.theme.databinding.ListItemImageCategoryBinding;
import lib.message.theme.model.ImageModel;
import lib.message.theme.utils.Glide4Engine;

public class GifCategoryAdapter extends RecyclerView.Adapter<GifCategoryAdapter.ViewHolder> {
    private List<ImageModel.Gif> imageCategoryModelList;
    private Context context;
    private GifCategorySelectListener listener;
    private Glide4Engine glide4Engine;

    public GifCategoryAdapter(Context context, GifCategorySelectListener categorySelectListener) {
        this.context = context;
        imageCategoryModelList = new ImageModel().getListGif();
        listener = categorySelectListener;
        glide4Engine = Glide4Engine.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        ListItemImageCategoryBinding view = DataBindingUtil.inflate(inflater, R.layout.list_item_image_category, parent, false);
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
        private ListItemImageCategoryBinding binding;

        ViewHolder(ListItemImageCategoryBinding view) {
            super(view.getRoot());
            binding = view;

            itemView.setOnTouchListener(getOnTouch());
        }

        private DragViewListener getOnTouch() {
            return new DragViewListener(v -> listener.onImageGifCategorySelected(imageCategoryModelList.get(getAdapterPosition())));
        }

        void bind(ImageModel.Gif imageCategoryModel) {
            glide4Engine.loadGifImage(binding.img, imageCategoryModel.getFirstImage());
            binding.tv.setText(imageCategoryModel.getName());
        }
    }

    public interface GifCategorySelectListener {
        void onImageGifCategorySelected(ImageModel.Gif imageCategoryModel);
    }
}
