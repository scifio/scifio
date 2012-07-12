package ome.scifio.examples;

import java.io.IOException;

import ome.scifio.FormatException;
import ome.scifio.Reader;
import ome.scifio.SCIFIO;

public class ReaderConvenienceExample {

  public static void main(String[] args) {

		FilePathBuilder fnf = new FilePathBuilder();
		String testFile = fnf.buildPath("testPNG.png");

    SCIFIO ctx = null;
    Reader<?> r = null;

    try {
      ctx = new SCIFIO();
      r = ctx.initializeReader(testFile);
      byte[] bytes = r.openBytes(0, 0);
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
