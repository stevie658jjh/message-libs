package lib.message.theme.presenter;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import lib.message.theme.adapter.MediaAddingAdapter;
import lib.message.theme.model.MediaAddingModel;

public class ComposePresenter implements MediaAddingAdapter.MediaAddingListener {
    private MediaAddingAdapter mediaAddingAdapter;
    private RecyclerView recyclerView;

    public ComposePresenter(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
        mediaAddingAdapter = new MediaAddingAdapter(this);
        recyclerView.setAdapter(mediaAddingAdapter);
    }

    public List<MediaAddingModel> getMediaAdding(){
        return mediaAddingAdapter.getListAddingModels();
    }

    public void addMediaList(MediaAddingModel mediaAddingModel) {
        if (recyclerView.getVisibility() == View.GONE) {
            recyclerView.setVisibility(View.VISIBLE);
        }
        mediaAddingAdapter.addMedia(mediaAddingModel);
    }

    @Override
    public void onMediaEmpty() {
        recyclerView.setVisibility(View.GONE);
        mediaAddingAdapter.removeAllItem();
    }

    public void removeTheLastOne() {
        mediaAddingAdapter.removeTheLastOne();
    }
}
