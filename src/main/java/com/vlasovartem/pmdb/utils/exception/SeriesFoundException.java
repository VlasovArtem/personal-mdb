package com.vlasovartem.pmdb.utils.exception;

/**
 * Created by artemvlasov on 04/12/15.
 */
public class SeriesFoundException extends RuntimeException {
    public SeriesFoundException() {
    }

    public SeriesFoundException(String message) {
        super(message);
    }
}
