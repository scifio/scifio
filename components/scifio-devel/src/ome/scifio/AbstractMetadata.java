package ome.scifio;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ome.scifio.io.RandomAccessInputStream;

public abstract class AbstractMetadata extends AbstractHasContext
  implements Metadata {

  // -- Constants --

  protected static final Logger LOGGER = LoggerFactory.getLogger(Metadata.class);

  // -- Fields --
  private RandomAccessInputStream source;

  // -- HasFormat API Methods --

  @Override
  public Format<?, ?, ?, ?, ?> getFormat() {
    return getContext().getFormatFromMetadata(getClass());
  }

  // -- Constructor --

  public AbstractMetadata(final SCIFIO ctx) {
    super(ctx);
  }

  // -- Metadata API Methods --

  /* @see Metadata#resetMeta(Class<?>) */
  @Override
  public void reset(final Class<?> type) {
    if (type == null || type == AbstractMetadata.class) return;

    for (final Field f : type.getDeclaredFields()) {
      f.setAccessible(true);

      if (Modifier.isFinal(f.getModifiers())) continue;
      final Class<?> fieldType = f.getType();
      try {
        if (fieldType == boolean.class) f.set(this, false);
        else if (fieldType == char.class) f.set(this, 0);
        else if (fieldType == double.class) f.set(this, 0.0);
        else if (fieldType == float.class) f.set(this, 0f);
        else if (fieldType == int.class) f.set(this, 0);
        else if (fieldType == long.class) f.set(this, 0l);
        else if (fieldType == short.class) f.set(this, 0);
        else f.set(this, null);
      }
      catch (final IllegalArgumentException e) {
        LOGGER.debug(e.getMessage());
      }
      catch (final IllegalAccessException e) {
        LOGGER.debug(e.getMessage());
      }

      // check superclasses and interfaces
      reset(type.getSuperclass());
      for (final Class<?> c : type.getInterfaces()) {
        reset(c);
      }
    }
  }

  /* @see Metadata#setSource(RandomAccessInputStream) */
  @Override
  public void setSource(final RandomAccessInputStream source) {
    this.source = source;
  }

  /* @see Metadata#getSource() */
  @Override
  public RandomAccessInputStream getSource() {
    return this.source;
  }

}
