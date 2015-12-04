package com.vlasovartem.pmdb.utils.exception;

/**
 * Created by artemvlasov on 04/12/15.
 */
public class SeriesParsingException extends RuntimeException {
    public SeriesParsingException() {
    }

    public SeriesParsingException(String message) {
        super(message);
    }

    public SeriesParsingException(String message, Throwable cause) {
        super(message, cause);
    }

    public SeriesParsingException(Throwable cause) {
        super(cause);
    }

    public SeriesParsingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
