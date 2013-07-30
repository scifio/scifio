/*
 * #%L
 * SCIFIO library for reading and converting scientific file formats.
 * %%
 * Copyright (C) 2011 - 2013 Open Microscopy Environment:
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

package io.scif.services;

import io.scif.SCIFIOPlugin;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.PluginInfo;
import org.scijava.plugin.PluginService;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

/**
 * Default {@link io.scif.services.PluginAttributeService} implementation.
 * 
 * @author Mark Hiner
 */
@Plugin(type = Service.class)
public class DefaultPluginAttributeService extends AbstractService implements
	PluginAttributeService
{

	// -- Parameters --

	@Parameter
	private PluginService pluginService;

	/*
	 * @see io.scif.services.PluginAttributeService#createInstance(java.lang.Class, java.util.Map, java.util.Map)
	 */
	public <PT extends SCIFIOPlugin> PT createInstance(final Class<PT> type,
		final Map<String, String> andPairs, final Map<String, String> orPairs)
	{
		return createInstance(type, andPairs, orPairs, false);
	}

	/*
	 * @see io.scif.services.PluginAttributeService#
	 * createInstance(java.lang.Class, java.util.Map, java.util.Map)
	 */
	public <PT extends SCIFIOPlugin> PT createInstance(final Class<PT> type,
		final Map<String, String> andPairs, final Map<String, String> orPairs,
		final boolean exact)
	{
		final PluginInfo<PT> plugin = getPlugin(type, andPairs, orPairs, exact);

		return plugin == null ? null : pluginService.createInstance(plugin);
	}

	/*
	 * @see io.scif.services.PluginAttributeService#
	 * getPlugin(java.lang.Class, java.util.Map, java.util.Map)
	 */
	public <PT extends SCIFIOPlugin> PluginInfo<PT> getPlugin(
		final Class<PT> type, final Map<String, String> andPairs,
		final Map<String, String> orPairs, final boolean exact)
	{
		final List<PluginInfo<PT>> pluginList =
			getPluginsOfType(type, andPairs, orPairs, exact);
		return pluginList.size() > 0 ? pluginList.get(0) : null;
	}

	/*
	 * @see io.scif.services.PluginAttributeService#
	 * getPluginsOfType(java.lang.Class, java.util.Map, java.util.Map)
	 */
	public <PT extends SCIFIOPlugin> List<PluginInfo<PT>> getPluginsOfType(
		final Class<PT> type, final Map<String, String> andPairs,
		final Map<String, String> orPairs, final boolean exact)
	{
		// Get the unfiltered plugin list
		final List<PluginInfo<PT>> plugins = pluginService.getPluginsOfType(type);

		// The list of filtered plugins we will return.
		final List<PluginInfo<PT>> filteredPlugins =
			new ArrayList<PluginInfo<PT>>();

		// loop through the unfiltered list, checking the "AND" and "OR"
		// parameters
		for (final PluginInfo<PT> info : plugins) {
			// If true, we will add this PluginInfo to our filtered list.
			boolean valid = true;

			// Checking "OR" key,value pairs. Just one @Attr needs to match
			// an entry on this list.
			if (orPairs != null) {
				boolean matchedOr = false;

				final Iterator<String> keyIter = orPairs.keySet().iterator();

				while (!matchedOr && keyIter.hasNext()) {
					final String key = keyIter.next();
					Class<?> c1 = null;
					Class<?> c2 = null;

					try {
						c1 = Class.forName(orPairs.get(key));
						c2 = Class.forName(info.get(key));
					}
					catch (final ClassNotFoundException e) {
						throw new IllegalArgumentException(
							"Class name attribute was invalid or not found.", e);
					}

					if (exact ? c2.equals(c1) : c2.isAssignableFrom(c1)) matchedOr = true;
				}

				if (!matchedOr) valid = false;
			}

			// Checking "AND" key,value pairs. All entries in this list
			// must have a matching @Attr, or this plugin will be filtered out.
			if (andPairs != null) {
				final Iterator<String> keyIter = andPairs.keySet().iterator();

				while (valid && keyIter.hasNext()) {
					final String key = keyIter.next();
					Class<?> c1 = null;
					Class<?> c2 = null;

					try {
						c1 = Class.forName(andPairs.get(key));
						c2 = Class.forName(info.get(key));
					}
					catch (final ClassNotFoundException e) {
						throw new IllegalArgumentException(
							"Class name attribute was invalid or not found.", e);
					}

					if (!(exact ? c2.equals(c1) : c2.isAssignableFrom(c1))) valid = false;
				}
			}

			if (valid) filteredPlugins.add(info);
		}

		return filteredPlugins;
	}

}
