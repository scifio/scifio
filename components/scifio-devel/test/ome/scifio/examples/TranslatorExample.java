package ome.scifio.examples;

import java.io.IOException;

import ome.scifio.DefaultDatasetMetadata;
import ome.scifio.Format;
import ome.scifio.FormatException;
import ome.scifio.Metadata;
import ome.scifio.Parser;
import ome.scifio.Reader;
import ome.scifio.SCIFIO;
import ome.scifio.Translator;
import ome.scifio.Writer;

public class TranslatorExample {
	
	public static void main(String[] args) {
		FilePathBuilder fnf = new FilePathBuilder();
		String testFile = fnf.buildPath("testICS.ics");
		String outFile = fnf.buildPath("testICStoPNG.png");
		
		SCIFIO ctx = null;
		Format<?, ?, ?, ?, ?> inFormat = null;
		Format<?, ?, ?, ?, ?> outFormat = null;
		
		try {
			ctx = new SCIFIO();
			inFormat = ctx.getFormat(testFile);
			Parser<?> p = inFormat.createParser();
			Metadata metaIn = p.parse(testFile);
			Reader reader = inFormat.createReader();
			reader.setMetadata(metaIn);
			reader.setSource(testFile);
			
			outFormat = ctx.getFormat(outFile);
			Writer writer = outFormat.createWriter();
			Metadata metaOut = outFormat.createMetadata();
			Translator inToCore = inFormat.findSourceTranslator(DefaultDatasetMetadata.class);
			DefaultDatasetMetadata datasetMeta = new DefaultDatasetMetadata();
			inToCore.translate(metaIn, datasetMeta);
			Translator coreToDest = outFormat.findDestTranslator(DefaultDatasetMetadata.class);
			coreToDest.translate(datasetMeta, metaOut);
			writer.setMetadata(metaOut);
			
			writer.setDest(outFile);
			
			System.out.println("***Reader Metadata***\n" + reader.getMetadata() + "\n");
			System.out.println("***Writer Metadata***\n" + writer.getMetadata());
			
			for(int i = 0; i < reader.getImageCount(); i++) {
				for(int j = 0; j < reader.getPlaneCount(i); j++) {
					byte[] bytes = reader.openBytes(i, j);
					writer.saveBytes(i, j, bytes);
				}
			}
			
			reader.close();
			writer.close();
			
		} catch (FormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
