package io.scif.io;


import io.scif.SCIFIO;
import net.imglib2.img.Img;
import org.junit.Before;
import org.junit.Test;
import org.scijava.io.IOPlugin;
import org.scijava.io.IOService;
import org.scijava.io.location.Location;
import org.scijava.io.location.LocationService;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.*;
import static org.junit.Assert.assertNull;

public class TestDatasetIOPlugin {

    private SCIFIO scifio;
    private LocationService locationService;

    @Before
    public void init(){
        scifio = new SCIFIO();
        locationService = scifio.context().getService(LocationService.class);
    }

    /**
     * Test opening an image by directly calling {@link DatasetIOPlugin} using the String path.
     *
     * @throws URISyntaxException
     * @throws IOException
     */
    @Test
    public void testDatasetIOPluginOpenString() throws URISyntaxException, IOException {
        Path path = Paths.get(this.getClass().getResource("test_img.tif").toURI());
        String str_path = path.toAbsolutePath().toString();

        DatasetIOPlugin datasetIOPlugin = scifio.io().getInstance(DatasetIOPlugin.class);

        assertTrue(datasetIOPlugin.supportsOpen(str_path));

        Img img = (Img) datasetIOPlugin.open(str_path);

        assertNotNull(img);
        assertEquals(32, img.dimension(0));
    }

    /**
     * Opening an image by directly calling {@link DatasetIOPlugin} using a {@link Location}
     * throws an UnsupportedOperationException exception.
     *
     * @throws URISyntaxException
     * @throws IOException
     */
    @Test(expected = UnsupportedOperationException.class)
    public void testDatasetIOPluginOpenLocation() throws URISyntaxException, IOException {
        Location loc = locationService.resolve(this.getClass().getResource("test_img.tif").toURI());

        DatasetIOPlugin datasetIOPlugin = scifio.io().getInstance(DatasetIOPlugin.class);

        assertFalse(datasetIOPlugin.supportsOpen(loc));

        Img img = (Img) datasetIOPlugin.open(loc);
    }

    /**
     * Opening an image using {@link IOService} with a String fails.
     *
     * @throws URISyntaxException
     * @throws IOException
     */
    @Test
    public void testIOLoadingImgAsString() throws URISyntaxException, IOException {
        Path path = Paths.get(this.getClass().getResource("test_img.tif").toURI());

        IOPlugin plugin = scifio.io().getOpener(path.toAbsolutePath().toString());

        assertNull(plugin);

        Img img = (Img) scifio.io().open(path.toAbsolutePath().toString());

        assertNull(img);
    }

    /**
     * Opening an image using {@link IOService} with a Location fails.
     *
     * @throws URISyntaxException
     * @throws IOException
     */
    @Test
    public void testIOLoadingImgAsLocation() throws URISyntaxException, IOException {
        Location loc = locationService.resolve(this.getClass().getResource("test_img.tif").toURI());

        IOPlugin plugin = scifio.io().getOpener(loc);

        assertNull(plugin);

        Img img = (Img) scifio.io().open(loc);

        assertNull(img);
    }
}
