package com.zhoukaifan.call4sc;

import feign.Client;
import feign.Feign;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.support.SpringMvcContract;
import org.springframework.core.annotation.AnnotationUtils;

/**
 * @author ZhouKaifan(宸凯)
 */
public class FeignClientFactory {
    private static final Logger log = LoggerFactory.getLogger(FeignClientFactory.class);
    public static <T> T getFeignClient(Class<T> aClass, Client client,Call4scConfigVO call4scConfigVO) {
        FeignClient feignClient = AnnotationUtils.findAnnotation(aClass,FeignClient.class);
        List<String> paths = new ArrayList<String>();
        paths.add("zuul");
        paths.add(feignClient.value());
        String path = null;
        try {
            path = call4scConfigVO.getPathProcess().process(paths);
        }catch (Exception e){
            log.error("pathProcess error:",e);
            System.exit(1);
        }
        log.info("pathProcess:",path);
        SpringMvcContract springMvcContract = new SpringMvcContract();
        T o = Feign.builder()
                .client(client)
                .contract(springMvcContract)
                .decoder(call4scConfigVO.getDecoder())
                .encoder(call4scConfigVO.getEncoder())
                .target(aClass, path);
        return o;
    }
}
