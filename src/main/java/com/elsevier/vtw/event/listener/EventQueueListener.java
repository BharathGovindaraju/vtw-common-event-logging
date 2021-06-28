package com.elsevier.vtw.event.listener;

import java.util.ArrayList;
import java.util.List;

import com.elsevier.events.aws.sqs.SQSHelper;
import com.elsevier.events.aws.sqs.SQSMessage;
import com.elsevier.events.aws.sqs.SQSMessageReturn;
import com.elsevier.vtw.event.processor.EventLogProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;



public class EventQueueListener implements Runnable {

    private static Logger LOG = LoggerFactory.getLogger(EventQueueListener.class);

    private final EventLogProcessor messageProcessor;
    private final String queueName;
    private final SQSHelper sqsHelper;
    private final boolean retryRequired;

    public EventQueueListener(EventLogProcessor messageProcessor, String queueName,
                              SQSHelper sqsHelper, boolean retryRequired) {
        this.messageProcessor = messageProcessor;
        this.queueName = queueName;
        this.sqsHelper = sqsHelper;
        this.retryRequired = retryRequired;
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

                boolean status = messageProcessor.processMessage(message);
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

    private void checkAndClearMessage(List<String> handles, boolean status) {
        if (retryRequired && status) {
            sqsHelper.deleteMessages(queueName, handles);
        }
    }
}
