package edu.umn.amicus;

/**
 * todo: flesh this out a little more (and document)
 *
 * todo: come up with a configuration formatting exception? Might make it easier for user to troubleshoot probs
 *
 * Created by gpfinley on 12/14/16.
 */
public class AmicusException extends RuntimeException {

    public AmicusException(String message) {
        super(message);
    }

    public AmicusException(String message, Object... vars) {
        super(String.format(message, vars));
    }

    // todo: test and possibly develop more
    public AmicusException(Throwable e) {
        super(e);
    }
}
