/*
 * #%L
 * SCIFIO library for reading and converting scientific file formats.
 * %%
 * Copyright (C) 2011 - 2020 SCIFIO developers.
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

package io.scif.img.cell;

import io.scif.config.SCIFIOConfig;
import io.scif.img.ImgOpener;
import io.scif.img.SCIFIOImgPlus;
import net.imglib2.type.numeric.real.FloatType;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests for the {@link SCIFIOCellImg} and related classes.
 *
 * @author Jon Fuller
 */
public class ImgOpenerNDPITest {
    private static ImgOpener opener;
    private static Path testImageFile;

    @BeforeClass
    public static void createOpener() {
        opener = new ImgOpener();
    }

    @AfterClass
    public static void disposeOpener() {
        opener.context().dispose();
    }

    @Before
    public void downloadTempFile() throws IOException {
        testImageFile = Files.createTempFile("test3-DAPI%202%20(387)%20", ".ndpi");

        URL url = new URL("https://downloads.openmicroscopy.org/images/Hamamatsu-NDPI/manuel/test3-DAPI%202%20(387)%20.ndpi");
        ReadableByteChannel readableByteChannel = Channels.newChannel(url.openStream());
        FileOutputStream fileOutputStream = new FileOutputStream(testImageFile.toFile());
        fileOutputStream.getChannel()
                .transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
    }

    @After
    public void removeTempFile() throws IOException {
        Files.delete(testImageFile);
    }

    /**
     * Test for https://github.com/scifio/scifio/issues/399
     * Test will fail after 10 seconds
     * Hangs for scifio version 0.40.0 and 0.41.0 (so timeout should be hit)
     * fails (with IllegalArgumentException v0.37.3 - and perhaps 0.39.2)
     */
    @Test(timeout=10000, expected=IllegalArgumentException.class)
    public void testNDPICompositeChannelLoadTimeout() {
        SCIFIOConfig config = new SCIFIOConfig();
        config.imgOpenerSetImgModes( SCIFIOConfig.ImgMode.CELL );

        loadImage(config);
    }

    /**
     * Test for https://github.com/scifio/scifio/issues/399
     * (v0.37.3) - fails (heapspace)
     * (v0.39.2) - fails (heapspace)
     */
    @Test(timeout=10000)
    public void testNDPICompositeChannelLoad() {
        SCIFIOConfig config = new SCIFIOConfig();

        loadImage(config);
    }

    private void loadImage(SCIFIOConfig config) {

        List<SCIFIOImgPlus<FloatType>> img;
        System.out.println(testImageFile.toFile().getAbsolutePath());
        img = new ImgOpener().openImgs(testImageFile.toFile().getAbsolutePath(), new FloatType(), config);

        SCIFIOImgPlus<FloatType> x = img.get(0);
        assertEquals(x.getCompositeChannelCount(), 3);

        FloatType y = x.firstElement();    // IllegalArgumentException here
        assertEquals(0f, y.get(), 0.1f);
    }

    /**
     * Test for https://github.com/scifio/scifio/issues/399
     * with a fake image (not currently possible to reproduce this way.
     */
    @Test
    public void testFakeImage() {
        // Make an id that will trigger cell creation
//        TestImgLocation loc = TestImgLocation.builder()
//                .name("test")
//                .axes(
//                    String.valueOf(Axes.X),
//                    String.valueOf(Axes.Y),
//                    String.valueOf(Axes.CHANNEL))
//                .lengths(3968, 4864, 3)
//                .units(null,null,null)
//                .pixelType(FormatTools.getPixelTypeString(
//                        FormatTools.UINT8))
//                .falseColor(true)
//                .orderCertain(false)
//                .build();
//
//        SCIFIOConfig config = new SCIFIOConfig();
//        config.imgOpenerSetImgModes( SCIFIOConfig.ImgMode.CELL );
//
//        DiskCachedCellImgOptions options = new DiskCachedCellImgOptions();
//        options = options.cellDimensions(512,512,3);
//
////        final List<SCIFIOImgPlus<FloatType>> img = opener.openImgs(loc, new FloatType(), config );
//        final List<SCIFIOImgPlus<FloatType>> img = new ImgOpener().openImgs(loc, new SCIFIOCellImgFactory<>( new FloatType(), options ) );
//        img.get(0).setCompositeChannelCount(3);
//
//        SCIFIOImgPlus<FloatType> x = img.get(0);
//        System.out.println("x: "+x);
//        FloatType y = x.firstElement();
//        System.out.println("y: "+y);
//        System.out.println(y.get());

    }
}
