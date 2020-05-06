package lib.message.theme.activity;

import android.os.Bundle;

import androidx.recyclerview.widget.RecyclerView;

import lib.message.theme.R;
import lib.message.theme.adapter.GroupMessageAdapter;
import lib.message.theme.base.BaseActivity;
import lib.message.theme.utils.MessageGroupDBHelper;

public class GroupMessageActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_message);

        MessageGroupDBHelper messageGroupDBHelper = new MessageGroupDBHelper(this);

        RecyclerView recyclerView = findViewById(R.id.recycler_group_message);
        GroupMessageAdapter groupMessageAdapter = new GroupMessageAdapter(this);
        groupMessageAdapter.setData(messageGroupDBHelper.getAllGroupMessage());
        recyclerView.setAdapter(groupMessageAdapter);

        findViewById(R.id.btn_back).setOnClickListener(v -> onBackPressed());
    }
}
