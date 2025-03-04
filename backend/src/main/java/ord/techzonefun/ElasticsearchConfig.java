package ord.techzonefun;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchClients;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@Configuration
public class ElasticsearchConfig {

    @Bean
    public RestClient getRestClient() {
        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials("elastic", "WlV3R1g1VUJGUUpBM0ZNeXpZTGU6dnVZWTNPZVdrY3ROcDI2eFRNQnBoUQ=="));

        return RestClient.builder(
                        new HttpHost("my-elasticsearch-project-c216e8.es.ap-southeast-1.aws.elastic.cloud", 443, "https"))
                .setHttpClientConfigCallback(httpClientBuilder ->
                        httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider))
                .build();
    }

    @Bean
    public ElasticsearchTransport getElasticsearchTransport(RestClient restClient) {
        return new RestClientTransport(restClient, new JacksonJsonpMapper());
    }

    @Bean
    public ElasticsearchClient getElasticsearchClient(ElasticsearchTransport transport) {
        return new ElasticsearchClient(transport);
    }

    @Bean
    public ClientConfiguration clientConfiguration(RestClient restClient) {
        return ClientConfiguration.builder()
                .connectedTo("my-elasticsearch-project-c216e8.es.ap-southeast-1.aws.elastic.cloud:443")
                .usingSsl()
                .withBasicAuth("elastic", "WlV3R1g1VUJGUUpBM0ZNeXpZTGU6dnVZWTNPZVdrY3ROcDI2eFRNQnBoUQ==")
                .build();
    }

}