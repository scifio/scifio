package ome.scifio.io.img.cell;

import java.io.Serializable;

import net.imglib2.img.cell.AbstractCell;

public class SCIFIOCell<A> extends AbstractCell<A> implements Serializable {
  private static final long serialVersionUID = 660070520155729477L;

  public SCIFIOCell(final int[] dimensions, final long[] min, final A data) {
    super(dimensions, min);
    this.data = data;
  }

  private final A data;

  public A getData() {
    return data;
  }

  @Override
  public boolean equals(final Object other) {
    if (this == other)
      return true;
    if (other instanceof SCIFIOCell<?>) {
      return data.toString().equals(
          ((SCIFIOCell<?>) other).getData().toString());
    }
    return false;
  }

  @Override
  public int hashCode() {
    return data.toString().hashCode();
  }
}