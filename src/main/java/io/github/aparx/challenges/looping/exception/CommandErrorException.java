package io.github.aparx.challenges.looping.exception;

/**
 * @author aparx (Vinzent Zeband)
 * @version 02:41 CET, 07.08.2022
 * @since 1.0
 */
public class CommandErrorException extends RuntimeException {

    public CommandErrorException() {
    }

    public CommandErrorException(String message) {
        super(message);
    }

    public CommandErrorException(String message, Throwable cause) {
        super(message, cause);
    }

    public CommandErrorException(Throwable cause) {
        super(cause);
    }

    public CommandErrorException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
