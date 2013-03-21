/*
 * #%L
 * Legacy layer preserving compatibility between legacy Bio-Formats and SCIFIO.
 * %%
 * Copyright (C) 2008 - 2013 Open Microscopy Environment:
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
package loci.legacy.utests;

import java.io.IOException;
import java.lang.ref.WeakReference;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import loci.common.adapter.IRandomAccessAdapter;
import loci.legacy.adapter.Wrapper;

import ome.scifio.io.ByteArrayHandle;
import ome.scifio.io.IRandomAccess;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Tests for mapping legacy classes via the LegacyAdapter interface.
 * 
 * ByteArrayHandle is used as a concrete implementation because it is
 * simple to instantiate, but the target of the tests is really the
 * AbstractLegacyAdapter.
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="http://trac.openmicroscopy.org.uk/ome/browser/bioformats.git/components/common/test/loci/common/utests/RandomAccessInputStreamTest.java">Trac</a>,
 * <a href="http://git.openmicroscopy.org/?p=bioformats.git;a=blob;f=components/common/test/loci/common/utests/RandomAccessInputStreamTest.java;hb=HEAD">Gitweb</a></dd></dl>
 *
 * @see loci.legacy.adapter.AbstractLegacyAdapter
 */
@Test(groups = "legacyTests")
public class LegacyAdapterTest {

  private IRandomAccessAdapter adapter;
  private loci.common.IRandomAccess legacyIRA;
  private IRandomAccess currentIRA;
  private static final byte[] BYTES = new byte[0];
  
  @BeforeMethod
  public void setUp() {
    adapter = new IRandomAccessAdapter();
    legacyIRA = null;
    currentIRA = null;
  }
  
  // -- Mapping tests -- (using a getter creates bidirectional mappings)
  
  @Test
  public void testGetCurrent() throws IOException {
    legacyIRA = new loci.common.ByteArrayHandle(BYTES);
    currentIRA = (IRandomAccess) adapter.get(legacyIRA);
    
    System.gc();

    // verify that the linkage between the legacy and modern
    // classes wasn't lost.
    isEqual(currentIRA, adapter.get(legacyIRA));
    
    // legacyIRA should be a wrapper.. verify:
    isEqual(currentIRA, ((Wrapper<?>)legacyIRA).unwrap());
  }
  
  @Test
  public void testGetLegacy() throws IOException {
    currentIRA = new ome.scifio.io.ByteArrayHandle(BYTES);
    legacyIRA = (loci.common.IRandomAccess) adapter.get(currentIRA);

    System.gc();
    
    // verify that the linkage between the legacy and modern
    // classes wasn't lost.
    isEqual(legacyIRA, adapter.get(currentIRA));
  }
  
  @Test
  public void testWeakRefs() throws IOException {
    currentIRA = new ome.scifio.io.ByteArrayHandle(BYTES);
    WeakReference<loci.common.IRandomAccess> weakIRA =
      new WeakReference<loci.common.IRandomAccess> (
          (loci.common.IRandomAccess) adapter.get(currentIRA));
    
    System.gc();
    
    // make sure the weak reference was preserved
    assertNotNull(weakIRA.get());
    
    // make sure when the original object is GC'd, the weak reference is as well
    
    currentIRA.close();
    currentIRA = null;
    
    // NB: this test can not be relied upon because System.gc() does
    // not guarantee immediate garbage collection. This code just
    // illustrates that the reference should eventually be gc'd
    // after this point.
    
//    System.gc();
//    System.runFinalization();
//    
//    assertNull(weakIRA.get());
  }
  
  // -- Null tests -- (null in --> null out)
  
  @Test
  public void testCurrentNull() {
    legacyIRA = null;
    currentIRA = (ByteArrayHandle) adapter.get(legacyIRA);
    
    assertEquals(currentIRA, null);
  }
  
  @Test
  public void testLegacyNull() {
    currentIRA = null;
    legacyIRA = (loci.common.ByteArrayHandle) adapter.get(currentIRA);
    
    assertEquals(legacyIRA, null);
  }
  
  private void isEqual(Object ob1, Object ob2) {
    boolean test = ob1 == ob2;
    
    assertEquals(test, true);
  }
  
  @AfterMethod
  public void tearDown() throws IOException {
    if (legacyIRA != null) legacyIRA.close();
    if (currentIRA != null) currentIRA.close();
    adapter.clear();
  }
}
