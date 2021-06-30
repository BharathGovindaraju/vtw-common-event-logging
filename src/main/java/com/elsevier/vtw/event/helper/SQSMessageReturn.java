package com.elsevier.vtw.event.helper;

import java.util.HashMap;
import java.util.Map;

public class SQSMessageReturn {

    public static final String SENT_TIMESTAMP = "SentTimestamp";
    public static final String APPROXIMATE_RECEIVE_COUNT = "ApproximateReceiveCount";
    private String id;
    private String handle;
    private String checkSum;
    private String body;
    private Map<String, String> attributes = new HashMap();

    public SQSMessageReturn() {
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getHandle() {
        return this.handle;
    }

    public void setHandle(String handle) {
        this.handle = handle;
    }

    public String getCheckSum() {
        return this.checkSum;
    }

    public void setCheckSum(String checkSum) {
        this.checkSum = checkSum;
    }

    public String getBody() {
        return this.body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Map<String, String> getAttributes() {
        return this.attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        if (attributes == null) {
            this.attributes.clear();
        }

        this.attributes = attributes;
    }

    public String toString() {
        return this.body;
    }

    public String getAttribute(String name) {
        return (String)this.getAttributes().get(name);
    }

    public int getReceiveCount() {
        return Integer.valueOf(this.getAttribute("ApproximateReceiveCount"));
    }

    public SQSMessage toSQSMessage() {
        SQSMessage message = new SQSMessage();
        message.setMessage(this.body);
        message.setSentTimestamp(this.getAttribute("SentTimestamp"));
        message.setReceiveCount(this.getReceiveCount());
        return message;
    }
}
