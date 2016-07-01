package com.gmail.ahmedozmaan.unote.model;

import android.net.Uri;

import java.io.Serializable;

/**
 * Created by AhmedOzmaan on 5/4/2016.
 */
public class Messages implements Serializable {
    String messageId,
            messageBody,
            messageTime,
            messageRoom,
            messageFileFlag,
            messageFileName,
            messageFileSize,
            messageFileLink,
            messageSenderId,
            messageSenderName,
            messageFilePath;

    public String getMessageFileName() {
        return messageFileName;
    }

    public void setMessageFileName(String messageFileName) {
        this.messageFileName = messageFileName;
    }

    public String getMessageFileSize() {
        return messageFileSize;
    }

    public void setMessageFileSize(String messageFileSize) {
        this.messageFileSize = messageFileSize;
    }

    public String getMessageFilePath() {
        return messageFilePath;
    }

    public void setMessageFilePath(String messageFilePath) {
        this.messageFilePath = messageFilePath;
    }

    public String getMessageFileLink() {
        return messageFileLink;
    }

    public void setMessageFileLink(String messageFileLink) {
        this.messageFileLink = messageFileLink;
    }

    public String getMessageFileFlag() {
        return messageFileFlag;
    }

    public void setMessageFileFlag(String messageFileFlag) {
        this.messageFileFlag = messageFileFlag;
    }

    public String getMessageSenderId() {
        return messageSenderId;
    }

    public void setMessageSenderId(String messageSenderId) {
        this.messageSenderId = messageSenderId;
    }


    public String getMessageSenderName() {
        return messageSenderName;
    }

    public void setMessageSenderName(String messageSenderName) {
        this.messageSenderName = messageSenderName;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getMessageRoom() {
        return messageRoom;
    }

    public void setMessageRoom(String messageRoom) {
        this.messageRoom = messageRoom;
    }

    public String getMessageBody() {
        return messageBody;
    }

    public void setMessageBody(String messageBody) {
        this.messageBody = messageBody;
    }

    public String getMessageTime() {
        return messageTime;
    }

    public void setMessageTime(String messageTime) {
        this.messageTime = messageTime;
    }
}
