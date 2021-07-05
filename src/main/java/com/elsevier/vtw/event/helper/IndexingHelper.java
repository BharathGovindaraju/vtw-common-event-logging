package com.elsevier.vtw.event.helper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;

public class IndexingHelper {

    private ObjectMapper mapper = new ObjectMapper();
    private String id;
    private String type;
    private String timestamp;
    private static final Logger LOG = LoggerFactory.getLogger(IndexingHelper.class);

    public IndexingHelper(String event) {
        try {
            JsonNode payload = this.mapper.readTree(event);
            this.id = payload.get("@id").textValue();
            this.type = "message";
            String eventType = payload.get("msg:type").textValue();
            if (eventType.equals("http://vtw.elsevier.com/data/voc/MessageTypes/ServiceCall-1")) {
                this.timestamp = this.getServcieCallTime(payload);
            } else {
                if (!eventType.equals("http://vtw.elsevier.com/data/voc/MessageTypes/EventNotification-1")) {
                    throw new Exception("Unknown message type");
                }

                this.timestamp = this.getNotificationTime(payload);
            }
        } catch (Exception var4) {
            LOG.error("Invalid Event:" + event, var4);
            this.timestamp = this.getCurrentTimestamp();
            this.type = "message";
        }

    }

    public String getId() {
        return this.id;
    }

    public String getType() {
        return this.type;
    }

    public String getTimestamp() {
        return this.timestamp;
    }

    private String getServcieCallTime(JsonNode payload) {
        String timestamp = payload.findValue("svc:time").asText();
        return this.validatedTimestamp(timestamp);
    }

    private String getNotificationTime(JsonNode payload) {
        String timestamp = payload.findValue("evt:time").asText();
        return this.validatedTimestamp(timestamp);
    }

    private String validatedTimestamp(String timestamp) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

        try {
            format.parse(timestamp);
        } catch (ParseException var4) {
            timestamp = this.getCurrentTimestamp();
        }

        return timestamp;
    }

    private String getCurrentTimestamp() {
        return DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").print((new DateTime()).toDateTime(DateTimeZone.UTC));
    }
}
