package edu.umn.amicus.util;

/**
 * Simple exception indicating that all Views do not share the same string data.
 * Views over different documents are currently not supported, as annotations are not guaranteed to align.
 *
 * Created by gpfinley on 3/6/16.
 */
public class MismatchedSofaDataException extends Exception {

}
