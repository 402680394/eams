package com.ztdx.eams.basic.config;

import com.ztdx.eams.basic.repository.CustomElasticsearchRepositoryImpl;
import com.ztdx.eams.basic.repository.CustomElasticsearchResultMapper;
import com.ztdx.eams.basic.repository.CustomElasticsearchTemplate;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.elasticsearch.xpack.client.PreBuiltXPackTransportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.TransportClientFactoryBean;
import org.springframework.data.elasticsearch.core.convert.ElasticsearchConverter;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.util.Assert;

import java.net.InetAddress;
import java.net.UnknownHostException;

import static org.apache.commons.lang.StringUtils.split;
import static org.apache.commons.lang.StringUtils.substringAfterLast;
import static org.apache.commons.lang.StringUtils.substringBeforeLast;

@Configuration
@EnableElasticsearchRepositories(
        basePackages = "com.ztdx.eams.domain.archives.repository.elasticsearch",
        repositoryBaseClass = CustomElasticsearchRepositoryImpl.class
)
public class ElasticsearchConfig {

    private static final Logger logger = LoggerFactory.getLogger(TransportClientFactoryBean.class);
    private final ElasticsearchProperties properties;
    static final String COLON = ":";
    static final String COMMA = ",";

    public ElasticsearchConfig(ElasticsearchProperties properties) {
        this.properties = properties;
    }

    @Bean
    public TransportClient transportClient() throws UnknownHostException {
        //TODO @lijie 按照自动配置在优化一下
        TransportClient client = new PreBuiltXPackTransportClient(Settings.builder()
                .put("cluster.name", properties.getClusterName())
                .put(properties.getProperties())
                .build())
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("192.168.1.200"), 9300));

        for (String clusterNode : split(properties.getClusterNodes(), COMMA)) {
            String hostName = substringBeforeLast(clusterNode, COLON);
            String port = substringAfterLast(clusterNode, COLON);
            Assert.hasText(hostName, "[Assertion failed] missing host name in 'clusterNodes'");
            Assert.hasText(port, "[Assertion failed] missing port in 'clusterNodes'");
            logger.info("adding transport node : " + clusterNode);
            client.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(hostName), Integer.valueOf(port)));
        }
        client.connectedNodes();

        return client;
    }

    @Bean
    public CustomElasticsearchTemplate elasticsearchTemplate(
            Client client,
            ElasticsearchConverter converter) {
        try {
            return new CustomElasticsearchTemplate(client, converter, new CustomElasticsearchResultMapper(converter.getMappingContext()));
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }
}
