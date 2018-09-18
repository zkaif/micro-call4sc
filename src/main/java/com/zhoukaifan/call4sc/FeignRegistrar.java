package com.zhoukaifan.call4sc;

import com.zhoukaifan.call4sc.feign.DefultPathProcess;
import com.zhoukaifan.call4sc.feign.PathProcess;
import com.zhoukaifan.call4sc.feign.RibbonClientFactory;
import feign.Client;
import feign.Contract;
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
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.support.ResponseEntityDecoder;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.cloud.openfeign.support.SpringMvcContract;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

/**
 * @author ZhouKaifan(宸凯)
 */
public class FeignRegistrar implements ImportBeanDefinitionRegistrar,
        ResourceLoaderAware, EnvironmentAware,BeanFactoryAware{

    private static final Logger log = LoggerFactory.getLogger(FeignRegistrar.class);
    private ResourceLoader resourceLoader;
    private Environment environment;
    private BeanFactory beanFactory;
    private Decoder decoder;
    private Encoder encoder;
    private PathProcess process;
    private Contract contract;
    @Override
    public void registerBeanDefinitions(AnnotationMetadata annotationMetadata,
            BeanDefinitionRegistry beanDefinitionRegistry) {
        Call4scConfigVO call4scConfigVO = new Call4scConfigVO();

        setBean(call4scConfigVO);
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
        call4scConfigVO.setZullAddrs(zullAddrs);
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
        if (call4scConfigVO.getContract()==null){
            throw new Call4scException("Contract is null");
        }
        if (call4scConfigVO.getClientPackage()==null||
                call4scConfigVO.getClientPackage().isEmpty()){
            throw new Call4scException("ClientPackage is null");
        }
        if (StringUtils.isEmpty(call4scConfigVO.getZullAddrs())){
            throw new Call4scException("ZullAddrs is null");
        }
    }

    public void setBean(Call4scConfigVO call4scConfigVO){
        process = getBeanByBeanFactory(PathProcess.class);
        decoder = getBeanByBeanFactory(Decoder.class);
        encoder = getBeanByBeanFactory(Encoder.class);
        contract = getBeanByBeanFactory(Contract.class);

        ObjectFactory<HttpMessageConverters> messageConverters = new ObjectFactory<HttpMessageConverters>() {
            @Override
            public HttpMessageConverters getObject() throws BeansException {
                return new HttpMessageConverters();
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
        if (contract==null){
            contract = new SpringMvcContract();
        }
        call4scConfigVO.setDecoder(decoder);
        call4scConfigVO.setEncoder(encoder);
        call4scConfigVO.setPathProcess(process);
        call4scConfigVO.setContract(contract);
    }

    private <T> T getBeanByBeanFactory(Class<T> aClass){
        T t;
        try {
            t = beanFactory.getBean(aClass);
        }catch (NoSuchBeanDefinitionException e){
            t = null;
        }
        return t;
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
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }
}
