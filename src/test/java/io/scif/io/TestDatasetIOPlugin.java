/*-
 * #%L
 * SCIFIO library for reading and converting scientific file formats.
 * %%
 * Copyright (C) 2011 - 2021 SCIFIO developers.
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
 * #L%
 */
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
     * Opening an image by directly calling {@link DatasetIOPlugin} using a {@link Location}.
     *
     * @throws URISyntaxException
     * @throws IOException
     */
    @Test
    public void testDatasetIOPluginOpenLocation() throws URISyntaxException, IOException {
        Location loc = locationService.resolve(this.getClass().getResource("test_img.tif").toURI());

        DatasetIOPlugin datasetIOPlugin = scifio.io().getInstance(DatasetIOPlugin.class);

        assertTrue(datasetIOPlugin.supportsOpen(loc));

        Img img = (Img) datasetIOPlugin.open(loc);

        assertNotNull(img);
        assertEquals(32, img.dimension(0));
    }

    /**
     * Opening an image using {@link IOService} with a String path.
     *
     * @throws URISyntaxException
     * @throws IOException
     */
    @Test
    public void testIOLoadingImgAsString() throws URISyntaxException, IOException {
        Path path = Paths.get(this.getClass().getResource("test_img.tif").toURI());

        IOPlugin plugin = scifio.io().getOpener(path.toAbsolutePath().toString());

        assertNotNull(plugin);

        Img img = (Img) scifio.io().open(path.toAbsolutePath().toString());

        assertNotNull(img);
        assertEquals(32, img.dimension(0));
    }

    /**
     * Opening an image using {@link IOService} with a Location.
     *
     * @throws URISyntaxException
     * @throws IOException
     */
    @Test
    public void testIOLoadingImgAsLocation() throws URISyntaxException, IOException {
        Location loc = locationService.resolve(this.getClass().getResource("test_img.tif").toURI());

        IOPlugin plugin = scifio.io().getOpener(loc);

        assertNotNull(plugin);

        Img img = (Img) scifio.io().open(loc);

        assertNotNull(img);
        assertEquals(32, img.dimension(0));
    }
}
