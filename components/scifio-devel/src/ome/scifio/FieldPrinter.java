package ome.scifio;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.StringTokenizer;

/**
 * This class can be used as a flag to ensure all SCIFIO objects are printed in a consistent
 * manner via their toString methods.
 * 
 * This printing traverses up the dependency tree of the concrete implementation, and will also
 * follow paths down through fields (of any class in the hierarchy) that extend ScifioPrintable.
 * 
 * @author Mark Hiner
 *
 */
public class FieldPrinter {
  
  private final Object obj;

  public FieldPrinter(Object o) {
    obj = o;
  }

    // Prints out a list of all the fields of this Metadata object
    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append(super.toString() + "\n");
      sb.append(indent(dumpString(obj.getClass())));
      return sb.toString();
    }

    private String dumpString(Class<?> type) {
      if (type == null) return "";

      
      final StringBuilder sb = new StringBuilder();
      
      sb.append("--" + type + "--\n");
      
      // iterate over fields
      sb.append("{\n");

      for (final Field f : type.getDeclaredFields()) {
        f.setAccessible(true);

        final int mods = f.getModifiers();
        if (Modifier.isFinal(mods)) continue;
        if (Modifier.isStatic(mods)) continue;
        
        final String name = f.getName();
        final Object value;
        try {
          value = f.get(obj);
        }
        catch (final IllegalArgumentException e) {
          continue;
        }
        catch (final IllegalAccessException e) {
          continue;
        }
        sb.append(name + " = " + value + "\n");
      }
        sb.append("}\n");


      // Ascends the class hierarchy
      sb.append(indent(dumpString(type.getSuperclass())));
      return sb.toString();
    }
    
    /*
     * Adds one tab to each line in the provided string
     */
    private String indent(String s) {
      StringBuilder superSB = new StringBuilder();
      StringTokenizer stk = new StringTokenizer(s, "\n");
      while(stk.hasMoreTokens()) {
        superSB.append("\t" + stk.nextToken() + "\n");
      }

      return superSB.toString();
    }
}
