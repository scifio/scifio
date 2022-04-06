package io.scif.io;

import io.scif.img.IO;

import java.io.IOException;

import net.imagej.ImgPlus;

import org.junit.Test;

public class TestIO {

	@Test
	public void testCellImg() throws NegativeArraySizeException, IOException {
		ImgPlus<?> img = IO.open("img&axes=X,Y&lengths=50000,50000.fake");
		System.out.println(img.getImg());

	}
}
