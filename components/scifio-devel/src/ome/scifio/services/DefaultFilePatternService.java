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

package ome.scifio.services;

import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import ome.scifio.AxisGuesser;
import ome.scifio.FilePattern;
import ome.scifio.NumberFilter;
import ome.scifio.common.DataTools;
import ome.scifio.io.Location;

import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;

/**
 * Default {@link FilePatternService} implementation.
 * 
 * @see FilePatternService
 * 
 * @author Mark Hiner
 *
 */
@Plugin(type=FilePatternService.class)
public class DefaultFilePatternService extends AbstractService 
	implements FilePatternService
{

  /*
   * @see ome.scifio.services.FilePatternService#findPattern(java.lang.String)
   */
  public String findPattern(String path) {
    return findPattern(new Location(getContext(), path));
  }

  /*
   * @see ome.scifio.services.FilePatternService#findPattern(ome.scifio.io.Location)
   */
  public String findPattern(Location file) {
    return findPattern(file.getName(), file.getAbsoluteFile().getParent());
  }


  /*
   * @see ome.scifio.services.FilePatternService#findPattern(java.io.File)
   */
  public String findPattern(File file) {
    return findPattern(file.getName(), file.getAbsoluteFile().getParent());
  }

  /*
   * @see ome.scifio.services.FilePatternService#findPattern(java.lang.String, java.lang.String)
   */
  public String findPattern(String name, String dir) {
    if (dir == null) dir = ""; // current directory
    else if (!dir.equals("") && !dir.endsWith(File.separator)) {
      dir += File.separator;
    }
    Location dirFile = new Location(getContext(), dir.equals("") ? "." : dir);

    // list files in the given directory
    Location[] f = dirFile.listFiles();
    if (f == null) return null;
    String[] nameList = new String[f.length];
    for (int i=0; i<nameList.length; i++) nameList[i] = f[i].getName();

    return findPattern(name, dir, nameList);
  }

  /*
   * @see ome.scifio.services.FilePatternService#
   *   findPattern(java.lang.String, java.lang.String, java.lang.String[])
   */
  public String findPattern(String name, String dir, String[] nameList) {
    return findPattern(name, dir, nameList, null);
  }

  /*
   * @see ome.scifio.services.FilePatternService#
   *   findPattern(java.lang.String, java.lang.String, java.lang.String[], int[])
   */
  public String findPattern(String name, String dir, String[] nameList,
    int[] excludeAxes)
  {
    if (excludeAxes == null) excludeAxes = new int[0];

    if (dir == null) dir = ""; // current directory
    else if (!dir.equals("") && !dir.endsWith(File.separator)) {
      dir += File.separator;
    }

    // compile list of numerical blocks
    int len = name.length();
    int bound = (len + 1) / 2;
    int[] indexList = new int[bound];
    int[] endList = new int[bound];
    int q = 0;
    boolean num = false;
    int ndx = -1, e = 0;
    for (int i=0; i<len; i++) {
      char c = name.charAt(i);
      if (c >= '0' && c <= '9') {
        if (num) e++;
        else {
          num = true;
          ndx = i;
          e = ndx + 1;
        }
      }
      else if (num) {
        num = false;
        indexList[q] = ndx;
        endList[q] = e;
        q++;
      }
    }
    if (num) {
      indexList[q] = ndx;
      endList[q] = e;
      q++;
    }

    // analyze each block, building pattern as we go
    StringBuffer sb = new StringBuffer(dir);

    for (int i=0; i<q; i++) {
      int last = i > 0 ? endList[i - 1] : 0;
      String prefix = name.substring(last, indexList[i]);
      int axisType = AxisGuesser.getAxisType(prefix);
      if (DataTools.containsValue(excludeAxes, axisType)) {
        sb.append(name.substring(last, endList[i]));
        continue;
      }

      sb.append(prefix);
      String pre = name.substring(0, indexList[i]);
      String post = name.substring(endList[i]);

      NumberFilter filter = new NumberFilter(pre, post);
      String[] list = matchFiles(nameList, filter);
      if (list == null || list.length == 0) return null;
      if (list.length == 1) {
        // false alarm; this number block is constant
        sb.append(name.substring(indexList[i], endList[i]));
        continue;
      }
      boolean fix = true;
      for (String s : list) {
        if (s.length() != len) {
          fix = false;
          break;
        }
      }
      if (fix) {
        // tricky; this fixed-width block could represent multiple numberings
        int width = endList[i] - indexList[i];

        // check each character for duplicates
        boolean[] same = new boolean[width];
        for (int j=0; j<width; j++) {
          same[j] = true;
          int jx = indexList[i] + j;
          char c = name.charAt(jx);
          for (String s : list) {
            if (s.charAt(jx) != c) {
              same[j] = false;
              break;
            }
          }
        }

        // break down each sub-block
        int j = 0;
        while (j < width) {
          int jx = indexList[i] + j;
          if (same[j]) {
            sb.append(name.charAt(jx));
            j++;
          }
          else {
            while (j < width && !same[j]) j++;
            String p = findPattern(name, nameList, jx, indexList[i] + j, "");
            char c = indexList[i] > 0 ? name.charAt(indexList[i] - 1) : '.';
            // check if this block represents the series axis
            if (p == null && c != 'S' && c != 's' && c != 'E' && c != 'e') {
              // unable to find an appropriate breakdown of numerical blocks
              return null;
            }
            else if (p == null) {
              sb.append(name.charAt(endList[i] - 1));
            }
            else sb.append(p);
          }
        }
      }
      else {
        // assume variable-width block represents only one numbering
        BigInteger[] numbers = new BigInteger[list.length];
        for (int j=0; j<list.length; j++) {
          numbers[j] = filter.getNumber(list[j]);
        }
        Arrays.sort(numbers);
        String bounds = getBounds(numbers, false);
        if (bounds == null) return null;
        sb.append(bounds);
      }
    }
    sb.append(q > 0 ? name.substring(endList[q - 1]) : name);

    for (int i=0; i<sb.length(); i++) {
      if (sb.charAt(i) == '\\') {
        sb.insert(i, '\\');
        i++;
      }
    }

    return sb.toString();
  }

  /*
   * @see ome.scifio.services.FilePatternService#findPattern(java.lang.String[])
   */
  public String findPattern(String[] names) {
    String dir =
      names[0].substring(0, names[0].lastIndexOf(File.separator) + 1);

    StringBuffer pattern = new StringBuffer();
    pattern.append(Pattern.quote(dir));

    for (int i=0; i<names.length; i++) {
      pattern.append("(?:");
      String name =
        names[i].substring(names[i].lastIndexOf(File.separator) + 1);
      pattern.append(Pattern.quote(name));
      pattern.append(")");
      if (i < names.length - 1) {
        pattern.append("|");
      }
    }
    return pattern.toString();
  }

  /*
   * @see ome.scifio.services.FilePatternService#findImagePatterns(java.lang.String)
   */
  public String[] findImagePatterns(String base) {
    Location file = new Location(getContext(), base).getAbsoluteFile();
    Location parent = file.getParentFile();
    String[] list = parent.list(true);
    return findImagePatterns(base, parent.getAbsolutePath(), list);
  }

  /*
   * @see ome.scifio.services.FilePatternService#
   * findImagePatterns(java.lang.String, java.lang.String, java.lang.String[])
   */
  public String[] findImagePatterns(String base, String dir,
    String[] nameList)
  {
    String baseSuffix = base.substring(base.lastIndexOf(File.separator) + 1);
    int dot = baseSuffix.indexOf(".");
    if (dot < 0) baseSuffix = "";
    else baseSuffix = baseSuffix.substring(dot + 1);

    ArrayList<String> patterns = new ArrayList<String>();
    int[] exclude = new int[] {AxisGuesser.S_AXIS};
    for (String name : nameList) {
      String pattern = findPattern(name, dir, nameList, exclude);
      if (pattern == null) continue;
      int start = pattern.lastIndexOf(File.separator) + 1;
      if (start < 0) start = 0;
      String patternSuffix = pattern.substring(start);
      dot = patternSuffix.indexOf(".");
      if (dot < 0) patternSuffix = "";
      else patternSuffix = patternSuffix.substring(dot + 1);

      String checkPattern = findPattern(name, dir, nameList);
      String[] checkFiles = new FilePattern(getContext(), checkPattern).getFiles();

      if (!patterns.contains(pattern) && (!new Location(getContext(), pattern).exists() ||
        base.equals(pattern)) && patternSuffix.equals(baseSuffix) &&
        DataTools.indexOf(checkFiles, base) >= 0)
      {
        patterns.add(pattern);
      }
    }
    String[] s = patterns.toArray(new String[patterns.size()]);
    Arrays.sort(s);
    return s;
  }

  // -- Utility helper methods --

  /** Recursive method for parsing a fixed-width numerical block. */
  private String findPattern(String name,
    String[] nameList, int ndx, int end, String p)
  {
    if (ndx == end) return p;
    for (int i=end-ndx; i>=1; i--) {
      NumberFilter filter = new NumberFilter(
        name.substring(0, ndx), name.substring(ndx + i));
      String[] list = matchFiles(nameList, filter);
      BigInteger[] numbers = new BigInteger[list.length];
      for (int j=0; j<list.length; j++) {
        numbers[j] = new BigInteger(list[j].substring(ndx, ndx + i));
      }
      Arrays.sort(numbers);
      String bounds = getBounds(numbers, true);
      if (bounds == null) continue;
      String pat = findPattern(name, nameList, ndx + i, end, p + bounds);
      if (pat != null) return pat;
    }
    // no combination worked; this parse path is infeasible
    return null;
  }

  /**
   * Gets a string containing start, end and step values
   * for a sorted list of numbers.
   */
  private String getBounds(BigInteger[] numbers, boolean fixed) {
    if (numbers.length < 2) return null;
    BigInteger b = numbers[0];
    BigInteger e = numbers[numbers.length - 1];
    BigInteger s = numbers[1].subtract(b);
    if (s.equals(BigInteger.ZERO)) {
      // step size must be positive
      return null;
    }
    for (int i=2; i<numbers.length; i++) {
      if (!numbers[i].subtract(numbers[i - 1]).equals(s)) {
        // step size is not constant
        return null;
      }
    }
    String sb = b.toString();
    String se = e.toString();
    StringBuffer bounds = new StringBuffer("<");
    if (fixed) {
      int zeroes = se.length() - sb.length();
      for (int i=0; i<zeroes; i++) bounds.append("0");
    }
    bounds.append(sb);
    bounds.append("-");
    bounds.append(se);
    if (!s.equals(BigInteger.ONE)) {
      bounds.append(":");
      bounds.append(s);
    }
    bounds.append(">");
    return bounds.toString();
  }

  /** Filters the given list of filenames according to the specified filter. */
  private String[] matchFiles(String[] inFiles, NumberFilter filter) {
    List<String> list = new ArrayList<String>();
    for (int i=0; i<inFiles.length; i++) {
      if (filter.accept(inFiles[i])) list.add(inFiles[i]);
    }
    return list.toArray(new String[0]);
  }
}
