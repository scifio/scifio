package ome.scifio.examples;

import java.io.IOException;

import ome.scifio.FormatException;
import ome.scifio.SCIFIO;
import ome.scifio.filters.ChannelFiller;
import ome.scifio.filters.DimensionSwapper;
import ome.scifio.filters.ReaderFilter;

public class WrapperExample {
  
  public static void main(String[] args) {

    FilePathBuilder fnf = new FilePathBuilder();
    String testFile = fnf.buildPath("testPNG.png");

    SCIFIO ctx = null;
    ReaderFilter r = null;

    try {
      ctx = new SCIFIO();
      r = ctx.initializeReader(testFile);
      
      r.enable(DimensionSwapper.class);
      ChannelFiller c = r.enable(ChannelFiller.class);
      
      c.setFilled(true);
      
      // use full wrapper stack
      byte[] bytes = r.openPlane(0, 0).getBytes();
      
      // use only the wrappers at or below ChannelFiller
      bytes = c.openPlane(0, 0).getBytes();
      
      r.close();
      
      System.out.println("Num bytes:" + bytes.length);
      System.out.println(r.getMetadata());
    }
    catch (FormatException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }
}
