package ome.scifio.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import ome.scifio.Metadata;

public class MetadataSerializer<M extends Metadata> {

  private Class<M> type;

  // Constructor

  private MetadataSerializer(Class<M> type) {
    this.type = type;
  }

  public M load(InputStream is) throws IOException, ClassNotFoundException {
    final ObjectInputStream ois = new ObjectInputStream(is);
    final Object o = ois.readObject();
    if (type.isInstance(o)) {
      @SuppressWarnings("unchecked")
      final M m = (M) o;
      return m;
    }
    throw new IllegalArgumentException("Invalid object from stream: " +
      o.getClass().getName());
  }

  public void save(OutputStream os, M meta) throws IOException {
    final ObjectOutputStream oos = new ObjectOutputStream(os);
    oos.writeObject(meta);
  }
}
