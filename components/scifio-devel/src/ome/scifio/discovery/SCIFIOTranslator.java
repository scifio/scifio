package ome.scifio.discovery;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.java.sezpoz.Indexable;
import ome.scifio.Metadata;
import ome.scifio.Translator;

/**
 * 
 * Sezpoz annotation to mark SCIFIO Translators for discovery.
 * 
 * @author Mark Hiner
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Indexable(type = Translator.class)
public @interface SCIFIOTranslator {
  Class<? extends Metadata> metaIn();

  Class<? extends Metadata> metaOut();
}
