package com.zhoukaifan.call4sc;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhoukaifan.call4sc.feign.DefultPathProcess;
import com.zhoukaifan.call4sc.feign.PathProcess;
import com.zhoukaifan.call4sc.feign.RibbonClientFactory;
import feign.Client;
import feign.codec.Decoder;
import feign.codec.Encoder;
import feign.optionals.OptionalDecoder;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.support.ResponseEntityDecoder;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

/**
 * @author ZhouKaifan(宸凯)
 */
public class FeignRegistrar implements ImportBeanDefinitionRegistrar,
        ResourceLoaderAware, EnvironmentAware,ApplicationContextAware{

    private static final Logger log = LoggerFactory.getLogger(FeignRegistrar.class);
    private ResourceLoader resourceLoader;
    private Environment environment;
    private ApplicationContext applicationContext;
    @Autowired
    private Decoder decoder;
    @Autowired
    private Encoder encoder;
    @Autowired
    private PathProcess process;
    @Override
    public void registerBeanDefinitions(AnnotationMetadata annotationMetadata,
            BeanDefinitionRegistry beanDefinitionRegistry) {
        Call4scConfigVO call4scConfigVO = new Call4scConfigVO();

        processEnableCall4scClient(call4scConfigVO,annotationMetadata);
        processEnvironment(call4scConfigVO,environment);
        try {
            checkoutCall4scConfigVO(call4scConfigVO);
        }catch (Call4scException e){
            e.printStackTrace();
            log.error("Call4scException:",e);
            System.exit(1);
        }

        RibbonClientFactory ribbonClientFactory = new RibbonClientFactory(call4scConfigVO.getZullAddrs());
        Reflections reflections = new Reflections(call4scConfigVO.getClientPackage());
        Set<Class<?>> set = reflections.getTypesAnnotatedWith(FeignClient.class);
        for (Class aClass : set) {
            Client client = ribbonClientFactory.getRibbonClient();
            registerClient(client,aClass,call4scConfigVO,beanDefinitionRegistry);
        }
    }
    private void processEnableCall4scClient(Call4scConfigVO call4scConfigVO,
            AnnotationMetadata annotationMetadata){
        Map<String, Object> attrs = annotationMetadata.
                getAnnotationAttributes(EnableCall4scClients.class.getName());
        Set<String> basePackages = new HashSet<String>();
        if (attrs!=null&&attrs.values().size()>0) {
            String[] basePackagesArray = (String[]) attrs.get("value");
            if (basePackagesArray!=null&&basePackagesArray.length>0) {
                basePackages.addAll(Arrays.asList(basePackagesArray));
            }else {
                basePackages.add(
                        ClassUtils.getPackageName(annotationMetadata.getClassName()));
            }
        }
        call4scConfigVO.setClientPackage(basePackages);
    }
    private void processEnvironment(Call4scConfigVO call4scConfigVO,
            Environment environment){

        if (call4scConfigVO.getClientPackage().isEmpty()) {
            String basePackagesStr = environment.getProperty("call4sc.client-package");
            call4scConfigVO.getClientPackage().addAll(Arrays.asList(basePackagesStr.trim().split(",")));
        }
        String zullAddrs = environment.getProperty("call4sc.zull-addrs");

        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        ObjectFactory<HttpMessageConverters> messageConverters = new ObjectFactory<HttpMessageConverters>() {
            @Override
            public HttpMessageConverters getObject() throws BeansException {
                HttpMessageConverter jacksonConverter = new MappingJackson2HttpMessageConverter(objectMapper);
                return new HttpMessageConverters(jacksonConverter);
            }
        };
        if (encoder == null) {
            encoder = new SpringEncoder(messageConverters);
        }
        if (decoder == null) {
            decoder = new OptionalDecoder(
                    new ResponseEntityDecoder(new SpringDecoder(messageConverters)));
        }
        if (process==null){
            process = new DefultPathProcess();
        }
        call4scConfigVO.setZullAddrs(zullAddrs);
        call4scConfigVO.setDecoder(decoder);
        call4scConfigVO.setEncoder(encoder);
        call4scConfigVO.setPathProcess(process);
    }

    private void registerClient(Client client,Class aClass,
            Call4scConfigVO call4scConfigVO,BeanDefinitionRegistry beanDefinitionRegistry){
        String beanName = aClass.getSimpleName() + "FeignClient";
        GenericBeanDefinition genericBeanDefinition = new GenericBeanDefinition();
        genericBeanDefinition.setBeanClass(FeignClientFactory.class);
        ConstructorArgumentValues argumentValues = new ConstructorArgumentValues();
        argumentValues.addIndexedArgumentValue(0, aClass);
        argumentValues.addIndexedArgumentValue(1, client);
        argumentValues.addIndexedArgumentValue(2, call4scConfigVO);
        genericBeanDefinition.setFactoryMethodName("getFeignClient");
        genericBeanDefinition.setScope("singleton");       //设置scope
        genericBeanDefinition.setLazyInit(false);          //设置是否懒加载
        genericBeanDefinition.setAutowireCandidate(true);  //设置是否可以被其他对象自动注入
        genericBeanDefinition.setConstructorArgumentValues(argumentValues);
        beanDefinitionRegistry.registerBeanDefinition(beanName, genericBeanDefinition);
    }
    private void checkoutCall4scConfigVO(Call4scConfigVO call4scConfigVO) throws Call4scException {
        if (call4scConfigVO==null){
            throw new Call4scException("call4scConfigVO is null");
        }
        if (call4scConfigVO.getPathProcess()==null){
            throw new Call4scException("PathProcess is null");
        }
        if (call4scConfigVO.getDecoder()==null){
            throw new Call4scException("Decoder is null");
        }
        if (call4scConfigVO.getEncoder()==null){
            throw new Call4scException("Encoder is null");
        }
        if (call4scConfigVO.getClientPackage()==null||
                call4scConfigVO.getClientPackage().isEmpty()){
            throw new Call4scException("ClientPackage is null");
        }
        if (StringUtils.isEmpty(call4scConfigVO.getZullAddrs())){
            throw new Call4scException("ZullAddrs is null");
        }
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;

    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
