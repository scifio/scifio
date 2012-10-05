/*
 * #%L
 * OME SCIFIO package for reading and converting scientific file formats.
 * %%
 * Copyright (C) 2005 - 2012 Open Microscopy Environment:
 *   - Board of Regents of the University of Wisconsin-Madison
 *   - Glencoe Software, Inc.
 *   - University of Dundee
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of any organization.
 * #L%
 */

package loci.common.adapter;

import java.io.IOException;
import java.nio.ByteBuffer;

import loci.common.RandomAccessInputStream;
import loci.legacy.adapter.AbstractLegacyAdapter;
import loci.legacy.adapter.AdapterTools;
import loci.legacy.adapter.Wrapper;

/**
 * This class manages delegation between {@link loci.common.RandomAccessInputStream}
 * and {@link ome.scifio.io.RandomAccessInputStream}.
 * <p>
 * Delegation is maintained by two WeakHashTables. See {@link AbstractLegacyAdapter}
 * </p>
 * <p>
 * Functionally, the delegation is handled in the nested classes - one for
 * wrapping from ome.scifio.io.RandomAccessInputStream to
 * loci.common.RandomAccessInputStream, and one for the reverse direction.
 * </p>
 * @author Mark Hiner
 */
public class RandomAccessInputStreamAdapter
  extends AbstractLegacyAdapter<RandomAccessInputStream, ome.scifio.io.RandomAccessInputStream> {

  // -- LegacyAdapter API --
  
  /* @see LegacyAdapter#wrapToLegacy(M) */
  public RandomAccessInputStream wrapToLegacy(ome.scifio.io.RandomAccessInputStream modern) {
    return new RandomAccessInputStream(modern);
  }

  /* @see LegacyAdapter#wrapToModern(L) */
  public ome.scifio.io.RandomAccessInputStream wrapToModern(RandomAccessInputStream legacy) {
    return new LegacyWrapper(legacy);
  }
  
  // -- Delegation Classes --
  
  /**
   * This class can be used to wrap loci.common.RandomAccessInputStream
   * objects and be passed to API expecting an ome.scifio.io.RandomAccessInputStream
   * object.
   * <p>
   * All functionality is delegated to the loci-common implementation.
   * </p>
   * 
   * @author Mark Hiner
   */
  public static class LegacyWrapper extends ome.scifio.io.RandomAccessInputStream
    implements Wrapper<RandomAccessInputStream> {

    // -- Fields --
    
    /* Legacy RandomAccessInputStream for delegation */
    private RandomAccessInputStream rais;
    
    // -- Constructors --
   
    /**
     * Constructs a hybrid RandomAccessFile/DataInputStream
     * around the given file.
     */
    public LegacyWrapper(String file) throws IOException {
//      super(file);
      rais = new RandomAccessInputStream(file);
    }

    /** Constructs a random access stream around the given handle. */
    public LegacyWrapper(ome.scifio.io.IRandomAccess handle) throws IOException {
//      super(handle);
      rais = 
        new RandomAccessInputStream(
            AdapterTools.getAdapter(IRandomAccessAdapter.class).getLegacy(handle));
    }

    /**
     * Constructs a random access stream around the given handle,
     * and with the associated file path.
     */
    public LegacyWrapper(ome.scifio.io.IRandomAccess handle, String file)
      throws IOException
    {
//      super(handle, file);
      rais = 
        new RandomAccessInputStream(
            AdapterTools.getAdapter(IRandomAccessAdapter.class).getLegacy(handle), file);
    }

    /** Constructs a random access stream around the given byte array. */
    public LegacyWrapper(byte[] array) throws IOException {
//      super(array);
      rais = new RandomAccessInputStream(array);
    }
    
    /** Wrapper constructor. */
    public LegacyWrapper(RandomAccessInputStream rais) {
//      super(rais);
      this.rais = rais;
    }
    
    // -- Wrapper API Methods --

    /* @see Wrapper#unwrap() */
    public RandomAccessInputStream unwrap() {
      return rais;
    }
    
    // -- RandomAccessStream API --
    
    /**
     * @param pos
     * @throws IOException
     * @see loci.common.RandomAccessInputStream#seek(long)
     */
    public void seek(long pos) throws IOException {
      rais.seek(pos);
    }

    /**
     * @return
     * @throws IOException
     * @see loci.common.RandomAccessInputStream#length()
     */
    public long length() throws IOException {
      return rais.length();
    }

    /**
     * @param newLength
     * @throws IOException
     * @see loci.common.RandomAccessInputStream#setLength(long)
     */
    public void setLength(long newLength) throws IOException {
      rais.setLength(newLength);
    }

    /**
     * @return
     * @throws IOException
     * @see loci.common.RandomAccessInputStream#getFilePointer()
     */
    public long getFilePointer() throws IOException {
      return rais.getFilePointer();
    }

    /**
     * @throws IOException
     * @see loci.common.RandomAccessInputStream#close()
     */
    public void close() throws IOException {
      rais.close();
    }

    /**
     * @param little
     * @see loci.common.RandomAccessInputStream#order(boolean)
     */
    public void order(boolean little) {
      rais.order(little);
    }

    /**
     * @return
     * @see loci.common.RandomAccessInputStream#isLittleEndian()
     */
    public boolean isLittleEndian() {
      return rais.isLittleEndian();
    }

    /**
     * @param lastChars
     * @return
     * @throws IOException
     * @see loci.common.RandomAccessInputStream#readString(java.lang.String)
     */
    public String readString(String lastChars) throws IOException {
      return rais.readString(lastChars);
    }

    /**
     * @param terminators
     * @return
     * @throws IOException
     * @see loci.common.RandomAccessInputStream#findString(java.lang.String[])
     */
    public String findString(String... terminators) throws IOException {
      return rais.findString(terminators);
    }

    /**
     * @param saveString
     * @param terminators
     * @return
     * @throws IOException
     * @see loci.common.RandomAccessInputStream#findString(boolean, java.lang.String[])
     */
    public String findString(boolean saveString, String... terminators)
      throws IOException
    {
      return rais.findString(saveString, terminators);
    }

    /**
     * @param blockSize
     * @param terminators
     * @return
     * @throws IOException
     * @see loci.common.RandomAccessInputStream#findString(int, java.lang.String[])
     */
    public String findString(int blockSize, String... terminators)
      throws IOException
    {
      return rais.findString(blockSize, terminators);
    }

    /**
     * @param saveString
     * @param blockSize
     * @param terminators
     * @return
     * @throws IOException
     * @see loci.common.RandomAccessInputStream#findString(boolean, int, java.lang.String[])
     */
    public String findString(boolean saveString, int blockSize,
      String... terminators) throws IOException
    {
      return rais.findString(saveString, blockSize, terminators);
    }

    /**
     * @return
     * @throws IOException
     * @see loci.common.RandomAccessInputStream#readBoolean()
     */
    public boolean readBoolean() throws IOException {
      return rais.readBoolean();
    }

    /**
     * @return
     * @throws IOException
     * @see loci.common.RandomAccessInputStream#readByte()
     */
    public byte readByte() throws IOException {
      return rais.readByte();
    }

    /**
     * @return
     * @throws IOException
     * @see loci.common.RandomAccessInputStream#readChar()
     */
    public char readChar() throws IOException {
      return rais.readChar();
    }

    /**
     * @return
     * @throws IOException
     * @see loci.common.RandomAccessInputStream#readDouble()
     */
    public double readDouble() throws IOException {
      return rais.readDouble();
    }

    /**
     * @return
     * @throws IOException
     * @see loci.common.RandomAccessInputStream#readFloat()
     */
    public float readFloat() throws IOException {
      return rais.readFloat();
    }

    /**
     * @return
     * @throws IOException
     * @see loci.common.RandomAccessInputStream#readInt()
     */
    public int readInt() throws IOException {
      return rais.readInt();
    }

    /**
     * @return
     * @throws IOException
     * @see loci.common.RandomAccessInputStream#readLine()
     */
    public String readLine() throws IOException {
      return rais.readLine();
    }

    /**
     * @return
     * @throws IOException
     * @see loci.common.RandomAccessInputStream#readCString()
     */
    public String readCString() throws IOException {
      return rais.readCString();
    }

    /**
     * @param n
     * @return
     * @throws IOException
     * @see loci.common.RandomAccessInputStream#readString(int)
     */
    public String readString(int n) throws IOException {
      return rais.readString(n);
    }

    /**
     * @return
     * @throws IOException
     * @see loci.common.RandomAccessInputStream#readLong()
     */
    public long readLong() throws IOException {
      return rais.readLong();
    }

    /**
     * @return
     * @throws IOException
     * @see loci.common.RandomAccessInputStream#readShort()
     */
    public short readShort() throws IOException {
      return rais.readShort();
    }

    /**
     * @return
     * @throws IOException
     * @see loci.common.RandomAccessInputStream#readUnsignedByte()
     */
    public int readUnsignedByte() throws IOException {
      return rais.readUnsignedByte();
    }

    /**
     * @return
     * @throws IOException
     * @see loci.common.RandomAccessInputStream#readUnsignedShort()
     */
    public int readUnsignedShort() throws IOException {
      return rais.readUnsignedShort();
    }

    /**
     * @return
     * @throws IOException
     * @see loci.common.RandomAccessInputStream#readUTF()
     */
    public String readUTF() throws IOException {
      return rais.readUTF();
    }

    /**
     * @param array
     * @return
     * @throws IOException
     * @see loci.common.RandomAccessInputStream#read(byte[])
     */
    public int read(byte[] array) throws IOException {
      return rais.read(array);
    }

    /**
     * @param array
     * @param offset
     * @param n
     * @return
     * @throws IOException
     * @see loci.common.RandomAccessInputStream#read(byte[], int, int)
     */
    public int read(byte[] array, int offset, int n) throws IOException {
      return rais.read(array, offset, n);
    }

    /**
     * @param buf
     * @return
     * @throws IOException
     * @see loci.common.RandomAccessInputStream#read(java.nio.ByteBuffer)
     */
    public int read(ByteBuffer buf) throws IOException {
      return rais.read(buf);
    }

    /**
     * @param buf
     * @param offset
     * @param n
     * @return
     * @throws IOException
     * @see loci.common.RandomAccessInputStream#read(java.nio.ByteBuffer, int, int)
     */
    public int read(ByteBuffer buf, int offset, int n) throws IOException {
      return rais.read(buf, offset, n);
    }

    /**
     * @param array
     * @throws IOException
     * @see loci.common.RandomAccessInputStream#readFully(byte[])
     */
    public void readFully(byte[] array) throws IOException {
      rais.readFully(array);
    }

    /**
     * @param array
     * @param offset
     * @param n
     * @throws IOException
     * @see loci.common.RandomAccessInputStream#readFully(byte[], int, int)
     */
    public void readFully(byte[] array, int offset, int n) throws IOException {
      rais.readFully(array, offset, n);
    }

    /**
     * @return
     * @throws IOException
     * @see loci.common.RandomAccessInputStream#read()
     */
    public int read() throws IOException {
      return rais.read();
    }

    /**
     * @return
     * @throws IOException
     * @see loci.common.RandomAccessInputStream#available()
     */
    public int available() throws IOException {
      return rais.available();
    }

    /**
     * @param readLimit
     * @see loci.common.RandomAccessInputStream#mark(int)
     */
    public void mark(int readLimit) {
      rais.mark(readLimit);
    }

    /**
     * @return
     * @see loci.common.RandomAccessInputStream#markSupported()
     */
    public boolean markSupported() {
      return rais.markSupported();
    }

    /**
     * @throws IOException
     * @see loci.common.RandomAccessInputStream#reset()
     */
    public void reset() throws IOException {
      rais.reset();
    }

    /**
     * @param obj
     * @return
     * @see loci.common.RandomAccessInputStream#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
      return rais.equals(obj);
    }

    /**
     * @return
     * @see loci.common.RandomAccessInputStream#hashCode()
     */
    public int hashCode() {
      return rais.hashCode();
    }

    /**
     * @param arg0
     * @return
     * @throws IOException
     * @see java.io.InputStream#skip(long)
     */
    public long skip(long arg0) throws IOException {
      return rais.skip(arg0);
    }

    /**
     * @param n
     * @return
     * @throws IOException
     * @see loci.common.RandomAccessInputStream#skipBytes(int)
     */
    public int skipBytes(int n) throws IOException {
      return rais.skipBytes(n);
    }

    /**
     * @return
     * @see loci.common.RandomAccessInputStream#toString()
     */
    public String toString() {
      return rais.toString();
    }
        
    // -- Object delegators --
  }
}
