package com.gmail.ahmedozmaan.unote.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gmail.ahmedozmaan.unote.R;
import com.gmail.ahmedozmaan.unote.app.MyApplication;
import com.gmail.ahmedozmaan.unote.model.Messages;

import java.util.ArrayList;

/**
 * Created by AhmedOzmaan on 5/28/2016.
 */
public class ThreadAdapter extends RecyclerView.Adapter<ThreadAdapter.ThreadHolder>{

    private static ArrayList<Messages> messageArrayList;
    LayoutInflater layoutInflater;
    private ItemClickCallback itemClickCallback;
    public interface ItemClickCallback{
        void onItemClick(int position);
        void onDownloadClick(int position);
        void onMessageClick(int position);

    }
    public void setItemClickCallback(final ItemClickCallback itemClickCallback){
        this.itemClickCallback = itemClickCallback;
    }
    public ThreadAdapter(Context mContext, ArrayList<Messages> messageArrayList){
        layoutInflater = LayoutInflater.from(mContext);
       this.messageArrayList = messageArrayList;

    }
    @Override
    public ThreadHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View  view = layoutInflater.inflate(R.layout.chat_item, parent, false);
          return new ThreadHolder(view);
    }

    @Override
    public void onBindViewHolder(ThreadHolder holder, int position) {
        Messages message = messageArrayList.get(position);

        holder.message.setText(message.getMessageBody());
        if(message.getMessageSenderName().equals(MyApplication.getInstance().getPrefManager().getUser().getName())){
            holder.downloadButton.setText("Open");
        }
        if(!message.getMessageFilePath().equals("0")){
            holder.downloadButton.setText("Open");
        }
        if(message.getMessageRoom().equals("0")){
            holder.sender.setText(message.getMessageSenderName());
        }else {
            holder.sender.setText(message.getMessageSenderName() + " : " + message.getMessageRoom());
        }
        holder.timestamp.setText(message.getMessageTime());
        if(message.getMessageFileFlag().equals("0")){
            holder.fileLayout.setVisibility(View.GONE);
        }else{
            holder.fileLayout.setVisibility(View.VISIBLE);
            holder.fileName.setText(message.getMessageFileName());
            holder.fileSize.setText(message.getMessageFileSize());
            if(message.getMessageFileName().contains("ppt")){
                holder.fileIcon.setImageResource(R.mipmap.ppt_icon);
            }else if(message.getMessageFileName().contains("pdf")){
                holder.fileIcon.setImageResource(R.mipmap.pdf_icon);
            }else   if(message.getMessageFileName().contains("doc") || message.getMessageFileName().contains("docx")){
                holder.fileIcon.setImageResource(R.mipmap.doc_icon);

            }  if(message.getMessageFileName().contains("txt")){
                holder.fileIcon.setImageResource(R.mipmap.txt_icon);

            }
        }
    }

    @Override
    public int getItemCount() {
        return messageArrayList.size();
    }

    class ThreadHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener{
        TextView message, timestamp, sender, fileName, fileSize, fileLink;
        Button downloadButton;
        RelativeLayout fileLayout;
        ImageView fileIcon;
        CardView cardView;
        public ThreadHolder(View itemView) {
            super(itemView);
           itemView.setOnLongClickListener(this);
            sender = (TextView) itemView.findViewById(R.id.message_sender);
            message = (TextView) itemView.findViewById(R.id.message);
            timestamp = (TextView) itemView.findViewById(R.id.message_time);
            fileName = (TextView) itemView.findViewById(R.id.file_name);
            fileSize = (TextView) itemView.findViewById(R.id.file_size);
            fileLayout = (RelativeLayout)itemView.findViewById(R.id.file);
            cardView =(CardView)itemView.findViewById(R.id.card_view);
            downloadButton =(Button)itemView.findViewById(R.id.download_button);
            fileIcon = (ImageView)itemView.findViewById(R.id.file_icon);
            downloadButton.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if(v.getId() == R.id.download_button){
                itemClickCallback.onDownloadClick(getAdapterPosition());
            }else {
                itemClickCallback.onItemClick(getAdapterPosition());

            }
        }

        @Override
        public boolean onLongClick(View v) {
            itemClickCallback.onMessageClick(getAdapterPosition());
            return true;
        }
    }
}
