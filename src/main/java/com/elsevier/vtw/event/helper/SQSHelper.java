package com.elsevier.vtw.event.helper;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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

    public SQSMessageReturn enqueueSingleMessage(String queueName, String message) {
        try {
            logger.info("Sending a message to '{}' with message {}.", queueName, message);
            SQSMessageReturn msgReturn = new SQSMessageReturn();
            SendMessageResult result = this.sqs.sendMessage(new SendMessageRequest(this.prepareQueueURL(queueName), message));
            logger.debug("Message Id {}", result.getMessageId());
            msgReturn.setId(result.getMessageId());
            msgReturn.setCheckSum(result.getMD5OfMessageBody());
            return msgReturn;
        } catch (AmazonServiceException var5) {
            this.logErrorMessage(var5);
            throw new RuntimeException(var5);
        }
    }

    public List<SQSMessageReturn> dequeueMessages(String queueName, int messageCount) {
        try {
            List<SQSMessageReturn> messagesToReturn = new ArrayList();
            String queueUrl = this.prepareQueueURL(queueName);
            ReceiveMessageRequest request = (new ReceiveMessageRequest(queueUrl)).withMaxNumberOfMessages(messageCount).withAttributeNames(ATTRIBUTES);
            List<Message> messagesFromQueue = sqs.receiveMessage(request).getMessages();
            Iterator var7 = messagesFromQueue.iterator();

            while(var7.hasNext()) {
                Message message = (Message)var7.next();
                SQSMessageReturn msgReturn = this.prepareMessageReturn(message);
                messagesToReturn.add(msgReturn);
            }

            return messagesToReturn;
        } catch (AmazonServiceException var10) {
            this.logErrorMessage(var10);
            throw new RuntimeException(var10);
        }
    }

    public void deleteMessages(String queueName, List<String> handles) {
        try {
            List<DeleteMessageBatchRequestEntry> deleteEntries = new ArrayList();
            int index = 0;

            for(Iterator var6 = handles.iterator(); var6.hasNext(); ++index) {
                String handle = (String)var6.next();
                DeleteMessageBatchRequestEntry entry = new DeleteMessageBatchRequestEntry(Integer.toString(index), handle);
                deleteEntries.add(entry);
            }

            this.sqs.deleteMessageBatch(new DeleteMessageBatchRequest(this.prepareQueueURL(queueName), deleteEntries));
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
        StringBuilder errorMsg = (new StringBuilder()).append("Error Msg:").append(ase.getMessage()).append("Status code:").append(ase.getStatusCode()).append("Error code:").append(ase.getErrorCode()).append("Error Type").append(ase.getErrorType()).append("Request ID:").append(ase.getRequestId());
        logger.error(errorMsg.toString());
    }
}
