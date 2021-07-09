package com.elsevier.vtw.event.processor;

import com.elsevier.vtw.event.elasticsearch.ElasticSearchRepository;
import com.elsevier.vtw.event.helper.SQSMessageReturn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class EventLogProcessor {
    private static final Logger logger = LoggerFactory.getLogger(EventLogProcessor.class);

    private final ElasticSearchRepository elasticSearchRepository;

    @Autowired
    public EventLogProcessor(ElasticSearchRepository elasticSearchRepository) {
        this.elasticSearchRepository = elasticSearchRepository;
    }

    public boolean processMessage(SQSMessageReturn message) {
        String payload = message.getBody();
        logger.debug("Processing Message [{}].", payload);

        try {
            //TO DO
            elasticSearchRepository.addEvent(payload);
            logger.info("Logged the notification/servicecall into CEL - {}.", payload);

        } catch (Exception ex) {
            logger.warn("Exception while adding the message {} to Elasticsearch", message, ex);
            throw ex;
        }
        return true;
    }
}
