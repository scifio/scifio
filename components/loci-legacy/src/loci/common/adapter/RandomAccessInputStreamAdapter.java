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
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;

import org.scijava.plugin.Plugin;

import loci.common.RandomAccessInputStream;
import loci.legacy.adapter.AbstractLegacyAdapter;
import loci.legacy.adapter.Wrapper;
import loci.legacy.context.LegacyContext;

/**
 * This class manages delegation between {@link loci.common.RandomAccessInputStream}
 * and {@link io.scif.io.RandomAccessInputStream}.
 * <p>
 * Delegation is maintained by two WeakHashTables. See {@link AbstractLegacyAdapter}
 * </p>
 * <p>
 * Functionally, the delegation is handled in the nested classes - one for
 * wrapping from io.scif.io.RandomAccessInputStream to
 * loci.common.RandomAccessInputStream, and one for the reverse direction.
 * </p>
 * @author Mark Hiner
 */
@Plugin(type=RandomAccessInputStreamAdapter.class)
public class RandomAccessInputStreamAdapter
  extends AbstractLegacyAdapter<RandomAccessInputStream, io.scif.io.RandomAccessInputStream> {
  
  // -- Constructor --
  
  public RandomAccessInputStreamAdapter() {
    super(RandomAccessInputStream.class, io.scif.io.RandomAccessInputStream.class);
  }

  // -- LegacyAdapter API --
  
  /* @see LegacyAdapter#wrapToLegacy(M) */
  public RandomAccessInputStream wrapToLegacy(io.scif.io.RandomAccessInputStream modern) {
    return new RandomAccessInputStream(modern);
  }

  /* @see LegacyAdapter#wrapToModern(L) */
  public io.scif.io.RandomAccessInputStream wrapToModern(RandomAccessInputStream legacy) {
    try {
      return new LegacyWrapper(legacy);
    } catch (IOException e) {
      throw new RuntimeException("Failed to create legacy wrapper", e);
    }
  }
  
  // -- Delegation Classes --
  
  /**
   * This class can be used to wrap loci.common.RandomAccessInputStream
   * objects and be passed to API expecting an io.scif.io.RandomAccessInputStream
   * object.
   * <p>
   * All functionality is delegated to the loci-common implementation.
   * </p>
   * 
   * @author Mark Hiner
   */
  public static class LegacyWrapper extends io.scif.io.RandomAccessInputStream
    implements Wrapper<RandomAccessInputStream> {

    // -- Fields --
    
    /* Legacy RandomAccessInputStream for delegation */
    private WeakReference<RandomAccessInputStream> rais;
    
    // -- Constructors --
   
    /**
     * Constructs a hybrid RandomAccessFile/DataInputStream
     * around the given file.
     */
    public LegacyWrapper(String file) throws IOException {
      super(LegacyContext.get());
      throw new UnsupportedOperationException(
          "Wrappers must be constructed using the objects they will wrap.");
    }

    /** Constructs a random access stream around the given handle. */
    public LegacyWrapper(io.scif.io.IRandomAccess handle) throws IOException {
//      super(handle);
      super(LegacyContext.get());
      throw new UnsupportedOperationException(
          "Wrappers must be constructed using the objects they will wrap.");
    }

    /**
     * Constructs a random access stream around the given handle,
     * and with the associated file path.
     */
    public LegacyWrapper(io.scif.io.IRandomAccess handle, String file)
      throws IOException
    {
      super(LegacyContext.get());
      throw new UnsupportedOperationException(
          "Wrappers must be constructed using the objects they will wrap.");
    }

    /** Constructs a random access stream around the given byte array. */
    public LegacyWrapper(byte[] array) throws IOException {
      super(LegacyContext.get());
      throw new UnsupportedOperationException(
          "Wrappers must be constructed using the objects they will wrap.");
    }
    
    /** Wrapper constructor. 
     * @throws IOException */
    public LegacyWrapper(RandomAccessInputStream rais) throws IOException {
      super(LegacyContext.get());
      this.rais = new WeakReference<RandomAccessInputStream>(rais);
    }
    
    // -- Wrapper API Methods --

    /* @see Wrapper#unwrap() */
    public RandomAccessInputStream unwrap() {
      return rais.get();
    }
    
    // -- RandomAccessStream API --
    
    @Override
    public void seek(long pos) throws IOException {
      unwrap().seek(pos);
    }

    @Override
    public long length() throws IOException {
      return unwrap().length();
    }

    @Override
    public void setLength(long newLength) throws IOException {
      unwrap().setLength(newLength);
    }

    @Override
    public long getFilePointer() throws IOException {
      return unwrap().getFilePointer();
    }
    
    @Override
    public void close() throws IOException {
      unwrap().close();
    }

    @Override
    public void order(boolean little) {
      unwrap().order(little);
    }

    @Override
    public boolean isLittleEndian() {
      return unwrap().isLittleEndian();
    }

    @Override
    public String readString(String lastChars) throws IOException {
      return unwrap().readString(lastChars);
    }

    @Override
    public String findString(String... terminators) throws IOException {
      return unwrap().findString(terminators);
    }

    @Override
    public String findString(boolean saveString, String... terminators)
      throws IOException
    {
      return unwrap().findString(saveString, terminators);
    }

    @Override
    public String findString(int blockSize, String... terminators)
      throws IOException
    {
      return unwrap().findString(blockSize, terminators);
    }

    @Override
    public String findString(boolean saveString, int blockSize,
      String... terminators) throws IOException
    {
      return unwrap().findString(saveString, blockSize, terminators);
    }

    @Override
    public boolean readBoolean() throws IOException {
      return unwrap().readBoolean();
    }

    @Override
    public byte readByte() throws IOException {
      return unwrap().readByte();
    }

    @Override
    public char readChar() throws IOException {
      return unwrap().readChar();
    }

    @Override
    public double readDouble() throws IOException {
      return unwrap().readDouble();
    }

    @Override
    public float readFloat() throws IOException {
      return unwrap().readFloat();
    }

    @Override
    public int readInt() throws IOException {
      return unwrap().readInt();
    }

    @Override
    public String readLine() throws IOException {
      return unwrap().readLine();
    }

    @Override
    public String readCString() throws IOException {
      return unwrap().readCString();
    }

    @Override
    public String readString(int n) throws IOException {
      return unwrap().readString(n);
    }

    @Override
    public long readLong() throws IOException {
      return unwrap().readLong();
    }

    @Override
    public short readShort() throws IOException {
      return unwrap().readShort();
    }

    @Override
    public int readUnsignedByte() throws IOException {
      return unwrap().readUnsignedByte();
    }

    @Override
    public int readUnsignedShort() throws IOException {
      return unwrap().readUnsignedShort();
    }

    @Override
    public String readUTF() throws IOException {
      return unwrap().readUTF();
    }

    @Override
    public int read(byte[] array) throws IOException {
      return unwrap().read(array);
    }

    @Override
    public int read(byte[] array, int offset, int n) throws IOException {
      return unwrap().read(array, offset, n);
    }

    @Override
    public int read(ByteBuffer buf) throws IOException {
      return unwrap().read(buf);
    }

    @Override
    public int read(ByteBuffer buf, int offset, int n) throws IOException {
      return unwrap().read(buf, offset, n);
    }

    @Override
    public void readFully(byte[] array) throws IOException {
      unwrap().readFully(array);
    }

    @Override
    public void readFully(byte[] array, int offset, int n) throws IOException {
      unwrap().readFully(array, offset, n);
    }

    @Override
    public int read() throws IOException {
      return unwrap().read();
    }

    @Override
    public int available() throws IOException {
      return unwrap().available();
    }
    
    @Override
    public void mark(int readLimit) {
      unwrap().mark(readLimit);
    }

    @Override
    public boolean markSupported() {
      return unwrap().markSupported();
    }

    @Override
    public void reset() throws IOException {
      unwrap().reset();
    }

    @Override
    public long skip(long arg0) throws IOException {
      return unwrap().skip(arg0);
    }

    @Override
    public int skipBytes(int n) throws IOException {
      return unwrap().skipBytes(n);
    }
  }
}
