package com.elsevier.vtw.event.helper;

public class SQSMessage {

    private String message;
    private int receiveCount;
    private String sentTimestamp;

    public SQSMessage() {
        this.message = "";
        this.receiveCount = 0;
    }

    public SQSMessage(String message) {
        this(message, 0);
    }

    public SQSMessage(String message, int receiveCount) {
        this.message = "";
        this.receiveCount = 0;
        this.message = message;
        this.receiveCount = receiveCount;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getReceiveCount() {
        return this.receiveCount;
    }

    public void setReceiveCount(int receiveCount) {
        this.receiveCount = receiveCount;
    }

    public String getSentTimestamp() {
        return this.sentTimestamp;
    }

    public void setSentTimestamp(String sentTimestamp) {
        this.sentTimestamp = sentTimestamp;
    }
}
