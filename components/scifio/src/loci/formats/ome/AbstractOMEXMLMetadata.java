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

package loci.formats.ome;

import java.lang.reflect.InvocationTargetException;

/**
 * A legacy delegator class for ome.xml.meta.AbstractOMEXMLMetadata
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="http://trac.openmicroscopy.org.uk/ome/browser/bioformats.git/components/bio-formats/src/loci/formats/ome/AbstractOMEXMLMetadata.java">Trac</a>,
 * <a href="http://git.openmicroscopy.org/?p=bioformats.git;a=blob;f=components/bio-formats/src/loci/formats/ome/AbstractOMEXMLMetadata.java;hb=HEAD">Gitweb</a></dd></dl>
 *
 * @author Curtis Rueden ctrueden at wisc.edu
 * @author Melissa Linkert melissa at glencoesoftware.com
 */
public abstract class AbstractOMEXMLMetadata implements OMEXMLMetadata {

  // -- Constants --

  /** XSI namespace. */
  public static final String XSI_NS =
    ome.xml.meta.AbstractOMEXMLMetadata.XSI_NS;

  /** OME-XML schema location. */
  public static final String SCHEMA = 
    ome.xml.meta.AbstractOMEXMLMetadata.SCHEMA;

  // -- Fields --

  protected loci.utils.ProtectedMethodInvoker pmi;
  
  protected ome.xml.meta.AbstractOMEXMLMetadata meta;

  // -- Constructors --

  /** Creates a new OME-XML metadata object. */
  public AbstractOMEXMLMetadata() {
    pmi = new loci.utils.ProtectedMethodInvoker();
  }

  // -- OMEXMLMetadata API methods --

  /**
   * Dumps the given OME-XML DOM tree to a string.
   * @return OME-XML as a string.
   */
  public String dumpXML() {
    return meta.dumpXML();
  }

  // -- MetadataRetrieve API methods --

  /* @see loci.formats.meta.MetadataRetrieve#getUUID() */
  public String getUUID() {
    return meta.getUUID();
  }

  // -- MetadataStore API methods --

  /* @see loci.formats.meta.MetadataStore#setRoot(Object) */
  public void setRoot(Object root) {
    meta.setRoot(root);
  }

  /* @see loci.formats.meta.MetadataStore#getRoot() */
  public Object getRoot() {
    return meta.getRoot();
  }

  /* @see loci.formats.meta.MetadataRetrieve#setUUID(String) */
  public void setUUID(String uuid) {
    meta.setUUID(uuid);
  }

  // -- Type conversion methods --

  /**
   * Converts Boolean value to Integer. Used to convert
   * from 2003-FC Laser FrequencyDoubled Boolean value
   * to Laser FrequencyMultiplication Integer value.
   */
  protected Integer booleanToInteger(Boolean value) {
    Class<?>[] c = new Class<?>[] {value.getClass()};
    Object[] o = new Object[] {value};
    try {
      return (Integer) pmi.invokeProtected(meta, "booleanToInteger", c, o);
    }
    catch (InvocationTargetException e) {
      throw new IllegalStateException(e);
    }
  }

  /**
   * Converts Integer value to Boolean. Used to convert
   * from Laser FrequencyMultiplication Integer value
   * to 2003-FC Laser FrequencyDoubled Boolean value.
   */
  protected Boolean integerToBoolean(Integer value) {
    Class<?>[] c = new Class<?>[] {value.getClass()};
    Object[] o = new Object[] {value};
    try {
      return (Boolean) pmi.invokeProtected(meta, "integerToBoolean", c, o);
    }
    catch (InvocationTargetException e) {
      throw new IllegalStateException(e);
    }  }

  /**
   * Converts Double value to Integer. Used to convert
   * from 2008-02 LogicalChannel PinholeSize Integer value
   * to LogicalChannel PinholeSize Double value.
   */
  protected Integer doubleToInteger(Double value) {
    Class<?>[] c = new Class<?>[] {value.getClass()};
    Object[] o = new Object[] {value};
    try {
      return (Integer) pmi.invokeProtected(meta, "doubleToInteger", c, o);
    }
    catch (InvocationTargetException e) {
      throw new IllegalStateException(e);
    }
  }

  /**
   * Converts Integer value to Double. Used to convert
   * from LogicalChannel PinholeSize Double value
   * to 2008-02 LogicalChannel PinholeSize Integer value.
   */
  protected Double integerToDouble(Integer value) {
    Class<?>[] c = new Class<?>[] {value.getClass()};
    Object[] o = new Object[] {value};
    try {
      return (Double) pmi.invokeProtected(meta, "integerToDouble", c, o);
    }
    catch (InvocationTargetException e) {
      throw new IllegalStateException(e);
    }
  }
}
