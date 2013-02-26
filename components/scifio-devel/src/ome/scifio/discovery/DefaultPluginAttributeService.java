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

package ome.scifio.discovery;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ome.scifio.ScifioPlugin;

import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.PluginInfo;
import org.scijava.plugin.PluginService;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

/**
 * Service for finding plugins with particular attribute values.
 * <p>
 * In each method signature, plugins will be returned if they
 * are annotated with {@link org.scijava.plugin.Attr}'s matching
 * all key,value pairs in {@code andPairs}, and at least one of
 * any key, value pairs in {@code orPairs}.
 * </p>
 * 
 * @author Mark Hiner
 *
 */
@Plugin(type = Service.class)
public class DefaultPluginAttributeService extends AbstractService implements PluginAttributeService {
  
  // -- Parameters --
  
  @Parameter
  PluginService pluginService;

  /**
   * As {@link org.scijava.plugin.PluginService#createInstancesOfType(Class)}
   * but with key,value pair parameters to allow for filtering based on
   * {@code Attr} annotation.
   */
  public <PT extends ScifioPlugin> PT createInstance(
      Class<PT> type, Map<String, String> andPairs, Map<String, String> orPairs) {
    PluginInfo<PT> plugin = getPlugin(type, andPairs, orPairs);
    
    return plugin == null ? null : pluginService.createInstance(plugin);
  }
  
  /**
   * As {@link org.scijava.plugin.PluginService#getPlugin(Class)}
   * but with key,value pair parameters to allow for filtering based on
   * {@code Attr} annotation.
   */
  public <PT extends ScifioPlugin> PluginInfo<PT> getPlugin(
      Class<PT> type, Map<String, String> andPairs, Map<String, String> orPairs) {
    List<PluginInfo<PT>> pluginList = getPluginsOfType(type, andPairs, orPairs);
    return pluginList.size() > 0 ? pluginList.get(0) : null;
  }
  
  /**
   * As {@link org.scijava.plugin.PluginService#getPluginsOfType(Class)}
   * but with key,value pair parameters to allow for filtering based on
   * {@code Attr} annotation.
   */
  public <PT extends ScifioPlugin> List<PluginInfo<PT>> getPluginsOfType(
      Class<PT> type, Map<String, String> andPairs, Map<String, String> orPairs) {
    // Get the unfiltered plugin list
    List<PluginInfo<PT>> plugins = pluginService.getPluginsOfType(type);
    
    // The list of filtered plugins we will return.
    List<PluginInfo<PT>> filteredPlugins = new ArrayList<PluginInfo<PT>>();
    
    // loop through the unfiltered list, checking the "AND" and "OR"
    // parameters
    for(PluginInfo<PT> info : plugins) {
      // If true, we will add this PluginInfo to our filtered list.
      boolean valid = true;
      
      // Checking "OR" key,value pairs. Just one @Attr needs to match
      // an entry on this list.
      if (orPairs != null) {
        boolean matchedOr = false;
        
        Iterator<String> keyIter = orPairs.keySet().iterator();
        
        while (!matchedOr && keyIter.hasNext()) {
          String key = keyIter.next();
          
          if (orPairs.get(key).equals(info.get(key)))
            matchedOr = true;
        }
        
        if (!matchedOr)
          valid = false;
      }
      
      // Checking "AND" key,value pairs. All entries in this list
      // must have a matching @Attr, or this plugin will be filtered out.
      if (andPairs != null) {
        Iterator<String> keyIter = andPairs.keySet().iterator();
        
        while(valid && keyIter.hasNext()) {
          String key = keyIter.next();
          
          if (!(andPairs.get(key).equals(info.get(key))))
            valid = false;
        }
      }
      
      if (valid)
        filteredPlugins.add(info);
    }
    
    return filteredPlugins;
  }
}
