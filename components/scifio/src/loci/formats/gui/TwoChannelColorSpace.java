/*
 * #%L
 * OME SCIFIO package for reading and converting scientific file formats.
 * %%
 * Copyright (C) 2005 - 2012 Open Microscopy Environment:
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

package loci.formats.gui;

import java.awt.color.ColorSpace;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Legacy delegator for ome.scfio.util.TwoChannelColorSpace
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="http://trac.openmicroscopy.org.uk/ome/browser/bioformats.git/components/bio-formats/src/loci/formats/gui/TwoChannelColorSpace.java">Trac</a>,
 * <a href="http://git.openmicroscopy.org/?p=bioformats.git;a=blob;f=components/bio-formats/src/loci/formats/gui/TwoChannelColorSpace.java;hb=HEAD">Gitweb</a></dd></dl>
 *
 * @author Melissa Linkert melissa at glencoesoftware.com
 */
public class TwoChannelColorSpace extends ColorSpace {

  // -- Constants --

  public static final int CS_2C = ome.scifio.util.TwoChannelColorSpace.CS_2C;
  
  private ome.scifio.util.TwoChannelColorSpace cs;

  // -- Constructor --

  protected TwoChannelColorSpace(int type, int components) {
    super(type, components);
    cs = null;
    
    /*NB: TwoChannelColorSpace has a protected constructor, and ONLY
     * a protected constructor. Thus we must use Class.forName to
     * get a handle on the appropriate class instance, and
     * reflection to find the desired 2-parameter constructor.
     */
    Class<?> tcccClass = null;
    try {
      tcccClass = Class.forName("ome.scifio.util.TwoChannelColorSpace");
    }
    catch (ClassNotFoundException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
    
    Constructor<?>[] ctorList = tcccClass.getDeclaredConstructors();
    for(int i = 0; i < ctorList.length; i++) {
      Constructor<?> constructor = ctorList[i];
      Class<?>[] pVec = constructor.getParameterTypes();
      
      if(pVec.length == 2 && pVec[0] == int.class && pVec[1] == int.class) {
        try {
          cs = (ome.scifio.util.TwoChannelColorSpace) constructor.newInstance(type, components);
        }
        catch (IllegalArgumentException e) {
          e.printStackTrace();
          throw new RuntimeException(e);
        }
        catch (InstantiationException e) {
          e.printStackTrace();
          throw new RuntimeException(e);
        }
        catch (IllegalAccessException e) {
          e.printStackTrace();
          throw new RuntimeException(e);
        }
        catch (InvocationTargetException e) {
          e.printStackTrace();
          throw new RuntimeException(e);
        }
      }
    }
    
  }

  // -- ColorSpace API methods --

  public float[] fromCIEXYZ(float[] color) {
   return cs.fromCIEXYZ(color);
  }

  public float[] fromRGB(float[] rgb) {
    return cs.fromRGB(rgb);
  }

  public static ColorSpace getInstance(int colorSpace) {
    return getInstance(colorSpace);
  }

  public String getName(int idx) {
    return cs.getName(idx);
  }

  public int getNumComponents() {
    return cs.getNumComponents();
  }

  public int getType() {
    return cs.getType();
  }

  public boolean isCS_sRGB() { return cs.isCS_sRGB(); }

  public float[] toCIEXYZ(float[] color) {
    return cs.toCIEXYZ(color);
  }

  public float[] toRGB(float[] color) {
    return cs.toRGB(color);
  }

}
