package io.scif;

import net.imglib2.Dimensions;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.type.NativeType;

public class DefaultBlock< T extends NativeType< T >, A > implements TypedBlock<T, A> {

	private Dimensions offsets;

	@Override
	public ArrayImg<T, A> getInterval() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Dimensions getOffsets() {
		return offsets;
	}

	public void setOffsets(final Dimensions offsets) {
		this.offsets = offsets;
	}

}
