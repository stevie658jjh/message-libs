package com.color.sms.messages.theme.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.color.sms.messages.theme.R;
import com.color.sms.messages.theme.activity.ComposeActivity;
import com.color.sms.messages.theme.model.GroupMessage;
import com.color.sms.messages.theme.utils.Constants;

public class GroupMessageAdapter extends RecyclerView.Adapter<GroupMessageAdapter.ViewHolder> {
    private Context mContext;
    private List<GroupMessage> groupMessages = new ArrayList<>();

    public GroupMessageAdapter(Context context) {
        mContext = context;
    }

    public void setData(List<GroupMessage> groupMessages) {
        if (groupMessages == null) {
            return;
        }
        this.groupMessages.addAll(groupMessages);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public GroupMessageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View view = inflater.inflate(R.layout.list_item_conversation, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupMessageAdapter.ViewHolder viewHolder, int position) {
        viewHolder.mTextViewTitle.setText(groupMessages.get(position).getGroupName());
        if (groupMessages.get(position).getBody() == null || groupMessages.get(position).getBody().equalsIgnoreCase("")) {
            viewHolder.mTextViewSnippet.setText("(No topic)");
        } else {
            viewHolder.mTextViewSnippet.setText(groupMessages.get(position).getBody());
        }

        viewHolder.mTextViewDate.setText(getFullDate2StringFromTimestampNotTimeZone(
                groupMessages.get(position).getDate()));
        viewHolder.mImageViewReadState.setVisibility(View.GONE);
        viewHolder.mImageViewAvatar.setImageResource(R.drawable.ic_group_black_24dp);
    }

    private String getFullDate2StringFromTimestampNotTimeZone(long timestamp) {
        SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("MMM dd");
        return mSimpleDateFormat.format(new Date(timestamp));
    }

    @Override
    public int getItemCount() {
        return groupMessages != null ? groupMessages.size() : 0;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView mImageViewAvatar, mImageViewReadState;
        TextView mTextViewTitle, mTextViewSnippet;
        TextView mTextViewDate;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            mImageViewAvatar = itemView.findViewById(R.id.avatars);
            mTextViewTitle = itemView.findViewById(R.id.title);
            mTextViewSnippet = itemView.findViewById(R.id.snippet);
            mTextViewDate = itemView.findViewById(R.id.date);
            mImageViewReadState = itemView.findViewById(R.id.unread);

            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(mContext, ComposeActivity.class);
                intent.putExtra("id", groupMessages.get(getAdapterPosition()).getGroupId());
                intent.putExtra("group_name", groupMessages.get(getAdapterPosition()).getGroupName());
                intent.putExtra("group_number", groupMessages.get(getAdapterPosition()).getGroupNumber());
                intent.putExtra("id_contact", groupMessages.get(getAdapterPosition()).getContactId());
                intent.putExtra("type", Constants.TYPE_GROUP_MESSAGE);
                mContext.startActivity(intent);
            });
        }
    }
}