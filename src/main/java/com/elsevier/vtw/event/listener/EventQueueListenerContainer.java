package com.elsevier.vtw.event.listener;

import com.elsevier.vtw.event.helper.SQSHelper;
import com.elsevier.vtw.event.processor.EventLogProcessor;
import org.springframework.core.task.TaskExecutor;

public class EventQueueListenerContainer {

    private static final boolean RETRY = true;

    private final int threadCount;
    private final String queueName;
    private final TaskExecutor taskExecutor;
    private final EventLogProcessor messageProcessor;
    private final SQSHelper sqsHelper;

    public EventQueueListenerContainer(EventLogProcessor messageProcessor, TaskExecutor taskExecutor,
                                       int threadCount, String queueName, SQSHelper sqsHelper) {
        this.sqsHelper = sqsHelper;
        this.messageProcessor = messageProcessor;
        this.taskExecutor = taskExecutor;
        this.threadCount = threadCount;
        this.queueName = queueName;
    }

    public void runListeners() {

        for (int index = 0; index < threadCount; index++) {
            taskExecutor.execute(newQueueListener());
        }
    }

    private EventQueueListener newQueueListener() {
        return new EventQueueListener(messageProcessor, queueName, RETRY, sqsHelper);
    }
}
