package edu.umn.amicus;

/**
 * todo: flesh this out a little more (and document)
 *
 * Created by gpfinley on 12/14/16.
 */
public class AmicusConfigurationException extends RuntimeException {

    public AmicusConfigurationException(String message) {
        super(message);
    }

    public AmicusConfigurationException(String message, Object... vars) {
        super(String.format(message, vars));
    }

    public AmicusConfigurationException(Throwable e) {
        super(e);
    }

}
