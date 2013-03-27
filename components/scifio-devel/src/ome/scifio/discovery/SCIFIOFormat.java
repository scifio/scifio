package ome.scifio.discovery;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.java.sezpoz.Indexable;
import ome.scifio.Format;

/**
 * 
 * Sezpoz annotation to flag SCIFIO Checker components for discovery.
 * 
 * @author Mark Hiner
 *
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Indexable(type = Format.class)
public @interface SCIFIOFormat {

}
