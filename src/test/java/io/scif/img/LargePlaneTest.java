
package io.scif.img;

import net.imglib2.Cursor;

public class LargePlaneTest {

	public static void main(String... args) {
		final String sampleImage =
			"8bit-unsigned&pixelType=uint8&planarDims=2&lengths=200000,200000&axes=X,Y.fake";
		SCIFIOImgPlus<?> img = IO.openImgs(sampleImage).get(0);

		Cursor<?> cursor = img.cursor();
		int index = 1;
		while (cursor.hasNext() ) {

			System.out.println(index++ + " - " + cursor.next());
		}

		System.out.println("done");
	}
}
