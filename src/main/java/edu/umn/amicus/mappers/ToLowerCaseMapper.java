package edu.umn.amicus.mappers;

/**
 * Map all String values to lower case
 *
 * Created by gpfinley on 10/13/16.
 */
public class ToLowerCaseMapper extends Mapper {

    protected Object mappingFunction(Object k) {
        return k == null ? null : k.toString().toLowerCase();
    }

}