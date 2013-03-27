package ome.scifio.discovery;

import java.lang.annotation.Annotation;
import java.util.List;

import ome.scifio.FormatException;

public interface Discoverer<N extends Annotation, M> {

  List<M> discover() throws FormatException;

}
