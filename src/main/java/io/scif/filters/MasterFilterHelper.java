/*
 * #%L
 * SCIFIO library for reading and converting scientific file formats.
 * %%
 * Copyright (C) 2011 - 2017 SCIFIO developers.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package io.scif.filters;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.scijava.Contextual;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.PluginService;

/**
 * Helper class for {@link io.scif.filters.MasterFilter} implementations. Takes
 * the place of an abstract superclass for containing boilerplate code.
 * <p>
 * As all {@code MasterFilter} implementations are also {@code Filters}, it
 * makes sense for concrete implementations of the former to be free to inherit
 * from abstract implementations of the latter. Thus, any boilerplate code that
 * would be common to all {@code MasterFilter} implementations, regardless of
 * their wrapped type, must go in a helper class to avoid multiple inheritance
 * conflicts.
 * </p>
 * <p>
 * On construction, all Filter classes annotated with {@link Plugin} are
 * discovered by the SciJava Common framework. All items whose
 * {@code wrappedClass()} return the same class as was used to construct this
 * {@code MasterFilterHelper} will be added to the list of filters for future
 * use. Filters are initially enabled or disabled based on their
 * {@code isDefaultEnabled()} annotation return value.
 * </p>
 * <p>
 * Filters can be enabled or disabled using the {@link #enable(Class)} and
 * {@link #disable(Class)} methods. This process can be done in any order, as
 * execution ordering of enabled filters is maintained automatically per their
 * natural order.
 * </p>
 *
 * @author Mark Hiner
 * @see io.scif.filters.MasterFilter
 * @see io.scif.filters.Filter
 */
public class MasterFilterHelper<T extends Contextual> extends AbstractFilter<T>
	implements MasterFilter<T>
{

	// -- Parameters --

	@Parameter
	private PluginService pluginService;

	// -- Fields --

	// The non-filter object ultimately delegated to
	private final T tail;

	// Instance map to maintain singletons of matching plugins
	private final HashMap<Class<? extends Filter>, Filter> instanceMap =
		new HashMap<>();

	// A sorted set of enabled filters
	private final TreeSet<Filter> enabled = new TreeSet<>();

	// -- Constructor --

	public MasterFilterHelper(final T wrapped,
		final Class<? extends T> wrappedClass)
	{
		super(wrappedClass);
		tail = wrapped;

		setParent(tail);

		setContext(wrapped.getContext());

		final List<Filter> filters = pluginService.createInstancesOfType(
			Filter.class);

		// check for matching filter types
		for (final Filter filter : filters) {
			if (filter.target().isAssignableFrom(wrappedClass)) {
				instanceMap.put(filter.getClass(), filter);
				if (filter.enabledDefault()) {
					enable(filter.getClass());
				}
			}
		}
	}

	// -- MasterFilter API Methods --

	@Override
	public <F extends Filter> F enable(final Class<F> filterClass) {
		@SuppressWarnings("unchecked")
		final F filter = (F) instanceMap.get(filterClass);

		if (filter != null) {
			enabled.add(filter);
			updateParents();
		}

		return filter;
	}

	@Override
	public boolean disable(final Class<? extends Filter> filterClass) {
		final Filter filter = instanceMap.get(filterClass);
		boolean disabled = false;

		if (filter != null) {
			enabled.remove(filter);
			updateParents();
			filter.reset();
			disabled = true;
		}

		return disabled;
	}

	@Override
	public T getTail() {
		return tail;
	}

	// -- Filter API Methods --

	@Override
	public Class<?> target() {
		// Helper class doesn't need a target
		return null;
	}

	@Override
	public void reset() {
		super.reset();
		enabled.clear();
		updateParents();
	}

	@Override
	public Set<Class<? extends Filter>> getFilterClasses() {
		return instanceMap.keySet();
	}

	// -- Helper Methods --

	/*
	 * Re-generates the hierarchical wrappings between each enabled filter,
	 * based on their ordering per {@link java.util.TreeSet}.
	 */
	private void updateParents() {
		if (enabled.isEmpty()) setParent(tail);
		else {
			// need to wrap in reverse order to prevent changing the state of a
			// parent's wrapped object
			final Iterator<Filter> filterIterator = enabled.descendingIterator();
			Filter currentFilter = filterIterator.next();
			currentFilter.setParent(tail);

			while (filterIterator.hasNext()) {
				final Filter nextFilter = filterIterator.next();
				nextFilter.setParent(currentFilter);
				currentFilter = nextFilter;
			}

			setParent(currentFilter);
		}
	}
}
