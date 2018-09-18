package com.zhoukaifan.test;

import com.yunpian.vsms.vendor.client.VsmsVendorClientConfig;
import com.zhoukaifan.call4sc.EnableCall4scClients;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Created with IntelliJ IDEA. User: ZhouKaifan Date:2018/9/18 Time:上午9:59
 */
@SpringBootApplication
@EnableCall4scClients(VsmsVendorClientConfig.PACKAGE_PATH)
public class TestApplication {
    public static void main(String[] args){
        SpringApplication.run(TestApplication.class,args);
    }
}
