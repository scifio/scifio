/*
 * #%L
 * OME SCIFIO package for reading and converting scientific file formats.
 * %%
 * Copyright (C) 2005 - 2013 Open Microscopy Environment:
 *   - Board of Regents of the University of Wisconsin-Madison
 *   - Glencoe Software, Inc.
 *   - University of Dundee
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
 * 
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of any organization.
 * #L%
 */
package ome.scifio.utests;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertEquals;

import java.io.IOException;

import ome.scifio.DatasetMetadata;
import ome.scifio.DefaultDatasetMetadata;
import ome.scifio.Format;
import ome.scifio.FormatException;
import ome.scifio.Metadata;
import ome.scifio.Parser;
import ome.scifio.SCIFIO;
import ome.scifio.fake.FakeFormat;

import org.scijava.Context;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Unit tests for {@link ome.scifio.DatasetMetadata} interface and 
 * {@link ome.scifio.ImageMetadata} methods.
 * 
 * @author Mark Hiner
 *
 */
@Test(groups="datasetMetadataTests")
public class DatasetMetadataTest {

  private Context context;
  private Parser p;
  private Format f;
  private DatasetMetadata dm;
  
  private String noC = "8bit-unsigned&pixelType=uint8&sizeZ=1&sizeC=5&sizeT=1.fake";
  private String rgb = "8bit-unsigned&pixelType=uint8&sizeZ=5&sizeC=9&sizeT=7&sizeX=128&sizeY=128&rgb=3.fake";
  
  @BeforeMethod
  public void setUp() throws FormatException {
    context = new Context();
    f = context.getService(SCIFIO.class).formats().getFormatFromClass(FakeFormat.class);
    p = f.createParser();
    dm = new DefaultDatasetMetadata(context);
  }
  
  @Test
  public void test1() throws IOException, FormatException {
    initializeMeta(noC);
    
//    assertEquals(dm.getEffectiveSizeC(0), 0);
  }
  
  @Test
  public void test2() throws IOException, FormatException {
    initializeMeta(rgb);
    
  }
  
  
  @AfterMethod
  public void tearDown() {
    context = null;
    f = null;
    p = null;
    dm = null;
  }
  
  // -- Helper Methods --
  
  private void initializeMeta(String id) throws IOException, FormatException {
    Metadata m = p.parse(id);
    dm.reset(dm.getClass());
    f.findSourceTranslator(dm).translate(m, dm);
  }
}
