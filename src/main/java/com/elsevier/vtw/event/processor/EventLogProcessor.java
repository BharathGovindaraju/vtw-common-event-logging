package com.elsevier.vtw.event.processor;

import com.elsevier.vtw.event.elasticsearch.ElasticSearchRepository;
import com.elsevier.vtw.event.helper.SQSHelper;
import com.elsevier.vtw.event.helper.SQSMessage;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;

public class EventLogProcessor {
    private static final Logger logger = LoggerFactory.getLogger(EventLogProcessor.class);
    private static final String QUEUE_MISSING_ERROR_MESSAGE = "Cannot find the queue for Subscriber";
    private static final String QUEUE_MISSING_ERROR = "Null key found for public java.lang.String com.elsevier.events.service.SubscriberQueueTable.getSubscriberId";
    private static final String EVT_ERROR_MSG = "err:msg";

    private final ElasticSearchRepository elasticSearchRepository;
    private final String queueName;
    private final SQSHelper sqsHelper;
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private final String celIndexRoot;

    @Autowired
    public EventLogProcessor(ElasticSearchRepository elasticSearchRepository,
                               @Value("%[event.router.listener.queue.name]") String queueName, SQSHelper sqsHelper, @Value("cr{cel.index.root.name}") String celIndexRoot) {
        this.elasticSearchRepository = elasticSearchRepository;
        this.queueName = queueName;
        this.sqsHelper = sqsHelper;
        this.celIndexRoot = celIndexRoot;
    }

    public boolean processMessage(SQSMessage message) {
        String payload = message.getMessage();
        JsonNode messageNode = null;
        try {
            messageNode = MAPPER.readTree(payload);
        } catch (IOException ex) {
            logger.warn("Failed to convert the message to a JsonNode: {}", payload, ex);
        }

        logger.debug("Processing Message [{}].", payload);

        try {
            //TO DO
            elasticSearchRepository.addEvent(payload);
            logger.info("Logged the notification/servicecall into CEL - {}.", payload);

            /**
             * This change is to make sure Failed Notifications from router is not sent to Router again.
             * Instead of changing each rule to have an idetifier tag, the change was made to check whether
             * the failure is because of subscriber queue not existing in the list.
             */
            if (shouldEnqueueToEventQueue(messageNode)) {
                logger.info("Enqueuing the message to Event Queue - {}", payload);
                sqsHelper.enqueueSingleMessage(queueName, payload);
            }

        } catch (Exception ex) {

            logger.warn("Exception while adding the message {} to Elasticsearch", message, ex);
            logger.warn("Unable to enqueue message {} onto the Event queue, due to execption - {}", message, ex);

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
