package com.challions.dao.shardbatis;

/**
 * @author qian.cheng
 */
public class ShardException extends Exception {

    public ShardException() {
        super();
    }

    public ShardException(String msg) {
        super(msg);
    }

    public ShardException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public ShardException(Throwable cause) {
        super(cause);
    }
}
