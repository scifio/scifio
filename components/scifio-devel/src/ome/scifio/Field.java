package ome.scifio;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Field {

  /** Original label of this field */
  String label() default "";

  /** Whether or not this field is actually a list of fields */
  boolean isList() default false;
}
