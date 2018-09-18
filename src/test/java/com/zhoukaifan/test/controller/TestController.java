package com.zhoukaifan.test.controller;

import com.yunpian.commons.vo.Click;
import com.yunpian.vsms.vendor.client.entity.VideoVendorDO;
import com.yunpian.vsms.vendor.client.sync.IVideoVendorSyncService;
import java.util.ArrayList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created with IntelliJ IDEA. User: ZhouKaifan Date:2018/9/18 Time:上午10:00
 */
@RestController
public class TestController {
    @Autowired
    private IVideoVendorSyncService videoVendorSyncService;

    @RequestMapping("test")
    public Click<ArrayList<VideoVendorDO>> test(){
        return videoVendorSyncService.findEnable();
    }
}
