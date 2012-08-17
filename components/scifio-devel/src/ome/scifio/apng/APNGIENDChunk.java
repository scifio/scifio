package ome.scifio.apng;

/**
 * This class represents the critical IEND chunk that signifies
 * the end of a PNG stream.
 * 
 * @author Mark Hiner
 *
 */
public class APNGIENDChunk extends APNGChunk {
	
	// -- Constructor --
	public APNGIENDChunk() {
		this.CHUNK_SIGNATURE = new byte[] {(byte) 0x49, 0x45, 0x4E, 0x44};
	}
}
