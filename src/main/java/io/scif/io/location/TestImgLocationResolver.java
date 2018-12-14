
package io.scif.io.location;

import io.scif.MetadataService;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import org.scijava.Priority;
import org.scijava.io.location.AbstractLocationResolver;
import org.scijava.io.location.LocationResolver;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * Implementation of {@link LocationResolver} for {@link TestImgLocation}.
 * 
 * @author Gabriel Einsdorf
 */
@Plugin(type = LocationResolver.class, priority = Priority.HIGH)
public class TestImgLocationResolver extends AbstractLocationResolver {

	public TestImgLocationResolver() {
		super("scifioTestImg");
	}

	@Parameter
	private MetadataService meta;

	@Override
	public boolean supports(final URI uri) {
		final String scheme = uri.getScheme();
		return "scifioTestImg".equals(scheme) || //
			"file".equals(scheme) && uri.getPath().endsWith(".fake");
	}

	@Override
	public TestImgLocation resolve(final URI uri) throws URISyntaxException {
		final String data;
		switch (uri.getScheme()) {
			case "scifioTestImg":
				data = uri.getHost() + "&" + uri.getQuery();
				break;
			case "file":
				final String path = uri.getPath();
				data = path.substring(0, path.length() - 5); // strip .fake extension
				break;
			default:
				throw new UnsupportedOperationException("Invalid scheme: " + uri.getScheme());
		}
		final Map<String, Object> map = meta.parse(data);
		return TestImgLocation.fromMap(map);
	}
}
