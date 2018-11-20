
package io.scif.util;

import java.io.IOException;

import org.scijava.io.location.FileLocation;
import org.scijava.io.location.Location;
import org.scijava.service.Service;

public interface SampleFileService extends Service {

	/**
	 * Retrieves the compressed sample folder from the source location, extracts
	 * it to a temporary folder and returns it's {@link Location}.
	 * 
	 * @param zipSource the source location for the
	 * @return the decompressed folder
	 * @throws IOException 
	 */
	FileLocation prepareFormatTestFolder(Location zipSource) throws IOException;

}
