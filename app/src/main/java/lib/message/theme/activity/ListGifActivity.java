package lib.message.theme.activity;

import androidx.databinding.DataBindingUtil;
import android.os.Bundle;
import com.google.android.material.tabs.TabLayout;

import lib.message.theme.R;
import lib.message.theme.base.BaseActivity;
import lib.message.theme.databinding.ActivityListGifBinding;
import lib.message.theme.model.ImageCategoryModel;
import lib.message.theme.utils.FileController;

import java.util.List;

public class ListGifActivity extends BaseActivity {
    private ActivityListGifBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_gif);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_list_gif);

        setupTabLayout();
    }

    private void setupTabLayout() {
        List<ImageCategoryModel> imageCategoryModels = FileController.getGifCategory();

        for (ImageCategoryModel imageCategoryModel :
                imageCategoryModels) {
            binding.tabLayout.addTab(binding.tabLayout.newTab().setText(imageCategoryModel.getName()));
        }

        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                setRecyclerView(imageCategoryModels, tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

    }

    private void setRecyclerView(List<ImageCategoryModel> imageCategoryModels, int position) {

    }
}
