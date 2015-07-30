package io.scif;

import org.scijava.convert.ConvertService;
import org.scijava.plugin.Parameter;

import net.imglib2.Interval;

public abstract class AbstractBlock<T> implements TypedBlock<T> {

	@Parameter
	ConvertService convertService;

	@Override
	public Interval getInterval() {
		return convertService.convert(get(), Interval.class);
	}

}
