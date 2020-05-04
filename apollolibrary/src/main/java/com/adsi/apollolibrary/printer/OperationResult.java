package com.adsi.apollolibrary.printer;

import java.util.ArrayList;

public class OperationResult
{
    private boolean success;
    private ArrayList<Message> messages;

    public OperationResult() {
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public boolean isSuccess() {
        return this.success;
    }

    public ArrayList<OperationResult.Message> getMessages() {
        return this.messages;
    }

    public void addMessage(String description, MessageType messageType) {
        if (this.messages == null) {
            this.messages = new ArrayList();
        }

        this.messages.add(new OperationResult.Message(description, messageType));
    }

    public String toString() {
        String value = "";
        if (null != this.getMessages()) {
            for(int i = 0; i < this.getMessages().size(); ++i) {
                value = value + ((OperationResult.Message)this.getMessages().get(i)).description + " ";
            }
        } else {
            value = "";
        }

        return value;
    }

    public class Message {
        private String description;
        private MessageType messageType;

        public Message() {
        }

        public Message(String description, MessageType messageType) {
            this.description = description;
            this.messageType = messageType;
        }

        public String getDescription() {
            return this.description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public MessageType getMessageType() {
            return this.messageType;
        }

        public void setMessageType(MessageType messageType) {
            this.messageType = messageType;
        }

    }
}
