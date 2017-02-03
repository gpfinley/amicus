package edu.umn.ensembles;

/**
 * todo: flesh this out a little more (and document)
 *
 * Created by gpfinley on 12/14/16.
 */
public class EnsemblesException extends RuntimeException {

    public EnsemblesException() {
        super();
    }

    public EnsemblesException(String message) {
        super(message);
    }

    public EnsemblesException(String message, String... vars) {
        super(String.format(message, vars));
    }

    // todo: test and possibly develop more
    public EnsemblesException(Throwable e) {
        super(e);
        e.printStackTrace();
    }
}
