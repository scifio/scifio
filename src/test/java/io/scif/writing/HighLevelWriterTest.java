package io.scif.writing;

import static org.junit.Assert.assertEquals;

import io.scif.img.IO;
import io.scif.io.location.TestImgLocation;
import io.scif.services.DatasetIOService;
import io.scif.util.ImageHash;

import java.io.File;
import java.io.IOException;

import net.imagej.Dataset;
import net.imagej.DatasetService;
import net.imagej.ImgPlus;
import net.imglib2.type.numeric.RealType;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.scijava.Context;


public class HighLevelWriterTest {

	private Context context;
	private DatasetIOService datasetIOService;
	private DatasetService datasetService;
	
	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	@Before
	public void setUp() {
		context = new Context();
		datasetIOService = context.getService(DatasetIOService.class);
		datasetService = context.getService(DatasetService.class);
	}

	@After
	public void tearDown() {
		context.dispose();
	}

	@SuppressWarnings("unchecked")
	@Test
	public <T extends RealType<T>> void testSavingICS() throws IOException {
		ImgPlus<T> sourceImg = (ImgPlus<T>) IO.open(new TestImgLocation.Builder().name(
				"testimg").pixelType("uint8").axes("X", "Y", "C", "Z", "T").lengths(32,
					32, 3, 5, 4).build());
		File tempFile = new File(folder.getRoot().getAbsolutePath(), "testSavingICS.ics");
		//System.out.println(tempFile.getAbsolutePath());
		Dataset dataset = datasetService.create(sourceImg);
		datasetIOService.save(dataset, tempFile.getAbsolutePath());
		Dataset written = datasetIOService.open(tempFile.getAbsolutePath());
		assertEquals(ImageHash.hashImg(sourceImg), ImageHash.hashImg(written));
	}
}
