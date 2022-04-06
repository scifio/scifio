
package io.scif;

import java.io.IOException;

import net.imagej.Dataset;

import org.junit.Test;
import org.scijava.io.location.FileLocation;


public class FormatServiceNullTest {

	private static SCIFIO scifio;

	@Test
	public void testNull() throws NullPointerException,
		FormatException,
		IOException
	{
		// SCIFIOConfig config = new SCIFIOConfig().checkerSetOpen(true);

		Dataset dataset = scifio.datasetIO().open(new FileLocation("test.stk"));

		Format format = scifio.format().getFormat(new FileLocation("test.stk"));
		System.out.println(format);
		
		Metadata metadata = scifio.initializer().parseMetadata(new FileLocation(
			"test.stk"));
		System.out.println(metadata);

	}
}
