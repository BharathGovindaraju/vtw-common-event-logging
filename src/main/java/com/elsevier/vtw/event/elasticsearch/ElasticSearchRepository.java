package com.elsevier.vtw.event.elasticsearch;

import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.config.HttpClientConfig;
import io.searchbox.core.Index;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;

public class ElasticSearchRepository {
    private static final Logger LOG = LoggerFactory.getLogger(ElasticSearchRepository.class);
    private JestClient client;
    private String endpoint;
    private String celIndexRoot;
    private int esRequestTimeout;
    private String baseUrl;
    private int maxSearchResults;
    private String username;
    private String password;

    @Autowired
    public ElasticSearchRepository(@Value("cr{cel.es.endpoint}") String endpoint, @Value("cr{cel.index.root.name}") String celIndexRoot, @Value("cr{cel.es.request.timeout}") int esRequestTimeout, @Value("cr{cel.es.baseUrl}") String baseUrl, @Value("cr{cel.max.search.results}") int maxSearchResults, @Value("cr{cel.es.client.username}") String username, @Value("cr{cel.es.client.password}") String password) {
        this.endpoint = endpoint;
        this.celIndexRoot = celIndexRoot;
        this.esRequestTimeout = esRequestTimeout;
        this.baseUrl = baseUrl;
        this.maxSearchResults = maxSearchResults;
        this.username = username;
        this.password = password;
        LOG.debug("Initialised HTTP Client");
    }

    @PostConstruct
    public void createClient() {
        JestClientFactory factory = new JestClientFactory();
        CredentialsProvider credentialsProvider = this.createCredentialsProvider();
        factory.setHttpClientConfig(((HttpClientConfig.Builder)((HttpClientConfig.Builder)(new HttpClientConfig.Builder(this.endpoint)).multiThreaded(true)).connTimeout(this.esRequestTimeout)).credentialsProvider(credentialsProvider).build());
        this.client = factory.getObject();
    }

    private CredentialsProvider createCredentialsProvider() {
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(this.username, this.password));
        return credentialsProvider;
    }

    /*
        TO DO : This method still has dependency over import com.elsevier.events.elasticsearch.IndexingHelper;
     */
    /*public void addEvent(String event) {
        IndexingHelper indexHelper = new IndexingHelper(event);
        String id = indexHelper.getId();
        String type = indexHelper.getType();
        String timestamp = indexHelper.getTimestamp();
        event = this.insertTimestamp(event, timestamp);
        LOG.debug("Adding event {} to elastic search via http client", event);
        Index index = ((io.searchbox.core.Index.Builder)((io.searchbox.core.Index.Builder)((io.searchbox.core.Index.Builder)(new io.searchbox.core.Index.Builder(event)).id(id)).index(this.getCurrentIndexName())).type(type)).build();

        try {
            this.client.execute(index);
        } catch (Exception var8) {
            LOG.error("Elasticsearch Error for event:" + event, var8);
            throw new RuntimeException(var8);
        }
    }*/

    private String insertTimestamp(String event, String timestamp) {
        return StringUtils.replaceOnce(event, "{", "{\"@timestamp\" : \"" + timestamp + "\",");
    }

    public String getCurrentIndexName() {
        return this.celIndexRoot + DateTimeFormat.forPattern("-yyyy-MM").print((new DateTime()).toDateTime(DateTimeZone.UTC));
    }
}
