package com.zhoukaifan.call4sc;

/**
 * Created with IntelliJ IDEA. User: ZhouKaifan Date:2018/9/18 Time:上午9:31
 */
public class Call4scException extends Exception {

    public Call4scException() {
    }

    public Call4scException(String message) {
        super(message);
    }

    public Call4scException(String message, Throwable cause) {
        super(message, cause);
    }

    public Call4scException(Throwable cause) {
        super(cause);
    }

    public Call4scException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
