package com.elsevier.vtw.event.listener;

import com.elsevier.vtw.event.helper.SQSHelper;
import com.elsevier.vtw.event.helper.SQSMessageReturn;
import com.elsevier.vtw.event.processor.EventLogProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventQueueListener implements Runnable {
    private final Logger logger = LoggerFactory.getLogger(com.elsevier.vtw.event.listener.EventQueueListener.class);
    private final EventLogProcessor eventLogProcessor;
    private final String queueName;
    private final boolean retryRequired;
    private final SQSHelper sqsHelper;

    public EventQueueListener(EventLogProcessor eventLogProcessor, String queueName,
                              boolean retryRequired, SQSHelper sqsHelper) {
        this.eventLogProcessor = eventLogProcessor;
        this.queueName = queueName;
        this.retryRequired = retryRequired;
        this.sqsHelper = sqsHelper;
    }

    public void run() {
        String handle = null;
        try {
            SQSMessageReturn messageReturn = sqsHelper.dequeueMessage(
                    queueName, 1);

                handle = messageReturn.getHandle();

                boolean status = eventLogProcessor.processMessage(messageReturn);
                logger.debug("Message processed, status [{}].", status);
                
                if (messageReturn.getReceiveCount() > 3) {
                    // if received more than 3 times, delete message
                    status = true;
                }
                checkAndClearMessage(handle, status);
        } catch (Exception exception) {
            // don't propagate the exception, 'cos this kills the thread and we
            // don't want that
            logger.error("Error while processing messages.", exception);
        } finally {
            // if retry is not required, delete the message irrespective of
            // status
            if (handle != null && !retryRequired) {
                logger.error("Clearing the messages in any case, {}.", handle);
                sqsHelper.deleteMessage(queueName, handle);
            }
        }
    }

    private void checkAndClearMessage(String handle, boolean status) {
        if (retryRequired && status) {
            sqsHelper.deleteMessage(queueName, handle);
        }
    }
}
