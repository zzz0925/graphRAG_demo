package com.chat.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.tinkerpop.gremlin.driver.Client;
import org.apache.tinkerpop.gremlin.driver.Cluster;
import org.apache.tinkerpop.gremlin.driver.ser.Serializers;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class GremlinConfig implements InitializingBean, DisposableBean {
    @Value("${gremlin.gdbHost}")
    private String gdbHost;
    @Value("${gremlin.gdbPort}")
    private int gdbPort;

    @Value("${gremlin.username}")
    private String username;
    @Value("${gremlin.password}")
    private String password;

    private Cluster cluster;
    private Client client;
    @Bean("gremlinClient")
    public Client getGremlinClientBean(){
        return client;
    }

    @Override
    public void destroy() throws Exception {
        if (client != null) {
            client.close();
        }
        if (cluster != null) {
            cluster.close();
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        cluster = Cluster.build(gdbHost)
                .port(gdbPort)
                .serializer(Serializers.GRAPHBINARY_V1D0)
                .maxConnectionPoolSize(8)
                .minConnectionPoolSize(8)
                .maxContentLength(10*1024*1024)
                .credentials(username, password).create();

        log.info("Gremlin client gdbHost:{}, gdbPort:{}, username:{}, password:{}", gdbHost, gdbPort, username, password);
        client = cluster.connect().init();
    }
}
