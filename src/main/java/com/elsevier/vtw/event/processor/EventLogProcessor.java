package com.elsevier.vtw.event.processor;

import com.amazonaws.services.sqs.AmazonSQS;
import com.elsevier.vtw.event.elasticsearch.ElasticSearchRepository;
import com.elsevier.vtw.event.helper.SQSMessage;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.searchbox.client.JestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;

public class EventLogProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(EventLogProcessor.class);
    public static final String QUEUE_MISSING_ERROR_MESSAGE = "Cannot find the queue for Subscriber";
    public static final String QUEUE_MISSING_ERROR = "Null key found for public java.lang.String com.elsevier.events.service.SubscriberQueueTable.getSubscriberId";
    public static final String EVT_ERROR_MSG = "err:msg";

    private ElasticSearchRepository elasticSearchRepository;
    private String queueName;
    private  AmazonSQS sqs;
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private JestClient client;
    private String celIndexRoot;

    @Autowired
    public EventLogProcessor(ElasticSearchRepository elasticSearchRepository,
                               @Value("%[event.router.listener.queue.name]") String queueName, AmazonSQS sqs, @Value("cr{cel.index.root.name}") String celIndexRoot) {
        this.elasticSearchRepository = elasticSearchRepository;
        this.queueName = queueName;
        this.sqs = sqs;
        this.celIndexRoot = celIndexRoot;
    }

    public boolean processMessage(SQSMessage message) throws Exception {
        String payload = message.getMessage();
        JsonNode messageNode = null;
        try {
            messageNode = MAPPER.readTree(payload);
        } catch (IOException ex) {
            LOG.warn("Failed to convert the message to a JsonNode: {}", payload, ex);
        }

        LOG.debug("Processing Message [{}].", payload);

        try {
            //TO DO
            //elasticSearchRepository.addEvent(payload);
            LOG.info("Logged the notification/servicecall into CEL - {}.", payload);

            /**
             * This change is to make sure Failed Notifications from router is not sent to Router again.
             * Instead of changing each rule to have an idetifier tag, the change was made to check whether
             * the failure is because of subscriber queue not existing in the list.
             */
          /*  if (shouldEnqueueToEventQueue(messageNode)) {
                LOG.info("Enqueuing the message to Event Queue - {}", payload);
                sqsHelper.enqueueSingleMessage(queueName, payload);
            }*/

        } catch (Exception ex) {

            LOG.warn("Exception while adding the message {} to Elasticsearch", message, ex);
            LOG.warn("Unable to enqueue message {} onto the Event queue, due to execption - {}", message, ex);

            throw ex;
        }

        return true;
    }

    private boolean shouldEnqueueToEventQueue(JsonNode messageNode) {
        JsonNode value = messageNode != null ? messageNode.findValue("ef:toggle") : null;
        boolean shouldEnqueueToEventQueue = (value == null || !value.asBoolean());

        JsonNode evtErrorMessage = messageNode != null ? messageNode.findValue(EVT_ERROR_MSG) : null;

        if (evtErrorMessage != null && (evtErrorMessage.asText().contains(QUEUE_MISSING_ERROR_MESSAGE)
                || evtErrorMessage.asText().contains(QUEUE_MISSING_ERROR))) {
            shouldEnqueueToEventQueue = false;
        }
        return shouldEnqueueToEventQueue;
    }
}
