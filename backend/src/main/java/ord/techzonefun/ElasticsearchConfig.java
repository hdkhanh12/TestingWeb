package ord.techzonefun;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.elasticsearch.client.RestClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticsearchConfig {

    @Bean
    public ElasticsearchClient elasticsearchClient() {
        BasicCredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials("elastic", "WlV3R1g1VUJGUUpBM0ZNeXpZTGU6dnVZWTNPZVdrY3ROcDI2eFRNQnBoUQ=="));

        CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultCredentialsProvider(credsProvider)
                .build();

        RestClient restClient = RestClient.builder(
                        new org.apache.http.HttpHost("my-elasticsearch-project-c216e8.es.ap-southeast-1.aws.elastic.cloud", 443, "https"))
                .setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder.setDefaultCredentialsProvider(credsProvider))
                .build();

        RestClientTransport transport = new RestClientTransport(
                restClient, new JacksonJsonpMapper());

        return new ElasticsearchClient(transport);
    }
}
