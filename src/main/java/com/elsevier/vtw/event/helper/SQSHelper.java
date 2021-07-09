package com.elsevier.vtw.event.helper;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class SQSHelper {

    private static final String[] ATTRIBUTES = new String[]{"All"};
    private static final Logger logger = LoggerFactory.getLogger(com.elsevier.vtw.event.helper.SQSHelper.class);
    private final AmazonSQS sqs;

    @Autowired
    public SQSHelper(AmazonSQS sqs) {
        this.sqs = sqs;
    }

    public SQSMessageReturn dequeueMessage(String queueName, int messageCount) {
        try {
            String queueUrl = this.prepareQueueURL(queueName);
            ReceiveMessageRequest request = (new ReceiveMessageRequest(queueUrl)).withMaxNumberOfMessages(messageCount).withAttributeNames(ATTRIBUTES);
            Message messagesFromQueue = (Message) sqs.receiveMessage(request).getMessages();
            return prepareMessageReturn(messagesFromQueue);
        } catch (AmazonServiceException var10) {
            this.logErrorMessage(var10);
            throw new RuntimeException(var10);
        }
    }

    public void deleteMessage(String queueName, String handle) {
        try {

            String queueUrl = prepareQueueURL(queueName);
            this.sqs.deleteMessage(queueUrl, handle);

        } catch (AmazonServiceException var8) {
            this.logErrorMessage(var8);
            throw new RuntimeException(var8);
        }
    }

    protected String prepareQueueURL(String queueName) {
        return this.sqs.getQueueUrl(queueName).getQueueUrl();
    }

    private SQSMessageReturn prepareMessageReturn(Message message) {
        SQSMessageReturn msgReturn = new SQSMessageReturn();
        msgReturn.setHandle(message.getReceiptHandle());
        msgReturn.setBody(message.getBody());
        msgReturn.setId(message.getMessageId());
        msgReturn.setCheckSum(message.getMD5OfBody());
        Map<String, String> attributes = message.getAttributes();
        msgReturn.setAttributes(attributes);
        return msgReturn;
    }

    private void logErrorMessage(AmazonServiceException ase) {
        String errorMsg = (new StringBuilder()).append("Error Msg:").append(ase.getMessage()).append("Status code:").append(ase.getStatusCode()).append("Error code:").append(ase.getErrorCode()).append("Error Type").append(ase.getErrorType()).append("Request ID:").append(ase.getRequestId()).toString();
        logger.error(errorMsg);
    }
}
