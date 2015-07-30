package io.scif;

import net.imglib2.Dimensions;
import net.imglib2.Interval;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.type.NativeType;

public class DefaultBlock< T > implements TypedBlock<T> {

	private Dimensions offsets;

	@Override
	public Interval getInterval() {
		return offsets;
	}

	public void setOffsets(final Dimensions offsets) {
		this.offsets = offsets;
	}

	@Override
	public T get() {
		// TODO Auto-generated method stub
		return null;
	}

}
