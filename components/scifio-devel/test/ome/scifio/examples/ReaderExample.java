package ome.scifio.examples;

import java.io.IOException;

import ome.scifio.Format;
import ome.scifio.FormatException;
import ome.scifio.Metadata;
import ome.scifio.Parser;
import ome.scifio.Reader;
import ome.scifio.SCIFIO;

public class ReaderExample {

	public static void main(String[] args) {

		FilePathBuilder fnf = new FilePathBuilder();
		String testFile = fnf.buildPath("testPNG.png");

		SCIFIO ctx = null;
		Format<?, ?, ?, ?, ?> format = null;


		try {
			ctx = new SCIFIO();
			
			format = ctx.getFormat(testFile);
			Parser<?> p = format.createParser();
			Metadata m = p.parse(testFile);
			Reader r = format.createReader();
			r.setMetadata(m);
			r.setSource(testFile);

			byte[] bytes = r.openPlane(0, 0).getBytes();
			r.close();

			System.out.println("Num bytes:" + bytes.length);
			System.out.println(r.getMetadata());
		} catch (FormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
