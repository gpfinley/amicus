package edu.umn.ensembles.processing;

/**
 * Created by gpfinley on 10/13/16.
 */
public interface Mapper<T, U> {

    U map(T t);

}