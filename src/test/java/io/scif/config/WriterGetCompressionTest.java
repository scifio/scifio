
package io.scif.config;

import static org.junit.Assert.fail;

import io.scif.codec.CompressionType;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.scijava.Context;

public class WriterGetCompressionTest {

	private Context context;
	
	@Before
	public void init() {
		context = new Context();
	}
	
	@Test
	public void testWriterGetCompression() {
		SCIFIOConfig scifioConfig = new SCIFIOConfig(context);

		// test invalid compression
		scifioConfig.writerSetCompression("Invalid");
		if (scifioConfig.writerGetCompression() != null) {
			fail(
				"Compression is expected to be null when using invlalid compressiong value, but is set to " +
					scifioConfig.writerGetCompression());
		}

		// test valid compression
		for (CompressionType ct : CompressionType.values()) {
			scifioConfig.writerSetCompression(ct.getCompression());
			if (!scifioConfig.writerGetCompression().equals(ct.getCompression())) {
				fail("Method did not set scifioConfig's non-static field to " + ct
					.getCompression() + ".");
			}
		}
	}
	
	@After
	public void tearDown() {
		context.dispose();
	}

}
