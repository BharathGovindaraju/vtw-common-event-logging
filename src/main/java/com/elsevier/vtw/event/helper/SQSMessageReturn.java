package com.elsevier.vtw.event.helper;

import java.util.HashMap;
import java.util.Map;

public class SQSMessageReturn {

    private String id;
    private String handle;
    private String checkSum;
    private String body;
    private Map<String, String> attributes = new HashMap<>();

    public void setId(String id) {
        this.id = id;
    }

    public String getHandle() {
        return this.handle;
    }

    public void setHandle(String handle) {
        this.handle = handle;
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
        return this.getAttributes().get(name);
    }

    public int getReceiveCount() {
        return Integer.parseInt(this.getAttribute("ApproximateReceiveCount"));
    }

}
