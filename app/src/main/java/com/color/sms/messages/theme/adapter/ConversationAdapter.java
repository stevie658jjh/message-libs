package com.color.sms.messages.theme.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.color.sms.messages.theme.R;
import com.color.sms.messages.theme.block.utilBlock.BlockListController;
import com.color.sms.messages.theme.model.Contact;
import com.color.sms.messages.theme.model.Conversation;
import com.color.sms.messages.theme.utils.Constants;
import com.color.sms.messages.theme.utils.Glide4Engine;
import com.color.sms.messages.theme.utils.MessageUtils;
import com.color.sms.messages.theme.utils.SharedPreferenceHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class ConversationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context mContext;
    private List<Conversation> mConversations = new ArrayList<>();
    private List<Conversation> mAllConversations = new ArrayList<>();
    private BlockListController blockListController;
    private ConversationListener listener;
    private Glide4Engine glide4Engine = new Glide4Engine();

    public ConversationAdapter(Context context, ConversationListener listener) {
        this.listener = listener;
        mContext = context;

    }

    public void setData(List<Conversation> conversations) {
        if (conversations == null) {
            return;
        }
        if (mConversations.size() == 0) {
            mAllConversations.addAll(conversations);
            mConversations.addAll(getUnblockList(mAllConversations));
            notifyDataSetChanged();
        } else {
            for (int i = 0; i < conversations.size(); i++) {
                Conversation conversation = conversations.get(i);
                if (conversation == null) continue;
                int indexOfAll = mAllConversations.indexOf(conversation);
                if (indexOfAll != -1) {
                    mAllConversations.set(indexOfAll, conversation);
                }
                int indexOf = mConversations.indexOf(conversation);
                if (indexOf != -1) {
                    mConversations.set(indexOf, conversation);
                    notifyItemChanged(indexOf);
                }
            }
        }
    }

    public void updateBlackList() {
        if (blockListController == null) {
            blockListController = new BlockListController();
        }
        for (Conversation con : mAllConversations) {
            if (con != null)
                con.setBlocked(blockListController.isInBlacklist(con.getAddress()));
        }

        mConversations.clear();
        mConversations.addAll(getUnblockList(mAllConversations));
        notifyDataSetChanged();
    }

    public void setData(Conversation conversations) {
        if (conversations == null) {
            return;
        }
        mConversations.add(conversations);
        notifyItemInserted(0);
    }


    private ArrayList<Conversation> getUnblockList(List<Conversation> mConversations) {
        ArrayList<Conversation> conversation = new ArrayList<>();
        for (Conversation c :
                mConversations) {
            if (c == null || !c.isBlocked()) {
                conversation.add(c);
            }
        }
        return conversation;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View view;
        view = inflater.inflate(R.layout.list_item_conversation, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public int getItemViewType(int position) {
        if (mConversations.get(position) == null) return -1;
        return 0;
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolderParent, int position) {
        final Conversation conversation = mConversations.get(position);
        ViewHolder viewHolder = (ViewHolder) viewHolderParent;

        viewHolder.mTextViewTitle.setText(MessageUtils.getContactbyPhoneNumber(mContext, conversation.getAddress()));
        if (conversation.getSnippet() == null || conversation.getSnippet().equalsIgnoreCase("")) {
            viewHolder.mTextViewSnippet.setText("You: Media");
        } else {
            viewHolder.mTextViewSnippet.setText(conversation.getSnippet());
        }
        viewHolder.mTextViewDate.setText(getFullDate2StringFromTimestampNotTimeZone(
                conversation.getDate()));

        if (conversation.getRead() == 0) {
            viewHolder.mImageViewReadState.setVisibility(View.VISIBLE);
        } else {
            viewHolder.mImageViewReadState.setVisibility(View.GONE);
        }

        String avatar = getAvatar(conversation.getAddress());
        viewHolder.mImageViewAvatar.setCircleBackgroundColor(conversation.getColor());
        viewHolder.mImageViewAvatar.setBorderColor(conversation.getColor());
        if (TextUtils.isEmpty(avatar)) {
            viewHolder.mImageViewAvatar.setBorderWidth(12);
            glide4Engine.loadImageDefault(viewHolder.mImageViewAvatar);
        } else {
            viewHolder.mImageViewAvatar.setBorderWidth(1);
            glide4Engine.loadImage(viewHolder.mImageViewAvatar, avatar);
        }
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

    private String getFullDate2StringFromTimestampNotTimeZone(long timestamp) {
        SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("MMM dd", Locale.getDefault());
        return mSimpleDateFormat.format(new Date(timestamp));
    }

    @Override
    public int getItemCount() {
        return mConversations != null ? mConversations.size() : 0;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView mImageViewReadState;
        CircleImageView mImageViewAvatar;
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
                Conversation conversation = mConversations.get(getAdapterPosition());
                listener.onConversationClicked(conversation);
                mImageViewReadState.setVisibility(View.GONE);
                conversation.setRead(1);
            });
        }
    }


    public interface ConversationListener {
        void onConversationClicked(Conversation conversation);
    }
}