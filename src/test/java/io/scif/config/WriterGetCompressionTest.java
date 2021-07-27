
package io.scif.config;

import static org.junit.Assert.fail;

import io.scif.codec.CompressionType;

import org.junit.Test;

public class SCIFIOConfigTest {

	@Test
	public void testWriterGetCompression() {
		SCIFIOConfig scifioConfig = new SCIFIOConfig();
		// use a invalid string
		if (scifioConfig.writerSetCompression("Invalid") != null) {
			fail(
				"Null expected when using invlalid compressiong value, but not returned");
		}
		
		for (CompressionType ct : CompressionType.values()) {
			if (scifioConfig.writerSetCompression(ct.getCompression()) == null) {
				fail("An instance of CompressionType is expected, but returned null.");
			}
			if (!scifioConfig.writerGetCompression().equals(ct.getCompression())) {
				fail("Method did not set scifioConfig's non-static field.");
			}
		}
	}

}
