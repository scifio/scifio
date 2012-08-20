package ome.scifio.discovery;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import ome.scifio.services.Service;
import net.java.sezpoz.Indexable;

/**
 * 
 * Sezpoz annotation to mark SCIFIO Services for discovery.
 * 
 * @author Mark Hiner
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Indexable(type = Service.class)
public @interface SCIFIOService {
  String interfaceName();

  String implementationName();
}