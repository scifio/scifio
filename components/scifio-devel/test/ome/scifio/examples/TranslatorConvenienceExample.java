package ome.scifio.examples;

import java.io.File;
import java.io.IOException;

import ome.scifio.FormatException;
import ome.scifio.Reader;
import ome.scifio.SCIFIO;
import ome.scifio.Writer;

public class TranslatorConvenienceExample {

	public static void main(String[] args) {
		
		FilePathBuilder fnf = new FilePathBuilder();
		String inFile = fnf.buildPath("testICS.ics");
		String outFile = fnf.buildPath("testICStoPNG.png");
		
		File out = new File(outFile);
		if(out.exists())
			out.delete();

		SCIFIO ctx = null;
		Reader<?> r = null;
		Writer<?> w = null;

		try {
			ctx = new SCIFIO();
			r = ctx.initializeReader(inFile);
			w = ctx.initializeWriter(inFile, outFile);
			
			System.out.println("***Reader Metadata***\n" + r.getMetadata() + "\n");
			System.out.println("***Writer Metadata***\n" + w.getMetadata());
			byte[] bytes = null;
			
			for(int i = 0; i < r.getImageCount(); i++) {
				for(int j = 0; j < r.getPlaneCount(i); j++) {
					bytes = r.openBytes(i, j);
					w.saveBytes(i, j, bytes);
				}
			}
			
			r.close();
			w.close();
			
		} catch (FormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
