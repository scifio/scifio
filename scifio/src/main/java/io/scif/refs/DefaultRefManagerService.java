/*
 * #%L
 * SCIFIO library for reading and converting scientific file formats.
 * %%
 * Copyright (C) 2011 - 2014 Open Microscopy Environment:
 *   - Board of Regents of the University of Wisconsin-Madison
 *   - Glencoe Software, Inc.
 *   - University of Dundee
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
 * 
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of any organization.
 * #L%
 */

package io.scif.refs;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.PluginService;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;
import org.scijava.thread.ThreadService;

/**
 * Default {@link RefManagerService} implementation.
 * 
 * @author Mark Hiner
 */
@Plugin(type = Service.class)
public class DefaultRefManagerService extends AbstractService implements
	RefManagerService
{

	// -- Parameters --

	@Parameter
	private ThreadService threadService;

	@Parameter
	private PluginService pluginService;

	@Parameter
	private LogService logService;

	// -- Fields --

	/**
	 * Maps managed objects to the reference types which refer to them, ensuring
	 * individual instances are not managed in the same way.
	 */
	private Map<Object, Set<Class<?>>> managed =
		new WeakHashMap<Object, Set<Class<?>>>();

	/**
	 * A list of all managed references.
	 */
	private Set<Reference> knownRefs = new HashSet<Reference>();

	/**
	 * Queue used for all references. This is polled on a separate thread, and
	 * whenever a reference is pulled from the queue its
	 * {@link CleaningRef#cleanup()} method is called.
	 */
	private ReferenceQueue queue;

	// -- RefManagerService API --

	@Override
	public void manage(Object toManage, Object... params) {
		// Check known RefProviders for all appropriate constructors
		for (RefProvider refProvider : pluginService
			.createInstancesOfType(RefProvider.class))
		{
			if (!isManaged(toManage, refProvider.getClass()) &&
				refProvider.handles(toManage, params))
			{
				// found a match
				Reference ref = refProvider.makeRef(toManage, queue, params);

				synchronized (managed) {
					Set<Class<?>> refs = managed.get(toManage);

					if (refs == null) {
						refs = new HashSet<Class<?>>();
						managed.put(toManage, refs);
					}
					refs.add(ref.getClass());
				}

				synchronized (knownRefs) {
					knownRefs.add(ref);

					if (knownRefs.size() == 1) {
						// If this is the first entry in knownRefs, start a RefCleaner
						// thread
						threadService.run(new RefCleaner(queue, knownRefs, logService));
					}
				}
			}
		}
	}

	// -- Service API --

	@Override
	public void dispose() {
		// Basically.. if there are any pending Weak/Phantom references that haven't
		// been queued yet, this should clear them. During the dispose process,
		// there is probably not going to be more object allocation that could
		// naturally trigger the GC, thus we need to give some encouragement.
		System.gc();
	}

	@Override
	public void initialize() {
		// Set default values
		queue = new ReferenceQueue();
	}

	// -- Helper Methods --

	/**
	 * @return true iff a {@link RefProvider} of the given class was constructed
	 *         around the given object
	 */
	private boolean isManaged(Object referent, Class<?> pClass) {
		synchronized (managed) {
			Set<Class<?>> refs = managed.get(referent);
			if (refs != null && refs.contains(pClass)) return true;
		}
		return false;
	}

	// -- Helper class --

	/**
	 * {@link ReferenceQueue} manager for running on a separate thread. As long as
	 * the set of references provided is not empty, the queue will be polled.
	 * Removed {@link CleaningRef} instances will have their
	 * {@link CleaningRef#cleanup()} method invoked, and then will be removed from
	 * the originally provided {@code RefProvider} set.
	 * <p>
	 * NB: make sure that as new refs are added to the provided ref set, they are
	 * registered with the given queue! Otherwise it's quite possible this thread
	 * will never die and resources will not properly be cleaned.
	 * </p>
	 * 
	 * @author Mark Hiner
	 */
	private static class RefCleaner implements Runnable {

		// -- Fields --

		private ReferenceQueue queue;
		private Set<Reference> refs;
		private LogService logService;

		// -- Constructor --

		public RefCleaner(ReferenceQueue queue, Set<Reference> refs, LogService log)
		{
			this.queue = queue;
			this.refs = refs;
			logService = log;
		}

		// -- Runnable API --

		@Override
		public void run() {
			int size = refs.size();
			while (size > 0) {
				CleaningRef cleaningRef = null;
				try {
					cleaningRef = (CleaningRef) queue.remove(50);
				}
				catch (InterruptedException e) {
					logService.error("RefCleaner: interrupted while polling queue", e);
				}
				synchronized (refs) {
					if (cleaningRef != null) {
						// When a reference is pulled from the queue, call its cleanup
						// method and remove it from the list
						cleaningRef.cleanup();
						refs.remove(cleaningRef);
					}
					size = refs.size();
				}
			}
		}
	}
}
