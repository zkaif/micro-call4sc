package com.zhoukaifan.call4sc;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.context.annotation.Import;

/**
 * @author ZhouKaifan(宸凯)
 * @since 0.1.0
 */
@Target(ElementType.TYPE)
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Import(FeignRegistrar.class)
public @interface EnableCall4scClients {
    String[] value() default {};
}
