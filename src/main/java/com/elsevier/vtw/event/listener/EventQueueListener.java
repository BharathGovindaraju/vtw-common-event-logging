package com.elsevier.vtw.event.listener;

import com.amazonaws.services.sqs.model.Message;
import com.elsevier.vtw.event.helper.SQSMessage;
import com.elsevier.vtw.event.helper.SQSHelper;
import com.elsevier.vtw.event.helper.SQSMessageReturn;
import com.elsevier.vtw.event.processor.EventLogProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EventQueueListener implements Runnable {
    private Logger LOG = LoggerFactory.getLogger(com.elsevier.vtw.event.listener.EventQueueListener.class);
    private final EventLogProcessor eventLogProcessor;
    private final String queueName;
    private final boolean retryRequired;
    private final SQSHelper sqsHelper;
    private static String[] ATTRIBUTES = new String[]{"All"};
    public EventQueueListener(EventLogProcessor eventLogProcessor, String queueName,
                              boolean retryRequired, SQSHelper sqsHelper) {
        this.eventLogProcessor = eventLogProcessor;
        this.queueName = queueName;
        this.retryRequired = retryRequired;
        this.sqsHelper = sqsHelper;
    }

    public void run() {
        List<String> handles = new ArrayList<>();
        try {
            List<SQSMessageReturn> messageReturns = sqsHelper.dequeueMessages(
                    queueName, 1);

            if (!messageReturns.isEmpty()) {
                LOG.debug("Retrieved {} messages.", messageReturns.size());

                SQSMessageReturn messageReturn = messageReturns.get(0);
                handles.add(messageReturn.getHandle());
                SQSMessage message = messageReturn.toSQSMessage();

                boolean status = eventLogProcessor.processMessage(message);
                LOG.debug("Message processed, status [{}].", status);
                if (messageReturn.getReceiveCount() > 3) {
                    // if received more than 3 times, delete message
                    status = true;
                }
                checkAndClearMessage(handles, status);
            }
        } catch (Exception exception) {
            // don't propagate the exception, 'cos this kills the thread and we
            // don't want that
            LOG.error("Error while processing messages.", exception);
        } finally {
            // if retry is not required, delete the message irrespective of
            // status
            if (!handles.isEmpty() && !retryRequired) {
                LOG.error("Clearing the messages in any case, {}.", handles);
                sqsHelper.deleteMessages(queueName, handles);
            }
        }
    }

    private int getReceiveCount(Message messageReturn) {
        Map<String, String> messageAttributes;
        messageAttributes = messageReturn.getAttributes();
        return Integer.valueOf(messageAttributes.get("ApproximateReceiveCount"));
    }

    private void checkAndClearMessage(List<String> handles, boolean status) {
        if (retryRequired && status) {
            sqsHelper.deleteMessages(queueName, handles);
        }
    }
}
