package edu.umn.amicus;

/**
 * Exception used by AMICUS analysis pieces. UIMA engines should catch this and log where the error occurred.
 *
 * Created by gpfinley on 12/14/16.
 */
public class AmicusException extends Exception {

    public AmicusException(String message, Object... vars) {
        super(String.format(message, vars));
    }

    public AmicusException(Throwable e) {
        super(e);
    }
}
