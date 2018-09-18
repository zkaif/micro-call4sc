package com.zhoukaifan.call4sc.feign;

import java.util.List;

/**
 * @author ZhouKaifan(宸凯)
 */
public class DefultPathProcess implements PathProcess {

    @Override
    public String process(List<String> paths) {
        StringBuilder stringBuilder = new StringBuilder("http://");
        for (String path:paths){
            stringBuilder.append(path).append("/");
        }
        return stringBuilder.toString();
    }
}
