/*
 * #%L
 * Tutorials for SCIFIO API
 * %%
 * Copyright (C) 2011 - 2013 Open Microscopy Environment:
 *   - Board of Regents of the University of Wisconsin-Madison
 *   - Glencoe Software, Inc.
 *   - University of Dundee
 * %%
 * To the extent possible under law, the SCIFIO developers have waived
 * all copyright and related or neighboring rights to this tutorial code.
 * 
 * See the CC0 1.0 Universal license for details:
 * http://creativecommons.org/publicdomain/zero/1.0/
 * #L%
 */

package io.scif.tutorials;

import io.scif.Format;
import io.scif.FormatException;
import io.scif.SCIFIO;
import io.scif.formats.FakeFormat;
import io.scif.services.FormatService;

import org.scijava.Context;
import org.scijava.plugin.PluginService;

/**
 * A guide to using the scijava context for discovery in SCIFIO.
 * 
 * @author Mark Hiner
 */
public class T3aDiscoveryTutorial {

	public static void main(final String... args) throws FormatException {
		// The org.scijava.Context provides a framework for automatic discovery
		// which SCIFIO uses extensively. In the briefest of summaries, each
		// context instance is a container for two general types of discoverables:
		// - org.scijava.plugin.Plugin. Typically plugins are classified by "type",
		// a common interface, with different instances of that type required
		// depending on the situation. io.scif.Format and io.scif.Translator
		// are types of plugins you may want to discover in SCIFIO.
		// - org.scijava.service.Service. Services act more like static utility
		// classes within their parent context. Each is instantiated once
		// by the context and called upon repeatedly to provide its functionality.
		// In SCIFIO, the TranslatorService and FormatService are provided to
		// perform convenient operations regarding managing instances of the
		// Format and Translator plugins.

		// To explore these concepts more practically, we will start by creating a
		// context:
		final Context context = new Context();

		// Now, let's get a SCIFIO to work with:
		final SCIFIO scifio = new SCIFIO(context);

		// Note that we could have done the same thing more simply using:
		//
		// final SCIFIO scifio = new SCIFIO();
		//
		// The other tutorials use this shorthand, for convenience.

		// The SCIFIO gateway was created as a convenience entry point to access
		// commonly required services. For example, if we want to work with
		// Formats, we can get a FormatService implementation:
		final FormatService fService = scifio.format();

		// Note that this service could also have been retrieved from the context
		// itself, and because it is a service you would get the same instance back:
		final FormatService fService2 = context.getService(FormatService.class);
		System.out.println("Format services are equal? " + (fService == fService2));

		// Note that FormatService is an interface. If you look at
		// io.scif.DefaultFormatService you'll see that it's annotated as a Plugin
		// with type=FormatService.class. This allows it to be discovered by the
		// context as a FormatService instance. If more than one class on the
		// classpath was similarly annotated, the context would have returned the
		// instance with the highest priority field in its Plugin annotation.

		// Now let's play with a sample image path
		final String sampleImage =
			"8bit-signed&pixelType=int8&sizeZ=3&sizeC=5&sizeT=7&sizeY=50.fake";

		// We can use our FormatService to find an appropriate Format plugin:
		final Format format = fService.getFormat(sampleImage);

		// Formats are special: they are singletons within the FormatService:
		final Format format2 = fService2.getFormat(sampleImage);
		System.out.println("Formats from FormatService are equal? " +
			(format == format2));

		// We'll look at creating plugins now, using the PluginService:
		final PluginService pluginService = scifio.plugin();

		// PluginService is a very handy Service to use when you need to
		// discover or instantiate plugins. This service works with PluginInfo
		// objects, which contain metadata that describes plugins without even
		// needing to load the plugin's class using a class loader.

		// Let's use the PluginService to instantiate some Formats:
		Format format3 = null;

		for (final Format f : pluginService.createInstancesOfType(Format.class)) {
			if (f.getClass().equals(FakeFormat.class)) format3 = f;
		}
		System.out.println("FakeFormat found: " + (format3 != null));
		System.out.println("Newly created Format is equal? " + (format == format3));

		// Note that we created new Format instances here *only* for illustration.
		// In practice you would never want to instantiate one, because the
		// FormatService is already maintaining stateless singleton instances,
		// as shown above, for your use.

		// Understanding the Plugin annotation is critical to developing new SCIFIO
		// components. Some general tips to keep in mind:
		// - Use the "priority" field to control the order plugins are returned
		// - Use the "attrs" field to allow for string-matching queries.

		// For a discussion of how to define your own services, take a look at
		// T3bCustomFormats.

		// For examples of using and querying attributes, look at
		// io.scifDefaultTranslatorService,
		// io.scif.PluginAttributeService and the Translator implementations.
	}
}
