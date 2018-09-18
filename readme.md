### micro-call4sc

0.1.0

1. 引入依赖

   ```xml
   <dependency>
       <groupId>com.zhoukaifan</groupId>
       <artifactId>micro-call4sc</artifactId>
       <version>0.1.0</version>
   </dependency>
   ```

2. 引入注册类

   ```java
   @Import(FeignRegistrar.class)
   //Spring Boot
   @EnableCall4scClient("com.zhoukaifan");
   ```

3. 配置文件

   ```ini
   call4sc:
     #zull地址,多个用','隔开，会进行负载均衡(必填)
     zull-addrs: localhost:8888,localhost:8889
     #feignclient所在的包,多个用','隔开(必填,使用EnableCall4scClient注解这项配置无效)
     client-package: com.zhoukaifan
   ```

4. 使用（与原生的一样去使用就好）

   ```java
   @Autowired
   private IVideoSmsTplSyncService videoSmsTplSyncService;
   @RequestMapping("test")
   public Click test() {
       return videoSmsTplSyncService.noauditPush("test", "test", "test");
   }
   ```

   ​