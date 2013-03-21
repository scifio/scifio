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

import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertEquals;

import java.io.IOException;

import ome.scifio.Checker;
import ome.scifio.Format;
import ome.scifio.FormatException;
import ome.scifio.SCIFIO;
import ome.scifio.io.RandomAccessInputStream;

import org.scijava.Context;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Unit tests for {@link ome.scifio.Checker} interface methods.
 * 
 * @author Mark Hiner
 *
 */
@Test(groups="checkerTests")
public class CheckerTest {

  private String id = "8bit-signed&pixelType=int8&sizeZ=3&sizeC=5&sizeT=7&sizeY=50.fake";
  private String falseId = "testFile.png";
  private Checker c;
  private FakeChecker fc;
  private Context context;
  
  @BeforeMethod
  public void setUp() throws FormatException {
    context = new Context();
    Format f = context.getService(SCIFIO.class).formats().getFormat(id);
    c = f.createChecker();
    fc = new FakeChecker(context, f);
  }
  
  @Test
  public void isFormatTests() throws IOException {
    boolean isFormat = false;
    
    isFormat = c.isFormat(id);
    assertTrue(isFormat);
    
    isFormat = c.isFormat(id, false);
    assertTrue(isFormat);
    
    isFormat = c.isFormat(id, true);
    assertTrue(isFormat);
    
    isFormat = c.isFormat(new RandomAccessInputStream(context, id));
    assertFalse(isFormat);
    
    isFormat = c.isFormat(falseId, false);
    assertFalse(isFormat);
  }
  
  @Test
  public void checkHeaderTest() {
    boolean isFormat = false;

    isFormat = c.checkHeader(id.getBytes());
    assertFalse(isFormat);
  }
  
  @Test
  public void suffixSufficientTests() throws IOException {
    fc.setSuffixSufficient(false);
    boolean isFormat = false;
    
    isFormat = fc.isFormat(id);
    assertTrue(isFormat);
    
    isFormat = fc.isFormat(id, false);
    assertFalse(isFormat);
    
    isFormat = fc.isFormat(id, true);
    assertTrue(isFormat);
    
    isFormat = fc.isFormat(new RandomAccessInputStream(context, id));
    assertTrue(isFormat); 
    
    isFormat = fc.checkHeader(id.getBytes());
    assertTrue(isFormat);
  }
  
  @Test
  public void hasContextTests() {
    assertNotNull(c.getContext());
  }
  
  public void hasFormatTests() {
    Format format = c.getFormat();
    
    assertNotNull(format);
    
    if (format != null) {
      assertEquals(c.getFormat().getCheckerClass(), c.getClass());
    }
  }
  
  @AfterMethod
  public void tearDown() {
    context = null;
    c = null;
    fc = null;
  }
  
  /*
   * Private inner class for testing suffix flags.
   * 
   * @author Mark Hiner
   *
   */
  private static class FakeChecker extends ome.scifio.fake.FakeFormat.Checker {
    
    // -- Constructors --
    
    public FakeChecker() {
      this(null, null);
    }
    
    public FakeChecker(final Context context, final Format format) {
      super(context, format);
    }
    
    // -- FakeChecker Methods --
    
    public void setSuffixSufficient(boolean s) {
      suffixSufficient = s;
    }
    
    public boolean isFormat(final RandomAccessInputStream stream) throws IOException
    {
      return true;
    }
  }
}
