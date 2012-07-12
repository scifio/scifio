package ome.scifio.examples;

import java.io.IOException;

import ome.scifio.Format;
import ome.scifio.FormatException;
import ome.scifio.Metadata;
import ome.scifio.Parser;
import ome.scifio.Reader;
import ome.scifio.SCIFIO;
import ome.scifio.Writer;

public class WriterExample {


	public static void main(String[] args) {
		FilePathBuilder fnf = new FilePathBuilder();
		String testFile = fnf.buildPath("testPNG.png");
		String outFile = fnf.buildPath("testPNGOut.png");
		
		SCIFIO ctx = null;
		Format<?, ?, ?, ?, ?> format = null;
		Format<?, ?, ?, ?, ?> outFormat = null;
		
		try {
			ctx = new SCIFIO();
			format = ctx.getFormat(testFile);
			Parser<?> p = format.createParser();
			Metadata m = p.parse(testFile);
			Reader r = format.createReader();
			r.setMetadata(m);
			r.setSource(testFile);
			
			outFormat = ctx.getFormat(outFile);
			Writer w = outFormat.createWriter();
			w.setMetadata(m);
			w.setDest(outFile);
			
			System.out.println("***Reader Metadata***\n" + r.getMetadata() + "\n");
			System.out.println("***Writer Metadata***\n" + w.getMetadata());
			
			for(int i = 0; i < r.getImageCount(); i++) {
				for(int j = 0; j < r.getPlaneCount(i); j++) {
					byte[] bytes = r.openBytes(i, j);
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
