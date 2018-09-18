package com.zhoukaifan.call4sc;

import com.zhoukaifan.call4sc.feign.PathProcess;
import feign.codec.Decoder;
import feign.codec.Encoder;
import java.util.HashSet;
import java.util.Set;

/**
 * Created with IntelliJ IDEA. User: ZhouKaifan Date:2018/9/18 Time:上午8:59
 */
public class Call4scConfigVO {
    private String zullAddrs;
    private Decoder decoder;
    private Encoder encoder;
    private PathProcess pathProcess;
    private Set<String> clientPackage = new HashSet<String>();

    public String getZullAddrs() {
        return zullAddrs;
    }

    public void setZullAddrs(String zullAddrs) {
        this.zullAddrs = zullAddrs;
    }

    public Decoder getDecoder() {
        return decoder;
    }

    public void setDecoder(Decoder decoder) {
        this.decoder = decoder;
    }

    public Encoder getEncoder() {
        return encoder;
    }

    public void setEncoder(Encoder encoder) {
        this.encoder = encoder;
    }

    public PathProcess getPathProcess() {
        return pathProcess;
    }

    public void setPathProcess(PathProcess pathProcess) {
        this.pathProcess = pathProcess;
    }

    public Set<String> getClientPackage() {
        return clientPackage;
    }

    public void setClientPackage(Set<String> clientPackage) {
        this.clientPackage = clientPackage;
    }
}
