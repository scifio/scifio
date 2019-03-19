
package io.scif.util;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.scijava.io.handle.DataHandle;
import org.scijava.io.handle.DataHandleService;
import org.scijava.io.location.BytesLocation;
import org.scijava.io.location.Location;
import org.scijava.io.nio.ByteBufferByteBank;

public class FormatTestHelpers {

	public static DataHandle<Location> createLittleEndianHandle(
		final int capacity, final DataHandleService dataHandleService)
	{
		try {
			return createLittleEndianHandle(capacity, dataHandleService, false);
		}
		catch (IOException exc) {
			throw new IllegalStateException(exc);
		}
	}

	public static DataHandle<Location> createLittleEndianHandle(
		final int capacity, final DataHandleService dataHandleService,
		boolean zerofill) throws IOException
	{
		// little endian bytebank
		final ByteBufferByteBank buffer = new ByteBufferByteBank(cap -> {
			return ByteBuffer.allocate(cap).order(java.nio.ByteOrder.LITTLE_ENDIAN);
		}, capacity);
		DataHandle<Location> handle = dataHandleService.create(new BytesLocation(
			buffer));
		if (zerofill) {
			handle.write(new byte[capacity]);
			handle.seek(0l);
		}
		handle.setLittleEndian(true);
		return handle;
	}
}
