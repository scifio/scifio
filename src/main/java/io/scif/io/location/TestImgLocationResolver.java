
package io.scif.io.location;

import io.scif.MetadataService;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import org.scijava.io.location.AbstractLocationResolver;
import org.scijava.io.location.Location;
import org.scijava.io.location.LocationResolver;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * Implementation of {@link LocationResolver} for {@link TestImgLocation}.
 * 
 * @author Gabriel Einsdorf
 */
@Plugin(type = LocationResolver.class)
public class TestImgLocationResolver extends AbstractLocationResolver {

	public TestImgLocationResolver() {
		super("scifioTestImg");
	}

	@Parameter
	private MetadataService meta;

	@Override
	public Location resolve(final URI uri) throws URISyntaxException {
		final String query = uri.getQuery();
		Map<String, Object> val = meta.parse(query);
		return TestImgLocation.fromMap(val);
	}
}
