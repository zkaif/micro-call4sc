package com.zhoukaifan.call4sc.feign;

import com.netflix.client.ClientFactory;
import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.ILoadBalancer;
import com.netflix.loadbalancer.Server;
import com.netflix.loadbalancer.ZoneAwareLoadBalancer;
import feign.Client;
import feign.ribbon.LBClient;
import feign.ribbon.LBClientFactory;
import feign.ribbon.RibbonClient;
import feign.ribbon.RibbonClient.Builder;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ZhouKaifan(宸凯)
 */
public class RibbonClientFactory {
    private static final Logger log = LoggerFactory.getLogger(RibbonClientFactory.class);

    private Builder builder;

    public RibbonClientFactory(final String zullAddrs) {
        builder = RibbonClient.builder().lbClientFactory(new LBClientFactory() {
            @SuppressWarnings("rawtypes")
            public LBClient create(String clientName) {
                IClientConfig config = ClientFactory.getNamedConfig(clientName);
                ILoadBalancer lb = ClientFactory.getNamedLoadBalancer(clientName);
                ZoneAwareLoadBalancer zb = (ZoneAwareLoadBalancer) lb;
                List<Server> servers = new ArrayList<Server>();
                for (String addr : zullAddrs.split(",")) {
                    addr = addr.trim();
                    String[] hostAndPort = addr.split(":");
                    String host = null;
                    int port = 0;
                    if (hostAndPort.length == 1) {
                        host = hostAndPort[0];
                        port = 80;
                    } else if (hostAndPort.length == 2) {
                        host = hostAndPort[0];
                        port = Integer.parseInt(hostAndPort[1]);
                    } else {
                        log.error("zullAddrs is error");
                        System.exit(1);
                    }
                    Server s = new Server(host, port);
                    servers.add(s);
                }
                zb.setServersList(servers);
                LBClient lbClient = LBClient.create(zb, config);
                return lbClient;
            }
        });
    }

    public Client getRibbonClient(){
        return builder.build();
    }
}
