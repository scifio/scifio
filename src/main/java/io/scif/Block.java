package io.scif;

import net.imglib2.Dimensions;
import net.imglib2.Interval;

public interface Block {

	Interval getInterval();

	/**
	 * @return The offsets of this Plane relative to the origin image
	 */
	Dimensions getOffsets();

}
