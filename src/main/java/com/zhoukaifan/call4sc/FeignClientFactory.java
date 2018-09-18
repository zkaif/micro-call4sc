package com.zhoukaifan.call4sc;

import feign.Client;
import feign.Contract;
import feign.Feign;
import feign.codec.Decoder;
import feign.codec.Encoder;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.core.annotation.AnnotationUtils;

/**
 * @author ZhouKaifan(宸凯)
 */
public class FeignClientFactory {
    private static final Logger log = LoggerFactory.getLogger(FeignClientFactory.class);
    public static <T> T getFeignClient(Class<T> aClass, Client client,Call4scConfigVO call4scConfigParent) {
        FeignClient feignClient = AnnotationUtils.findAnnotation(aClass,FeignClient.class);
        Call4scConfigVO call4scConfigVO = null;
        try {
            call4scConfigVO = getConfigByFeignClientAndParent(call4scConfigParent,feignClient);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            log.error("IllegalAccessException:",e);
            System.exit(1);
        } catch (InstantiationException e) {
            e.printStackTrace();
            log.error("InstantiationException:",e);
            System.exit(1);
        }
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
        T o = Feign.builder()
                .client(client)
                .contract(call4scConfigVO.getContract())
                .decoder(call4scConfigVO.getDecoder())
                .encoder(call4scConfigVO.getEncoder())
                .target(aClass, path);
        return o;
    }
    private static Call4scConfigVO getConfigByFeignClientAndParent(Call4scConfigVO parent,FeignClient feignClient)
            throws IllegalAccessException, InstantiationException {
        Call4scConfigVO call4scConfigVO = new Call4scConfigVO();
        for (Class aClass:feignClient.configuration()){
            if (aClass.isAssignableFrom(Decoder.class)){
                call4scConfigVO.setDecoder((Decoder) aClass.newInstance());
            }
            if (aClass.isAssignableFrom(Encoder.class)){
                call4scConfigVO.setEncoder((Encoder) aClass.newInstance());
            }
            if (aClass.isAssignableFrom(Contract.class)){
                call4scConfigVO.setContract((Contract) aClass.newInstance());
            }
        }
        call4scConfigVO.setPathProcess(parent.getPathProcess());
        call4scConfigVO.setZullAddrs(parent.getZullAddrs());
        call4scConfigVO.setClientPackage(parent.getClientPackage());
        return call4scConfigVO;
    }
}
